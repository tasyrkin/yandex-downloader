package com.tasyrkin.yandex.downloader;

import static com.google.common.base.Preconditions.checkArgument;

import static com.tasyrkin.yandex.downloader.DownloaderStateEnum.INITIAL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadController {
    private final List<DownloadSourceAndDestination> sourcesAndDestinations;
    private Map<DownloadSourceAndDestination, ThreadAndDownloader> threadsAndDownloaders;

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

    public DownloadController(final List<DownloadSourceAndDestination> sourcesAndDestinations) {

        checkArgument(sourcesAndDestinations != null && !sourcesAndDestinations.isEmpty(),
            "missing at least one download source and destination");

        this.sourcesAndDestinations = sourcesAndDestinations;
    }

    public void startDownload() {

        threadsAndDownloaders = new HashMap<>();

        for (DownloadSourceAndDestination srcAndDst : sourcesAndDestinations) {

            Downloader downloader = new Downloader(srcAndDst.getSourceUrl(), srcAndDst.getDestinationFile());

            Thread thread = new Thread(downloader);

            thread.start();

            threadsAndDownloaders.put(srcAndDst, new ThreadAndDownloader(thread, downloader));
        }
    }

    public void cancelDownload() {
        if (threadsAndDownloaders != null) {
            for (ThreadAndDownloader threadAndDownloader : threadsAndDownloaders.values()) {
                threadAndDownloader.getThread().interrupt();
            }
        }
    }

    public void restartDownload() {
        cancelDownload();
        startDownload();
    }

    public void pauseDownload() {
        if (threadsAndDownloaders != null) {
            for (ThreadAndDownloader threadAndDownloader : threadsAndDownloaders.values()) {
                threadAndDownloader.getDownloader().pause();
            }
        }

    }

    public void resumeDownload() {
        if (threadsAndDownloaders != null) {
            for (ThreadAndDownloader threadAndDownloader : threadsAndDownloaders.values()) {
                threadAndDownloader.getDownloader().resume();
            }
        }
    }

    public Map<DownloadSourceAndDestination, DownloaderState> getState() {

        final Map<DownloadSourceAndDestination, DownloaderState> result = new HashMap<>();

        if (threadsAndDownloaders != null) {
            for (Map.Entry<DownloadSourceAndDestination, ThreadAndDownloader> entry : threadsAndDownloaders.entrySet()) {
                result.put(entry.getKey(), entry.getValue().getDownloader().getState());
            }
        } else {
            for (DownloadSourceAndDestination srcAndDst : sourcesAndDestinations) {
                result.put(srcAndDst, new DownloaderState(INITIAL));
            }
        }

        return result;
    }
}