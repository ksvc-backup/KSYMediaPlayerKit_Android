package com.ksy.media.widget.ui.base;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ksy.media.widget.util.MediaPlayerUtils;
import com.ksy.mediaPlayer.widget.R;

public class MediaPlayerSeekView extends RelativeLayout {

	private static final int SEEK_STATE_NONE = 0;
	private static final int SEEK_STATE_FORWARD = 1;
	private static final int SEEK_STATE_BACK = 2;

	private static final int MAX_SEEK_DURATION = 2 * 60 * 1000;

	private volatile int mSeekState = SEEK_STATE_NONE;

	private Context mContext;

	private static final int DEFAULT_TIMEOUT = 1000;
	private static final int MSG_SHOW = 0;
	private static final int MSG_HIDE = MSG_SHOW + 1;
	private static final int MSG_PARAM_HIDE_NO_ANIMATION = 100;

	private static final int LEVEL_SEEK = 3600;

	private static final int SEEK_END_DELAY_TIME = 2000;

//	private RelativeLayout mLayoutSeekState;
	private ImageView imageStateForward;
	private ImageView imageStateRewind;
	private TextView textViewSign;
	
//	private TextView mTvSeekStateFastForward;
//	private TextView mTvSeekStateRewind;
	private TextView mTvSeekCurrentPosition;
//	private TextView mTvSeekTotalPosition;

	private long mInitPosition = -1;
	private long mTotalPosition = -1;
	private long mSeekPosition = -1;

	private Animation mAnimationHide;

	private float mMinSeekDistance;
	private OnGuestureChangeListener mOnGuestureChangeListener;

	public interface OnGuestureChangeListener {
		void onLightChanged();

		void onVolumeChanged();

