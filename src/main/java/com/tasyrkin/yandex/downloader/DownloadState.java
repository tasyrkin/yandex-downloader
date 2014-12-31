package com.tasyrkin.yandex.downloader;

import com.google.common.base.MoreObjects;

public class DownloadState {

    static final DownloadState INITIAL = new DownloadState(DownloadStateEnum.INITIAL);
    static final DownloadState IN_PROGRESS = new DownloadState(DownloadStateEnum.IN_PROGRESS);
    static final DownloadState PAUSED = new DownloadState(DownloadStateEnum.PAUSED);
    static final DownloadState CANCELLED = new DownloadState(DownloadStateEnum.CANCELLED);
    static final DownloadState FINISHED = new DownloadState(DownloadStateEnum.FINISHED);

    private DownloadStateEnum downloadStateEnum;
    private Exception failureException;

    public DownloadState(final DownloadStateEnum downloadStateEnum) {
        this(downloadStateEnum, null);
    }

    public DownloadState(final DownloadStateEnum downloadStateEnum, final Exception failureException) {
        this.downloadStateEnum = downloadStateEnum;
        this.failureException = failureException;
    }

    public DownloadStateEnum getDownloadStateEnum() {
        return downloadStateEnum;
    }

    public Exception getFailureException() {
        return failureException;
    }

    @Override
    public String toString() {
        //J-
        return MoreObjects.toStringHelper(DownloadState.class)
                .add("downloadStateEnum", downloadStateEnum)
                .add("failureException", failureException)
                .toString();
        //J+
    }
}
