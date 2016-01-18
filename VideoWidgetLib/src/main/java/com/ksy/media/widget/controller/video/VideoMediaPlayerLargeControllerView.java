package com.ksy.media.widget.controller.video;

import android.content.Context;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ksy.media.widget.controller.base.MediaPlayerBaseControllerView;
import com.ksy.media.widget.model.MediaPlayMode;
import com.ksy.media.widget.model.MediaPlayerMovieRatio;
import com.ksy.media.widget.ui.base.MediaPlayerScreenSizePopupView;
import com.ksy.media.widget.util.MediaPlayerUtils;
import com.ksy.media.widget.model.MediaPlayerVideoQuality;
import com.ksy.media.widget.ui.base.MediaPlayerControllerBrightView;
import com.ksy.media.widget.ui.base.MediaPlayerControllerVolumeView;
import com.ksy.media.widget.ui.base.MediaPlayerLockView;
import com.ksy.media.widget.ui.base.MediaPlayerMovieRatioView;
import com.ksy.media.widget.ui.base.MediaPlayerQualityPopupView;
import com.ksy.media.widget.ui.base.MediaPlayerSeekView;
import com.ksy.media.widget.ui.base.MediaPlayerVideoSeekBar;
import com.ksy.media.widget.ui.base.MediaPlayerVolumeSeekBar.onScreenShowListener;
import com.ksy.mediaPlayer.widget.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 横屏控制页面
 */