		void onPlayProgressChanged();

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
				if (null != mOnGuestureChangeListener) {
					mOnGuestureChangeListener.onLightChanged();
				}
				setVisibility(View.VISIBLE);
				break;
			default:
				break;
			}
		}
	};

	public MediaPlayerSeekView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public MediaPlayerSeekView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public MediaPlayerSeekView(Context context) {
		super(context);
		init(context);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
//		mLayoutSeekState = (RelativeLayout) findViewById(R.id.layout_seek_status);
//		mTvSeekStateFastForward = (TextView) findViewById(R.id.tv_seek_status_fastforward);
//		mTvSeekStateRewind = (TextView) findViewById(R.id.tv_seek_status_rewind);
		
		//快进
		imageStateForward = (ImageView)findViewById(R.id.image_seek_speed);
		imageStateRewind = (ImageView)findViewById(R.id.image_seek_rewind);
		textViewSign = (TextView)findViewById(R.id.tv_sign);
		mTvSeekCurrentPosition = (TextView) findViewById(R.id.tv_seek_current_position);
		
//		mTvSeekTotalPosition = (TextView) findViewById(R.id.tv_seek_total_position);
	}

	private void init(Context context) {

		this.mContext = context;
		View rootView = LayoutInflater.from(mContext).inflate(
				R.layout.blue_media_player_seek_view, null);
		
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		addView(rootView, params);

		mMinSeekDistance = ViewConfiguration.get(mContext).getScaledTouchSlop();

		mAnimationHide = new AlphaAnimation(1, 0.5f);
		mAnimationHide.setInterpolator(new AccelerateInterpolator());
		mAnimationHide.setDuration(300);
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

	// 记录一次有效手势滑动的总距离,改值是由于多次的delta累加而成,要大于一个基础阀值,才能真正实现效果
	private float mTotalDeltaSeekDistance = 0;

	public void onGestureSeekChange(float deltaSeekDistance,
			float totalSeekDistance) {
		mTotalDeltaSeekDistance = mTotalDeltaSeekDistance + deltaSeekDistance;
		// if(mTotalPosition > 1000){
		// mMinSeekDistance = totalSeekDistance / (mTotalPosition / 1000);
		// }else{
		// mMinSeekDistance = totalSeekDistance / LEVEL_SEEK;
		// }
		// Log.d("onGestureSeekChange deltaSeekDistance,mTotalDeltaSeekDistance = "
		// + deltaSeekDistance+"-"+mTotalDeltaSeekDistance);
		if (Math.abs(mTotalDeltaSeekDistance) >= mMinSeekDistance) {
			float deltaSeekPercentage = mTotalDeltaSeekDistance
					/ totalSeekDistance;
			mSeekPosition = (int) (mSeekPosition + (deltaSeekPercentage * MAX_SEEK_DURATION)); // 表示每次最多只操作MAX_SEEK_DURATION的mTotalPosition
			// 快进
			if (mSeekPosition > mInitPosition) {
				if (mSeekState != SEEK_STATE_FORWARD) {
					mSeekState = SEEK_STATE_FORWARD;
					if (imageStateRewind.getVisibility() == View.VISIBLE) {
						imageStateRewind.setVisibility(View.INVISIBLE);
					}
					
					if (imageStateForward.getVisibility() != View.VISIBLE) {
						imageStateForward.setVisibility(View.VISIBLE);
						textViewSign.setText(R.string.time_plus);
					}
				}
			}
			// 快退
			else {
				if (mSeekState != SEEK_STATE_BACK) {
					mSeekState = SEEK_STATE_BACK;
					if (imageStateForward.getVisibility() == View.VISIBLE) {
						imageStateForward.setVisibility(View.INVISIBLE);
					}
					if (imageStateRewind.getVisibility() != View.VISIBLE) {
						imageStateRewind.setVisibility(View.VISIBLE);
						textViewSign.setText(R.string.time_minute);
					}
				}
			}

			if (mSeekPosition < 0) {
				mSeekPosition = 0;
			}
			// else if(mSeekPosition > mTotalPosition){
			// //
			// 当视频seek到结尾时,时间提前SEEK_END_DELAY_TIME秒作为最后seek点,防止播放器直接seek到结尾不响应结束标志
			// if(mTotalPosition > SEEK_END_DELAY_TIME){
			// mSeekPosition = mTotalPosition - SEEK_END_DELAY_TIME;
			// }
			// else{
			// mSeekPosition = mTotalPosition;
			// }
			// }
			else {
				// 当视频seek到结尾时,时间提前SEEK_END_DELAY_TIME秒作为最后seek点,防止直接拖动到视频末尾出现一些异常情况
				long totalEndDelayTime = mTotalPosition - SEEK_END_DELAY_TIME;
				if (totalEndDelayTime <= 0) {
					mSeekPosition = 0;
				} else if (mSeekPosition >= totalEndDelayTime) {
					mSeekPosition = totalEndDelayTime;
				}
			}

			mTvSeekCurrentPosition.setText(MediaPlayerUtils
					.getVideoDisplayTime(mSeekPosition));

			mTotalDeltaSeekDistance = 0;
			show();

		}

	}

	public void onGestureSeekBegin(long currentPosition, long totalPosition) {

		if (currentPosition < 0 || totalPosition < 0
				|| currentPosition > totalPosition)
			return;

		mInitPosition = currentPosition;
		mTotalPosition = totalPosition;
		mSeekPosition = currentPosition;
		mSeekState = SEEK_STATE_NONE;

//		mTvSeekTotalPosition.setText(MediaPlayerUtils
//				.getVideoDisplayTime(totalPosition));

	}

	public long onGestureSeekFinish() {

		long resultSeekPosition = mSeekPosition;

		mTotalDeltaSeekDistance = 0;
		mInitPosition = -1;
		mTotalPosition = -1;
		mSeekPosition = -1;
		mSeekState = SEEK_STATE_NONE;

		return resultSeekPosition;

	}

	private void show() {
		show(DEFAULT_TIMEOUT);
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

	public void setOnGuestureChangeListener(
			OnGuestureChangeListener onGuestureChangeListener) {
		this.mOnGuestureChangeListener = onGuestureChangeListener;
	}

}
