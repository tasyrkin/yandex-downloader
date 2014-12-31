package com.tasyrkin.yandex.downloader;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;

public class DownloadRequest {

    private final ImmutableList<DownloadRequestEntry> entries;

    public DownloadRequest(final DownloadRequestEntry... entries) {

        checkArgument(entries != null && entries.length > 0, "At least one request entry is required");

        this.entries = ImmutableList.copyOf(entries);
    }

    public ImmutableList<DownloadRequestEntry> getEntries() {
        return entries;
    }
}
