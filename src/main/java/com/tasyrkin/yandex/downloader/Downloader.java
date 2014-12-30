package com.tasyrkin.yandex.downloader;

import static com.tasyrkin.yandex.downloader.DownloaderStateEnum.INITIAL;

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
    private DownloaderState downloaderState;

    private final Lock pauseLock = new ReentrantLock();
    private final Condition pauseCondition = pauseLock.newCondition();
    private boolean pauseRequest = false;

    private static final byte[] buffer = new byte[1024];

    public Downloader(final URL sourceUrl, final File destinationFile) {
        this.sourceUrl = sourceUrl;
        this.destinationFile = destinationFile;
        setState(new DownloaderState(INITIAL));
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

            setState(new DownloaderState(DownloaderStateEnum.DONE));

        } catch (IOException e) {
            setState(new DownloaderState(DownloaderStateEnum.FAILED, e, e.getMessage()));
        } catch (InterruptedException e) {
            setState(new DownloaderState(DownloaderStateEnum.CANCELLED));
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) { }
            }
        }

    }

    public synchronized DownloaderState getState() {
        return downloaderState;
    }

    private synchronized void setState(final DownloaderState downloaderState) {
        this.downloaderState = downloaderState;
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
