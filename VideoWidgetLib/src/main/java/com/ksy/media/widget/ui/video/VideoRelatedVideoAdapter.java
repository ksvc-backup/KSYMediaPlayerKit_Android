package com.ksy.media.widget.ui.video;

import java.util.List;

import com.ksy.mediaPlayer.widget.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @description 相关视频
 * @author LIXIAOPENG
 *
 */
public class VideoRelatedVideoAdapter extends BaseAdapter {

	private List<VideoRelateVideoInfo> videoInfoList;

	private LayoutInflater inflater;

	public VideoRelatedVideoAdapter(List<VideoRelateVideoInfo> videoInfoList,
									Context mContext) {
		this.videoInfoList = videoInfoList;
		// this.mContext = mContext;
		inflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		return 6/*videoInfoList.size()*/;
	}

	@Override
	public Object getItem(int position) {
		return videoInfoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public void refreshList(List<VideoRelateVideoInfo> list) {
		this.videoInfoList = list;
		notifyDataSetInvalidated();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = inflater.inflate(R.layout.blue_media_player_relate_videoinfo, null);
			
			viewHolder.videoImage = (ImageView)convertView.findViewById(R.id.imageview_poster);
			viewHolder.videoTextName = (TextView)convertView.findViewById(R.id.tv_movie_name);
			viewHolder.videoTextDuration = (TextView)convertView.findViewById(R.id.tv_movie_time);
			
			convertView.setTag(viewHolder);
			
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.videoImage.setImageResource(R.drawable.video_relate);
//		viewHolder.videoTextName.setText(videoInfoList.get(position).getDisplayName());
		viewHolder.videoTextName.setText("速度与激情");
		viewHolder.videoTextDuration.setText("片长:" + "120" + "分钟");
		
		return convertView;
	}

	
	class ViewHolder {
		public ImageView videoImage;
		public TextView videoTextName;
		public TextView videoTextDuration;
	}
	
}

