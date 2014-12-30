package com.tasyrkin.yandex.downloader;

public class DownloaderState {

    DownloaderStateEnum downloaderStateEnum;
    Exception failureException;
    String failureMessage;

    public DownloaderState(final DownloaderStateEnum downloaderStateEnum) {
        this(downloaderStateEnum, null, null);
    }

    public DownloaderState(final DownloaderStateEnum downloaderStateEnum, final Exception failureException,
            final String failureMessage) {
        this.downloaderStateEnum = downloaderStateEnum;
        this.failureException = failureException;
        this.failureMessage = failureMessage;
    }

    public DownloaderStateEnum getDownloaderStateEnum() {
        return downloaderStateEnum;
    }

    public Exception getFailureException() {
        return failureException;
    }

    public String getFailureMessage() {
        return failureMessage;
    }
}
