package com.ksy.media.widget.ui.shortvideo;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ksy.media.widget.controller.shortvideo.IShortVideoController;
import com.ksy.media.widget.controller.shortvideo.ShortVideoMediaPlayerControllerView;
import com.ksy.media.widget.model.MediaPlayMode;
import com.ksy.media.widget.player.IMediaPlayerPlus;
import com.ksy.media.widget.ui.base.MediaPlayerBufferingView;
import com.ksy.media.widget.ui.base.MediaPlayerLoadingView;
import com.ksy.media.widget.util.Constants;
import com.ksy.media.widget.util.IPowerStateListener;
import com.ksy.media.widget.util.MediaPlayerUtils;
import com.ksy.media.widget.util.NetReceiver;
import com.ksy.media.widget.util.NetReceiver.NetState;
import com.ksy.media.widget.util.NetReceiver.NetStateChangedListener;
import com.ksy.media.widget.util.NetworkUtil;
import com.ksy.media.widget.util.PlayConfig;
import com.ksy.media.widget.util.WakeLocker;
import com.ksy.media.widget.videoview.MediaPlayerTextureView;
import com.ksy.mediaPlayer.widget.R;
import com.ksyun.media.player.IMediaPlayer;

public class ShortVideoMediaPlayerView extends RelativeLayout implements
        IPowerStateListener {
    private static final int QUALITY_BEST = 100;
    private static final String CAPTURE_SCREEN_PATH = "KSY_SDK_SCREENSHOT";
    private Activity mActivity;
    private LayoutInflater mLayoutInflater;
    private Window mWindow;
    private ViewGroup mRootView;
    private MediaPlayerTextureView mMediaPlayerVideoView;
    private ShortVideoMediaPlayerControllerView mMediaPlayerSmallControllerView;
    private MediaPlayerBufferingView mMediaPlayerBufferingView;
    private MediaPlayerLoadingView mMediaPlayerLoadingView;
    private ShortVideoMediaPlayerEventActionView mMediaPlayerEventActionView;
    private PlayerViewCallback mPlayerViewCallback;
    private volatile int mPlayMode = MediaPlayMode.PLAY_MODE_FULLSCREEN;
    private volatile boolean mScreenLockMode = false;
    private volatile boolean mScreenshotPreparing = false;
    private boolean mVideoReady = false;
    private PlayConfig playConfig = PlayConfig.getInstance();
    private int mPausePosition = 0;
    private LayoutParams mMediaPlayerControllerViewSmallParams;
    private boolean mDeviceNavigationBarExist;
    private NetReceiver mNetReceiver;
    private NetStateChangedListener mNetChangedListener;
    private boolean mIsComplete = false;
    private float mCurrentPlayingVolumeRatio = 1f;
    public static float MAX_PLAYING_VOLUME_RATIO = 3.0f;
    // add for replay
    private boolean mRecyclePlay = false;
    private RelativeLayout layoutPop;
    private Handler mHandler = new Handler();
    private Context mContext;
    private IPowerStateListener powerStateListener;
    private boolean mIsTextureViewVisible = true;

    public ShortVideoMediaPlayerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init(context, attrs, defStyle);

    }

    public ShortVideoMediaPlayerView(Context context, AttributeSet attrs) {

        super(context, attrs);
        mContext = context;
        init(context, attrs, -1);

    }

    public ShortVideoMediaPlayerView(Context context) {

        super(context);
        mContext = context;
        init(context, null, -1);

    }

    private void init(Context context, AttributeSet attrs, int defStyle)
            throws IllegalArgumentException, NullPointerException {

        if (null == context)
            throw new NullPointerException("Context can not be null !");
        registerPowerReceiver();
        setPowerStateListener(this);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.PlayerView);
        typedArray.recycle();
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mActivity = (Activity) context;
        this.mWindow = mActivity.getWindow();

        this.setBackgroundColor(Color.BLACK);
        this.mDeviceNavigationBarExist = MediaPlayerUtils
                .hasNavigationBar(mWindow);

		/* 初始化UI组件 */
        this.mRootView = (ViewGroup) mLayoutInflater.inflate(
                R.layout.short_video_blue_media_player_view, null);

        this.layoutPop = (RelativeLayout) mRootView
                .findViewById(R.id.layoutPop);

        this.mMediaPlayerVideoView = (MediaPlayerTextureView) mRootView
                .findViewById(R.id.ks_camera_video_view);
        this.mMediaPlayerBufferingView = (MediaPlayerBufferingView) mRootView
                .findViewById(R.id.ks_camera_buffering_view);
        this.mMediaPlayerLoadingView = (MediaPlayerLoadingView) mRootView
                .findViewById(R.id.ks_camera_loading_view);
        this.mMediaPlayerEventActionView = (ShortVideoMediaPlayerEventActionView) mRootView
                .findViewById(R.id.short_camera_event_action_view);
        this.mMediaPlayerSmallControllerView = (ShortVideoMediaPlayerControllerView) mRootView
                .findViewById(R.id.media_player_controller_view_small);

		/* 设置播放器监听器 */
        this.mMediaPlayerVideoView.setOnPreparedListener(mOnPreparedListener);
        this.mMediaPlayerVideoView
                .setOnBufferingUpdateListener(mOnPlaybackBufferingUpdateListener);
        this.mMediaPlayerVideoView
                .setOnCompletionListener(mOnCompletionListener);
        this.mMediaPlayerVideoView.setOnInfoListener(mOnInfoListener);
        this.mMediaPlayerVideoView.setOnErrorListener(mOnErrorListener);
        this.mMediaPlayerVideoView
                .setMediaPlayerController(mMediaPlayerPlus);
        this.mMediaPlayerVideoView.setFocusable(false);
        setPowerStateListener(this.mMediaPlayerVideoView);

        /* 设置playerVideoView UI 参数 */
        LayoutParams mediaPlayerVideoViewParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mediaPlayerVideoViewParams.addRule(RelativeLayout.CENTER_IN_PARENT);

		/* 设置playerVideoView UI 参数 */
        LayoutParams mediaPlayerBufferingViewParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mediaPlayerBufferingViewParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        this.mMediaPlayerBufferingView.hide();

		/* 设置loading UI 参数 */
        LayoutParams mediaPlayerLoadingViewParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mediaPlayerLoadingViewParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        this.mMediaPlayerLoadingView.hide();

        LayoutParams mediaPlayerPopViewParams = new LayoutParams(
                240, 230);
        mediaPlayerPopViewParams.addRule(RelativeLayout.CENTER_IN_PARENT);

		/* 设置eventActionView UI 参数 */
        LayoutParams mediaPlayereventActionViewParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mediaPlayereventActionViewParams
                .addRule(RelativeLayout.CENTER_IN_PARENT);

        this.mMediaPlayerControllerViewSmallParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		/* 设置eventActionView callback */
        this.mMediaPlayerEventActionView
                .setCallback(new ShortVideoMediaPlayerEventActionView.EventActionViewCallback() {

                    @Override
                    public void onActionPlay() {
                        if (NetworkUtil.isNetworkAvailable(mContext)) {
                            mIsComplete = false;
                            Log.i(Constants.LOG_TAG,
                                    "event action  view action play");
                            mMediaPlayerEventActionView.hide();
                            mMediaPlayerLoadingView.hide();
                            mMediaPlayerVideoView.start();
                        } else {
                            Toast.makeText(mContext, "no network",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onActionReplay() {
                        if (NetworkUtil.isNetworkAvailable(mContext)) {
                            Log.i(Constants.LOG_TAG,
                                    "event action  view action replay");
                            mMediaPlayerEventActionView.hide();
                            mIsComplete = false;
                            if (mMediaPlayerController != null) {
                                mMediaPlayerController.start();
                            } else {
                                mMediaPlayerVideoView.start();
                            }
                        } else {
                            Toast.makeText(mContext, "no network",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onActionError() {
                        if (NetworkUtil.isNetworkAvailable(mContext)) {
                            mIsComplete = false;
                            Log.i(Constants.LOG_TAG,
                                    "event action  view action error");
                            mMediaPlayerEventActionView.hide();
                            mMediaPlayerSmallControllerView.hide();
                            mMediaPlayerLoadingView.show();
                            mMediaPlayerVideoView.setVideoPath(url);
                        } else {
                            Toast.makeText(mContext, "no network",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onActionBack() {
                        mIsComplete = false;

                        Log.i(Constants.LOG_TAG,
                                "event action  view action back");
                        mMediaPlayerController.onBackPress(mPlayMode);
                    }
                });

		/* 初始化:ControllerViewLarge */
        this.mMediaPlayerSmallControllerView
                .setMediaPlayerController(mMediaPlayerController);
        this.mMediaPlayerSmallControllerView.setHostWindow(mWindow);
        this.mMediaPlayerSmallControllerView
                .setDeviceNavigationBarExist(mDeviceNavigationBarExist);
        this.mMediaPlayerSmallControllerView.setNeedGestureDetector(true);
        this.mMediaPlayerSmallControllerView.setNeedGestureAction(false, false,
                false);

        removeAllViews();
        mRootView.removeView(mMediaPlayerVideoView);
        mRootView.removeView(mMediaPlayerBufferingView);
        mRootView.removeView(mMediaPlayerLoadingView);
        mRootView.removeView(mMediaPlayerEventActionView);
        mRootView.removeView(mMediaPlayerSmallControllerView);
        mRootView.removeView(layoutPop);

		/* 添加全屏或者是窗口模式初始状态下所需的view */
        addView(mMediaPlayerVideoView, mediaPlayerVideoViewParams);
        addView(mMediaPlayerBufferingView, mediaPlayerBufferingViewParams);
        addView(mMediaPlayerLoadingView, mediaPlayerLoadingViewParams);
        addView(mMediaPlayerEventActionView, mediaPlayereventActionViewParams);
        addView(layoutPop, mediaPlayerPopViewParams);
        addView(mMediaPlayerSmallControllerView,
                mMediaPlayerControllerViewSmallParams);

        mMediaPlayerSmallControllerView.hide();
        mMediaPlayerBufferingView.hide();
        mMediaPlayerLoadingView.hide();
        mMediaPlayerEventActionView.hide();
        // Default not use,if need it ,open it
        // initOrientationEventListener(context);

        mNetReceiver = NetReceiver.getInstance();
        mNetChangedListener = new NetStateChangedListener() {

            @Override
            public void onNetStateChanged(NetState netCode) {

                switch (netCode) {
                    case NET_NO:
                        Log.i(Constants.LOG_TAG, "网络断了");
                        break;
                    case NET_2G:
                        Log.i(Constants.LOG_TAG, "2g网络");
                        break;
                    case NET_3G:
                        Log.i(Constants.LOG_TAG, "3g网络");
                        break;
                    case NET_4G:
                        Log.i(Constants.LOG_TAG, "4g网络");
                        break;
                    case NET_WIFI:
                        Log.i(Constants.LOG_TAG, "WIFI网络");
                        break;

                    case NET_UNKNOWN:
                        Log.i(Constants.LOG_TAG, "未知网络");
                        break;
                    default:
                        Log.i(Constants.LOG_TAG, "不知道什么情况~>_<~");
                }
            }
        };
    }

    private String url = null;

    private void setPowerStateListener(IPowerStateListener powerStateListener) {
        this.powerStateListener = powerStateListener;
    }

    public void play(String path) {

        if (this.mMediaPlayerVideoView != null) {
            Log.d(Constants.LOG_TAG, "play() path =" + path);
            url = path;
            this.mMediaPlayerVideoView.setVideoPath(path);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mMediaPlayerEventActionView.isShowing()) {
            return mMediaPlayerEventActionView.dispatchTouchEvent(ev);
        }

        if (mVideoReady && !mMediaPlayerEventActionView.isShowing()) {
            Log.d("Constants.LOG_TAG", "touch");
            return mMediaPlayerSmallControllerView.dispatchTouchEvent(ev);
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (mScreenLockMode) {
                return true;
            }
            if (mPlayerViewCallback != null)
                mMediaPlayerVideoView.stopPlayback();
            mPlayerViewCallback.onFinish(mPlayMode);
            return true;

        } else if (event.getKeyCode() == KeyEvent.KEYCODE_MENU
                || event.getKeyCode() == KeyEvent.KEYCODE_SEARCH) {
            if (mScreenLockMode) {
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void setPlayerViewCallback(PlayerViewCallback callback) {

        this.mPlayerViewCallback = callback;
    }

    public void setmRecyclePlay(boolean mRecyclePlay) {
        this.mRecyclePlay = mRecyclePlay;
    }

    public int getPlayMode() {

        return this.mPlayMode;
    }

    public void onResume() {
        Log.d(Constants.LOG_TAG, "PlayView onResume");
        powerStateListener.onPowerState(Constants.APP_SHOWN);
        mNetReceiver.registNetBroadCast(getContext());
        mNetReceiver.addNetStateChangeListener(mNetChangedListener);
    }

    public void onPause() {
        Log.d(Constants.LOG_TAG, "PlayView OnPause");
        powerStateListener.onPowerState(Constants.APP_HIDDEN);
        mNetReceiver.remoteNetStateChangeListener(mNetChangedListener);
        mNetReceiver.unRegistNetBroadCast(getContext());
        mPausePosition = mMediaPlayerController.getCurrentPosition();
        WakeLocker.release();
    }

    public void onDestroy() {
        mIsComplete = false;
        mMediaPlayerVideoView.release(true);
        unregisterPowerReceiver();
    }

    private void updateVideoInfo2Controller() {

        mMediaPlayerSmallControllerView.updateVideoTitle(getResources().getString(R.string.short_video_title));
        mMediaPlayerEventActionView.updateVideoTitle(getResources().getString(R.string.short_video_title));
        mMediaPlayerEventActionView.updateVideoTitle(getResources().getString(R.string.short_video_title));
    }

    IMediaPlayer.OnPreparedListener mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {

        @Override
        public void onPrepared(IMediaPlayer mp) {
            Log.d(Constants.LOG_TAG,
                    "IMediaPlayer.OnPreparedListener onPrepared");
            int duration = 0;
            if (mMediaPlayerController != null)
                duration = mMediaPlayerController.getDuration();

            if (mIsComplete) {
                mMediaPlayerSmallControllerView.hide();
                mMediaPlayerEventActionView
                        .updateEventMode(
                                ShortVideoMediaPlayerEventActionView.EVENT_ACTION_VIEW_MODE_COMPLETE,
                                null);
                mMediaPlayerEventActionView.show();
                WakeLocker.release();
            }
            if (mPausePosition > 0 && duration > 0) {
                if (!mIsComplete) {
                    mMediaPlayerController.pause();
                    mMediaPlayerController.seekTo(mPausePosition);
                    mPausePosition = 0;
                }

            }
            if (!WakeLocker.isScreenOn(getContext())
                    && mMediaPlayerController.canPause()) {
                if (!mIsComplete) {
                    mMediaPlayerController.pause();
                }
            }
            updateVideoInfo2Controller();
            mMediaPlayerLoadingView.hide();

            if (!mIsComplete) {
                if (!mMediaPlayerVideoView.mNeedPauseAfterLeave) {
                    mMediaPlayerVideoView.start();
                } else {
                    Log.d(Constants.LOG_TAG, "mOnPreparedListener ingore start for last paused state");
                    mMediaPlayerVideoView.mNeedPauseAfterLeave = false;
                }
            }

            mVideoReady = true;
            if (mPlayerViewCallback != null)
                mPlayerViewCallback.onPrepared();
        }

    };

    IMediaPlayer.OnCompletionListener mOnCompletionListener = new IMediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(IMediaPlayer mp) {

            Log.i(Constants.LOG_TAG, "================onCompletion============");
            if (mRecyclePlay) {
                Log.i(Constants.LOG_TAG, "==replay==");
                mMediaPlayerEventActionView.hide();
                if (mMediaPlayerController != null) {
                    mMediaPlayerController.start();
                } else {
                    mMediaPlayerVideoView.start();
                }
            } else {
                mIsComplete = true;
                mMediaPlayerSmallControllerView.hide();
                mMediaPlayerEventActionView
                        .updateEventMode(
                                ShortVideoMediaPlayerEventActionView.EVENT_ACTION_VIEW_MODE_COMPLETE,
                                null);
                mMediaPlayerEventActionView.show();
                WakeLocker.release();
            }

        }

    };

    IMediaPlayer.OnInfoListener mOnInfoListener = new IMediaPlayer.OnInfoListener() {

        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {

            switch (what) {
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    Log.i(Constants.LOG_TAG, "MEDIA_INFO_BUFFERING_START");
                    mMediaPlayerBufferingView.show();
                    break;
                // 视频缓冲结束
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    Log.i(Constants.LOG_TAG, "MEDIA_INFO_BUFFERING_END");
                    mMediaPlayerBufferingView.hide();
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    IMediaPlayer.OnBufferingUpdateListener mOnPlaybackBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {

        @Override
        public void onBufferingUpdate(IMediaPlayer mp, int percent) {

            if (percent > 0 && percent <= 100) {
            } else {
            }
        }
    };

    IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {

        @Override
        public boolean onError(IMediaPlayer mp, int what, int extra) {

            Log.e(Constants.LOG_TAG, "On Native Error,what :" + what
                    + " , extra :" + extra);
            mMediaPlayerSmallControllerView.hide();
            mMediaPlayerBufferingView.hide();
            mMediaPlayerLoadingView.hide();
            mMediaPlayerEventActionView.updateEventMode(
                    ShortVideoMediaPlayerEventActionView.EVENT_ACTION_VIEW_MODE_ERROR,
                    what + "," + extra);
            mMediaPlayerEventActionView.show();
            return true;
        }
    };

    public void setTextureViewVisible(boolean isTextureViewVisible) {
        mIsTextureViewVisible = isTextureViewVisible;
        if (isTextureViewVisible) {
            // Become Visible
            if (mMediaPlayerController.canStart()) {
                mMediaPlayerController.start();
            }
        } else {
            // Become Invisible
            if (mMediaPlayerVideoView.isPlaying()) {
                mMediaPlayerController.pause();
            }
        }
    }

    public boolean isTextureViewVisible() {
        return mIsTextureViewVisible;
    }

    public interface PlayerViewCallback {
        void onPrepared();

        void onFinish(int playMode);

        void onError(int errorCode, String errorMsg);
    }

    private final IShortVideoController mMediaPlayerController = new IShortVideoController() {

        @Override
        public void start() {
            Log.i(Constants.LOG_TAG, " MediaPlayerView  start()  canStart()="
                    + canStart());
            if (canStart()) {
                mMediaPlayerVideoView.start();
                WakeLocker.acquire(getContext());
            }
        }

        @Override
        public void pause() {
            Log.i(Constants.LOG_TAG, " MediaPlayerView  pause() ");
            if (canPause()) {
                mMediaPlayerVideoView.pause();
                WakeLocker.release();
            }

        }

        @Override
        public int getDuration() {

            return mMediaPlayerVideoView.getDuration();
        }

        @Override
        public int getCurrentPosition() {
            if (mIsComplete) {
                return getDuration();
            }
            return mMediaPlayerVideoView.getCurrentPosition();
        }

        @Override
        public void seekTo(long pos) {
            Log.i(Constants.LOG_TAG, " MediaPlayerView  seekTo ");
            if (canSeekBackward() && canSeekForward()) {
                mMediaPlayerVideoView.seekTo(pos);
            } else {
                Toast.makeText(getContext(),
                        "current is real stream, seek is unSupported !",
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public boolean isPlaying() {

            return mMediaPlayerVideoView.isPlaying();
        }

        @Override
        public int getBufferPercentage() {

            return mMediaPlayerVideoView.getBufferPercentage();
        }

        @Override
        public boolean canPause() {

            Log.i(Constants.LOG_TAG,
                    "can pause ? " + (mMediaPlayerVideoView.canPause()));
            return mMediaPlayerVideoView.canPause();
        }

        @Override
        public boolean canSeekBackward() {

            Log.i(Constants.LOG_TAG, " can Seek Backward ? "
                    + (mMediaPlayerVideoView.canSeekBackward()));
            return mMediaPlayerVideoView.canSeekBackward();
        }

        @Override
        public boolean canSeekForward() {

            Log.i(Constants.LOG_TAG, " can Seek Forward ? "
                    + (mMediaPlayerVideoView.canSeekForward()));
            return mMediaPlayerVideoView.canSeekForward();
        }

        @Override
        public void onBackPress(int playMode) {
            Log.i(Constants.LOG_TAG,
                    "========playerview back pressed ==============playMode :"
                            + playMode + ", mPlayerViewCallback is null "
                            + (mPlayerViewCallback == null));
            if (mPlayerViewCallback != null)
                mPlayerViewCallback.onFinish(playMode);
        }

        @Override
        public void onControllerShow(int playMode) {
            Log.d(Constants.LOG_TAG,"onControllerShow");
        }

        @Override
        public void onControllerHide(int playMode) {
            Log.d(Constants.LOG_TAG,"onControllerHide");
        }

        @Override
        public boolean canStart() {
            Log.i(Constants.LOG_TAG,
                    "can Start ? " + mMediaPlayerVideoView.canStart());
            return mMediaPlayerVideoView.canStart();
        }
    };

    private IMediaPlayerPlus mMediaPlayerPlus = new IMediaPlayerPlus() {

        @Override
        public void onVideoPreparing() {
            Log.i(Constants.LOG_TAG, "on video preparing");
            mMediaPlayerLoadingView.setLoadingTip("loading ...");
            mMediaPlayerLoadingView.show();
        }

        @Override
        public void onPlay() {
            Log.i(Constants.LOG_TAG, "on play called");
            mMediaPlayerEventActionView.hide();
            mMediaPlayerSmallControllerView.updateVideoPlaybackState(true);

        }

        @Override
        public void onPause() {
            Log.i(Constants.LOG_TAG, "on pause called");
            mMediaPlayerEventActionView.hide();
            mMediaPlayerSmallControllerView.updateVideoPlaybackState(false);
        }
    };

    @Override
    public void onPowerState(int state) {
        if (powerStateListener != null) {
            this.powerStateListener.onPowerState(state);
        }
    }

    /*
    *
    * For power state
    * */
    private void registerPowerReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mContext.registerReceiver(mBatInfoReceiver, filter);
    }

    private void unregisterPowerReceiver() {
        if (mBatInfoReceiver != null) {
            try {
                mContext.unregisterReceiver(mBatInfoReceiver);
            } catch (Exception e) {
                Log.e(Constants.LOG_TAG,
                        "unregisterReceiver mBatInfoReceiver failure :"
                                + e.getCause());
            }
        }
    }

    private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                Log.d(Constants.LOG_TAG, "screen off");
                if (powerStateListener != null) {
                    powerStateListener.onPowerState(Constants.POWER_OFF);
                }
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                Log.d(Constants.LOG_TAG, "screen on");
                if (powerStateListener != null) {
                    if (isAppOnForeground()) {
                        powerStateListener.onPowerState(Constants.POWER_ON);
                    }
                }
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                if (isAppOnForeground()) {
                    powerStateListener.onPowerState(Constants.USER_PRESENT);
                }
            }
        }
    };

    private boolean isAppOnForeground() {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String currentPackageName = cn.getPackageName();
        if (!TextUtils.isEmpty(currentPackageName)
                && currentPackageName.equals(mContext.getPackageName())) {
            return true;
        }
        return false;
    }

    public void setPlayConfig(boolean isStream, int interruptMode) {
        playConfig.setStream(isStream);
        playConfig.setInterruptMode(interruptMode);
    }

}
