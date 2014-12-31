package com.tasyrkin.yandex.downloader;

import static com.tasyrkin.yandex.downloader.DownloadState.CANCELLED;
import static com.tasyrkin.yandex.downloader.DownloadState.FINISHED;
import static com.tasyrkin.yandex.downloader.DownloadState.INITIAL;
import static com.tasyrkin.yandex.downloader.DownloadState.IN_PROGRESS;
import static com.tasyrkin.yandex.downloader.DownloadState.PAUSED;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.io.Closer;

public class Downloader implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(Downloader.class);

    private final URL sourceUrl;
    private final File destinationFile;
    private DownloadState downloadState;

    private final Lock pauseLock = new ReentrantLock();
    private final Condition pauseCondition = pauseLock.newCondition();
    private boolean pauseRequest = false;

    private static final byte[] buffer = new byte[1024];

    public Downloader(final URL sourceUrl, final File destinationFile) {
        this.sourceUrl = sourceUrl;
        this.destinationFile = destinationFile;
        setState(INITIAL);
    }

    @Override
    public void run() {

        URLConnection connection = null;
        final Closer closer = Closer.create();

        setState(IN_PROGRESS);

        try {
            connection = sourceUrl.openConnection();

            final InputStream inputStream = closer.register(connection.getInputStream());
            final FileOutputStream fileOutputStream = closer.register(new FileOutputStream(destinationFile));

            int readCount;
            while (checkIfInProgressOrBlock() && 0 < (readCount = inputStream.read(buffer))) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                fileOutputStream.write(buffer, 0, readCount);
            }

            setState(FINISHED);

        } catch (InterruptedException e) {
            setState(CANCELLED);
        } catch (Exception e) {
            setState(new DownloadState(DownloadStateEnum.FAILED, e));
        } finally {
            try {
                closer.close();
            } catch (IOException e) {
                LOGGER.error("Failed to close input / output stream", e);
            }

            if (connection != null && connection instanceof HttpURLConnection) {
                ((HttpURLConnection) connection).disconnect();
            }
        }

    }

    public synchronized DownloadState getState() {
        return downloadState;
    }

    private synchronized void setState(final DownloadState downloadState) {
        if (!downloadState.equals(this.downloadState)) {
            LOGGER.debug("Changed state [{}] -> [{}]", this.downloadState, downloadState);
        }

        this.downloadState = downloadState;
    }

    private boolean checkIfInProgressOrBlock() throws InterruptedException {
        pauseLock.lock();
        try {
            while (pauseRequest) {
                setState(PAUSED);
                pauseCondition.await();
            }

            setState(IN_PROGRESS);

            return true;
        } finally {
            pauseLock.unlock();
        }
    }

    public void pause() {
        pauseLock.lock();
        pauseRequest = true;
        pauseLock.unlock();
    }

    public void resume() {
        pauseLock.lock();
        pauseRequest = false;
        pauseCondition.signalAll();
        pauseLock.unlock();
    }
}
