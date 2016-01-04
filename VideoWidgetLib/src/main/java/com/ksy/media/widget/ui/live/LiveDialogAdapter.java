package com.ksy.media.widget.ui.live;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ksy.mediaPlayer.widget.R;

import java.util.List;

/**
 * @author LIXIAOPENG
 *
 */
public class LiveDialogAdapter extends BaseAdapter {

	private List<LiveDialogInfo> liveReplayInfoList;

	private LayoutInflater inflater;

	public LiveDialogAdapter(List<LiveDialogInfo> videoInfoList,
							 Context mContext) {
		this.liveReplayInfoList = videoInfoList;
		// this.mContext = mContext;
		inflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		return 5/*videoInfoList.size()*/;
	}

	@Override
	public Object getItem(int position) {
		return liveReplayInfoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public void refreshList(List<LiveDialogInfo> list) {
		this.liveReplayInfoList = list;
		notifyDataSetInvalidated();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		
		if (convertView == null) {
			viewHolder = new ViewHolder();

			convertView = inflater.inflate(R.layout.live_top_item, null);
			
			viewHolder.commendHeadImage = (ImageView)convertView.findViewById(R.id.live_imageview);
			viewHolder.userNameText = (TextView)convertView.findViewById(R.id.live_username);
			viewHolder.commendText = (TextView)convertView.findViewById(R.id.live_comment);
			
			convertView.setTag(viewHolder);
			
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.commendHeadImage.setImageResource(R.drawable.live_person_grey);
//		viewHolder.videoTextName.setText(videoInfoList.get(position).getDisplayName());
		viewHolder.userNameText.setText("用户名");
		viewHolder.commendText.setText("评论内容评论内容评论内容");
		
		return convertView;
	}

	
	class ViewHolder {
		public ImageView commendHeadImage;
		public TextView userNameText;
		public TextView commendText;
	}
	
}

