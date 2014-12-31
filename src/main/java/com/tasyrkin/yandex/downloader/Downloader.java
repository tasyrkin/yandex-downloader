package com.tasyrkin.yandex.downloader;

import static com.google.common.base.Preconditions.checkArgument;

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

    private static final Logger LOG = LogManager.getLogger(Downloader.class);

    private final URL sourceUrl;
    private final File destinationFile;
    private DownloadState downloadState;

    private final Lock requestsGuard = new ReentrantLock();
    private final Condition requestsCondition = requestsGuard.newCondition();
    private boolean pauseRequest = false;
    private boolean cancelRequest = false;

    private static final byte[] buffer = new byte[1024];

    public Downloader(final URL sourceUrl, final File destinationFile) {

        checkArgument(sourceUrl != null, "Missing source url");
        checkArgument(destinationFile != null, "Missing destination file");

        this.sourceUrl = sourceUrl;
        this.destinationFile = destinationFile;
        setState(INITIAL);
    }

    @Override
    public void run() {

        setState(IN_PROGRESS);

        URLConnection connection = null;
        final Closer closer = Closer.create();

        try {
            connection = sourceUrl.openConnection();

            final InputStream inputStream = closer.register(connection.getInputStream());
            final FileOutputStream fileOutputStream = closer.register(new FileOutputStream(destinationFile));

            int readCount;
            while (continueOrBlock() && 0 < (readCount = inputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, readCount);
            }

            if (IN_PROGRESS.equals(getState())) {
                setState(FINISHED);
            }

        } catch (InterruptedException e) {
            setState(CANCELLED);
        } catch (Exception e) {
            setState(new DownloadState(DownloadStateEnum.FAILED, e));
        } finally {
            try {
                closer.close();
            } catch (IOException e) {
                LOG.error("Failed to close input / output stream", e);
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
            LOG.debug("Changed state [{}] -> [{}]", this.downloadState, downloadState);
        }

        this.downloadState = downloadState;
    }

    private boolean continueOrBlock() throws InterruptedException {
        requestsGuard.lock();
        try {
            while (pauseRequest && !cancelRequest) {
                setState(PAUSED);
                requestsCondition.await();
            }

            if (cancelRequest) {
                setState(CANCELLED);
            } else {
                setState(IN_PROGRESS);
            }

            return getState() == IN_PROGRESS;
        } finally {
            requestsGuard.unlock();
        }
    }

    public void requestPause() {
        requestsGuard.lock();
        pauseRequest = true;
        requestsGuard.unlock();
    }

    public void requestResume() {
        requestsGuard.lock();
        pauseRequest = false;
        requestsCondition.signalAll();
        requestsGuard.unlock();
    }

    public void requestCancel() {
        requestsGuard.lock();
        cancelRequest = true;
        requestsCondition.signalAll();
        requestsGuard.unlock();
    }
}
