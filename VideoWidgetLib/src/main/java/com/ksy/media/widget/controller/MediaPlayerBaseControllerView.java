package com.ksy.media.widget.controller;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.ksy.media.widget.ui.common.MediaPlayerControllerBrightView;
import com.ksy.media.widget.ui.common.MediaPlayerControllerVolumeView;
import com.ksy.media.widget.ui.common.MediaPlayerScreenSizePopupView;
import com.ksy.media.widget.ui.common.MediaPlayerSeekView;
import com.ksy.media.widget.model.MediaPlayerMovieRatio;
import com.ksy.media.widget.util.MediaPlayerUtils;
import com.ksy.media.widget.model.MediaPlayerVideoQuality;

/*
*   Base controller, handle show/hide and gesture event
* */
public abstract class MediaPlayerBaseControllerView extends FrameLayout {

    // Gesture control
    private volatile boolean mNeedGesture = false;
    private volatile boolean mNeedGestureLight = false;
    private volatile boolean mNeedGestureVolume = false;
    private volatile boolean mNeedGestureSeek = false;
    // Timer
    private volatile boolean mEnableTicker = true;
    private volatile boolean mIsTickerStarted = false;
    // Duration
    protected static final int HIDE_TIMEOUT_DEFAULT = 3000;
    protected static final int TICKER_INTERVAL_DEFAULT = 1000;
    protected static final int MAX_VIDEO_PROGRESS = 1000;
    // Message type
    protected static final int MSG_SHOW = 0x10;
    protected static final int MSG_HIDE = 0x11;
    protected static final int MSG_TICKE = 0x12;
    // Gesture recognise
    private static final double RADIUS_SLOP = Math.PI * 1 / 4;
    // Gesture type
    private static final int GESTURE_NONE = 0x00;
    private static final int GESTURE_LIGHT = 0x01;
    private static final int GESTURE_VOLUME = 0x02;
    private static final int GESTURE_SEEK = 0x03;
    // State
    private volatile int mCurrentGesture = GESTURE_NONE;
    protected volatile boolean mVideoProgressTrackingTouch = false;
    protected boolean mDeviceNavigationBarExist = false;
    protected volatile boolean mScreenLock = false;
    // Default config
    protected MediaPlayerVideoQuality mCurrentQuality = MediaPlayerVideoQuality.HD;
    protected MediaPlayerMovieRatio mCurrentMovieRatio = MediaPlayerMovieRatio.WIDESCREEN;
    // Views
    protected LayoutInflater mLayoutInflater;
    protected Window mHostWindow;
    protected WindowManager.LayoutParams mHostWindowLayoutParams;
    protected MediaPlayerController mMediaPlayerController;
    protected GestureDetector mGestureDetector;
    protected MediaPlayerScreenSizePopupView mScreenPopup;
    protected MediaPlayerControllerBrightView mControllerBrightView;
    protected MediaPlayerControllerVolumeView mWidgetVolumeControl;
    protected MediaPlayerSeekView mWidgetSeekView;
//    protected MediaPlayerBrightView mWidgetLightView;
//    protected MediaPlayerVolumeView mWidgetVolumeView;


