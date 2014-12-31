package com.tasyrkin.yandex.downloader;

import static com.google.common.base.Preconditions.checkArgument;

import static com.tasyrkin.yandex.downloader.DownloadStateEnum.INITIAL;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DownloadController {

    private static final Logger LOG = LogManager.getLogger(DownloadController.class);

    private final DownloadRequest downloadRequest;
    private Map<DownloadRequestEntry, ThreadAndDownloader> threadsAndDownloaders;

    private static class ThreadAndDownloader {
        private Thread thread;
        private Downloader downloader;

        public ThreadAndDownloader(final Thread thread, final Downloader downloader) {
            this.thread = thread;
            this.downloader = downloader;
        }

        public Thread getThread() {
            return thread;
        }

        public Downloader getDownloader() {
            return downloader;
        }
    }

    public DownloadController(final DownloadRequest downloadRequest) {

        checkArgument(downloadRequest != null, "Missing download request");

        this.downloadRequest = downloadRequest;
    }

    public void startDownload() {

        threadsAndDownloaders = new HashMap<>();

        for (final DownloadRequestEntry requestEntry : downloadRequest.getEntries()) {

            Downloader downloader = new Downloader(requestEntry.getSourceUrl(), requestEntry.getDestinationFile());

            Thread thread = new Thread(downloader);

            thread.start();

            threadsAndDownloaders.put(requestEntry, new ThreadAndDownloader(thread, downloader));
        }
    }

    public void requestDownloadCancel() {
        if (threadsAndDownloaders != null) {
            for (ThreadAndDownloader threadAndDownloader : threadsAndDownloaders.values()) {
                threadAndDownloader.getDownloader().requestCancel();
            }
        }
    }

    public void restartDownload() {
        requestDownloadCancel();
        joinThreads();
        startDownload();
    }

    private void joinThreads() {
        if (threadsAndDownloaders != null) {
            for (ThreadAndDownloader threadAndDownloader : threadsAndDownloaders.values()) {
                try {
                    threadAndDownloader.getThread().join();
                } catch (InterruptedException e) {
                    LOG.error("Unable to join thread for downloading url", e);
                }
            }
        }
    }

    public void requestDownloadPause() {
        if (threadsAndDownloaders != null) {
            for (ThreadAndDownloader threadAndDownloader : threadsAndDownloaders.values()) {
                threadAndDownloader.getDownloader().requestPause();
            }
        }

    }

    public void requestDownloadResume() {
        if (threadsAndDownloaders != null) {
            for (ThreadAndDownloader threadAndDownloader : threadsAndDownloaders.values()) {
                threadAndDownloader.getDownloader().requestResume();
            }
        }
    }

    public Map<DownloadRequestEntry, DownloadState> getState() {

        final Map<DownloadRequestEntry, DownloadState> result = new HashMap<>();

        if (threadsAndDownloaders != null) {
            for (Map.Entry<DownloadRequestEntry, ThreadAndDownloader> entry : threadsAndDownloaders.entrySet()) {
                result.put(entry.getKey(), entry.getValue().getDownloader().getState());
            }
        } else {
            for (DownloadRequestEntry requestEntry : downloadRequest.getEntries()) {
                result.put(requestEntry, new DownloadState(INITIAL));
            }
        }

        return result;
    }
}
