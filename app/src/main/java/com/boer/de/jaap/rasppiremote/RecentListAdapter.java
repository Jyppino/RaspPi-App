package com.boer.de.jaap.rasppiremote;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class RecentListAdapter extends BaseAdapter {
    private List<RecentItem> recentItemList;
    private LayoutInflater inflater;
    private Context context;


    RecentListAdapter(Context c) {
        this.recentItemList = new ArrayList<>();
        this.inflater = ((Activity)c).getLayoutInflater();
        this.context = c;
    }

    void addItem(RecentItem recentItem) {
        this.removeIfPresent(recentItem);
        this.recentItemList.add(0, recentItem);
    }

    void removeItem(int pos) {
        this.recentItemList.remove(pos);
    }

    void clearList() {
        this.recentItemList = new ArrayList<>();
    }

    private void removeIfPresent(RecentItem recentItem) {
        for (int i = 0 ; i < this.recentItemList.size() ; i++) {
            RecentItem obj = this.recentItemList.get(i);
            if (obj.getSearchTerm().equals(recentItem.getSearchTerm())) {
                this.recentItemList.remove(i);
                break;
            }
        }
    }

    @Override
    public int getCount() {
        return recentItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return recentItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.recent_item, null);
            viewHolder = new ViewHolder();
            viewHolder.imageView = convertView.findViewById(R.id.recentImageView);
            viewHolder.titleText = convertView.findViewById(R.id.recentTitleText);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String title = ((RecentItem) this.getItem(position)).getSearchTerm();
        viewHolder.imageURL = ((RecentItem) this.getItem(position)).getImageURL();
        viewHolder.titleText.setText(title);

        if (viewHolder.imageURL.equals("-")) {
            Picasso.get().load(R.drawable.ic_picture).into(viewHolder.imageView);
        } else {
            Picasso.get().load(viewHolder.imageURL).into(viewHolder.imageView);
            viewHolder.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
        return convertView;
    }

    public JSONArray getJSSON() {
        JSONArray res = new JSONArray();
        for (int i = 0 ; i < this.getCount() ; i++) {
            JSONObject obj = ((RecentItem) this.getItem(i)).getJSON();
            res.put(obj);
        }
        return res;
    }

    private class ViewHolder {
        GridViewItem imageView;
        TextView titleText;
        String imageURL;
    }
}