    public MediaPlayerBaseControllerView(Context context, AttributeSet attrs,
                                         int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MediaPlayerBaseControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MediaPlayerBaseControllerView(Context context) {
        super(context);
        init();
    }

    protected void startTimerTicker() {
        if (mIsTickerStarted)
            return;
        mIsTickerStarted = true;
        mHandler.removeMessages(MSG_TICKE);
        mHandler.sendEmptyMessage(MSG_TICKE);
    }

    protected void stopTimerTicker() {
        if (!mIsTickerStarted)
            return;
        mIsTickerStarted = false;
        mHandler.removeMessages(MSG_TICKE);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initViews();
        initListeners();
    }

    private void hideGestureView() {
        if (mNeedGesture) {
            /*if (mWidgetLightView != null && mWidgetLightView.isShowing()) {
                mWidgetLightView.hide(true);
			}*/

			/*
             * if (mWidgetVolumeView != null && mWidgetVolumeView.isShowing()) {
			 * mWidgetVolumeView.hide(true); }
			 */
            if (mWidgetSeekView != null && mWidgetSeekView.isShowing())
                mWidgetSeekView.hide(true);
        }
    }

    public void show() {
        show(HIDE_TIMEOUT_DEFAULT);
    }

    public void show(int timeout) {
        mHandler.sendEmptyMessage(MSG_SHOW);
        mHandler.removeMessages(MSG_HIDE);
        if (timeout > 0) {
            Message msgHide = mHandler.obtainMessage(MSG_HIDE);
            mHandler.sendMessageDelayed(msgHide, timeout);
        }
    }

    public void hide() {
        mHandler.sendEmptyMessage(MSG_HIDE);
    }

    public void toggle() {
        if (isShowing()) {
            hide();
        } else {
            if (!mMediaPlayerController.isPlaying()) {
                show(0);
            } else {
                show();
            }
        }
    }

    public boolean isShowing() {
        if (getVisibility() == View.VISIBLE)
            return true;
        return false;
    }

    public void setNeedGestureDetector(boolean need) {
        this.mNeedGesture = need;
    }

    public void setNeedGestureAction(boolean needLightGesture,
                                     boolean needVolumeGesture, boolean needSeekGesture) {

        this.mNeedGestureLight = needLightGesture;
        this.mNeedGestureVolume = needVolumeGesture;
        this.mNeedGestureSeek = needSeekGesture;
    }

    public void setNeedTicker(boolean need) {
        this.mEnableTicker = need;
    }

    public void setMediaPlayerController(MediaPlayerController mediaPlayerController) {
        mMediaPlayerController = mediaPlayerController;
        mScreenPopup = new MediaPlayerScreenSizePopupView(getContext(), mMediaPlayerController);
    }

    public void setHostWindow(Window window) {
        if (window != null) {
            mHostWindow = window;
            mHostWindowLayoutParams = window.getAttributes();
        }
    }

    public void setDeviceNavigationBarExist(boolean deviceNavigationBarExist) {
        mDeviceNavigationBarExist = deviceNavigationBarExist;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mNeedGesture && !mScreenLock) {
                    if (mNeedGestureSeek) {
                        if (mWidgetSeekView != null)
                            mWidgetSeekView.onGestureSeekBegin(
                                    mMediaPlayerController.getCurrentPosition(),
                                    mMediaPlayerController.getDuration());
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isShowing() && !mScreenLock) {
                    show();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mNeedGesture && !mScreenLock) {
                    if (mCurrentGesture == GESTURE_LIGHT) {
                        if (mNeedGestureLight) {
                        /*if (mWidgetLightView != null)
                            mWidgetLightView.onGestureLightFinish();*/
                        }
                    } else if (mCurrentGesture == GESTURE_VOLUME) {
                        if (mNeedGestureVolume) {
						/*
						 * if (mWidgetVolumeView != null)
						 * mWidgetVolumeView.onGestureVolumeFinish();
						 */

                            if (mWidgetVolumeControl != null) {

                            }
                        }
                    } else if (mCurrentGesture == GESTURE_SEEK) {
                        if (mNeedGestureSeek) {
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
                    }
                    mCurrentGesture = GESTURE_NONE;
                }
                break;
            default:
                break;
        }

        if (mNeedGesture) {
            if (mGestureDetector != null)
                mGestureDetector.onTouchEvent(event);
        }
        return true;
    }


    private void init() {
        mLayoutInflater = LayoutInflater.from(getContext());
        mGestureDetector = new GestureDetector(getContext(),
                new GestureDetector.OnGestureListener() {

                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {

                        if (mCurrentGesture == GESTURE_NONE) {
                            toggle();
                        }
                        return false;
                    }

                    @Override
                    public void onShowPress(MotionEvent e) {

                    }

                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                            float distanceX, float distanceY) {

                        if (e1 == null || e2 == null || mScreenLock) {
                            return false;
                        }

                        float oldX = e1.getX();
                        final double distance = Math.sqrt(Math
                                .pow(distanceX, 2) + Math.pow(distanceY, 2));
                        int selfWidth = getMeasuredWidth();
                        final double radius = distanceY / distance;

                        if (Math.abs(radius) > RADIUS_SLOP) {
                            // for voice control
                            if (oldX > selfWidth / 2) {
                                if (!mNeedGestureVolume)
                                    return false;
                                if (mCurrentGesture == GESTURE_NONE
                                        || mCurrentGesture == GESTURE_VOLUME) {
                                    mCurrentGesture = GESTURE_VOLUME;
                                    if (!isShowing())
                                        show();
									/*if (mWidgetLightView != null)
										mWidgetLightView.hide(true);*/
                                    if (mWidgetSeekView != null)
                                        mWidgetSeekView.hide(true);
                                    AudioManager audioManager = (AudioManager) getContext()
                                            .getSystemService(
                                                    Context.AUDIO_SERVICE);
                                    float totalVolumeDistance = getMeasuredHeight();
                                    if (totalVolumeDistance <= 0)
                                        totalVolumeDistance = MediaPlayerUtils
                                                .getRealDisplayHeight(mHostWindow);
									/*
									 * if (mWidgetVolumeView != null) {
									 * mWidgetVolumeView.onGestureVolumeChange
									 * (distanceY, totalVolumeDistance / 4,
									 * audioManager); }
									 */

                                    if (mWidgetVolumeControl != null) { // for voice gesture
//										Log.d(Constants.LOG_TAG, "351 basecontrol mWidgetVolumeControl .......");
                                        mWidgetVolumeControl
                                                .onGestureVolumeChange(
                                                        distanceY,
                                                        totalVolumeDistance / 4,
                                                        audioManager);
                                    }

                                }
                            }
                            // for light gesture
                            else {
                                if (!mNeedGestureLight)
                                    return false;
                                if (mCurrentGesture == GESTURE_NONE
                                        || mCurrentGesture == GESTURE_LIGHT) {
                                    mCurrentGesture = GESTURE_LIGHT;
                                    if (!isShowing())
                                        show();
                                    // if (mWidgetVolumeView != null) {
                                    // mWidgetVolumeView.hide(true);
                                    // }
                                    if (mWidgetSeekView != null)
                                        mWidgetSeekView.hide(true);
                                    float totalLightDistance = getMeasuredHeight();
                                    if (totalLightDistance <= 0) {
                                        totalLightDistance = MediaPlayerUtils
                                                .getRealDisplayHeight(mHostWindow);
                                    }
                                    if (mControllerBrightView != null) {
                                        // mWidgetLightView.onGestureLightChange(distanceY,
                                        // totalLightDistance / 4, mHostWindow);

                                        mControllerBrightView
                                                .onGestureLightChange(distanceY, mHostWindow);
                                    }
                                }
                            }
                        }
                        // for seek gesture
                        else {
                            if (!mNeedGestureSeek)
                                return false;
                            if (mCurrentGesture == GESTURE_NONE
                                    || mCurrentGesture == GESTURE_SEEK) {
                                mCurrentGesture = GESTURE_SEEK;
                                if (!isShowing())
                                    show();
                                // if (mWidgetVolumeView != null) {
                                // mWidgetVolumeView.hide(true);
                                // }
								/*if (mWidgetLightView != null)
									mWidgetLightView.hide(true);*/

                                float totalSeekDistance = getMeasuredWidth();
                                if (totalSeekDistance <= 0)
                                    totalSeekDistance = MediaPlayerUtils
                                            .getRealDisplayWidth(mHostWindow);
                                if (mWidgetSeekView != null)
                                    mWidgetSeekView.onGestureSeekChange(
                                            -distanceX, totalSeekDistance);
                            }
                        }
                        return false;
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {

                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2,
                                           float velocityX, float velocityY) {

                        return false;
                    }

                    @Override
                    public boolean onDown(MotionEvent e) {

                        return false;
                    }
                });

        mGestureDetector.setOnDoubleTapListener(new OnDoubleTapListener() {

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {

                return false;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {

                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {

                if (mScreenLock)
                    return false;
                if (mMediaPlayerController.isPlaying()) {
                    mMediaPlayerController.pause();
                } else {
                    mMediaPlayerController.start();
                }
                return true;
            }
        });

    }

    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_SHOW:
                    startTimerTicker();
                    setVisibility(View.VISIBLE);
                    onShow();
                    break;
                case MSG_HIDE:
                    stopTimerTicker();
                    hideGestureView();
                    setVisibility(View.GONE);
                    onHide();
                    break;
                case MSG_TICKE:
                    if (mEnableTicker) {
                        onTimerTicker();
                    }
                    sendEmptyMessageDelayed(MSG_TICKE, TICKER_INTERVAL_DEFAULT);
                    break;
                default:
                    break;
            }

        }

        ;
    };

    public void setMediaQuality(MediaPlayerVideoQuality quality) {

        this.mCurrentQuality = quality;
    }

    public MediaPlayerVideoQuality getQuality() {

        return this.mCurrentQuality;
    }

    public void setMovieRatio(MediaPlayerMovieRatio movieRatio) {
        this.mCurrentMovieRatio = movieRatio;
    }

    public MediaPlayerMovieRatio getMovieRatio() {

        return this.mCurrentMovieRatio;
    }

    abstract void initViews();

    abstract void initListeners();

    abstract void onShow();

    abstract void onHide();

    abstract void onTimerTicker();

}
