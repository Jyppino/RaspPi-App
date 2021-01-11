package com.boer.de.jaap.rasppiremote;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jaap on 31-3-18.
 */

public class RecentItem {
    private String searchTerm, imageURL;
    private int season, episode;

    RecentItem(String searchTerm, String imageURL, int season, int episode) {
        this.searchTerm = searchTerm;
        this.imageURL = imageURL;
        this.season = season;
        this.episode = episode;
    }

    public String getImageURL() {
        return this.imageURL;
    }

    public String getSearchTerm() {
        return this.searchTerm;
    }

    public int getSeason() {
        return this.season;
    }

    public int getEpisode() {
        return this.episode;
    }

    public JSONObject getJSON() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("searchTerm", this.searchTerm);
            obj.put("imageURL", this.imageURL);
            obj.put("season", this.season);
            obj.put("episode", this.episode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
