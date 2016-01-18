package com.ksy.media.widget.controller.video;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.ksy.media.widget.controller.base.MediaPlayerBaseControllerView;
import com.ksy.media.widget.model.MediaPlayMode;
import com.ksy.media.widget.util.MediaPlayerUtils;
import com.ksy.media.widget.ui.base.MediaPlayerVideoSeekBar;
import com.ksy.mediaPlayer.widget.R;

public class VideoMediaPlayerSmallControllerView extends MediaPlayerBaseControllerView implements View.OnClickListener {

    private RelativeLayout mControllerTopView;
    private ImageView backImage;
    private TextView mTitleTextView;

    private RelativeLayout mControllerBottomView;
    private MediaPlayerVideoSeekBar mSeekBar;
    private ImageView mPlaybackImageView;
    private ImageView mScreenModeImageView;
    private TextView mCurrentTimeTextView;
    private TextView mTotalTimeTextView;
    private PopupWindow mVideoPopWindow;
    private View stream_share_tv;
    private View stream_alarm_tv;
    private View stream_setting_tv;
    private Context mContext;
    private ImageView overflowImage;

    public VideoMediaPlayerSmallControllerView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
        this.mContext = context;
    }

    public VideoMediaPlayerSmallControllerView(Context context, AttributeSet attrs) {

        super(context, attrs);
        this.mContext = context;
    }

    public VideoMediaPlayerSmallControllerView(Context context) {
        super(context);
        this.mContext = context;
        mLayoutInflater.inflate(R.layout.video_blue_media_player_controller_small, this);

        initViews();
        initListeners();
    }

    @Override
    public void initViews() {

        mControllerTopView = (RelativeLayout) findViewById(R.id.controller_top_layout);
        backImage = (ImageView) findViewById(R.id.image_back);
        mTitleTextView = (TextView) findViewById(R.id.title_text_view);
        overflowImage = (ImageView) findViewById(R.id.image_overflow_video);

        mControllerBottomView = (RelativeLayout) findViewById(R.id.controller_bottom_layout);
        mSeekBar = (MediaPlayerVideoSeekBar) findViewById(R.id.seekbar_video_progress);
        mPlaybackImageView = (ImageView) findViewById(R.id.video_playback_image_view);
        mScreenModeImageView = (ImageView) findViewById(R.id.video_fullscreen_image_view);
        mCurrentTimeTextView = (TextView) findViewById(R.id.video_small_current_time_tv);
        mTotalTimeTextView = (TextView) findViewById(R.id.video_small_duration_time_tv);
        mSeekBar.setMax(MAX_VIDEO_PROGRESS);
        mSeekBar.setProgress(0);

        View view = LayoutInflater.from(mContext).inflate(R.layout.stream_small_pop, null);
        stream_share_tv = view.findViewById(R.id.stream_share_tv);
        stream_alarm_tv = view.findViewById(R.id.stream_alarm_tv);
        stream_setting_tv = view.findViewById(R.id.stream_setting_tv);
        mVideoPopWindow = new PopupWindow(view, getResources().getDimensionPixelSize(R.dimen.stream_pop_width), getResources().getDimensionPixelOffset(R.dimen.stream_pop_height));
        mVideoPopWindow.setFocusable(true);
        mVideoPopWindow.setTouchable(true);
        mVideoPopWindow.setBackgroundDrawable(new BitmapDrawable());

    }

    @Override
    public void initListeners() {

        backImage.setOnClickListener(this);
        overflowImage.setOnClickListener(this);
        mTitleTextView.setOnClickListener(this);
        mPlaybackImageView.setOnClickListener(this);
        mScreenModeImageView.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                mVideoProgressTrackingTouch = false;

                int curProgress = seekBar.getProgress();
                int maxProgress = seekBar.getMax();

                if (curProgress >= 0 && curProgress <= maxProgress) {
                    float percentage = ((float) curProgress) / maxProgress;
                    int position = (int) (mMediaPlayerController.getDuration() * percentage);
                    mMediaPlayerController.seekTo(position);
                    // mMediaPlayerController.start();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                mVideoProgressTrackingTouch = true;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    if (isShowing()) {
                        show();
                    }
                }

            }
        });

    }

    @Override
    public void onTimerTicker() {

        long currentTime = mMediaPlayerController.getCurrentPosition();
        long durationTime = mMediaPlayerController.getDuration();

        if (durationTime > 0 && currentTime <= durationTime) {
            float percentage = ((float) currentTime) / durationTime;
            updateVideoProgress(percentage);
        }

    }

    @Override
    public void onShow() {

        mControllerTopView.setVisibility(VISIBLE);
        mControllerBottomView.setVisibility(VISIBLE);
    }

    @Override
    public void onHide() {

        mControllerTopView.setVisibility(INVISIBLE);
        mControllerBottomView.setVisibility(INVISIBLE);
    }

    public void updateVideoTitle(String title) {

        if (!TextUtils.isEmpty(title)) {
            mTitleTextView.setText(title);
        }
    }

    public void updateVideoProgress(float percentage) {

        if (percentage >= 0 && percentage <= 1) {
            int progress = (int) (percentage * mSeekBar.getMax());
            if (!mVideoProgressTrackingTouch)
                mSeekBar.setProgress(progress);

            long curTime = mMediaPlayerController.getCurrentPosition();
            long durTime = mMediaPlayerController.getDuration();

            if (durTime > 0 && curTime <= durTime) {
                mCurrentTimeTextView.setText(MediaPlayerUtils
                        .getVideoDisplayTime(curTime));
                mTotalTimeTextView.setText(
                        MediaPlayerUtils.getVideoDisplayTime(durTime));
            }
        }
    }

    public void updateVideoPlaybackState(boolean isStart) {

        // 播放中
        if (isStart) {

            mPlaybackImageView.setImageResource(R.drawable.blue_ksy_pause);

            if (mMediaPlayerController.canPause()) {
                mPlaybackImageView.setEnabled(true);
            } else {
                mPlaybackImageView.setEnabled(false);
            }
        }
        // 未播放
        else {
            mPlaybackImageView.setImageResource(R.drawable.blue_ksy_play);
            if (mMediaPlayerController.canStart()) {
                mPlaybackImageView.setEnabled(true);
            } else {
                mPlaybackImageView.setEnabled(false);
            }
        }
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == backImage.getId() || id == mTitleTextView.getId()) {

            ((IVideoController) mMediaPlayerController).onBackPress(MediaPlayMode.PLAY_MODE_WINDOW);

        } else if (id == mPlaybackImageView.getId()) {
            if (mMediaPlayerController.isPlaying()) {
                mMediaPlayerController.pause();
                show(0);
            } else if (!mMediaPlayerController.isPlaying()) {
                mMediaPlayerController.start();
                show();
            }
        } else if (id == mScreenModeImageView.getId()) {
            ((IVideoController) mMediaPlayerController).onRequestPlayMode(MediaPlayMode.PLAY_MODE_FULLSCREEN);
        } else if (id == overflowImage.getId()) {
            showPopWindow();

        } else if (id == stream_alarm_tv.getId() || id == stream_setting_tv.getId() || id == stream_share_tv.getId()) {
            hidePopWindow();
        }

    }

    public void updateVideoSecondProgress(int percent) {
        long duration = mMediaPlayerController.getDuration();
        long progress = duration * percent / 100;
        mSeekBar.setSecondaryProgress((int) progress);
    }

    private void showPopWindow() {
        if (!mVideoPopWindow.isShowing()) {
            mVideoPopWindow.showAsDropDown(overflowImage, 0, getResources().getDimensionPixelSize(R.dimen.stream_pop_offset));
            stream_share_tv.setOnClickListener(this);
            stream_alarm_tv.setOnClickListener(this);
            stream_setting_tv.setOnClickListener(this);
            stopTimerTicker();
        }
    }

    private void hidePopWindow() {
        if (mVideoPopWindow.isShowing()) {
            mVideoPopWindow.dismiss();
        }
    }

}
