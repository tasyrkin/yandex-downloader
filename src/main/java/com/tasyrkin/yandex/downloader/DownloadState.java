package com.tasyrkin.yandex.downloader;

public class DownloadState {

    DownloadStateEnum downloadStateEnum;
    Exception failureException;
    String failureMessage;

    public DownloadState(final DownloadStateEnum downloadStateEnum) {
        this(downloadStateEnum, null, null);
    }

    public DownloadState(final DownloadStateEnum downloadStateEnum, final Exception failureException,
            final String failureMessage) {
        this.downloadStateEnum = downloadStateEnum;
        this.failureException = failureException;
        this.failureMessage = failureMessage;
    }

    public DownloadStateEnum getDownloadStateEnum() {
        return downloadStateEnum;
    }

    public Exception getFailureException() {
        return failureException;
    }

    public String getFailureMessage() {
        return failureMessage;
    }
}
