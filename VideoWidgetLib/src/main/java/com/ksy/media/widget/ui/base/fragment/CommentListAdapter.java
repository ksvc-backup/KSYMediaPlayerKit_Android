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
public class CommentListAdapter extends BaseAdapter {

    private final ArrayList<CommentItem> items;
    private final Context context;

    public CommentListAdapter(Context context, ArrayList<CommentItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public CommentItem getItem(int position) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.video_comment_list_item_layout, null);
            holder.user_tv = (TextView) convertView.findViewById(R.id.user_tv);
            holder.comment_tv = (TextView) convertView.findViewById(R.id.comment_tv);
            holder.time_tv = (TextView) convertView.findViewById(R.id.time_tv);
            holder.headImg = (ImageView) convertView.findViewById(R.id.icon_img);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.comment_tv.setText(getItem(position).comment);
        holder.time_tv.setText(getItem(position).time);
        holder.user_tv.setText(getItem(position).user);
        return convertView;
    }

    static class ViewHolder {
        public TextView user_tv;
        public TextView time_tv;
        public TextView comment_tv;
        public ImageView headImg;

    }
}
