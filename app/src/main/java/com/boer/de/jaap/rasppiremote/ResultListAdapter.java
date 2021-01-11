package com.boer.de.jaap.rasppiremote;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ResultListAdapter extends BaseAdapter{

    private MainActivity main;
    private LayoutInflater inflater = null;
    public final int TYPE_TORRENT = 0;
    public final int TYPE_SUBS = 1;

    ResultListAdapter(MainActivity main) {
        this.main = main;
        this.inflater = (LayoutInflater) main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return this.main.results.size();
    }

    @Override
    public Object getItem(int position) {
        return this.main.results.get(position);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return (this.main.results.get(position) instanceof ResultTorrent) ? TYPE_TORRENT : TYPE_SUBS;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean isEnabled(int position) {
        return !(this.main.findViewById(R.id.loadingPanel).getVisibility() == View.VISIBLE);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        TorrentViewHolder torHolder = null;
        SubtitleViewHolder subHolder = null;

        int type = getItemViewType(position);

        if (view == null) {

            switch (type) {
                case TYPE_TORRENT:
                    torHolder = new TorrentViewHolder();
                    view = this.inflater.inflate(R.layout.item_torrent, null);
                    torHolder.title = (TextView) view.findViewById(R.id.title);
                    torHolder.date = (TextView) view.findViewById(R.id.date);
                    torHolder.seeds = (TextView) view.findViewById(R.id.seeds);
                    torHolder.size = (TextView) view.findViewById(R.id.size);
                    view.setTag(torHolder);
                    break;
                case TYPE_SUBS:
                    subHolder = new SubtitleViewHolder();
                    view = this.inflater.inflate(R.layout.item_subtitle, null);
                    subHolder.title = (TextView) view.findViewById(R.id.title);
                    subHolder.downloads = (TextView) view.findViewById(R.id.downloads);
                    view.setTag(subHolder);
                    break;
            }
        } else {
            switch (type) {
                case TYPE_TORRENT:
                    torHolder = (TorrentViewHolder) view.getTag();
                    break;
                case TYPE_SUBS:
                    subHolder = (SubtitleViewHolder) view.getTag();
                    break;
            }
        }

        switch (type) {
            case TYPE_TORRENT:
                ResultTorrent curTor = (ResultTorrent) this.main.results.get(position);
                torHolder.title.setText(curTor.title);
                torHolder.date.setText(curTor.date);
                torHolder.seeds.setText(curTor.seeds);
                torHolder.size.setText(curTor.size);
                break;
            case TYPE_SUBS:
                ResultSubtitle curSub = (ResultSubtitle) this.main.results.get(position);
                subHolder.title.setText(curSub.getTitle());
                subHolder.downloads.setText(curSub.getDownloads());
                break;
        }
        return view;
    }

    private static class TorrentViewHolder {
        TextView title, date, seeds, size;
    }

    private static class SubtitleViewHolder {
        TextView title, downloads;
    }
}
