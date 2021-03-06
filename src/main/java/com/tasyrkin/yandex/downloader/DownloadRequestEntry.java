package com.tasyrkin.yandex.downloader;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;

import java.net.URL;

import com.google.common.base.MoreObjects;

public class DownloadRequestEntry {
    private URL sourceUrl;
    private File destinationFile;

    public DownloadRequestEntry(final URL sourceUrl, final File destinationFile) {

        checkArgument(sourceUrl != null, "Missing source url");
        checkArgument(destinationFile != null, "Missing destination file");

        this.sourceUrl = sourceUrl;
        this.destinationFile = destinationFile;
    }

    public URL getSourceUrl() {
        return sourceUrl;
    }

    public File getDestinationFile() {
        return destinationFile;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DownloadRequestEntry that = (DownloadRequestEntry) o;

        if (!destinationFile.equals(that.destinationFile)) {
            return false;
        }

        if (!sourceUrl.equals(that.sourceUrl)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = sourceUrl.hashCode();
        result = 31 * result + destinationFile.hashCode();
        return result;
    }

    @Override
    public String toString() {
        //J-
        return MoreObjects.toStringHelper(DownloadRequestEntry.class)
                .add("sourceUrl", sourceUrl)
                .add("destinationFile", destinationFile)
                .toString();
        //J+
    }
}
