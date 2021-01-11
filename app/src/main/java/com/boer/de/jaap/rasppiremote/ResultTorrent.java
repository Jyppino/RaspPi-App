package com.boer.de.jaap.rasppiremote;

/**
 * Created by jaap on 1-3-18.
 */

class ResultTorrent implements Result {
    String title, date, seeds, size;
    private String desc, provider;

    ResultTorrent(String title, String date, String seeds, String size, String desc, String provider) {
        this.title = title;
        this.date = date;
        this.seeds = seeds;
        this.size = size;
        this.desc = desc;
        this.provider = provider;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public String getRequest() {
        return "{\"desc\":\"" + this.desc + "\",\"provider\":\"" + this.provider + "\"}";
    }
}
