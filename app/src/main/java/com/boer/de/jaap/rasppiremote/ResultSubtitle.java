package com.boer.de.jaap.rasppiremote;

/**
 * Created by jaap on 6-3-18.
 */

public class ResultSubtitle implements Result {
    private String title, downloads, url;

    ResultSubtitle (String title, String downloads, String url) {
        this.title = title;
        this.downloads = downloads;
        this.url = url;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    public String getDownloads() {
        return this.downloads;
    }

    public String getUrl() {
        return this.url;
    }

    @Override
    public String getRequest() {
        return "{\"title\":\"" + this.title + "\",\"url\":\"" + this.url + "\"}";
    }
}
