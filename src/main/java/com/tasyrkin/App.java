package com.tasyrkin;

import static com.google.common.collect.Iterables.all;

import static com.tasyrkin.yandex.downloader.DownloadStateEnum.CANCELLED;
import static com.tasyrkin.yandex.downloader.DownloadStateEnum.FINISHED;
import static com.tasyrkin.yandex.downloader.DownloadStateEnum.PAUSED;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.common.base.Predicate;

import com.tasyrkin.yandex.downloader.DownloadController;
import com.tasyrkin.yandex.downloader.DownloadRequest;
import com.tasyrkin.yandex.downloader.DownloadRequestEntry;
import com.tasyrkin.yandex.downloader.DownloadState;
import com.tasyrkin.yandex.downloader.DownloadStateEnum;

/**
 * Simple tests for executing the application code.
 */
public class App {

    private static final String URL1 = "https://www.cs.kent.ac.uk/people/staff/sjt/TTFP/ttfp.pdf";
    private static final String URL2 = "https://leanpub.com/nightowls/read#leanpub-auto-statistics";

    private static Predicate<DownloadState> checkState(final DownloadStateEnum downloadStateEnum) {
        return new Predicate<DownloadState>() {
            @Override
            public boolean apply(final DownloadState downloadState) {
                return downloadState.getDownloadStateEnum() == downloadStateEnum;
            }
        };
    }

    /**
     * <p>These trial executions of the downloads are designed to check how the client application would use it.
     *
     * <p>It is immediately clear that a client application would better supply callback functions to get notified when
     * all downloads are finished, for example. This feature is suggested in possible improvements list.
     */
    public static void main(final String[] args) throws MalformedURLException, InterruptedException {

        long currMs = System.currentTimeMillis();

        final DownloadRequestEntry srcDst1 = new DownloadRequestEntry(new URL(URL1), getFile("__ttfp", currMs, "pdf"));
        final DownloadRequestEntry srcDst2 = new DownloadRequestEntry(new URL(URL2),
                getFile("__nightowls", currMs, "html"));

        System.out.println("Tests started");

        justStartDownloads(srcDst1, srcDst2);

        startAndRestartDownloads(srcDst1, srcDst2);

        startAndPauseAndResumeDownloads(srcDst1, srcDst2);

        startAndCancelAndPauseDownloads(srcDst1, srcDst2);

        System.out.println("Tests finished");
    }

    private static void startAndCancelAndPauseDownloads(final DownloadRequestEntry srcDst1,
            final DownloadRequestEntry srcDst2) throws InterruptedException {
        final DownloadController controller = new DownloadController(new DownloadRequest(srcDst1, srcDst2));

        controller.startDownload();

        controller.requestDownloadCancel();

        while (!all(controller.getState().values(), checkState(CANCELLED))) {
            Thread.sleep(500);

        }

        controller.requestDownloadPause();

        while (!all(controller.getState().values(), checkState(CANCELLED))) {
            Thread.sleep(500);
        }

    }

    private static void startAndPauseAndResumeDownloads(final DownloadRequestEntry srcDst1,
            final DownloadRequestEntry srcDst2) throws InterruptedException {
        final DownloadController controller = new DownloadController(new DownloadRequest(srcDst1, srcDst2));

        controller.startDownload();

        controller.requestDownloadPause();

        while (!all(controller.getState().values(), checkState(PAUSED))) {
            Thread.sleep(500);

        }

        controller.requestDownloadResume();

        while (!all(controller.getState().values(), checkState(FINISHED))) {
            Thread.sleep(500);
        }

    }

    private static void startAndRestartDownloads(final DownloadRequestEntry srcDst1, final DownloadRequestEntry srcDst2)
        throws InterruptedException {
        final DownloadController controller = new DownloadController(new DownloadRequest(srcDst1, srcDst2));

        controller.startDownload();

        while (!all(controller.getState().values(), checkState(FINISHED))) {
            Thread.sleep(500);
        }

        controller.restartDownload();

        while (!all(controller.getState().values(), checkState(FINISHED))) {
            Thread.sleep(500);
        }

    }

    private static void justStartDownloads(final DownloadRequestEntry srcDst1, final DownloadRequestEntry srcDst2)
        throws InterruptedException {

        final DownloadController controller = new DownloadController(new DownloadRequest(srcDst1, srcDst2));

        controller.startDownload();

        while (!all(controller.getState().values(), checkState(FINISHED))) {
            Thread.sleep(500);
        }
    }

    private static File getFile(final String prefix, final long currMs, final String postfix) {
        return new File(String.format("/tmp/%s.%s.%s", prefix, currMs, postfix));
    }
}
