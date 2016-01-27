package com.ksy.media.widget.ui.base;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ksy.media.widget.videoview.MediaPlayerTextureView;
import com.ksy.mediaPlayer.widget.R;

/**
 * 视频的比例大小
 * 
 * @author LIXIAOPENG
 * 
 */
public class MediaPlayerMovieRatioView extends RelativeLayout {

	private Context mContext;
	private static final int DEFAULT_TIMEOUT = 1500;
	private static final int MSG_SHOW = 0;
	private static final int MSG_HIDE = MSG_SHOW + 1;
	private static final int MSG_PARAM_HIDE_NO_ANIMATION = 100;
	private TextView mCurrentRatioTv;
	private Animation mAnimationHide;
	private MoiveRatioChangeListener movieRatioChangeListener;

	public interface MoiveRatioChangeListener {
		public void onMovieRatioChange(int mode);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_HIDE:
				clearAnimation();
				if (msg.arg1 == MSG_PARAM_HIDE_NO_ANIMATION) {
					setVisibility(View.GONE);
				} else {
					startAnimation(mAnimationHide);
				}
				break;
			case MSG_SHOW:
				setVisibility(View.VISIBLE);
				break;
			default:
				break;
			}
		}
	};

	private String[] mRatios;

	private int mCurrentIndex;

	public MediaPlayerMovieRatioView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public MediaPlayerMovieRatioView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public MediaPlayerMovieRatioView(Context context) {
		super(context);
		init(context);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mCurrentRatioTv = (TextView) findViewById(R.id.tv_ratio_mode);
	}

	private void init(Context context) {
		this.mContext = context;
		
		View rootView = LayoutInflater.from(mContext).inflate(
				R.layout.blue_media_player_video_ratio_view, null);

		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		addView(rootView, params);
		mRatios = getResources().getStringArray(R.array.video_ratio);
		mAnimationHide = new AlphaAnimation(1, 0.5f);
		mAnimationHide.setInterpolator(new AccelerateInterpolator());
		mAnimationHide.setDuration(1000);
		mAnimationHide.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				setVisibility(View.GONE);
			}
		});

	}

	public void show() {
		show(DEFAULT_TIMEOUT);
		mCurrentRatioTv.setText(mRatios[mCurrentIndex]);
		mCurrentIndex++;
		if (mCurrentIndex > MediaPlayerTextureView.MOVIE_RATIO_MODE_4_3) {
			mCurrentIndex = 0;
		}
	}

	private void show(int timeMs) {

		mHandler.sendEmptyMessage(MSG_SHOW);

		mHandler.removeMessages(MSG_HIDE);

		if (timeMs > 0) {
			Message msgHide = mHandler.obtainMessage(MSG_HIDE);
			msgHide.arg1 = MSG_PARAM_HIDE_NO_ANIMATION;
			mHandler.sendMessageDelayed(msgHide, timeMs);
		}

	}

	public void hide(boolean now) {

		Message msgHide = mHandler.obtainMessage(MSG_HIDE);
		if (now) {
			msgHide.arg1 = MSG_PARAM_HIDE_NO_ANIMATION;
		}
		mHandler.sendMessage(msgHide);

	}

	public boolean isShowing() {
		return (getVisibility() == View.VISIBLE ? true : false);
	}

	public void setMovieRatioChangeListener(
			MoiveRatioChangeListener movieRatioChangeListener) {
		this.movieRatioChangeListener = movieRatioChangeListener;
	}

}
