package com.ksy.media.widget.ui.stream;

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
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
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
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ksy.media.widget.controller.stream.IStreamController;
import com.ksy.media.widget.controller.stream.StreamMediaPlayerLargeControllerView;
import com.ksy.media.widget.controller.stream.StreamMediaPlayerSmallControllerView;
import com.ksy.media.widget.model.MediaPlayMode;
import com.ksy.media.widget.player.IMediaPlayerPlus;
import com.ksy.media.widget.util.MediaPlayerUtils;
import com.ksy.media.widget.model.MediaPlayerVideoQuality;
import com.ksy.media.widget.util.NetReceiver;
import com.ksy.media.widget.util.NetReceiver.NetState;
import com.ksy.media.widget.util.NetReceiver.NetStateChangedListener;
import com.ksy.media.widget.util.WakeLocker;
import com.ksy.media.widget.ui.base.MediaPlayerBufferingView;
import com.ksy.media.widget.ui.base.MediaPlayerEventActionView;
import com.ksy.media.widget.ui.base.MediaPlayerLoadingView;
import com.ksy.media.widget.util.Constants;
import com.ksy.media.widget.util.drm.DRMKey;
import com.ksy.media.widget.util.drm.DRMRetrieverManager;
import com.ksy.media.widget.util.drm.DRMRetrieverResponseHandler;
import com.ksy.media.widget.util.drm.IDRMRetriverRequest;
import com.ksy.media.widget.util.NetworkUtil;
import com.ksy.media.widget.util.PlayConfig;
import com.ksy.media.widget.util.IPowerStateListener;
import com.ksy.media.widget.videoview.MediaPlayerTextureView;
import com.ksy.media.widget.videoview.MediaPlayerVideoView;
import com.ksy.mediaPlayer.widget.R;
import com.ksyun.media.player.IMediaPlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class StreamMediaPlayerView extends RelativeLayout implements
        IPowerStateListener {
    private static final int QUALITY_BEST = 100;
    private static final String CAPUTRE_SCREEN_PATH = "KSY_SDK_SCREENSHOT";
    private Activity mActivity;
    private LayoutInflater mLayoutInflater;
    private Window mWindow;

    private ViewGroup mRootView;
    private MediaPlayerVideoView mMediaPlayerVideoView;

    private StreamMediaPlayerLargeControllerView mMediaPlayerLargeControllerView;
    private StreamMediaPlayerSmallControllerView mMediaPlayerSmallControllerView;
    private MediaPlayerBufferingView mMediaPlayerBufferingView;
    private MediaPlayerLoadingView mMediaPlayerLoadingView;
    private MediaPlayerEventActionView mMediaPlayerEventActionView;

    private PlayerViewCallback mPlayerViewCallback;

    private final int ORIENTATION_UNKNOWN = -2;
    private final int ORIENTATION_HORIZON = -1;
    private final int ORIENTATION_PORTRAIT_NORMAL = 0;
    private final int ORIENTATION_LANDSCAPE_REVERSED = 90;
    private final int ORIENTATION_PORTRAIT_REVERSED = 180;
    private final int ORIENTATION_LANDSCAPE_NORMAL = 270;

    private volatile boolean mNeedGesture = true;
    private volatile boolean mNeedLightGesture = true;
    private volatile boolean mNeedVolumeGesture = true;
    private volatile boolean mNeedSeekGesture = true;

    private volatile int mScreenOrientation = ORIENTATION_UNKNOWN;
    private volatile int mPlayMode = MediaPlayMode.PLAY_MODE_FULLSCREEN;
    private volatile boolean mLockMode = false;
    private volatile boolean mScreenLockMode = false;
    private volatile boolean mScreenshotPreparing = false;

    private boolean mVideoReady = false;
    private boolean mStartAfterPause = false;
    private int mPausePosition = 0;

    private OrientationEventListener mOrientationEventListener;

    private ViewGroup.LayoutParams mLayoutParamWindowMode;
    private ViewGroup.LayoutParams mLayoutParamFullScreenMode;

    private LayoutParams mMediaPlayerControllerViewLargeParams;
    private LayoutParams mMediaPlayerControllerViewSmallParams;

    private volatile boolean mWindowActived = false;

    private boolean mDeviceNaturalOrientationLandscape;
    private boolean mCanLayoutSystemUI;
    private boolean mDeviceNavigationBarExist;
    private int mFullScreenNavigationBarHeight;
    private int mDeviceNavigationType = MediaPlayerUtils.DEVICE_NAVIGATION_TYPE_UNKNOWN;
    private int mDisplaySizeMode = MediaPlayerTextureView.MOVIE_RATIO_MODE_16_9;

    private NetReceiver mNetReceiver;
    private NetStateChangedListener mNetChangedListener;
    private boolean mIsComplete = false;

    private float mCurrentPlayingRatio = 1f;
    private float mCurrentPlayingVolumeRatio = 1f;
    public static float MAX_PLAYING_RATIO = 4f;
    public static float MAX_PLAYING_VOLUME_RATIO = 3.0f;
    // add for replay
    private boolean mRecyclePlay = false;

    private DRMRetrieverManager mDrmManager;
    private DRMRetrieverResponseHandler mDrmHandler;

    private RelativeLayout layoutPop;
    private Handler mHandler = new Handler();

    private PlayConfig playConfig = PlayConfig.getInstance();
    private Context mContext;
    private IPowerStateListener powerStateListener;

    public StreamMediaPlayerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init(context, attrs, defStyle);

    }

    public StreamMediaPlayerView(Context context, AttributeSet attrs) {

        super(context, attrs);
        mContext = context;
        init(context, attrs, -1);

    }

    public StreamMediaPlayerView(Context context) {

        super(context);
        mContext = context;
        init(context, null, -1);

    }

    private void init(Context context, AttributeSet attrs, int defStyle)
            throws IllegalArgumentException, NullPointerException {

        if (null == context)
            throw new NullPointerException("Context can not be null !");

        // For power btn pressed
        registerPowerReceiver();
        setPowerStateListener(this);

        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.PlayerView);
        int playmode = typedArray.getInt(R.styleable.PlayerView_playmode,
                MediaPlayMode.PLAY_MODE_FULLSCREEN);
        if (playmode == 0) {
            this.mPlayMode = MediaPlayMode.PLAY_MODE_FULLSCREEN;
        } else if (playmode == 1) {
            this.mPlayMode = MediaPlayMode.PLAY_MODE_WINDOW;
        }
        this.mLockMode = typedArray.getBoolean(R.styleable.PlayerView_lockmode,
                false);
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
                R.layout.stream_blue_media_player_view, null);

        this.layoutPop = (RelativeLayout) mRootView
                .findViewById(R.id.layoutPop);

        this.mMediaPlayerVideoView = (MediaPlayerVideoView) mRootView
                .findViewById(R.id.ks_camera_video_view);
        this.mMediaPlayerBufferingView = (MediaPlayerBufferingView) mRootView
                .findViewById(R.id.ks_camera_buffering_view);
        this.mMediaPlayerLoadingView = (MediaPlayerLoadingView) mRootView
                .findViewById(R.id.ks_camera_loading_view);
        this.mMediaPlayerEventActionView = (MediaPlayerEventActionView) mRootView
                .findViewById(R.id.ks_camera_event_action_view);
        this.mMediaPlayerLargeControllerView = (StreamMediaPlayerLargeControllerView) mRootView
                .findViewById(R.id.media_player_controller_view_large);
        this.mMediaPlayerSmallControllerView = (StreamMediaPlayerSmallControllerView) mRootView
                .findViewById(R.id.media_player_controller_view_small);

		/* 设置播放器监听器 */
        this.mMediaPlayerVideoView.setOnPreparedListener(mOnPreparedListener);
        this.mMediaPlayerVideoView
                .setOnBufferingUpdateListener(mOnPlaybackBufferingUpdateListener);
        this.mMediaPlayerVideoView
                .setOnCompletionListener(mOnCompletionListener);
        this.mMediaPlayerVideoView.setOnInfoListener(mOnInfoListener);
        this.mMediaPlayerVideoView.setOnErrorListener(mOnErrorListener);
