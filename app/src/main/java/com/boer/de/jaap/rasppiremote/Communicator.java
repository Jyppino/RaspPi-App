package com.boer.de.jaap.rasppiremote;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

class Communicator {

    private String host, port;
    private int state; //DISCONNECTED=-1, IDLE=0, PAUSED=1, PLAYING=2
    private TextView torrentView, subtitleView;
    private ImageButton togglePlay1, togglePlay2;
    private RelativeLayout loadingView;
    private ImageView statusView, coverView;
    private boolean hasStarted = false;

    Communicator(View view, View actionbarView, String host, String port) {
        this.host = host;
        this.port = port;
        this.coverView = view.findViewById(R.id.coverView);
        this.torrentView = view.findViewById(R.id.torrentview);
        this.subtitleView = view.findViewById(R.id.subtitleview);
        this.togglePlay1 = view.findViewById(R.id.togglePlay2);
        this.togglePlay2 = view.findViewById(R.id.togglePlay);
        this.loadingView = view.findViewById(R.id.loadingPanel);
        this.statusView = actionbarView.findViewById(R.id.statusview);
        this.state = -1;
    }

    void setHost(String host) {
        this.host = host;
    }

    void setPort(String port) {
        this.port = port;
    }

    String getHost() {
        return this.host;
    }

    String getPort() {
        return this.port;
    }

    String getAddress() {
        return "http://" + this.host + ":" + this.port + "/";
    }

    boolean isConnected() {
        return this.state >= 0;
    }

    boolean isIdle() {
        return this.state == 0;
    }

    boolean isActive() {
        return this.state >= 1;
    }

    boolean isExpandedOnStart() {
        if (this.isActive() && !hasStarted) {
            this.hasStarted = true;
            return false;
        }
        return this.hasStarted;
    }

    int getState() {
        return this.state;
    }

    void update(String status) {
        switch (status) {
            case "IDLE":
                this.state = 0;
                statusView.setImageResource(R.drawable.ic_idle);
                this.hasStarted = false;
                break;
            case "DISCONNECTED":
                this.state = -1;
                statusView.setImageResource(R.drawable.ic_disconnected);
                break;
            case "PLAYING":
                this.state = 2;
                statusView.setImageResource(R.drawable.ic_active);
                break;
            case "PAUSED":
                this.state = 1;
                statusView.setImageResource(R.drawable.ic_active);
                break;
        }
        this.updatePlayButtons();
        this.setLoading(false);
    }

    public void updateImage(String title, String imageurl) {
        if (imageurl.equals("-")) {
            this.coverView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            this.coverView.setImageResource(R.mipmap.ic_launcher_pi_round);
        } else {
            Picasso.get().load(imageurl).into(this.coverView);
            this.coverView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
    }

    private void updatePlayButtons() {
        if (this.state == 2) {
            togglePlay1.setImageResource(R.drawable.ic_pause_circle);
            togglePlay2.setImageResource(R.drawable.ic_pause);
        } else {
            togglePlay1.setImageResource(R.drawable.ic_play_circle);
            togglePlay2.setImageResource(R.drawable.ic_play);
        }
    }

    void updateTorrentTitle(String title) {
        if (title.equals("")) {
            this.torrentView.setText("No torrent specified");
        } else {
            this.torrentView.setText(title);
        }
    }

    void updateSubtitleTitle(String title) {
        if (title.equals("")) {
            this.subtitleView.setText("No subtitles");
        } else {
            this.subtitleView.setText(title);
        }
    }

    boolean hasSubs() {
        return !this.subtitleView.getText().toString().equals("No subtitles");
    }

    void setLoading(boolean loading) {
        if (loading) {
            this.loadingView.setVisibility(View.VISIBLE);
        } else {
            this.loadingView.setVisibility(View.GONE);
        }
    }
}
