package com.tasyrkin.yandex.downloader;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableMap;

/**
 * <p>Manages a download, consisting of multiple urls.
 *
 * <p>This class is not synchronized.
 */
public class DownloadController {

    private static final Logger LOG = LogManager.getLogger(DownloadController.class);

    private final HashMap<DownloadRequestEntry, ThreadAndDownloader> threadsAndDownloaders;

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
        checkArgument(!isEmpty(downloadRequest.getEntries()), "Missing download request entries");

        this.threadsAndDownloaders = new HashMap<>();

        for (final DownloadRequestEntry requestEntry : downloadRequest.getEntries()) {
            threadsAndDownloaders.put(requestEntry, toThreadAndDownloader(requestEntry));
        }
    }

    /**
     * <p>Starts downloads and its status changes from INITIAL to IN_PROGRESS.
     */
    public void startDownload() {
        for (ThreadAndDownloader threadAndDownloader : threadsAndDownloaders.values()) {
            threadAndDownloader.getThread().start();
        }
    }

    /**
     * <p>Requests download to cancel if it is possible. The status is changed from every status except FINISHED or
     * FAILED to CANCELLED.
     *
     * <p>If the status of the download is FINISHED or FAILED, then it is not possible to change its status to
     * CANCELLED.
     */
    public void requestDownloadCancel() {
        for (ThreadAndDownloader threadAndDownloader : threadsAndDownloaders.values()) {
            threadAndDownloader.getDownloader().requestCancel();
        }
    }

    /**
     * <p>Restarts download by cancelling it first and then starting a new one.
     */
    public void restartDownload() {

        requestDownloadCancel();

        waitDownloadersToCancel();

        reinitialize();

        startDownload();
    }

    private void reinitialize() {
        for (ImmutableMap.Entry<DownloadRequestEntry, ThreadAndDownloader> entry : threadsAndDownloaders.entrySet()) {
            entry.setValue(toThreadAndDownloader(entry.getKey()));
        }
    }

    private static ThreadAndDownloader toThreadAndDownloader(final DownloadRequestEntry requestEntry) {

        final Downloader downloader = new Downloader(requestEntry.getSourceUrl(), requestEntry.getDestinationFile());

        final Thread thread = new Thread(downloader);

        return new ThreadAndDownloader(thread, downloader);
    }

    private void waitDownloadersToCancel() {
        for (ThreadAndDownloader threadAndDownloader : threadsAndDownloaders.values()) {
            try {
                threadAndDownloader.getThread().join();
            } catch (InterruptedException e) {
                LOG.error("Unable to wait for a downloader thread", e);
            }
        }
    }

    /**
     * <p>Requests download to pause if it is possible. The only possible states to request pause is INITIAL or
     * IN_PROGRESS.
     */
    public void requestDownloadPause() {
        for (ThreadAndDownloader threadAndDownloader : threadsAndDownloaders.values()) {
            threadAndDownloader.getDownloader().requestPause();
        }
    }

    /**
     * <p>Resuming is the opposite of pausing, so it only resumes the download if it is paused, otherwise the call has
     * no effect.
     */
    public void requestDownloadResume() {
        for (ThreadAndDownloader threadAndDownloader : threadsAndDownloaders.values()) {
            threadAndDownloader.getDownloader().requestResume();
        }
    }

    /**
     * @return  the states of every requested entry for the time of the call.
     */
    public Map<DownloadRequestEntry, DownloadState> getState() {

        final Map<DownloadRequestEntry, DownloadState> result = new HashMap<>();

        for (Map.Entry<DownloadRequestEntry, ThreadAndDownloader> entry : threadsAndDownloaders.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getDownloader().getState());
        }

        return result;
    }
}
