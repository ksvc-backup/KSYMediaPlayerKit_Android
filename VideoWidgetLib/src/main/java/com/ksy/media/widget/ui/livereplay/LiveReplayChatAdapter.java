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
	private List<Map<String, Object>> dataReplayReceive;

	public LiveReplayChatAdapter(Context context, List<Map<String, Object>> data) {
		dataReplayReceive = data;
		this.mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {

		return dataReplayReceive.size();
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

		if (dataReplayReceive.size() > position) {
			viewHolder.commendHeadImage.setBackgroundResource((Integer) dataReplayReceive.get(position)
					.get("img"));
			viewHolder.userNameText.setText((String) dataReplayReceive.get(position).get("title"));
			viewHolder.commendText.setText((String) dataReplayReceive.get(position).get("info"));
		}/* else {
			viewHolder.commendHeadImage.setBackgroundResource((Integer) dataReplayReceive.get(1)
					.get("img"));
			viewHolder.userNameText.setText((String) dataReplayReceive.get(1).get("title"));
			viewHolder.commendText.setText((String) dataReplayReceive.get(1).get("info"));
		}*/
		
		return convertView;
	}

	class ViewHolder {
		public ImageView commendHeadImage;
		public TextView userNameText;
		public TextView commendText;
	}

}