package com.ksy.media.widget.ui.base;

import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.ksy.media.widget.util.Constants;
import com.ksy.mediaPlayer.widget.R;

/**
 * @Description 亮度控制所有控制
 * @author LIXIAOPENG
 * @date 2015-6-3
 */
public class MediaPlayerControllerBrightView extends RelativeLayout {

	private static final int MAX_PROGRESS = 100;

	private MediaPlayerBrightSeekBar mSeekBrightProgress;

	private Callback mCallback;
	private MediaPlayerVolumeSeekBar.onScreenShowListener mOnScreenShowListener;

	// 新添加
	private static final int MAX_BRIGNTNESS = 255;
	private static final int MIN_BRIGNTNESS = 40;
	private Context mContext;
	private Window mWindow;
	WindowManager.LayoutParams wl;
	private float distanceY = 0;
	private int current = 0;

	public MediaPlayerControllerBrightView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public MediaPlayerControllerBrightView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public MediaPlayerControllerBrightView(Context context) {
		super(context);
		init(context);
	}

	public void setOnScreenShowListener(
			MediaPlayerVolumeSeekBar.onScreenShowListener mOnScreenShowListener) {
		Log.d(Constants.LOG_TAG, " listener set in C V");
		if (mOnScreenShowListener != null) {
			// mSeekBarVolumeProgress
			// .setOnScreenShowListener(mOnScreenShowListener);
		}
	}

	public void init(final Context context) {
		this.mContext = context;
		View root = LayoutInflater.from(context).inflate(
				R.layout.blue_media_player_controller_bright_view, this);

		mSeekBrightProgress = (MediaPlayerBrightSeekBar) root
				.findViewById(R.id.seekbar_bright_progress);

		// 进度条绑定最大亮度，255是最大亮度
		mSeekBrightProgress.setMax(MAX_BRIGNTNESS);
		// 取得当前亮度
		int normal = Settings.System.getInt(context.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS, MAX_BRIGNTNESS);

		// 进度条绑定当前亮度
		mSeekBrightProgress.setProgress(normal);

		mSeekBrightProgress
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						Log.d(Constants.LOG_TAG, "onStopTrackingTouch ....");

					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {

						// 取得当前进度
						int tmpInt = seekBar.getProgress();
						Log.d(Constants.LOG_TAG, "tmpInt =" + tmpInt + ">>mWindow=="
								+ mWindow);
						// 当进度小于40时，防止太黑看不见的后果
						if (tmpInt < MIN_BRIGNTNESS) {
							tmpInt = MIN_BRIGNTNESS;
						}

						Log.d(Constants.LOG_TAG,
								"mContext =" + mContext
										+ ">>mContext.getContentResolver()="
										+ mContext.getContentResolver());
						// 根据当前进度改变亮度
						Settings.System.putInt(mContext.getContentResolver(),
								Settings.System.SCREEN_BRIGHTNESS, tmpInt);
						tmpInt = Settings.System.getInt(
								mContext.getContentResolver(),
								Settings.System.SCREEN_BRIGHTNESS, -1);

						if (mWindow != null) {
							wl = mWindow.getAttributes();
						} else {

							return;
						}

						float tmpFloat = (float) tmpInt / MAX_BRIGNTNESS;

						if (tmpFloat > 0 && tmpFloat <= 1) {
							wl.screenBrightness = tmpFloat;
						}

						mWindow.setAttributes(wl);

					}
				});
	}

	/**
	 * @Description 传window过来
	 * @param window
	 */
	public void onGestureLightChange(float y, Window window) {
		mWindow = window;
		distanceY = y;

		current = (int) (distanceY) + mSeekBrightProgress.getProgress();

		mSeekBrightProgress.post(new Runnable() {
			@Override
			public void run() {
				mSeekBrightProgress.setProgress(current);
			}
		});

	}

	public void setCallback(Callback callback) {
		mCallback = callback;
	}

	public interface Callback {
		void onVolumeProgressChanged(AudioManager audioManager, float percentage);
	}


	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int eventAction = event.getAction();
		int keyCode = event.getKeyCode();
		/*
		 * if (eventAction == KeyEvent.ACTION_DOWN && (keyCode ==
		 * KeyEvent.KEYCODE_VOLUME_UP// 音量+ || keyCode ==
		 * KeyEvent.KEYCODE_VOLUME_DOWN)) {// 音量- isChangedFromOnKeyChange =
		 * true; updateSeekBarVolumeProgress(); return true; }
		 */
		return super.dispatchKeyEvent(event);
	}

}
