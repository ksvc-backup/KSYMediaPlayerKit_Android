package com.ksy.media.widget.ui.base;

import android.content.Context;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ksy.media.widget.util.Constants;
import com.ksy.mediaPlayer.widget.R;

/**
 * @Description 声音控制
 * @author LIXIAOPENG
 * @date 2015-6-2
 */
public class MediaPlayerControllerVolumeView extends RelativeLayout implements
		OnClickListener {

	private static final int MAX_PROGRESS = 100;

	private AudioManager mAudioManager;
//	private MediaPlayerVolumeSeekBar mSeekBarVolumeProgress;
	private ImageView mMuteIv; //中间可变的图片

	private Callback mCallback;

	private volatile boolean isShowUpdateProgress = false;

	private boolean isChangedFromOnKeyChange = false;
	private int mOldVolume = 0;

	private MediaPlayerVolumeSeekBar.onScreenShowListener mOnScreenShowListener;
	
	private static final int LEVEL_VOLUME = 100;

	public MediaPlayerControllerVolumeView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public MediaPlayerControllerVolumeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public MediaPlayerControllerVolumeView(Context context) {
		super(context);
		init(context);
	}

	public void setOnScreenShowListener(
			MediaPlayerVolumeSeekBar.onScreenShowListener mOnScreenShowListener) {
		Log.d(Constants.LOG_TAG, " listener set in C V");
		if (mOnScreenShowListener != null) {
//			mSeekBarVolumeProgress
//					.setOnScreenShowListener(mOnScreenShowListener);
		}
	}

	public void init(Context context) {
		View root = LayoutInflater.from(context).inflate(
				R.layout.blue_media_player_controller_volume_view, this);
		
		/*mSeekBarVolumeProgress = (MediaPlayerVolumeSeekBar) root
				.findViewById(R.id.seekbar_volume_progress);
		mSeekBarVolumeProgress.setMax(MAX_PROGRESS);

		mSeekBarVolumeProgress
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {

						if (isShowUpdateProgress) {
							isShowUpdateProgress = false;
							return;
						}
						if (isChangedFromOnKeyChange) {
							isChangedFromOnKeyChange = false;
							return;
						}

						float percentage = (float) progress / seekBar.getMax();

						if (percentage < 0 || percentage > 1)
							return;

						if (mAudioManager != null) {

							int maxVolume = mAudioManager
									.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
							int newVolume = (int) (percentage * maxVolume);
							setVolume(newVolume);
							if (mCallback != null) {
								mCallback.onVolumeProgressChanged(
										mAudioManager, percentage);
							}

						}

					}
				});*/

		mMuteIv = (ImageView) root.findViewById(R.id.iv_volume_status);
//		mMuteIv.setOnClickListener(this);
		
	}

	private void setVolume(int volume) {
		if (null != mAudioManager) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
			if (volume == 0) {
				mMuteIv.setSelected(true);
			} else {
				mMuteIv.setSelected(false);
			}
		}
	}

	private int getVolume() {
		if (null != mAudioManager) {
			return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		}
		return 0; 
	}

	public void update(AudioManager audioManager) {

		mAudioManager = audioManager;
		isChangedFromOnKeyChange = false;
		// 目前存在bug:手动调用setProgress thumb会移动至初始位置
		// 每次show的时候,更新下最新的Volume进度
		// 但是因为seekbar max值和audioManager max值存在很大差距,可能会导致show完后会跳跃比较大间距
		if (mAudioManager != null) {
			updateSeekBarVolumeProgress();
			isShowUpdateProgress = true;
		}
	}

	private void updateSeekBarVolumeProgress() {
		int curVolume = mAudioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		int maxVolume = mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		float percentage = (float) curVolume / maxVolume;

//		final int progress = (int) (percentage * mSeekBarVolumeProgress
//				.getMax());
//		mSeekBarVolumeProgress.post(new Runnable() {
//			@Override
//			public void run() {
//				mSeekBarVolumeProgress.setProgress(progress);
//			}
//		});
	}

	public void setCallback(Callback callback) {
		mCallback = callback;
	}

	public interface Callback {
		void onVolumeProgressChanged(AudioManager audioManager, float percentage);
	}

	private int getOldVolume() {
		if (mOldVolume == 0) {
			mOldVolume = 1;
		}
		return mOldVolume;
	}

	@Override
	public void onClick(View v) {
		if (v == mMuteIv) {
			if (mMuteIv.isSelected()) {
				setVolume(getOldVolume());
			} else {
				mOldVolume = getVolume();
				setVolume(0);
			}
			isChangedFromOnKeyChange = true;
			updateSeekBarVolumeProgress();
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int eventAction = event.getAction();
		int keyCode = event.getKeyCode();
		if (eventAction == KeyEvent.ACTION_DOWN
				&& (keyCode == KeyEvent.KEYCODE_VOLUME_UP// 音量+
				|| keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {// 音量-
			isChangedFromOnKeyChange = true;
			updateSeekBarVolumeProgress();
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	public void setOnShowListener() {

	}
	
	
	// 记录一次有效手势滑动的总距离,改值是由于多次的delta累加而成,要大于一个基础阀值,才能真正实现效果
    private float mTotalDeltaVolumeDistance = 0;
    private float mTotalLastDeltaVolumePercentage = 0;
    public void onGestureVolumeChange(float deltaVolumeDistance, float totalVolumeDistance, AudioManager audioManager){
		Log.d(Constants.LOG_TAG, "onGestureVolumeChange ....11....");
        mTotalDeltaVolumeDistance = mTotalDeltaVolumeDistance + deltaVolumeDistance;
        float minVolumeDistance = totalVolumeDistance / LEVEL_VOLUME;
        float minVolumePercentage = (float) 1 / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (Math.abs(mTotalDeltaVolumeDistance) >= minVolumeDistance) {
            
            float deltaVolumePercentage = mTotalDeltaVolumeDistance / totalVolumeDistance;
            mTotalLastDeltaVolumePercentage = mTotalLastDeltaVolumePercentage + deltaVolumePercentage;
            
            int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            float curVolumePercentage = (float) curVolume / maxVolume;
            
            int newVolume = curVolume;
            float newVolumePercentage = curVolumePercentage + mTotalLastDeltaVolumePercentage;
            
            if(mTotalLastDeltaVolumePercentage > 0 && mTotalLastDeltaVolumePercentage > minVolumePercentage){
                mTotalLastDeltaVolumePercentage = 0;
                newVolume++;
            }
            else if(mTotalLastDeltaVolumePercentage < 0 && mTotalLastDeltaVolumePercentage < -minVolumePercentage){
                mTotalLastDeltaVolumePercentage = 0;
                newVolume--;
            }
            
            if(newVolume < 0){
                newVolume = 0;
            }
            else if(newVolume > maxVolume){
                newVolume = maxVolume;
            }
            
            if(newVolumePercentage < 0){
                newVolumePercentage = 0.0f;
            }
            else if(newVolumePercentage > 1){
                newVolumePercentage = 1.0f;
            }

            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
            performVolumeChange(newVolumePercentage);

            mTotalDeltaVolumeDistance = 0;
            
        }
        
    }
    
    public void onGestureVolumeFinish(){
        
        mTotalDeltaVolumeDistance = 0;
        mTotalLastDeltaVolumePercentage = 0;
        
    }
    
    //声音改变图像
    private void performVolumeChange(float volumePercentage) {
        
        int level = 0;
        if (volumePercentage == 0.0f) {
            level = 0;
        } else if (volumePercentage <= 0.25f) {
            level = 1;
        } else if (volumePercentage <= 0.5f) {
            level = 2;
        } else if (volumePercentage <= 0.75f) {
        	level = 3;
        } else if (volumePercentage <1.0f) {
        	level = 4;
        } else if (volumePercentage == 1.0f){
            level = 5;
        }
        
        mMuteIv.setImageLevel(level);
//        mTvVolumeProgress.setText(((int)(volumePercentage*100)) + "%");

//        show();

    }


}
