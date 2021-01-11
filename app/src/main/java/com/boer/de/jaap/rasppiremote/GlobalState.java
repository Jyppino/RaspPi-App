package com.boer.de.jaap.rasppiremote;

import android.app.Application;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class GlobalState extends Application {

    private Socket socket = null;
    private String host = "";

    public Socket createSocket(String host) {
        this.destroy();
        this.host = host;
        try {
            IO.Options opts = new IO.Options();
            opts.reconnection = true;
            opts.forceNew = true;
            socket = IO.socket(host, opts);

            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                }

            }).on("state", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    String newState = (String) args[0];
                    EventBus.getDefault().post(new statusEvent(newState));
                }

            }).on("fullstate", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    JSONObject obj = (JSONObject) args[0];
                    try {
                        String state = obj.getString("state");
                        String torTitle = obj.getString("torTitle");
                        String subTitle = obj.getString("subTitle");
                        String torURL = obj.getString("torURL");
                        String subURL = obj.getString("subURL");
                        String imageURL = obj.getString("image");
                        String title = obj.getString("title");
                        int season = obj.getInt("season");
                        int episode = obj.getInt("episode");

                        if (torTitle.equals("-")) {
                            EventBus.getDefault().post(new statusEvent(state));
                        } else {
                            EventBus.getDefault().post(new fullStatusEvent(state, title, torTitle, imageURL, subTitle, torURL, subURL, season, episode));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        EventBus.getDefault().post(new messageEvent("Failed while parsing status", true));
                    }
                }

            }).on("message", new Emitter.Listener() {

                @Override
                public void call(final Object... args) {

                    try {
                        JSONObject object = (JSONObject) args[0];
                        String message = object.getString("message");
                        Boolean last = object.getBoolean("last");
                        EventBus.getDefault().post(new messageEvent(message, last));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        EventBus.getDefault().post(new messageEvent("Failed while parsing message", true));
                    }

                }

            }).on("torrentResults", new Emitter.Listener() {

                @Override
                public void call(final Object... args) {
                    JSONArray obj = (JSONArray) args[0];
                    EventBus.getDefault().post(new resultEvent(obj, "", "torrentresult"));
                }

            }).on("subtitleResults", new Emitter.Listener() {

                @Override
                public void call(final Object... args) {
                    try {
                        JSONObject object = (JSONObject) args[0];
                        JSONArray objectArray = new JSONArray("[]");
                        if (!object.isNull("en")) {
                            objectArray = (JSONArray) object.get("en");
                        }
                        EventBus.getDefault().post(new resultEvent(objectArray, "", "subtitleresult"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        EventBus.getDefault().post(new messageEvent("Failed while parsing results", true));
                    }

                }

            }).on("magnetURL", new Emitter.Listener() {

                @Override
                public void call(final Object... args) {
                    String url = (String) args[0];
                    EventBus.getDefault().post(new resultEvent(null, url, "magneturl"));
                }

            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    EventBus.getDefault().post(new statusEvent("DISCONNECTED"));
                }
            }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    EventBus.getDefault().post(new statusEvent("DISCONNECTED"));
                }
            });
            socket.connect();

        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            EventBus.getDefault().post(new statusEvent("DISCONNECTED"));
        }
        return socket;
    }

    public Socket reconnect() {
        if (this.socket == null) {
            return null;
        }
        return this.createSocket(this.host);
    }

    public Socket getSocket() {
        return this.socket;
    }

    public void destroy() {
        if (this.socket != null) {
            this.socket.disconnect();
            this.socket.off();
            this.socket = null;
        }
    }

    public boolean isConnected() {
        return this.socket != null && this.socket.connected();
    }

    class statusEvent {
        String status;

        statusEvent(String status) {
            this.status = status;
        }
    }

    class fullStatusEvent {
        String state, title, torTitle, subTitle, torURL, subURL, image;
        int season, episode;

        fullStatusEvent(String state, String title, String torTitle, String image, String subTitle, String torURL, String subURL, int season, int episode) {
            this.state = state;
            this.title = title;
            this.torTitle = torTitle;
            this.subTitle = subTitle;
            this.torURL = torURL;
            this.subURL = subURL;
            this.image = image;
            this.season = season;
            this.episode = episode;
        }
    }

    class resultEvent {
        JSONArray results;
        String result;
        String type;

        resultEvent(JSONArray results, String result, String type) {
            this.results = results;
            this.type = type;
            this.result = result;
        }
    }

    class messageEvent {
        String message;
        Boolean last;

        messageEvent(String message, Boolean last) {
            this.message = message;
            this.last = last;
        }
    }
}
