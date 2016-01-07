package com.ksy.media.widget.ui.livereplay;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ksy.mediaPlayer.widget.R;

import java.util.List;
import java.util.Map;

public class LiveReplayChatAdapter extends BaseAdapter {

	private LayoutInflater mInflater = null;
	private List<Map<String, Object>> dataReceive;

	public LiveReplayChatAdapter(Context context, List<Map<String, Object>> data) {
		dataReceive = data;
		this.mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {

		return dataReceive.size();
	}

	@Override
	public Object getItem(int position) {

		return position;
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.live_replay_top_item, null);

			viewHolder.commendHeadImage = (ImageView)convertView.findViewById(R.id.live_replay_imageview);
			viewHolder.userNameText = (TextView)convertView.findViewById(R.id.live_replay_username);
			viewHolder.commendText = (TextView)convertView.findViewById(R.id.live_replay_comment);

			convertView.setTag(viewHolder);
			
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.commendHeadImage.setBackgroundResource((Integer) dataReceive.get(position)
				.get("img"));
		viewHolder.userNameText.setText((String) dataReceive.get(position).get("title"));
		viewHolder.commendText.setText((String) dataReceive.get(position).get("info"));
		
		return convertView;
	}

	class ViewHolder {
		public ImageView commendHeadImage;
		public TextView userNameText;
		public TextView commendText;
	}

}