package com.ksy.media.widget.controller;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ksy.media.widget.model.MediaPlayMode;
import com.ksy.media.widget.model.MediaPlayerMovieRatio;
import com.ksy.media.widget.util.MediaPlayerUtils;
import com.ksy.media.widget.model.MediaPlayerVideoQuality;
import com.ksy.media.widget.ui.video.VideoRelateVideoInfo;
import com.ksy.media.widget.ui.video.VideoRelatedVideoAdapter;
import com.ksy.media.widget.ui.common.MediaPlayerLockView;
import com.ksy.media.widget.ui.common.MediaPlayerQualityPopupView;
import com.ksy.media.widget.ui.common.MediaPlayerSeekView;
import com.ksy.media.widget.ui.common.MediaPlayerVolumeSeekBar.onScreenShowListener;
import com.ksy.mediaPlayer.widget.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 横屏控制页面
 */
public class StreamMediaPlayerLargeControllerView extends
        MediaPlayerBaseControllerView implements View.OnClickListener,
        onScreenShowListener, OnSystemUiVisibilityChangeListener {

    protected static final String TAG = StreamMediaPlayerLargeControllerView.class.getSimpleName();

    private RelativeLayout mControllerTopView;
    private RelativeLayout mBackLayout;
    private TextView mTitleTextView;

    private ImageView mVideoPlayImageView; // 播放暂停


    private LinearLayout mVideoSizeLayout; //视频尺寸
    private TextView mVideoSizeTextView;

    private LinearLayout mQualityLayout; // 视频清晰度切换
    private TextView mQualityTextView;

    private List<VideoRelateVideoInfo> relationList;
    private VideoRelatedVideoAdapter relatedAdapter;
    private Context mContext;

    private RelativeLayout mVideoProgressLayout;
    private ImageView mScreenModeImageView;

    private MediaPlayerQualityPopupView mQualityPopup; // 清晰度

    private MediaPlayerLockView mLockView; // 锁屏
    private ImageView stream_queue_large;
    private ImageView stream_fav_large;
    private ImageView stream_setting_large;
    private Button steram_controller_send_btn;
    private EditText stream_controller_comment;

    public StreamMediaPlayerLargeControllerView(Context context, AttributeSet attrs,
                                                int defStyle) {

        super(context, attrs, defStyle);
        this.mContext = context;
    }

    public StreamMediaPlayerLargeControllerView(Context context, AttributeSet attrs) {

        super(context, attrs);
        this.mContext = context;
    }

    public StreamMediaPlayerLargeControllerView(Context context) {
        super(context);
        this.mContext = context;
        mLayoutInflater.inflate(R.layout.stream_blue_media_player_controller_large, this);

        initViews();
        initListeners();
    }

    @Override
    protected void initViews() {

        mControllerTopView = (RelativeLayout) findViewById(R.id.controller_top_layout);
        mBackLayout = (RelativeLayout) findViewById(R.id.back_layout); // 返回
        mTitleTextView = (TextView) findViewById(R.id.title_text_view);

        mVideoPlayImageView = (ImageView) findViewById(R.id.video_start_pause_image_view); // 播放控制
        stream_queue_large = (ImageView) findViewById(R.id.stream_queue_large);
        stream_fav_large = (ImageView) findViewById(R.id.stream_fav_large);
        stream_setting_large = (ImageView) findViewById(R.id.stream_setting_large);
        steram_controller_send_btn = (Button) findViewById(R.id.steram_controller_send_btn);

        mQualityLayout = (LinearLayout) findViewById(R.id.video_quality_layout); // 分辨率切换layout
        mQualityTextView = (TextView) findViewById(R.id.tv_definition); // 分辨率切换

        stream_controller_comment = (EditText)findViewById(R.id.stream_controller_comment);
        mLockView = (MediaPlayerLockView) findViewById(R.id.widget_lock_view);

        mVideoProgressLayout = (RelativeLayout) findViewById(R.id.video_progress_layout);
        mScreenModeImageView = (ImageView) findViewById(R.id.video_window_screen_image_view); // 大屏切小屏

        mQualityPopup = new MediaPlayerQualityPopupView(getContext());

        mWidgetSeekView = (MediaPlayerSeekView) findViewById(R.id.widget_seek_view);
        setOnSystemUiVisibilityChangeListener(this);
        //相关数据加载,没有数据，设置本地测试数据
        relationList = new ArrayList<VideoRelateVideoInfo>();
        relatedAdapter = new VideoRelatedVideoAdapter(relationList, mContext);
    }

    @Override
    protected void initListeners() {

        mScreenModeImageView.setOnClickListener(this);
        mBackLayout.setOnClickListener(this);
        mVideoPlayImageView.setOnClickListener(this);
        mQualityLayout.setOnClickListener(this);
        mTitleTextView.setOnClickListener(this);
        stream_queue_large.setOnClickListener(this);
        stream_fav_large.setOnClickListener(this);
        stream_setting_large.setOnClickListener(this);
        steram_controller_send_btn.setOnClickListener(this);
        //清晰度
        mQualityPopup.setCallback(new MediaPlayerQualityPopupView.Callback() {

            @Override
            public void onQualitySelected(MediaPlayerVideoQuality quality) {

                mQualityPopup.hide();
                mQualityTextView.setText(quality.getName());
                setMediaQuality(quality);
            }

            @Override
            public void onPopupViewDismiss() {

                mQualityLayout.setSelected(false);
                if (isShowing())
                    show();
            }
        });

        mLockView.setCallback(new MediaPlayerLockView.ScreenLockCallback() {

            @Override
            public void onActionLockMode(boolean lock) {

                // 加锁
                if (lock) {
                    mScreenLock = lock;
                    mMediaPlayerController.onRequestLockMode(lock);
                    show();
                }
                // 解锁
                else {
                    mScreenLock = lock;
                    mMediaPlayerController.onRequestLockMode(lock);
                    show();
                }
            }
        });


    }

    @Override
    void onTimerTicker() {

        long curTime = mMediaPlayerController.getCurrentPosition();
        long durTime = mMediaPlayerController.getDuration();

        if (durTime > 0 && curTime <= durTime) {
            float percentage = ((float) curTime) / durTime;
//            updateVideoProgress(percentage);
        }

    }

    @Override
    void onShow() {

        mMediaPlayerController
                .onControllerShow(MediaPlayMode.PLAYMODE_FULLSCREEN);

        mLockView.show();
        // 如果开启屏幕锁后,controller显示时把其他控件隐藏,只显示出LockView
        if (mScreenLock) {
            mControllerTopView.setVisibility(INVISIBLE);
            mVideoProgressLayout.setVisibility(INVISIBLE);
        } else {
            mControllerTopView.setVisibility(VISIBLE);
            mVideoProgressLayout.setVisibility(VISIBLE);
        }
        if (MediaPlayerUtils.isFullScreenMode(mMediaPlayerController
                .getPlayMode())) {
        }
    }

    @Override
    void onHide() {

        mMediaPlayerController
                .onControllerHide(MediaPlayMode.PLAYMODE_FULLSCREEN);

        mControllerTopView.setVisibility(INVISIBLE);
        mVideoProgressLayout.setVisibility(INVISIBLE);
        if (mQualityPopup.isShowing()) {
            mQualityPopup.hide();
        }

        // 当前全屏模式,隐藏系统UI
        if (mDeviceNavigationBarExist) {
            if (MediaPlayerUtils.isFullScreenMode(mMediaPlayerController
                    .getPlayMode())) {
                Log.d(TAG, "346  onHide ....");
                MediaPlayerUtils.hideSystemUI(mHostWindow, false);
            }
        }

        mLockView.hide();

    }

    @Override
    protected void onAttachedToWindow() {

        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {

        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {

        super.onFinishInflate();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public void updateVideoTitle(String title) {

        if (!TextUtils.isEmpty(title)) {
            mTitleTextView.setText(title);
        }
    }

    public void updateVideoPlaybackState(boolean isStart) {

        // 播放中
        Log.i(TAG, "updateVideoPlaybackState  ----> start ? " + isStart);
        if (isStart) {
            mVideoPlayImageView.setImageResource(R.drawable.blue_ksy_pause);
            // mVideoPlayImageView.setSelected(true);
        }
        // 未播放
        else {
            mVideoPlayImageView.setImageResource(R.drawable.blue_ksy_play);
            // mVideoPlayImageView.setSelected(false);
        }
    }

    public void updateVideoQualityState(MediaPlayerVideoQuality quality) {

        mQualityTextView.setText(quality.getName());
    }

    public void updateVideoVolumeState() {

    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == mBackLayout.getId() || id == mTitleTextView.getId()) {// 返回
            mMediaPlayerController
                    .onBackPress(MediaPlayMode.PLAYMODE_FULLSCREEN);

        } else if (id == mVideoPlayImageView.getId()) {// 播放暂停
            Log.i(TAG, "playing  ? " + (mMediaPlayerController.isPlaying()));
            if (mMediaPlayerController.isPlaying()) {
                mMediaPlayerController.pause();
                if (mScreenLock) {
                    show();
                } else {
                    show(0);
                }
            } else if (!mMediaPlayerController.isPlaying()) {
                mMediaPlayerController.start();
                show();
            }

        } else if (id == mQualityLayout.getId()) { //清晰度
            Log.d(TAG, "507  id == mVideoSizeLayout.getId() ......");
            displayQualityPopupWindow();
        } else if (id == mScreenModeImageView.getId()) { // 切换大小屏幕
            mMediaPlayerController
                    .onRequestPlayMode(MediaPlayMode.PLAYMODE_WINDOW);
        } else if (id == stream_setting_large.getId()) {
            Toast.makeText(mContext, "setting clicked", Toast.LENGTH_SHORT).show();
        } else if (id == stream_fav_large.getId()) {
            Toast.makeText(mContext, "favourate clicked", Toast.LENGTH_SHORT).show();
        } else if (id == stream_queue_large.getId()) {
            Toast.makeText(mContext, "hisroty clicked", Toast.LENGTH_SHORT).show();
        } else if (id == steram_controller_send_btn.getId()) {
            stream_controller_comment.setText("");
            Toast.makeText(mContext, "comment send clicked", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 由于拖动引起的视图变化的回调
     */
    public interface OnGuestureChangeListener {
        /**
         * 亮度有变化回调
         */
        void onLightChanged();

        /**
         * 声音有变化回调
         */
        void onVolumeChanged();

        /**
         * 播放进度有变化回调
         */
        void onPlayProgressChanged();
    }

    /**
     * 清晰度的弹框
     */
    private void displayQualityPopupWindow() {

        // 弹出清晰度框
        List<MediaPlayerVideoQuality> qualityList = new ArrayList<MediaPlayerVideoQuality>();
        qualityList.add(MediaPlayerVideoQuality.UNKNOWN);
        qualityList.add(MediaPlayerVideoQuality.HD);
        qualityList.add(MediaPlayerVideoQuality.SD);
        int widthExtra = MediaPlayerUtils.dip2px(getContext(), 5);
        int width = mQualityLayout.getMeasuredWidth() + widthExtra;
        int height = (MediaPlayerUtils.dip2px(getContext(), 30) + MediaPlayerUtils
                .dip2px(getContext(), 2)) * qualityList.size();

        int x = MediaPlayerUtils.getXLocationOnScreen(mQualityLayout)
                - widthExtra / 2;
        int y = MediaPlayerUtils.getYLocationOnScreen(mQualityLayout) - height;
        mQualityPopup.show(mQualityLayout, qualityList, this.mCurrentQuality,
                x, y, width, height);
        mQualityLayout.setSelected(true);
        show(0);
    }

    @Override
    public void onScreenShow() {
        show();
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        Log.d(TAG, "onSystemUiVisibilityChange :" + visibility);
    }

    @Override
    public void onWindowSystemUiVisibilityChanged(int visible) {
        Log.d(TAG, "onWindowSystemUiVisibilityChanged :"
                + visible);
    }

}
