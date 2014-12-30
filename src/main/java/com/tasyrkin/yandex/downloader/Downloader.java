package com.tasyrkin.yandex.downloader;

import static com.tasyrkin.yandex.downloader.DownloadStateEnum.INITIAL;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.net.URLConnection;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Downloader implements Runnable {

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
        setState(new DownloadState(INITIAL));
    }

    @Override
    public void run() {

        URLConnection connection = null;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;

        try {
            connection = sourceUrl.openConnection();

            inputStream = connection.getInputStream();
            fileOutputStream = new FileOutputStream(destinationFile);

            int readCount;
            while (checkIfPauseRequestedOrBlock() && 0 < (readCount = inputStream.read(buffer))) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                fileOutputStream.write(buffer, 0, readCount);
            }

            setState(new DownloadState(DownloadStateEnum.DONE));

        } catch (IOException e) {
            setState(new DownloadState(DownloadStateEnum.FAILED, e, e.getMessage()));
        } catch (InterruptedException e) {
            setState(new DownloadState(DownloadStateEnum.CANCELLED));
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) { }
            }
        }

    }

    public synchronized DownloadState getState() {
        return downloadState;
    }

    private synchronized void setState(final DownloadState downloadState) {
        this.downloadState = downloadState;
    }

    private boolean checkIfPauseRequestedOrBlock() throws InterruptedException {
        pauseLock.lock();
        try {
            while (pauseRequest) {
                pauseCondition.await();
            }

            return false;
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