public class VideoMediaPlayerLargeControllerView extends
        MediaPlayerBaseControllerView implements View.OnClickListener,
        onScreenShowListener, OnSystemUiVisibilityChangeListener {

    protected static final String TAG = VideoMediaPlayerLargeControllerView.class.getSimpleName();

    private RelativeLayout mControllerTopView;
    private RelativeLayout mBackLayout;
    private TextView mTitleTextView;

    private ImageView mVideoPlayImageView; // 播放暂停
    private MediaPlayerMovieRatioView mWidgetMovieRatioView;

//	private ImageView mVideoSizeImageView;

//    private LinearLayout mVideoSizeLayout; //视频尺寸
//    private TextView mVideoSizeTextView;

//    private LinearLayout mQualityLayout; // 视频清晰度切换
//    private TextView mQualityTextView;

    //    private TextView mEpisodeTextView; //剧集
//    private ListView mRelateListview; //剧集相关
//    private List<VideoRelateVideoInfo> relationList;
//    private VideoRelatedVideoAdapter relatedAdapter;
    private Context mContext;

    private RelativeLayout mVideoProgressLayout;
    private MediaPlayerVideoSeekBar mSeekBar;
    private TextView mCurrentTimeTextView; // 当前时间
    private TextView mTotalTimeTextView; // 总时间
    private ImageView mScreenModeImageView;

    private MediaPlayerQualityPopupView mQualityPopup; // 清晰度
//    private MediaPlayerScreenSizePopupView mScreenPopup; //屏幕尺寸

    private MediaPlayerLockView mLockView; // 锁屏
    private ImageView mVideoCropView; // 截图
    private ImageView video_top_setting;
    private ImageView video_top_camera;
    private ImageView changeScreenImage;
    private ImageView recentImage;
    private ImageView hdImage;
    protected MediaPlayerScreenSizePopupView mScreenPopup;
    protected MediaPlayerControllerBrightView mControllerBrightView;
    protected MediaPlayerControllerVolumeView mWidgetVolumeControl;
    protected MediaPlayerSeekView mWidgetSeekView;
    // private MediaPlayerControllerVolumeView mWidgetControllerVolumeView;
    //声音控制
    // private ImageView mVideoRatioBackView;
    // private ImageView mVideoRatioForwardView;

    public VideoMediaPlayerLargeControllerView(Context context, AttributeSet attrs,
                                               int defStyle) {

        super(context, attrs, defStyle);
        this.mContext = context;
    }

    public VideoMediaPlayerLargeControllerView(Context context, AttributeSet attrs) {

        super(context, attrs);
        this.mContext = context;
    }

    public VideoMediaPlayerLargeControllerView(Context context) {
        super(context);
        this.mContext = context;
        mLayoutInflater.inflate(R.layout.video_blue_media_player_controller_large, this);

        initViews();
        initListeners();
    }

    @Override
    public void initViews() {

        mControllerTopView = (RelativeLayout) findViewById(R.id.controller_top_layout);
        mBackLayout = (RelativeLayout) findViewById(R.id.back_layout); // 返回
        mTitleTextView = (TextView) findViewById(R.id.title_text_view);

        mVideoPlayImageView = (ImageView) findViewById(R.id.video_start_pause_image_view); // 播放控制

//        mVideoSizeLayout = (LinearLayout) findViewById(R.id.video_screen_size_layout); //视频尺寸切换layout
//        mVideoSizeTextView = (TextView) findViewById(R.id.tv_screen_size);
        video_top_setting = (ImageView) findViewById(R.id.video_top_setting);
        video_top_camera = (ImageView) findViewById(R.id.video_camera_image);
        changeScreenImage = (ImageView) findViewById(R.id.video_window_screen_image_view);
        recentImage = (ImageView) findViewById(R.id.video_recent_image);
        hdImage = (ImageView) findViewById(R.id.video_hq_image);


//		mVideoSizeImageView = (ImageView) findViewById(R.id.video_size_image_view); // 视频尺寸切换

        // mVideoRatioBackView = (ImageView) findViewById(R.id.video_fast_back_view);
        // mVideoRatioForwardView = (ImageView)findViewById(R.id.video_fast_forward_view);

//		mVideoCropView = (ImageView) findViewById(R.id.crop_view); // 截屏

//        mQualityLayout = (LinearLayout) findViewById(R.id.video_quality_layout); // 分辨率切换layout
//        mQualityTextView = (TextView) findViewById(R.id.tv_definition); // 分辨率切换

//        mEpisodeTextView = (TextView) findViewById(R.id.tv_episode); //剧集
//        mRelateListview = (ListView) findViewById(R.id.relatedlistview);

        mLockView = (MediaPlayerLockView) findViewById(R.id.widget_lock_view);
        mVideoProgressLayout = (RelativeLayout) findViewById(R.id.video_progress_layout);
        mSeekBar = (MediaPlayerVideoSeekBar) findViewById(R.id.video_seekbar);
        mCurrentTimeTextView = (TextView) findViewById(R.id.video_current_time_text_view);
        mTotalTimeTextView = (TextView) findViewById(R.id.video_total_time_text_view);
        mScreenModeImageView = (ImageView) findViewById(R.id.video_window_screen_image_view); // 大屏切小屏

        mSeekBar.setMax(MAX_VIDEO_PROGRESS);
        mSeekBar.setProgress(0);

        mQualityPopup = new MediaPlayerQualityPopupView(getContext());

//        mScreenPopup = new MediaPlayerScreenSizePopupView(getContext()/*, mMediaPlayerController*/);

        // mWidgetLightView = (MediaPlayerBrightView) findViewById(R.id.widget_light_view); //亮度调节

        mControllerBrightView = (MediaPlayerControllerBrightView) findViewById(R.id.widge_control_light_view); // 新亮度调节
        mWidgetMovieRatioView = (MediaPlayerMovieRatioView) findViewById(R.id.widget_video_ratio_view);
        // mWidgetVolumeView = (MediaPlayerVolumeView)
        // findViewById(R.id.widget_volume_view); //声音调节 进度条相关
        mWidgetVolumeControl = (MediaPlayerControllerVolumeView) findViewById(R.id.widget_controller_volume);
        mWidgetSeekView = (MediaPlayerSeekView) findViewById(R.id.widget_seek_view);

        // mWidgetControllerVolumeView = (MediaPlayerControllerVolumeView)
        // findViewById(R.id.widget_controller_volume);
        // Log.d(Constants.LOG_TAG, " listener set in L C");
        // mWidgetControllerVolumeView.setOnScreenShowListener(this);
        setOnSystemUiVisibilityChangeListener(this);


        //相关数据加载,没有数据，设置本地测试数据
//        relationList = new ArrayList<VideoRelateVideoInfo>();
//        relatedAdapter = new VideoRelatedVideoAdapter(relationList, mContext);
//		Log.d(Constants.LOG_TAG, "170 mContext =" + relationList + ">>relationList=" + relationList);
//        mRelateListview.setAdapter(relatedAdapter);
//		mRelateListview.setCacheColorHint(Color.TRANSPARENT);
    }

    @Override
    public void initListeners() {

        mScreenModeImageView.setOnClickListener(this);
//		mVideoCropView.setOnClickListener(this);
        // mVideoRatioBackView.setOnClickListener(this);
        // mVideoRatioForwardView.setOnClickListener(this);
        video_top_setting.setOnClickListener(this);
        video_top_camera.setOnClickListener(this);
        mBackLayout.setOnClickListener(this);
        mVideoPlayImageView.setOnClickListener(this);
//        mQualityLayout.setOnClickListener(this);
//        mVideoSizeLayout.setOnClickListener(this);
        mTitleTextView.setOnClickListener(this);
//        mEpisodeTextView.setOnClickListener(this);//剧集

//		mVideoSizeImageView.setOnClickListener(this);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                mVideoProgressTrackingTouch = false;

                int curProgress = seekBar.getProgress();
                int maxProgress = seekBar.getMax();

                if (curProgress >= 0 && curProgress <= maxProgress) {
                    float percentage = ((float) curProgress) / maxProgress;
                    int position = (int) (mMediaPlayerController.getDuration() * percentage);
                    mMediaPlayerController.seekTo(position);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                mVideoProgressTrackingTouch = true;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {

                if (fromUser) {
                    if (isShowing()) {
                        show();
                    }
                }
            }

        });

        //清晰度
        mQualityPopup.setCallback(new MediaPlayerQualityPopupView.Callback() {

            @Override
            public void onQualitySelected(MediaPlayerVideoQuality quality) {

                mQualityPopup.hide();
//                mQualityTextView.setText(quality.getName());
                setMediaQuality(quality);
            }

            @Override
            public void onPopupViewDismiss() {
//                mQualityLayout.setSelected(false);
                if (isShowing()) {
                    show();
                }
            }
        });

        //屏幕16：9
        /*mScreenPopup.setCallback(new MediaPlayerScreenSizePopupView.Callback() {

            @Override
            public void onQualitySelected(MediaPlayerMovieRatio screensize) {
                mScreenPopup.hide();
                mVideoSizeTextView.setText(screensize.getName());
            }

            @Override
            public void onPopupViewDismiss() {
                mVideoSizeLayout.setSelected(false);
                if (isShowing()) {
                    show();
                }
            }
        });*/

        mLockView.setCallback(new MediaPlayerLockView.ScreenLockCallback() {

            @Override
            public void onActionLockMode(boolean lock) {

                // 加锁
                if (lock) {
                    mScreenLock = lock;
                    ((IVideoController) mMediaPlayerController).onRequestLockMode(lock);
                    show();
                }
                // 解锁
                else {
                    mScreenLock = lock;
                    ((IVideoController) mMediaPlayerController).onRequestLockMode(lock);
                    show();
                }
            }
        });

		/*
         * mWidgetControllerVolumeView .setCallback(new
		 * MediaPlayerControllerVolumeView.Callback() {
		 * 
		 * @Override public void onVolumeProgressChanged( AudioManager
		 * audioManager, float percentage) {
		 * 
		 * // Auto-generated method stub } });
		 */

        //亮度调节
//		mControllerBrightView.setCallback(callback)

       /* mRelateListview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                Log.d(TAG, "mRelateListview  onItemClick position ====" + position);
                mRelateListview.setVisibility(GONE);
            }
        });*/

    }

    @Override
    public void onTimerTicker() {

        long curTime = mMediaPlayerController.getCurrentPosition();
        long durTime = mMediaPlayerController.getDuration();

        if (durTime > 0 && curTime <= durTime) {
            float percentage = ((float) curTime) / durTime;
            updateVideoProgress(percentage);
        }

    }

    @Override
    public void onShow() {

        ((IVideoController) mMediaPlayerController)
                .onControllerShow(MediaPlayMode.PLAY_MODE_FULLSCREEN);

        mLockView.show();
        // 如果开启屏幕锁后,controller显示时把其他控件隐藏,只显示出LockView
        if (mScreenLock) {
            mControllerTopView.setVisibility(INVISIBLE);
            mVideoProgressLayout.setVisibility(INVISIBLE);
            mWidgetVolumeControl.setVisibility(INVISIBLE);
            mControllerBrightView.setVisibility(INVISIBLE);

            // mWidgetControllerVolumeView.setVisibility(View.INVISIBLE);
        } else {
            mControllerTopView.setVisibility(VISIBLE);
            mVideoProgressLayout.setVisibility(VISIBLE);
            mWidgetVolumeControl.setVisibility(VISIBLE);
            mControllerBrightView.setVisibility(VISIBLE);

        }
        if (MediaPlayerUtils.isFullScreenMode(((IVideoController) mMediaPlayerController)
                .getPlayMode())) {
            Log.d(TAG, "325  onShow....");
            // MediaPlayerUtils.showSystemUI(mHostWindow, false);
        }
    }

    @Override
    public void onHide() {

        ((IVideoController) mMediaPlayerController)
                .onControllerHide(MediaPlayMode.PLAY_MODE_FULLSCREEN);

        mControllerTopView.setVisibility(INVISIBLE);
        mVideoProgressLayout.setVisibility(INVISIBLE);
        mWidgetVolumeControl.setVisibility(INVISIBLE);
        mControllerBrightView.setVisibility(INVISIBLE);

        if (mQualityPopup.isShowing()) {
            mQualityPopup.hide();
        }

//        if (mScreenPopup.isShowing()) {
//            mScreenPopup.hide();
//        }

        // 当前全屏模式,隐藏系统UI
        if (mDeviceNavigationBarExist) {
            if (MediaPlayerUtils.isFullScreenMode(((IVideoController) mMediaPlayerController)
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

	/*
     * @Override public boolean dispatchKeyEvent(KeyEvent event) {
	 * 
	 * return mWidgetControllerVolumeView.dispatchKeyEvent(event); }
	 */

    /***************************
     * Public Method
     ***************************/
    public void updateVideoTitle(String title) {

        if (!TextUtils.isEmpty(title)) {
            mTitleTextView.setText(title);
        }
    }

    //视频播放时间
    public void updateVideoProgress(float percentage) {
        Log.d("eflake", "percentage = " + percentage);
        if (percentage >= 0 && percentage <= 1) {
            int progress = (int) (percentage * mSeekBar.getMax());
            if (!mVideoProgressTrackingTouch)
                mSeekBar.setProgress(progress);

            long curTime = mMediaPlayerController.getCurrentPosition();
            long durTime = mMediaPlayerController.getDuration();
            if (durTime > 0 && curTime <= durTime) {
                mCurrentTimeTextView.setText(MediaPlayerUtils
                        .getVideoDisplayTime(curTime));
                mTotalTimeTextView.setText("/"
                        + MediaPlayerUtils.getVideoDisplayTime(durTime));
            }
        }
    }

    public void updateVideoSecondProgress(int percent) {
        long duration = mMediaPlayerController.getDuration();
        long progress = duration * percent / 100;
        mSeekBar.setSecondaryProgress((int) progress);
    }

    public void updateVideoPlaybackState(boolean isStart) {

        // 播放中
        Log.i(TAG, "updateVideoPlaybackState  ----> start ? " + isStart);
        if (isStart) {
            mVideoPlayImageView.setImageResource(R.drawable.video_pause_land_image);
        }
        // 未播放
        else {
            mVideoPlayImageView.setImageResource(R.drawable.video_play_land_image);
        }
    }

    public void updateVideoQualityState(MediaPlayerVideoQuality quality) {

//        mQualityTextView.setText(quality.getName());
    }

    public void updateVideoVolumeState() {

    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == mBackLayout.getId() || id == mTitleTextView.getId()) {// 返回
            ((IVideoController) mMediaPlayerController)
                    .onBackPress(MediaPlayMode.PLAY_MODE_FULLSCREEN);

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

        } /*else if (id == mQualityLayout.getId()) { //清晰度
            Log.d(TAG, "507  id == mVideoSizeLayout.getId() ......");
            displayQualityPopupWindow();

        } else if (id == mVideoSizeLayout.getId()) {//屏幕尺寸
            Log.d(TAG, "512 id == mVideoSizeLayout.getId() .");
//			mMediaPlayerController.onMovieRatioChange();
//			mWidgetMovieRatioView.show();
//			show();
            displayScreenSizePopupWindow();
			*//*
             * } else if (id == mVideoRatioForwardView.getId()) {//快进按纽
			 * mMediaPlayerController.onMoviePlayRatioUp(); show(); } else if
			 * (id == mVideoRatioBackView.getId()) {//快退按钮
			 * mMediaPlayerController.onMoviePlayRatioDown(); show();
			 *//*
        }*/ else if (id == mScreenModeImageView.getId()) { // 切换大小屏幕
            ((IVideoController) mMediaPlayerController)
                    .onRequestPlayMode(MediaPlayMode.PLAY_MODE_WINDOW);

//        } else if (id == mEpisodeTextView.getId()) { //剧集
//            Log.d(TAG, "id == mEpisodeTextView.getId()  ...");
//            mRelateListview.setVisibility(VISIBLE);
        } else if (id == video_top_setting.getId()) {
            Toast.makeText(mContext, "setting clicked", Toast.LENGTH_SHORT).show();
        } else if (id == video_top_camera.getId()) {
            Toast.makeText(mContext, "cropscreen clicked", Toast.LENGTH_SHORT).show();
        } else if (id == changeScreenImage.getId()) {
            ((IVideoController) mMediaPlayerController)
                    .onRequestPlayMode(MediaPlayMode.PLAY_MODE_WINDOW);
        } else if (id == recentImage.getId()) {
            Toast.makeText(mContext, "recent clicked", Toast.LENGTH_SHORT).show();
        } else if (id == hdImage.getId()) {
            Toast.makeText(mContext, "hd clicked", Toast.LENGTH_SHORT).show();
        }

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
//		int width = mQualityLayout.getMeasuredWidth() + widthExtra;
//		int height = (MediaPlayerUtils.dip2px(getContext(), 50) + MediaPlayerUtils
//				.dip2px(getContext(), 2)) * qualityList.size();

      /*  int width = mQualityLayout.getMeasuredWidth() + widthExtra;
        int height = (MediaPlayerUtils.dip2px(getContext(), 30) + MediaPlayerUtils
                .dip2px(getContext(), 2)) * qualityList.size();

        int x = MediaPlayerUtils.getXLocationOnScreen(mQualityLayout)
                - widthExtra / 2;
        int y = MediaPlayerUtils.getYLocationOnScreen(mQualityLayout) - height;
        mQualityPopup.show(mQualityLayout, qualityList, this.mCurrentQuality,
                x, y, width, height);
        mQualityLayout.setSelected(true);*/
        show(0);
    }

    /**
     * 屏幕尺寸的弹框
     */
    private void displayScreenSizePopupWindow() {

        List<MediaPlayerMovieRatio> screenList = new ArrayList<MediaPlayerMovieRatio>();
        screenList.add(MediaPlayerMovieRatio.WIDESCREEN);
        screenList.add(MediaPlayerMovieRatio.NORMAL);
        int widthExtra = MediaPlayerUtils.dip2px(getContext(), 5);

//		int width = mVideoSizeLayout.getMeasuredWidth() + widthExtra;
//		int height = (MediaPlayerUtils.dip2px(getContext(), 50) + MediaPlayerUtils
//				.dip2px(getContext(), 2)) * screenList.size();

      /*  int width = mVideoSizeLayout.getMeasuredWidth() + widthExtra;
        int height = (MediaPlayerUtils.dip2px(getContext(), 31) + MediaPlayerUtils
                .dip2px(getContext(), 2)) * screenList.size();

        int x = MediaPlayerUtils.getXLocationOnScreen(mVideoSizeLayout)
                - widthExtra / 2;
        int y = MediaPlayerUtils.getYLocationOnScreen(mVideoSizeLayout) - height;
        mScreenPopup.show(mVideoSizeLayout, screenList, this.mCurrentMovieRatio,
                x, y, width, height);
        mVideoSizeLayout.setSelected(true);
        show(0);*/
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

    protected void onHideSeekView() {
        if (mWidgetSeekView != null && mWidgetSeekView.isShowing())
            mWidgetSeekView.hide(true);
    }

    protected void onGestureSeekBegin(int currentPosition, int duration) {
        mWidgetSeekView.onGestureSeekBegin(currentPosition, duration);
    }

    protected void onGestureVolumeChange(float distanceY, float totalVolumeDistance, AudioManager audioManager) {
        if (mWidgetVolumeControl != null) {
            mWidgetVolumeControl.onGestureVolumeChange(distanceY, totalVolumeDistance / 4,
                    audioManager);
        }
    }

    protected void onGestureLightChange(float distanceY, Window mHostWindow) {
        if (mControllerBrightView != null) {
            mControllerBrightView.onGestureLightChange(distanceY, mHostWindow);
        }
    }

    protected void onGestureSeekChange(float distanceY, float totalSeekDistance) {
        if (mWidgetSeekView != null)
            mWidgetSeekView.onGestureSeekChange(distanceY, totalSeekDistance);
    }

    protected void onSeekTo() {
        if (mWidgetSeekView != null) {
            long seekPosition = mWidgetSeekView
                    .onGestureSeekFinish();
            if (seekPosition >= 0
                    && seekPosition <= mMediaPlayerController
                    .getDuration()) {
                mMediaPlayerController.seekTo(seekPosition);
                // mMediaPlayerController.start();
            }
        }
    }

    protected void onShowHide() {
        if (mWidgetSeekView != null)
            mWidgetSeekView.hide(true);
    }
}
