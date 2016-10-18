package com.ksy.media.widget.ui.shortvideo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ksy.mediaPlayer.widget.R;

import java.util.ArrayList;

/**
 * Created by eflakemac on 15/12/9.
 */
public class ShortVideoListAdapter extends BaseAdapter {

    private final ArrayList<ShortMovieItem> items;
    private final Context context;

    public ShortVideoListAdapter(Context context, ArrayList<ShortMovieItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ShortMovieItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.short_video_list_item_layout, null);
            holder.info_tv = (TextView) convertView.findViewById(R.id.info_tv);
            holder.info_time = (TextView) convertView.findViewById(R.id.info_time);
            holder.comment_tv = (TextView) convertView.findViewById(R.id.comment_tv);
            holder.headImg = (ImageView) convertView.findViewById(R.id.icon_img);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.comment_tv.setText(getItem(position).comment);
        holder.info_tv.setText(getItem(position).info);
        holder.info_time.setText(getItem(position).time);
        return convertView;
    }

    static class ViewHolder {
        public TextView info_tv;
        public TextView info_time;
        public TextView comment_tv;
        public ImageView headImg;

    }
}
