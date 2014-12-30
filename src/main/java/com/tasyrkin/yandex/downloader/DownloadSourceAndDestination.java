package com.tasyrkin.yandex.downloader;

import java.io.File;

import java.net.URL;

public class DownloadSourceAndDestination {
    private URL sourceUrl;
    private File destinationFile;

    public DownloadSourceAndDestination(final URL sourceUrl, final File destinationFile) {
        this.sourceUrl = sourceUrl;
        this.destinationFile = destinationFile;
    }

    public URL getSourceUrl() {
        return sourceUrl;
    }

    public File getDestinationFile() {
        return destinationFile;
    }
}
