package com.ksy.media.widget.ui.live;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ksy.media.widget.controller.live.ILiveController;
import com.ksy.media.widget.controller.live.LiveMediaPlayerControllerView;
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

import java.lang.reflect.Constructor;

public class LiveMediaPlayerView extends RelativeLayout implements
        IPowerStateListener {
    private Activity mActivity;
    private LayoutInflater mLayoutInflater;
    private Window mWindow;
    private ViewGroup mRootView;
//    private PhoneLiveMediaPlayerTextureView mLiveMediaPlayerVideoView;
    private MediaPlayerTextureView mLiveMediaPlayerVideoView;
    private LiveMediaPlayerControllerView mLiveMediaPlayerControllerView;
    private MediaPlayerBufferingView mMediaPlayerBufferingView;
    private MediaPlayerLoadingView mMediaPlayerLoadingView;
    private LiveMediaPlayerEventActionView mLiveMediaPlayerEventActionView;
    private PlayerViewCallback mPlayerViewCallback;
    private final int ORIENTATION_UNKNOWN = -2;
    private final int ORIENTATION_HORIZON = -1;
    public static final int ORIENTATION_PORTRAIT_NORMAL = 0;
    public static final int ORIENTATION_LANDSCAPE_REVERSED = 90;
    public static final int ORIENTATION_PORTRAIT_REVERSED = 180;
    public static final int ORIENTATION_LANDSCAPE_NORMAL = 270;
    public static final int ORIENTATION_NONE = -3;
    private volatile int mScreenOrientation = ORIENTATION_UNKNOWN;
    private volatile int mPlayMode = MediaPlayMode.PLAY_MODE_FULLSCREEN;
    private volatile boolean mScreenLockMode = false;
    private boolean mVideoReady = false;
    private int mPausePosition = 0;
    private OrientationEventListener mOrientationEventListener;
    private ViewGroup.LayoutParams mLayoutParamWindowMode;
    private LayoutParams mMediaPlayerControllerViewLargeParams;
    private LayoutParams mMediaPlayerControllerViewSmallParams;
    private boolean mDeviceNaturalOrientationLandscape;
    private boolean mCanLayoutSystemUI;
    private boolean mDeviceNavigationBarExist;
    private int mFullScreenNavigationBarHeight;
    private int mDeviceNavigationType = MediaPlayerUtils.DEVICE_NAVIGATION_TYPE_UNKNOWN;
    private NetReceiver mNetReceiver;
    private NetStateChangedListener mNetChangedListener;
    private boolean mIsComplete = false;
    private boolean mRecyclePlay = false;
    private PlayConfig playConfig = PlayConfig.getInstance();
    private Context mContext;
    private IPowerStateListener powerStateListener;
    private View live_share_bt;
    private EditText liveEditText;

    public LiveMediaPlayerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init(context, attrs, defStyle);
    }

    public LiveMediaPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(context, attrs, -1);
    }

    public LiveMediaPlayerView(Context context) {
        super(context);
        mContext = context;
        init(context, null, -1);
    }

    private void init(Context context, AttributeSet attrs, int defStyle)
            throws IllegalArgumentException, NullPointerException {

        if (null == context) {
            throw new NullPointerException("Context can not be null !");
        }
        registerPowerReceiver();
        setPowerStateListener(this);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.PlayerView);
        this.mPlayMode = MediaPlayMode.PLAY_MODE_WINDOW;
        typedArray.recycle();

        this.mLayoutInflater = LayoutInflater.from(context);
        this.mActivity = (Activity) context;
        this.mWindow = mActivity.getWindow();

        this.setBackgroundColor(Color.BLACK);
        this.mDeviceNavigationBarExist = MediaPlayerUtils
                .hasNavigationBar(mWindow);
        this.mDeviceNaturalOrientationLandscape = (MediaPlayerUtils
                .getDeviceNaturalOrientation(mWindow) == MediaPlayerUtils.DEVICE_NATURAL_ORIENTATION_LANDSCAPE ? true
                : false);
        this.mCanLayoutSystemUI = Build.VERSION.SDK_INT >= 16 ? true : false;
        if (mDeviceNavigationBarExist
                && MediaPlayerUtils.isFullScreenMode(mPlayMode)) {
            this.mFullScreenNavigationBarHeight = MediaPlayerUtils
                    .getNavigationBarHeight(mWindow);
            this.mDeviceNavigationType = MediaPlayerUtils
                    .getDeviceNavigationType(mWindow);
        }

		/* 初始化UI组件 */
        this.mRootView = (ViewGroup) mLayoutInflater.inflate(
                R.layout.live_blue_media_player_view, null);

        this.mLiveMediaPlayerVideoView = (MediaPlayerTextureView) mRootView
                .findViewById(R.id.live_ks_camera_video_view);
        this.mMediaPlayerBufferingView = (MediaPlayerBufferingView) mRootView
                .findViewById(R.id.ks_camera_buffering_view);
        this.mMediaPlayerLoadingView = (MediaPlayerLoadingView) mRootView
                .findViewById(R.id.ks_camera_loading_view);

        this.mLiveMediaPlayerEventActionView = (LiveMediaPlayerEventActionView) mRootView
                .findViewById(R.id.ks_camera_event_action_view_live);
        this.mLiveMediaPlayerControllerView = (LiveMediaPlayerControllerView) mRootView
                .findViewById(R.id.media_player_controller_view_live);
        live_share_bt = mLiveMediaPlayerControllerView.findViewById(R.id.live_share_bt);
        liveEditText = (EditText) mLiveMediaPlayerControllerView.findViewById(R.id.video_comment_text);
        live_share_bt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "clicked", Toast.LENGTH_SHORT).show();
            }
        });
        /* 设置播放器监听器 */
        this.mLiveMediaPlayerVideoView.setOnPreparedListener(mOnPreparedListener);
        this.mLiveMediaPlayerVideoView
                .setOnBufferingUpdateListener(mOnPlaybackBufferingUpdateListener);
        this.mLiveMediaPlayerVideoView
                .setOnCompletionListener(mOnCompletionListener);
        this.mLiveMediaPlayerVideoView.setOnInfoListener(mOnInfoListener);
        this.mLiveMediaPlayerVideoView.setOnErrorListener(mOnErrorListener);
        this.mLiveMediaPlayerVideoView
                .setMediaPlayerController(mMediaPlayerPlus);
        this.mLiveMediaPlayerVideoView.setFocusable(false);

        setPowerStateListener(this.mLiveMediaPlayerVideoView);
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

		/* 设置eventActionView callback */
        this.mLiveMediaPlayerEventActionView
                .setCallback(new LiveMediaPlayerEventActionView.EventActionViewCallback() {
                    @Override
                    public void onActionPlay() {
                        if (NetworkUtil.isNetworkAvailable(mContext)) {
                            mIsComplete = false;
                            Log.i(Constants.LOG_TAG,
                                    "event action  view action play");
                            mLiveMediaPlayerEventActionView.hide();
                            mMediaPlayerLoadingView.hide();
                            mLiveMediaPlayerVideoView.start();
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
                            mLiveMediaPlayerEventActionView.hide();
                            mIsComplete = false;
                            if (mMediaPlayerController != null) {
                                mMediaPlayerController.start();
                            } else {
                                mLiveMediaPlayerVideoView.start();
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
                            mLiveMediaPlayerEventActionView.hide();
                            mLiveMediaPlayerControllerView.setVisibility(VISIBLE);
                            mMediaPlayerLoadingView.show();
                            mLiveMediaPlayerVideoView.setVideoPath(url);
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
        this.mMediaPlayerControllerViewLargeParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        this.mMediaPlayerControllerViewLargeParams
                .addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        this.mMediaPlayerControllerViewLargeParams
                .addRule(RelativeLayout.ALIGN_PARENT_TOP);
        if (mDeviceNavigationBarExist && mCanLayoutSystemUI
                && mFullScreenNavigationBarHeight > 0) {
            if (mDeviceNavigationType == MediaPlayerUtils.DEVICE_NAVIGATION_TYPE_HANDSET) {
                mMediaPlayerControllerViewLargeParams.rightMargin = mFullScreenNavigationBarHeight;
            } else if (mDeviceNavigationType == MediaPlayerUtils.DEVICE_NAVIGATION_TYPE_TABLET) {
                mMediaPlayerControllerViewLargeParams.bottomMargin = mFullScreenNavigationBarHeight;
            }

        }

		/* 初始化:ControllerViewLarge */
        this.mMediaPlayerControllerViewSmallParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		/* 移除掉所有的view */
        removeAllViews();
        mRootView.removeView(mLiveMediaPlayerVideoView);
        mRootView.removeView(mMediaPlayerBufferingView);
        mRootView.removeView(mMediaPlayerLoadingView);
        mRootView.removeView(mLiveMediaPlayerEventActionView);
        mRootView.removeView(mLiveMediaPlayerControllerView);

		/* 添加全屏或者是窗口模式初始状态下所需的view */
        addView(mLiveMediaPlayerVideoView, mediaPlayerVideoViewParams);
        addView(mMediaPlayerBufferingView, mediaPlayerBufferingViewParams);
        addView(mMediaPlayerLoadingView, mediaPlayerLoadingViewParams);
        addView(mLiveMediaPlayerEventActionView, mediaPlayereventActionViewParams);

        if (MediaPlayerUtils.isWindowMode(mPlayMode)) {
            addView(mLiveMediaPlayerControllerView,
                    mMediaPlayerControllerViewSmallParams);
        }

        mMediaPlayerBufferingView.hide();
        mMediaPlayerLoadingView.hide();
        mLiveMediaPlayerEventActionView.hide();

        post(new Runnable() {
            @Override
            public void run() {
                if (MediaPlayerUtils.isWindowMode(mPlayMode)) {
                    mLayoutParamWindowMode = getLayoutParams();
                }

                try {
                    @SuppressWarnings("unchecked")
                    Class<? extends LayoutParams> parentLayoutParamClazz = (Class<? extends LayoutParams>) getLayoutParams()
                            .getClass();
                    Constructor<? extends LayoutParams> constructor = parentLayoutParamClazz
                            .getDeclaredConstructor(int.class, int.class);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        // Default not use,if need it ,open it
//        initOrientationEventListener(context);

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
        if (this.mLiveMediaPlayerVideoView != null) {
            Log.d(Constants.LOG_TAG, "play() path =" + path);
            url = path;
            this.mLiveMediaPlayerVideoView.setVideoPath(path);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (h > oldh && oldw != 0 && oldh != 0) {
            Log.d("eflake", "hide keyboard,distance = " + (h - oldh) + ", screen = " + h);

        } else if (h < oldh) {
            Log.d("eflake", "show keyboard,distance = " + (oldh - h) + ", screen = " + oldh);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (mLiveMediaPlayerEventActionView.isShowing()) {
            return mLiveMediaPlayerEventActionView.dispatchTouchEvent(ev);
        }

        if (mVideoReady && !mLiveMediaPlayerEventActionView.isShowing()) {
            if (MediaPlayerUtils.isWindowMode(mPlayMode)) {
                return mLiveMediaPlayerControllerView.dispatchTouchEvent(ev);
            }
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

            if (MediaPlayerUtils.isWindowMode(mPlayMode)) {
                if (mPlayerViewCallback != null) {
                    mPlayerViewCallback.onFinish(mPlayMode);
                }
                return true;
            }

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

    public void setRecyclePlay(boolean mRecyclePlay) {
        this.mRecyclePlay = mRecyclePlay;
    }

    public void onResume() {

        Log.d("Constants.LOG_TAG", "PlayView onResume");
        powerStateListener.onPowerState(Constants.APP_SHOWN);
        enableOrientationEventListener();
        mNetReceiver.registNetBroadCast(getContext());
        mNetReceiver.addNetStateChangeListener(mNetChangedListener);
    }

    public void onPause() {
        Log.d("Constants.LOG_TAG", "PlayView OnPause");
        powerStateListener.onPowerState(Constants.APP_HIDDEN);
        mNetReceiver.remoteNetStateChangeListener(mNetChangedListener);
        mNetReceiver.unRegistNetBroadCast(getContext());
        mPausePosition = mMediaPlayerController.getCurrentPosition();
        disableOrientationEventListener();
        WakeLocker.release();
    }

    public void onDestroy() {
        mIsComplete = false;
        unregisterPowerReceiver();
        mLiveMediaPlayerVideoView.release(true);
        mLiveMediaPlayerControllerView.stopLiveTimer();
        Log.d(Constants.LOG_TAG, "MediaPlayerView live  onDestroy....");
    }

    private void initOrientationEventListener(Context context) {

        if (null == context) {
            return;
        }

        if (null == mOrientationEventListener) {
            mOrientationEventListener = new OrientationEventListener(context,
                    SensorManager.SENSOR_DELAY_NORMAL) {

                @Override
                public void onOrientationChanged(int orientation) {
                    int preScreenOrientation = mScreenOrientation;
                    mScreenOrientation = convertAngle2Orientation(orientation);

                    if (preScreenOrientation == ORIENTATION_UNKNOWN)
                        return;
                    if (mScreenOrientation == ORIENTATION_UNKNOWN)
                        return;
                    if (mScreenOrientation == ORIENTATION_HORIZON)
                        return;

                    if (preScreenOrientation != mScreenOrientation) {
                        if (!MediaPlayerUtils.checkSystemGravity(getContext()))
                            return;
                        Log.i("eflake", "mScreenOrientation = " + mScreenOrientation);
                        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm.isActive(liveEditText)) {
                            Log.d("eflake", "keyboard is active");
                            imm.hideSoftInputFromWindow(liveEditText.getWindowToken(), 0);
                        }
                        if (mScreenOrientation == ORIENTATION_LANDSCAPE_NORMAL
                                || mScreenOrientation == ORIENTATION_LANDSCAPE_REVERSED) {
                            Log.i("eflake", "Accurate ScreenOrientation = " + mScreenOrientation);
//                            mLiveMediaPlayerVideoView.setNeedMatrixTransform(true);
//                            doScreenOrientationRotate(mScreenOrientation);
                        } else if (mScreenOrientation == ORIENTATION_PORTRAIT_NORMAL) {
                            Log.i("eflake", "Accurate ScreenOrientation = " + mScreenOrientation);
//                            mLiveMediaPlayerVideoView.setNeedMatrixTransform(true);
//                            doScreenOrientationRotate(mScreenOrientation);
                        }
                    }
                }
            };
            enableOrientationEventListener();
        }

    }

    private int convertAngle2Orientation(int angle) {

        int screentOrientation = ORIENTATION_HORIZON;

        if ((angle >= 315 && angle <= 359) || (angle >= 0 && angle < 45)) {
            screentOrientation = ORIENTATION_PORTRAIT_NORMAL;
            if (mDeviceNaturalOrientationLandscape) {
                screentOrientation = ORIENTATION_LANDSCAPE_NORMAL;
            }
        } else if (angle >= 45 && angle < 135) {
            screentOrientation = ORIENTATION_LANDSCAPE_REVERSED;
            if (mDeviceNaturalOrientationLandscape) {
                screentOrientation = ORIENTATION_PORTRAIT_NORMAL;
            }
        } else if (angle >= 135 && angle < 225) {
            screentOrientation = ORIENTATION_PORTRAIT_REVERSED;
            if (mDeviceNaturalOrientationLandscape) {
                screentOrientation = ORIENTATION_LANDSCAPE_REVERSED;
            }
        } else if (angle >= 225 && angle < 315) {
            screentOrientation = ORIENTATION_LANDSCAPE_NORMAL;
            if (mDeviceNaturalOrientationLandscape) {
                screentOrientation = ORIENTATION_PORTRAIT_REVERSED;
            }
        }

        return screentOrientation;

    }

    private void doScreenOrientationRotate(int screenOrientation) {
//        mLiveMediaPlayerVideoView.setTargetOrientation(mScreenOrientation);
        switch (screenOrientation) {
            case ORIENTATION_PORTRAIT_NORMAL:
                mActivity
                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case ORIENTATION_LANDSCAPE_REVERSED:

                mActivity
                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);

                break;
            case ORIENTATION_PORTRAIT_REVERSED:
                mActivity
                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                break;
            case ORIENTATION_LANDSCAPE_NORMAL:
                mActivity
                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                break;
        }
    }


    private void enableOrientationEventListener() {

        if (mOrientationEventListener != null
                && mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable();
        }
    }

    private void disableOrientationEventListener() {

        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
            mScreenOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
        }
    }

    private void updateVideoInfo2Controller() {
//        mLiveMediaPlayerControllerView.updateVideoTitle(url);
        mLiveMediaPlayerEventActionView.updateVideoTitle(url);
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
                mLiveMediaPlayerEventActionView
                        .updateEventMode(
                                mLiveMediaPlayerEventActionView.EVENT_ACTION_VIEW_MODE_COMPLETE,
                                null);
                mLiveMediaPlayerEventActionView.show();
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
                if (!mLiveMediaPlayerVideoView.mNeedPauseAfterLeave) {
                    mLiveMediaPlayerVideoView.start();
                } else {
                    Log.d(Constants.LOG_TAG, "mOnPreparedListener ingore start for last paused state");
                    mLiveMediaPlayerVideoView.mNeedPauseAfterLeave = false;
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
                mLiveMediaPlayerEventActionView.hide();
                if (mMediaPlayerController != null) {
                    mMediaPlayerController.start();
                } else {
                    mLiveMediaPlayerVideoView.start();
                }
            } else {
                mIsComplete = true;
                mLiveMediaPlayerControllerView.stopLiveTimer();
                mLiveMediaPlayerControllerView.setVisibility(GONE);

                mLiveMediaPlayerEventActionView
                        .updateEventMode(
                                mLiveMediaPlayerEventActionView.EVENT_ACTION_VIEW_MODE_COMPLETE,
                                null);
                mLiveMediaPlayerEventActionView.show();
                WakeLocker.release();
            }
        }
    };

    IMediaPlayer.OnInfoListener mOnInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {

            switch (what) {
                // 视频缓冲开始
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
            mLiveMediaPlayerControllerView.stopLiveTimer();
            mLiveMediaPlayerControllerView.setVisibility(GONE);
            mMediaPlayerBufferingView.hide();
            mMediaPlayerLoadingView.hide();
            mLiveMediaPlayerEventActionView.updateEventMode(
                    mLiveMediaPlayerEventActionView.EVENT_ACTION_VIEW_MODE_ERROR,
                    what + "," + extra);
            mLiveMediaPlayerEventActionView.show();
            return true;
        }
    };

    public void setPlayConfig(boolean isStream, int interruptMode, int videoMode) {
        playConfig.setStream(isStream);
        playConfig.setInterruptMode(interruptMode);
        playConfig.setVideoMode(videoMode);
    }

    public interface PlayerViewCallback {
        void onPrepared();

        void onFinish(int playMode);

        void onError(int errorCode, String errorMsg);
    }

    private final ILiveController mMediaPlayerController = new ILiveController() {

        private Bitmap bitmap;

        @Override
        public void start() {
            Log.i(Constants.LOG_TAG, " MediaPlayerView  start()  canStart()="
                    + canStart());
            if (canStart()) {
                mLiveMediaPlayerVideoView.start();
                WakeLocker.acquire(getContext());
            }
        }

        @Override
        public void pause() {
            Log.i(Constants.LOG_TAG, " MediaPlayerView  pause() ");
            if (canPause()) {
                mLiveMediaPlayerVideoView.pause();
                WakeLocker.release();
            }
        }

        @Override
        public int getDuration() {

            return mLiveMediaPlayerVideoView.getDuration();
        }

        @Override
        public int getCurrentPosition() {
            if (mIsComplete) {
                return getDuration();
            }
            return mLiveMediaPlayerVideoView.getCurrentPosition();
        }

        @Override
        public void seekTo(long pos) {
            Log.i(Constants.LOG_TAG, " MediaPlayerView  seekTo ");
            if (canSeekBackward() && canSeekForward()) {
                mLiveMediaPlayerVideoView.seekTo(pos);
            } else {
                Toast.makeText(getContext(),
                        "current is real stream, seek is unSupported !",
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public boolean isPlaying() {

            return mLiveMediaPlayerVideoView.isPlaying();
        }

        @Override
        public int getBufferPercentage() {

            return mLiveMediaPlayerVideoView.getBufferPercentage();
        }

        @Override
        public boolean canPause() {

            Log.i(Constants.LOG_TAG,
                    "can pause ? " + (mLiveMediaPlayerVideoView.canPause()));
            return mLiveMediaPlayerVideoView.canPause();
        }

        @Override
        public boolean canSeekBackward() {

            Log.i(Constants.LOG_TAG, " can Seek Backward ? "
                    + (mLiveMediaPlayerVideoView.canSeekBackward()));
            return mLiveMediaPlayerVideoView.canSeekBackward();
        }

        @Override
        public boolean canSeekForward() {

            Log.i(Constants.LOG_TAG, " can Seek Forward ? "
                    + (mLiveMediaPlayerVideoView.canSeekForward()));
            return mLiveMediaPlayerVideoView.canSeekForward();
        }

        @Override
        public void onBackPress(int playMode) {
            if (MediaPlayerUtils.isWindowMode(playMode)) {
                if (mPlayerViewCallback != null)
                    mPlayerViewCallback.onFinish(playMode);
            }
        }

        @Override
        public boolean canStart() {

            Log.i(Constants.LOG_TAG,
                    "can Start ? " + mLiveMediaPlayerVideoView.canStart());
            return mLiveMediaPlayerVideoView.canStart();
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
            mLiveMediaPlayerEventActionView.hide();
        }

        @Override
        public void onPause() {
            Log.i(Constants.LOG_TAG, "on pause called");
            mLiveMediaPlayerEventActionView.hide();
        }
    };

    @Override
    public void onPowerState(int state) {
        if (powerStateListener != null) {
            this.powerStateListener.onPowerState(state);
        }
    }

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

}