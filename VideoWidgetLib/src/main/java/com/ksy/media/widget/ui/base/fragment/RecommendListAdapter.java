package com.ksy.media.widget.ui.base.fragment;

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
public class RecommendListAdapter extends BaseAdapter {

    private final ArrayList<RecommendItem> items;
    private final Context context;

    public RecommendListAdapter(Context context, ArrayList<RecommendItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public RecommendItem getItem(int position) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.video_recomment_list_item_layout, null);
            holder.title_tv = (TextView) convertView.findViewById(R.id.title_tv);
            holder.watch_tv = (TextView) convertView.findViewById(R.id.watch_tv);
            holder.time_tv = (TextView) convertView.findViewById(R.id.time_tv);
            holder.content_tv = (TextView) convertView.findViewById(R.id.content_tv);
            holder.headImg = (ImageView) convertView.findViewById(R.id.icon_img);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.title_tv.setText(getItem(position).title);
        holder.time_tv.setText(getItem(position).time);
        holder.watch_tv.setText(getItem(position).watch);
        holder.content_tv.setText(getItem(position).content);
        return convertView;
    }

    static class ViewHolder {
        public TextView title_tv;
        public TextView time_tv;
        public TextView watch_tv;
        public TextView content_tv;
        public ImageView headImg;

    }
}