//        this.mMediaPlayerVideoView.setOnSurfaceListener(mOnSurfaceListener);
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

		/* 设置eventActionView callback */
        this.mMediaPlayerEventActionView
                .setCallback(new MediaPlayerEventActionView.EventActionViewCallback() {
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
                            switch (playConfig.getVideoMode()) {
                                case PlayConfig.SHORT_VIDEO_MODE:
                                    Log.d(Constants.LOG_TAG, "PlayConfig.SHORT_VIDEO_MODE  11111 ");
                                    playConfig.setInterruptMode(PlayConfig.INTERRUPT_MODE_PAUSE_RESUME);
                                    break;

                                case PlayConfig.LIVE_VIDEO_MODE:
                                    Log.d(Constants.LOG_TAG, "PlayConfig.LIVE_VIDEO_MODE  2222222 ");
                                    playConfig.setInterruptMode(PlayConfig.INTERRUPT_MODE_RELEASE_CREATE);
                                    break;

                                case PlayConfig.OTHER_MODE:

                                    break;
                            }

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
                            switch (playConfig.getVideoMode()) {
                                case PlayConfig.SHORT_VIDEO_MODE:
                                    Log.d(Constants.LOG_TAG, "PlayConfig.SHORT_VIDEO_MODE  11111 ");
                                    playConfig.setInterruptMode(PlayConfig.INTERRUPT_MODE_PAUSE_RESUME);
                                    break;

                                case PlayConfig.LIVE_VIDEO_MODE:
                                    Log.d(Constants.LOG_TAG, "PlayConfig.LIVE_VIDEO_MODE  2222222 ");
                                    playConfig.setInterruptMode(PlayConfig.INTERRUPT_MODE_RELEASE_CREATE);
                                    break;

                                case PlayConfig.OTHER_MODE:

                                    break;
                            }

                            mMediaPlayerEventActionView.hide();
                            mMediaPlayerLargeControllerView.hide();
                            mMediaPlayerSmallControllerView.hide();
                            mMediaPlayerLoadingView.show();
                            mMediaPlayerVideoView.release(true);
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
        this.mMediaPlayerLargeControllerView
                .setMediaPlayerController(mMediaPlayerController);
        this.mMediaPlayerLargeControllerView.setHostWindow(mWindow); // 声音和亮度获取
        this.mMediaPlayerLargeControllerView
                .setDeviceNavigationBarExist(mDeviceNavigationBarExist);
        this.mMediaPlayerLargeControllerView
                .setNeedGestureDetector(mNeedGesture);
        this.mMediaPlayerLargeControllerView.setNeedGestureAction(
                mNeedLightGesture, mNeedVolumeGesture, false);
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
        this.mMediaPlayerSmallControllerView
                .setMediaPlayerController(mMediaPlayerController);
        this.mMediaPlayerSmallControllerView.setHostWindow(mWindow);
        this.mMediaPlayerSmallControllerView
                .setDeviceNavigationBarExist(mDeviceNavigationBarExist);
        this.mMediaPlayerSmallControllerView.setNeedGestureDetector(true);
        this.mMediaPlayerSmallControllerView.setNeedGestureAction(false, false,
                false);
        this.mMediaPlayerControllerViewSmallParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		/* 移除掉所有的view */
        removeAllViews();
        mRootView.removeView(mMediaPlayerVideoView);
        mRootView.removeView(mMediaPlayerBufferingView);
        mRootView.removeView(mMediaPlayerLoadingView);
        mRootView.removeView(mMediaPlayerEventActionView);
        mRootView.removeView(mMediaPlayerLargeControllerView);
        mRootView.removeView(mMediaPlayerSmallControllerView);
        mRootView.removeView(layoutPop);

		/* 添加全屏或者是窗口模式初始状态下所需的view */
        addView(mMediaPlayerVideoView, mediaPlayerVideoViewParams);
        addView(mMediaPlayerBufferingView, mediaPlayerBufferingViewParams);
        addView(mMediaPlayerLoadingView, mediaPlayerLoadingViewParams);
        addView(mMediaPlayerEventActionView, mediaPlayereventActionViewParams);
        addView(layoutPop, mediaPlayerPopViewParams);

        if (MediaPlayerUtils.isFullScreenMode(mPlayMode)) {
            addView(mMediaPlayerLargeControllerView,
                    mMediaPlayerControllerViewLargeParams);
            mMediaPlayerLargeControllerView.hide();
            mWindow.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else if (MediaPlayerUtils.isWindowMode(mPlayMode)) {
            addView(mMediaPlayerSmallControllerView,
                    mMediaPlayerControllerViewSmallParams);
            mMediaPlayerSmallControllerView.hide();
        }

        mMediaPlayerBufferingView.hide();
        mMediaPlayerLoadingView.hide();
        mMediaPlayerEventActionView.hide();

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
                    mLayoutParamFullScreenMode = constructor.newInstance(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }

            }
        });
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

            if (MediaPlayerUtils.isFullScreenMode(mPlayMode)) {
                return mMediaPlayerLargeControllerView.dispatchTouchEvent(ev);
            }
            if (MediaPlayerUtils.isWindowMode(mPlayMode)) {
                return mMediaPlayerSmallControllerView.dispatchTouchEvent(ev);
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
            if (MediaPlayerUtils.isFullScreenMode(mPlayMode)) {
                if (mLockMode) {
                    if (mPlayerViewCallback != null)
                        mPlayerViewCallback.onFinish(mPlayMode);
                } else {
                    mMediaPlayerController
                            .onRequestPlayMode(MediaPlayMode.PLAY_MODE_WINDOW);
                }
                return true;
            } else if (MediaPlayerUtils.isWindowMode(mPlayMode)) {

                if (mPlayerViewCallback != null)
                    mPlayerViewCallback.onFinish(mPlayMode);
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

    public void setmRecyclePlay(boolean mRecyclePlay) {
        this.mRecyclePlay = mRecyclePlay;
    }

    public int getPlayMode() {

        return this.mPlayMode;
    }

    private boolean requestPlayMode(int requestPlayMode) {

        if (mPlayMode == requestPlayMode)
            return false;

        // 请求全屏模式
        if (MediaPlayerUtils.isFullScreenMode(requestPlayMode)) {

            if (mLayoutParamFullScreenMode == null)
                return false;

            removeView(mMediaPlayerSmallControllerView);
            addView(mMediaPlayerLargeControllerView,
                    mMediaPlayerControllerViewLargeParams);
            this.setLayoutParams(mLayoutParamFullScreenMode);
            mMediaPlayerLargeControllerView.hide();
            mMediaPlayerSmallControllerView.hide();

            if (mPlayerViewCallback != null)
                mPlayerViewCallback.hideViews();

            mWindow.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            if (mDeviceNavigationBarExist)
                MediaPlayerUtils.hideSystemUI(mWindow, true);

            mPlayMode = requestPlayMode;
            return true;

        }
        // 请求窗口模式
        else if (MediaPlayerUtils.isWindowMode(requestPlayMode)) {

            if (mLayoutParamWindowMode == null)
                return false;

            removeView(mMediaPlayerLargeControllerView);
            addView(mMediaPlayerSmallControllerView,
                    mMediaPlayerControllerViewSmallParams);
            this.setLayoutParams(mLayoutParamWindowMode);
            mMediaPlayerLargeControllerView.hide();
            mMediaPlayerSmallControllerView.hide();

            if (mPlayerViewCallback != null)
                mPlayerViewCallback.restoreViews();

            mWindow.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            if (mDeviceNavigationBarExist)
                MediaPlayerUtils.showSystemUI(mWindow, false);

            mPlayMode = requestPlayMode;
            return true;

        }

        return false;

    }

    public void onResume() {
        Log.d("Constants.LOG_TAG", "PlayView onResume");
        powerStateListener.onPowerState(Constants.APP_SHOWN);
        mWindowActived = true;
        enableOrientationEventListener();
        mNetReceiver.registNetBroadCast(getContext());
        mNetReceiver.addNetStateChangeListener(mNetChangedListener);
    }

    public void onPause() {
        Log.d("Constants.LOG_TAG", "PlayView OnPause");
        powerStateListener.onPowerState(Constants.APP_HIDDEN);

        mNetReceiver.remoteNetStateChangeListener(mNetChangedListener);
        mNetReceiver.unRegistNetBroadCast(getContext());
        mWindowActived = false;
        mPausePosition = mMediaPlayerController.getCurrentPosition();

        disableOrientationEventListener();

        WakeLocker.release();
    }

    public void onDestroy() {
        unregisterPowerReceiver();
        mIsComplete = false;
        mMediaPlayerVideoView.release(true);
        Log.d(Constants.LOG_TAG, "MediaPlayerView   onDestroy....");
    }

    private void initOrientationEventListener(Context context) {

        if (null == context)
            return;

        if (null == mOrientationEventListener) {
            mOrientationEventListener = new OrientationEventListener(context,
                    SensorManager.SENSOR_DELAY_NORMAL) {

                @Override
                public void onOrientationChanged(int orientation) {

                    int preScreenOrientation = mScreenOrientation;
                    mScreenOrientation = convertAngle2Orientation(orientation);
                    if (mScreenLockMode)
                        return;
                    if (!mWindowActived)
                        return;

                    if (preScreenOrientation == ORIENTATION_UNKNOWN)
                        return;
                    if (mScreenOrientation == ORIENTATION_UNKNOWN)
                        return;
                    if (mScreenOrientation == ORIENTATION_HORIZON)
                        return;

                    if (preScreenOrientation != mScreenOrientation) {
                        if (!MediaPlayerUtils.checkSystemGravity(getContext()))
                            return;
                        if (MediaPlayerUtils.isWindowMode(mPlayMode)) {
                            Log.i(Constants.LOG_TAG, " Window to FullScreen ");
                            if (mScreenOrientation == ORIENTATION_LANDSCAPE_NORMAL
                                    || mScreenOrientation == ORIENTATION_LANDSCAPE_REVERSED) {
                                if (!mLockMode) {
                                    boolean requestResult = requestPlayMode(MediaPlayMode.PLAY_MODE_FULLSCREEN);
                                    if (requestResult) {
                                        doScreenOrientationRotate(mScreenOrientation);
                                    }
                                }
                            }
                        } else if (MediaPlayerUtils.isFullScreenMode(mPlayMode)) {
                            Log.i(Constants.LOG_TAG, " Full Screen to Window");
                            if (mScreenOrientation == ORIENTATION_PORTRAIT_NORMAL) {
                                if (!mLockMode) {
                                    boolean requestResult = requestPlayMode(MediaPlayMode.PLAY_MODE_WINDOW);
                                    if (requestResult) {
                                        doScreenOrientationRotate(mScreenOrientation);
                                    }
                                }
                            } else if (mScreenOrientation == ORIENTATION_LANDSCAPE_NORMAL
                                    || mScreenOrientation == ORIENTATION_LANDSCAPE_REVERSED) {
                                doScreenOrientationRotate(mScreenOrientation);
                            }
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

        switch (screenOrientation) {
            case ORIENTATION_PORTRAIT_NORMAL:
                mActivity
                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case ORIENTATION_LANDSCAPE_REVERSED:
                mActivity
                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                if (mDeviceNavigationBarExist
                        && mFullScreenNavigationBarHeight <= 0
                        && MediaPlayerUtils.isFullScreenMode(mPlayMode)) {
                    this.mFullScreenNavigationBarHeight = MediaPlayerUtils
                            .getNavigationBarHeight(mWindow);
                    this.mDeviceNavigationType = MediaPlayerUtils
                            .getDeviceNavigationType(mWindow);
                    if (mCanLayoutSystemUI && mFullScreenNavigationBarHeight > 0) {
                        if (mDeviceNavigationType == MediaPlayerUtils.DEVICE_NAVIGATION_TYPE_HANDSET) {
                            mMediaPlayerControllerViewLargeParams.rightMargin = mFullScreenNavigationBarHeight;
                        } else if (mDeviceNavigationType == MediaPlayerUtils.DEVICE_NAVIGATION_TYPE_TABLET) {
                            mMediaPlayerControllerViewLargeParams.bottomMargin = mFullScreenNavigationBarHeight;
                        }
                    }
                }
                break;
            case ORIENTATION_PORTRAIT_REVERSED:
                mActivity
                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                break;
            case ORIENTATION_LANDSCAPE_NORMAL:
                mActivity
                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                if (mDeviceNavigationBarExist
                        && mFullScreenNavigationBarHeight <= 0
                        && MediaPlayerUtils.isFullScreenMode(mPlayMode)) {
                    this.mFullScreenNavigationBarHeight = MediaPlayerUtils
                            .getNavigationBarHeight(mWindow);
                    this.mDeviceNavigationType = MediaPlayerUtils
                            .getDeviceNavigationType(mWindow);
                    if (mCanLayoutSystemUI && mFullScreenNavigationBarHeight > 0) {
                        if (mDeviceNavigationType == MediaPlayerUtils.DEVICE_NAVIGATION_TYPE_HANDSET) {
                            mMediaPlayerControllerViewLargeParams.rightMargin = mFullScreenNavigationBarHeight;
                        } else if (mDeviceNavigationType == MediaPlayerUtils.DEVICE_NAVIGATION_TYPE_TABLET) {
                            mMediaPlayerControllerViewLargeParams.bottomMargin = mFullScreenNavigationBarHeight;
                        }
                    }
                }
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

        mMediaPlayerSmallControllerView.updateVideoTitle(getResources().getString(R.string.stream_small_controller_title));

        mMediaPlayerLargeControllerView.updateVideoTitle(getResources().getString(R.string.stream_small_controller_title));
        mMediaPlayerLargeControllerView
                .updateVideoQualityState(MediaPlayerVideoQuality.HD);
        mMediaPlayerLargeControllerView.updateVideoVolumeState();

        mMediaPlayerEventActionView.updateVideoTitle(getResources().getString(R.string.stream_small_controller_title));
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
                mMediaPlayerLargeControllerView.hide();
                mMediaPlayerSmallControllerView.hide();
                mMediaPlayerEventActionView
                        .updateEventMode(
                                MediaPlayerEventActionView.EVENT_ACTION_VIEW_MODE_COMPLETE,
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

            // mMediaPlayerEventActionView.updateEventMode(
            // MediaPlayerEventActionView.EVENT_ACTION_VIEW_MODE_WAIT,
            // null);
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
                mMediaPlayerLargeControllerView.hide();
                mMediaPlayerSmallControllerView.hide();
                mMediaPlayerEventActionView
                        .updateEventMode(
                                MediaPlayerEventActionView.EVENT_ACTION_VIEW_MODE_COMPLETE,
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
            mMediaPlayerLargeControllerView.hide();
            mMediaPlayerSmallControllerView.hide();
            mMediaPlayerBufferingView.hide();
            mMediaPlayerLoadingView.hide();
            mMediaPlayerEventActionView.updateEventMode(
                    MediaPlayerEventActionView.EVENT_ACTION_VIEW_MODE_ERROR,
                    what + "," + extra);
            mMediaPlayerEventActionView.show();
            return true;
        }
    };

    public interface PlayerViewCallback {

        void hideViews();

        void restoreViews();

        void onPrepared();

        void onFinish(int playMode);

        void onError(int errorCode, String errorMsg);
    }

    private final IStreamController mMediaPlayerController = new IStreamController() {

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
        public int getPlayMode() {

            return mPlayMode;
        }

        @Override
        public void onRequestPlayMode(int requestPlayMode) {

            if (mPlayMode == requestPlayMode)
                return;
            if (mLockMode)
                return;
            // 请求全屏模式
            if (MediaPlayerUtils.isFullScreenMode(requestPlayMode)) {
                boolean requestResult = requestPlayMode(requestPlayMode);
                if (requestResult) {
                    doScreenOrientationRotate(ORIENTATION_LANDSCAPE_NORMAL);
                }
            }
            // 请求窗口模式
            else if (MediaPlayerUtils.isWindowMode(requestPlayMode)) {
                boolean requestResult = requestPlayMode(requestPlayMode);
                if (requestResult) {
                    doScreenOrientationRotate(ORIENTATION_PORTRAIT_NORMAL);
                }
            }
        }

        @Override
        public void onBackPress(int playMode) {
            Log.i(Constants.LOG_TAG,
                    "========playerview back pressed ==============playMode :"
                            + playMode + ", mPlayerViewCallback is null "
                            + (mPlayerViewCallback == null));
            if (MediaPlayerUtils.isFullScreenMode(playMode)) {
                if (mLockMode) {
                    if (mPlayerViewCallback != null)
                        mPlayerViewCallback.onFinish(playMode);
                } else {
                    mMediaPlayerController
                            .onRequestPlayMode(MediaPlayMode.PLAY_MODE_WINDOW);
                }
            } else if (MediaPlayerUtils.isWindowMode(playMode)) {
                if (mPlayerViewCallback != null)
                    mPlayerViewCallback.onFinish(playMode);
            }
        }

        @Override
        public void onControllerShow(int playMode) {

        }

        @Override
        public void onControllerHide(int playMode) {

        }

        @Override
        public void onRequestLockMode(boolean lockMode) {

            if (mScreenLockMode != lockMode) {
                mScreenLockMode = lockMode;

                // 加锁:屏幕操作锁
                if (mScreenLockMode) {
                }
                // 解锁:屏幕操作锁
                else {
                }
            }
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
            mMediaPlayerLargeControllerView.updateVideoPlaybackState(true);
            mMediaPlayerSmallControllerView.updateVideoPlaybackState(true);
        }

        @Override
        public void onPause() {
            Log.i(Constants.LOG_TAG, "on pause called");
            mMediaPlayerEventActionView.hide();
            mMediaPlayerLargeControllerView.updateVideoPlaybackState(false);
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

    public void setPlayConfig(boolean isStream, int interruptMode, int videoMode) {
        playConfig.setStream(isStream);
        playConfig.setInterruptMode(interruptMode);
        playConfig.setVideoMode(videoMode);
    }

    public void stopPlayback() {
        mMediaPlayerVideoView.stopPlayback();
    }

    public void reopen() {
        mMediaPlayerVideoView.setVideoPath(url);
    }
}
