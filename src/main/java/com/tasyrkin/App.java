package com.tasyrkin;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import com.tasyrkin.yandex.downloader.DownloadController;
import com.tasyrkin.yandex.downloader.DownloadSourceAndDestination;
import com.tasyrkin.yandex.downloader.DownloadState;

/**
 * Hello world!
 */
public class App {
    public static void main(final String[] args) throws MalformedURLException, InterruptedException {

        DownloadSourceAndDestination srcDst1 = new DownloadSourceAndDestination(new URL("http://www.google.com"),
                new File("/tmp/google." + System.currentTimeMillis() + ".html"));

        List<DownloadSourceAndDestination> srcsAndDsts = Lists.newArrayList(srcDst1);
        DownloadController controller = new DownloadController(srcsAndDsts);

        controller.startDownload();

        Map<DownloadSourceAndDestination, DownloadState> stateMap = controller.getState();
        System.out.println(stateMap.get(srcDst1));

        int cnt = 0;
        while (true) {
            Thread.sleep(1000);

            stateMap = controller.getState();

            System.out.println("step[" + cnt++ + "]: " + stateMap.get(srcDst1));
        }

    }
}
