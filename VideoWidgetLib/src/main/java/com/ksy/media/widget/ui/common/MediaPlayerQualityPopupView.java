package com.ksy.media.widget.ui.common;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ksy.media.widget.model.MediaPlayerVideoQuality;
import com.ksy.mediaPlayer.widget.R;

public class MediaPlayerQualityPopupView {

	private Context mContext;
	private PopupWindow mPopupWindow;
	private ListView mListView;

	private QualityAdapter mAdapter;
	private List<MediaPlayerVideoQuality> mData;
	private Callback mCallback;

	private boolean isShowing = false;
	private MediaPlayerVideoQuality mCurrentSeletedQuality;

	public MediaPlayerQualityPopupView(Context context) {
		this.mContext = context;
		init();
	}

	private void init() {

		LayoutInflater inflater = LayoutInflater.from(mContext);
		View root = inflater.inflate(R.layout.blue_media_player_quality_popup_view,
				null);
		
		mListView = (ListView) root.findViewById(R.id.quality_list_view);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mCallback != null) {
					Toast.makeText(mContext.getApplicationContext(), "app should implements func by themselves", Toast.LENGTH_SHORT).show();
					if (mData != null && mData.size() > 0) {
						MediaPlayerVideoQuality quality = mData.get(position);
						if (quality != null) {
							mCallback.onQualitySelected(quality);
						}
					}
				}
			}
		});

		mAdapter = new QualityAdapter();
		mListView.setAdapter(mAdapter);

		mPopupWindow = new PopupWindow(mContext);
		mPopupWindow.setFocusable(true);
		mPopupWindow.setTouchable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					return true;
				}
				return false;
			}
		});
		
		mPopupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				isShowing = false;
				if (mCallback != null)
					mCallback.onPopupViewDismiss();
			}
		});
		mPopupWindow.setContentView(root);

	}

	public void show(View anchor, List<MediaPlayerVideoQuality> qualityList,
			MediaPlayerVideoQuality curQuality, int x, int y, int width,
			int height) {

		this.mData = qualityList;
		this.mCurrentSeletedQuality = curQuality;
		mAdapter.notifyDataSetChanged();
		mPopupWindow.setWidth(width);
		mPopupWindow.setHeight(height);
		mPopupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y);
		isShowing = true;

	}

	public void hide() {
		mPopupWindow.dismiss();
	}

	public boolean isShowing() {
		return isShowing;
	}

	public void setCallback(Callback callback) {
		mCallback = callback;
	}

	public Callback getCallback() {
		return mCallback;
	}

	public interface Callback {
		void onQualitySelected(MediaPlayerVideoQuality quality);

		void onPopupViewDismiss();
	}

	class QualityAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			if (mData != null)
				return mData.size();
			return 0;
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

			if (convertView == null) {
				convertView = new QualityItemView(mContext);
			}

			QualityItemView itemView = (QualityItemView) convertView;

			MediaPlayerVideoQuality quality = mData.get(position);
			itemView.initData(quality);

			return itemView;

		}

	}

	class QualityItemView extends RelativeLayout {

		private TextView mQualityTextView;
//		private ImageView mQualityImageView;

		public QualityItemView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			init(context);
		}

		public QualityItemView(Context context, AttributeSet attrs) {
			super(context, attrs);
			init(context);
		}

		public QualityItemView(Context context) {
			super(context);
			init(context);
		}

		private void init(Context context) {
			inflate(context, R.layout.blue_media_player_quality_item_view, this);
			mQualityTextView = (TextView) findViewById(R.id.quality_text_view);
//			mQualityImageView = (ImageView) findViewById(R.id.quality_image_view);

		}

		public void initData(MediaPlayerVideoQuality quality) {
			mQualityTextView.setText(quality.getName());
			if (null != mCurrentSeletedQuality
					&& quality == mCurrentSeletedQuality) {
//				setBackgroundResource(R.drawable.player_controller_list_item_grey_bg);
				setEnabled(false);
				/*mQualityTextView.setTextColor(getResources().getColor(
						R.color.player_quality_text_selector));*/
//				mQualityImageView.setVisibility(View.VISIBLE);
			} else {
				setEnabled(true);
				// mQualityTextView.setTextColor(getResources().getColor(R.color.controller_base_textcolor));
//				mQualityImageView.setVisibility(View.GONE);
			}
		}

	}

}
