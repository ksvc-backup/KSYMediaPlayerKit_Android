package com.ksy.media.widget.videoview;

import java.io.File;
import java.io.IOException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup.LayoutParams;

import com.ksy.media.widget.player.IMediaPlayerPlus;
import com.ksy.media.widget.util.Constants;
import com.ksy.media.widget.util.auth.MD5Util;
import com.ksy.media.widget.util.PlayConfig;
import com.ksy.media.widget.controller.base.IMediaPlayerBaseControl;
import com.ksy.media.widget.util.IPowerStateListener;
import com.ksy.media.widget.util.ScreenResolution;
import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;
import com.ksyun.media.player.MediaInfo;

import android.view.TextureView.SurfaceTextureListener;

public class MediaPlayerTextureView extends TextureView implements
        IMediaPlayerBaseControl, IPowerStateListener, SurfaceTextureListener {

    protected Uri mUri;
    protected long mDuration;
    protected MediaInfo mMediaInfo;
    protected static final int STATE_ERROR = -1;
    protected static final int STATE_IDLE = 0;
    protected static final int STATE_PREPARING = 1;
    protected static final int STATE_PREPARED = 2;
    protected static final int STATE_PLAYING = 3;
    protected static final int STATE_PAUSED = 4;
    protected static final int STATE_PLAYBACK_COMPLETED = 5;

    public int mCurrentState = STATE_IDLE;
    public int mTargetState = STATE_IDLE;

    protected int mVideoLayout = MOVIE_RATIO_MODE_DEFAULT;
    public static final int MOVIE_RATIO_MODE_DEFAULT = -1;
    public static final int MOVIE_RATIO_MODE_16_9 = 0;
    public static final int MOVIE_RATIO_MODE_4_3 = 1;
    public static final int MOVIE_RATIO_MODE_FULLSCREEN = 2;
    public static final int MOVIE_RATIO_MODE_ORIGIN = 3;

    protected SurfaceTexture mSurfaceTexture = null;
    protected IMediaPlayer mMediaPlayer = null;
    protected int mVideoWidth;
    protected int mVideoHeight;
    protected int mVideoSarNum;
    protected int mVideoSarDen;
    protected int mSurfaceWidth;
    protected int mSurfaceHeight;
    protected IMediaPlayer.OnCompletionListener mOnCompletionListener;
    protected IMediaPlayer.OnPreparedListener mOnPreparedListener;
    protected IMediaPlayer.OnErrorListener mOnErrorListener;
    protected IMediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener;
    protected IMediaPlayer.OnInfoListener mOnInfoListener;
    protected IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;
    protected IMediaPlayerPlus mMediaPlayerController;
    protected int mCurrentBufferPercentage;
    protected Context mContext;
    KSYMediaPlayer ksyMediaPlayer = null;
    protected PlayConfig playConfig = PlayConfig.getInstance();
    protected Surface mSurface;
    protected boolean misTexturePowerEvent;
    protected boolean mNeedUnlock;
    public boolean mNeedPauseAfterLeave;

    public MediaPlayerTextureView(Context context) {
        super(context);
        initTextureView(context);
    }

    public MediaPlayerTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaPlayerTextureView(Context context, AttributeSet attrs,
                                  int defStyle) {
        super(context, attrs, defStyle);
        initTextureView(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private OnVideoComingToShowListener mOnVideoComingToShowListener;

    public interface OnVideoComingToShowListener{
        public void onVideoComingToShow();
    }

    public void setOnVideoComingToShowListener(OnVideoComingToShowListener mOnVideoComingToShowListener) {
        this.mOnVideoComingToShowListener = mOnVideoComingToShowListener;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    public void setVideoLayout(int layout) {

        Log.d(Constants.LOG_TAG, "SetVideoLayout ,Mode = " + layout);
        LayoutParams lp = getLayoutParams();
        Pair<Integer, Integer> res = ScreenResolution.getResolution(mContext);
        int windowWidth = res.first.intValue(), windowHeight = res.second
                .intValue();
        float windowRatio = windowWidth / (float) windowHeight;
        int sarNum = mVideoSarNum;
        int sarDen = mVideoSarDen;
        if (mVideoHeight > 0 && mVideoWidth > 0) {
            float videoRatio = ((float) (mVideoWidth)) / mVideoHeight;
            if (sarNum > 0 && sarDen > 0)
                videoRatio = videoRatio * sarNum / sarDen;
            mSurfaceHeight = mVideoHeight;
            mSurfaceWidth = mVideoWidth;

            if (layout == MediaPlayerTextureView.MOVIE_RATIO_MODE_16_9) {
                // 16:9
                float target_ratio = 16.0f / 9.0f;
                float dh = windowHeight;
                float dw = windowWidth;
                if (windowRatio < target_ratio) {
                    dh = dw / target_ratio;
                } else {
                    dw = dh * target_ratio;
                }
                lp.width = (int) dw;
                lp.height = (int) dh;

            } else if (layout == MediaPlayerTextureView.MOVIE_RATIO_MODE_4_3) {
                // 4:3
                float target_ratio = 4.0f / 3.0f;
                float source_height = windowHeight;
                float source_width = windowWidth;
                if (windowRatio < target_ratio) {
                    source_height = source_width / target_ratio;
                } else {
                    source_width = source_height * target_ratio;
                }
                lp.width = (int) source_width;
                lp.height = (int) source_height;
            } else if (layout ==
                    MediaPlayerTextureView.MOVIE_RATIO_MODE_ORIGIN &&
                    mSurfaceWidth < windowWidth && mSurfaceHeight < windowHeight) {
                // origin
                lp.width = (int) (mSurfaceHeight * videoRatio);
                lp.height = mSurfaceHeight;
            } else if (layout ==
                    MediaPlayerTextureView.MOVIE_RATIO_MODE_FULLSCREEN) {
                // fullscreen
                lp.width = (windowRatio < videoRatio) ? windowWidth :
                        (int) (videoRatio * windowHeight);
                lp.height = (windowRatio >
                        videoRatio) ? windowHeight : (int) (windowWidth / videoRatio);
            }
            setLayoutParams(lp);
            getSurfaceTexture().setDefaultBufferSize(mSurfaceWidth,
                    mSurfaceHeight);
        }
        mVideoLayout = layout;
    }

    public int getVideoLayoutMode() {
        return mVideoLayout;
    }

    protected void initTextureView(Context ctx) {

        mContext = ctx;
        mVideoWidth = 0;
        mVideoHeight = 0;
        mVideoSarNum = 0;
        mVideoSarDen = 0;
        setSurfaceTextureListener(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
        if (ctx instanceof Activity)
            ((Activity) ctx).setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    public boolean isValid() {
        return (mSurfaceTexture != null);
    }

    public void setVideoPath(String path) {
        Log.i(Constants.LOG_TAG, "setVideoPath : path=" + path);
        setVideoURI(Uri.parse(path));
    }

    public void setVideoURI(Uri uri) {
        mUri = uri;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void stopPlayback() {
        Log.i(Constants.LOG_TAG, "on stop ");
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
        }
    }

    protected void openVideo() {
        Log.i(Constants.LOG_TAG, "openVideo");
        if (mUri == null) {
            return;
        }

        stopMusicService();

        try {
            mDuration = -1;
            mCurrentBufferPercentage = 0;
            mMediaInfo = null;

            if (mUri != null) {
                String timeSec = String.valueOf(System.currentTimeMillis() / 1000);
                String skSign = MD5Util.md5("sb56661c74aabc0df83d723a8d3eba69" + timeSec);
                ksyMediaPlayer = new KSYMediaPlayer.Builder(mContext.getApplicationContext()).setAppId("QYA0788DA337D2E0EC45").setAccessKey("a8b4dff4665f6e69ba6cbeb8ebadc9a3").setSecretKeySign(skSign).setTimeSec(timeSec).build();
//                ksyMediaPlayer
//                        .setBufferSize(Constants.MEDIA_BUFFER_SIZE_DEFAULT);
//                ksyMediaPlayer.setTimeout(Constants.MEDIA_TIME_OUT_DEFAULT);
                Log.d(Constants.LOG_TAG, "isStream =  "
                        + playConfig.isStream());
//                ksyMediaPlayer.clearCachedFiles(new File(Environment
//                        .getExternalStorageDirectory(), "ksy_cached_temp")
//                        .getPath());
//                ksyMediaPlayer.setCachedDir(new File(Environment
//                        .getExternalStorageDirectory(), "ksy_cached_temp")
//                        .getPath());
            } else {
                Log.e(Constants.LOG_TAG, "mUri is null ");
            }

            mMediaPlayer = ksyMediaPlayer;
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
            String url = mUri.toString();
            if (!misTexturePowerEvent) {
                if (isValid()) {
                    mSurface = new Surface(mSurfaceTexture);
                } else {
                    mSurface = new Surface(getSurfaceTexture());
                }
            } else {
                misTexturePowerEvent = false;
            }
            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            if (mUri != null) {
                Log.d(Constants.LOG_TAG, "final url =" + url);
                mMediaPlayer.setDataSource(url);
            }
            mMediaPlayer.prepareAsync();
            if (mMediaPlayerController != null) {
                mMediaPlayerController.onVideoPreparing();
            }
            mCurrentState = STATE_PREPARING;
        } catch (IOException ex) {
            Log.e(Constants.LOG_TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer,
                    IMediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (IllegalArgumentException ex) {
            Log.e(Constants.LOG_TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer,
                    IMediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }
    }

    protected void stopMusicService() {
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        mContext.sendBroadcast(i);
    }

    IMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {

        @Override
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height,
                                       int sarNum, int sarDen) {

            Log.d(Constants.LOG_TAG, "OnVideoSizeChangedListener,width = " + mp.getVideoWidth() + ",height = " + mp.getVideoHeight());
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            mVideoSarNum = sarNum;
            mVideoSarDen = sarDen;
            // For override
            onParentVideoSizeChanged();
            mOnVideoComingToShowListener.onVideoComingToShow();
        }
    };

    protected void onParentVideoSizeChanged() {
    }

    IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {

        @Override
        public void onPrepared(IMediaPlayer mp) {
            Log.e(Constants.LOG_TAG, "MediaPlayerTextureView  OnPrepared");
            mCurrentState = STATE_PREPARED;
            mTargetState = STATE_PLAYING;

            if (mOnPreparedListener != null)
                mOnPreparedListener.onPrepared(mMediaPlayer);

            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
        }
    };

    protected final IMediaPlayer.OnCompletionListener mCompletionListener = new IMediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(IMediaPlayer mp) {

            playConfig.setInterruptMode(PlayConfig.INTERRUPT_MODE_FINISH_OR_ERROR);

            Log.d(Constants.LOG_TAG, "MediaPlayerTextureView  onCompletion");
            mCurrentState = STATE_PLAYBACK_COMPLETED;
            mTargetState = STATE_PLAYBACK_COMPLETED;

            if (mOnCompletionListener != null)
                mOnCompletionListener.onCompletion(mMediaPlayer);
        }
    };

    protected final IMediaPlayer.OnErrorListener mErrorListener = new IMediaPlayer.OnErrorListener() {

        @Override
        public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
            Log.e(Constants.LOG_TAG, "MediaPlayerTextureView onError");
            playConfig.setInterruptMode(PlayConfig.INTERRUPT_MODE_FINISH_OR_ERROR);

            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;

            if (mOnErrorListener != null) {
                if (mOnErrorListener.onError(mMediaPlayer, framework_err,
                        impl_err)) {
                    return true;
                }
            }
            return true;

        }
    };

    protected final IMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {

        @Override
        public void onBufferingUpdate(IMediaPlayer mp, int percent) {
            mCurrentBufferPercentage = percent;
            if (mOnBufferingUpdateListener != null) {
                mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
            }
        }
    };

    protected final IMediaPlayer.OnInfoListener mInfoListener = new IMediaPlayer.OnInfoListener() {

        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {
            Log.d(Constants.LOG_TAG, "MediaPlayerTextureView  mInfoListener");
            if (mOnInfoListener != null) {
                mOnInfoListener.onInfo(mp, what, extra);
            }
            return true;
        }
    };

    protected final IMediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {

        @Override
        public void onSeekComplete(IMediaPlayer mp) {

            Log.d(Constants.LOG_TAG, "MediaPlayerTextureView  onSeekComplete");
            if (mOnSeekCompleteListener != null)
                mOnSeekCompleteListener.onSeekComplete(mp);
        }
    };

    public void setMediaPlayerController(
            IMediaPlayerPlus mediaPlayerController) {

        mMediaPlayerController = mediaPlayerController;
    }

    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener l) {

        mOnPreparedListener = l;
    }

    public void setOnCompletionListener(IMediaPlayer.OnCompletionListener l) {

        mOnCompletionListener = l;
    }

    public void setOnErrorListener(IMediaPlayer.OnErrorListener l) {

        mOnErrorListener = l;
    }

    public void setOnBufferingUpdateListener(IMediaPlayer.OnBufferingUpdateListener l) {

        mOnBufferingUpdateListener = l;
    }

    public void setOnSeekCompleteListener(IMediaPlayer.OnSeekCompleteListener l) {

        mOnSeekCompleteListener = l;
    }

    public void setOnInfoListener(IMediaPlayer.OnInfoListener l) {

        mOnInfoListener = l;
    }

    protected boolean mIsDismiss;
    protected KeyguardManager km;
    protected KeyguardLock mKeyguardLock;
    protected boolean isAppShowing;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void release(final boolean cleartargetstate) {
        long current = System.currentTimeMillis();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            if (cleartargetstate)
                mTargetState = STATE_IDLE;
        }
        Log.e(Constants.LOG_TAG,
                "textureView release cost :"
                        + String.valueOf(System.currentTimeMillis() - current));
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        return false;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {

        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK
                && keyCode != KeyEvent.KEYCODE_VOLUME_UP
                && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN
                && keyCode != KeyEvent.KEYCODE_MENU
                && keyCode != KeyEvent.KEYCODE_CALL
                && keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (keyCode == KeyEvent.KEYCODE_APP_SWITCH) {
            mIsDismiss = true;
        }
        if (isInPlaybackState() && isKeyCodeSupported) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                    || keyCode == KeyEvent.KEYCODE_SPACE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();

                } else {
                    start();

                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    && mMediaPlayer.isPlaying()) {
                pause();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void start() {

        Log.i(Constants.LOG_TAG, "start , =========================="
                + isInPlaybackState());
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
            if (mMediaPlayerController != null)
                mMediaPlayerController.onPlay();
        }
        mTargetState = STATE_PLAYING;
    }

    @Override
    public void pause() {

        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
                if (mMediaPlayerController != null)
                    mMediaPlayerController.onPause();
            }
        }
        mTargetState = STATE_PAUSED;
    }

    @Override
    public int getDuration() {

        if (isInPlaybackState()) {
            if (mDuration > 0)
                return (int) mDuration;
            mDuration = mMediaPlayer.getDuration();
            return (int) mDuration;
        }
        mDuration = -1;
        return (int) mDuration;
    }

    public MediaInfo getMediaInfo() {

        if (isInPlaybackState()) {
            if (mMediaInfo == null) {
                mMediaInfo = mMediaPlayer.getMediaInfo();
            }
            return mMediaInfo;
        }

        mMediaInfo = null;
        return mMediaInfo;
    }

    @Override
    public int getCurrentPosition() {

        if (isInPlaybackState()) {
            long position = mMediaPlayer.getCurrentPosition();
            return (int) position;
        }
        return 0;
    }

    @Override
    public void seekTo(long msec) {

        Log.d(Constants.LOG_TAG, "seek called=========");
        if (isInPlaybackState())
            mMediaPlayer.seekTo(msec);
    }

    @Override
    public boolean isPlaying() {

        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {

        if (mMediaPlayer != null)
            return mCurrentBufferPercentage;
        return 0;
    }

    public int getVideoWidth() {

        return mVideoWidth;
    }

    public int getVideoHeight() {

        return mVideoHeight;
    }

    protected boolean isInPlaybackState() {

        return (mMediaPlayer != null && mCurrentState != STATE_ERROR
                && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
    }

    @Override
    public boolean canPause() {

        if (isPlaying())
            return true;
        return false;
    }

    @Override
    public boolean canSeekBackward() {

        if (this.getDuration() > 0)
            return true;
        return false;
    }

    @Override
    public boolean canSeekForward() {

        if (this.getDuration() > 0)
            return true;
        return false;
    }

    @Override
    public boolean canStart() {

        return isInPlaybackState();
    }

    @Override
    public void onPowerState(int state) {
        switch (state) {
            case Constants.POWER_OFF:
                Log.e(Constants.LOG_TAG, "POWER_OFF");
                misTexturePowerEvent = true;
                if (mCurrentState == STATE_PAUSED) {
                    mNeedPauseAfterLeave = true;
                }
                switch (playConfig.getInterruptMode()) {
                    case PlayConfig.INTERRUPT_MODE_RELEASE_CREATE:
                        Log.d(Constants.LOG_TAG, "POWER_OFF Release");
                        release(true);
                        break;
                    case PlayConfig.INTERRUPT_MODE_PAUSE_RESUME:
                        Log.d(Constants.LOG_TAG, "POWER_OFF Pause");
                        pause();
                        break;
                    case PlayConfig.INTERRUPT_MODE_FINISH_OR_ERROR:
                        Log.e(Constants.LOG_TAG, "MediaPlayerTextureView POWER_ON FINISH_OR_ERROR 111 ");

                        break;
                }
                break;
            case Constants.POWER_ON:
                Log.e(Constants.LOG_TAG, "POWER_ON");
                if (isKeyGuard()) {
                    Log.d(Constants.LOG_TAG, "is KeyGuard");
                    mNeedUnlock = true;
                } else {
                    Log.d(Constants.LOG_TAG, "no KeyGuard");
                    switch (playConfig.getInterruptMode()) {
                        case PlayConfig.INTERRUPT_MODE_RELEASE_CREATE:
                            Log.d(Constants.LOG_TAG, "POWER_ON Create");
                            openVideo();
                            break;
                        case PlayConfig.INTERRUPT_MODE_PAUSE_RESUME:
                            Log.d(Constants.LOG_TAG, "POWER_ON Start");
                            stopMusicService();
                            if (!mNeedPauseAfterLeave) {
                                start();
                            } else {
                                Log.d(Constants.LOG_TAG, "POWER_ON PAUSED STATE,Ingored start()");
                                mNeedPauseAfterLeave = false;
                            }
                            break;
                        case PlayConfig.INTERRUPT_MODE_FINISH_OR_ERROR:
                            Log.e(Constants.LOG_TAG, "MediaPlayerTextureView POWER_ON FINISH_OR_ERROR 222 ");

                            break;
                    }
                }
                break;
            case Constants.USER_PRESENT:
                Log.e(Constants.LOG_TAG, "USER_PRESENT");
                if (isAppShowing && mNeedUnlock) {
                    Log.d(Constants.LOG_TAG, "is KeyGuard");
                    mNeedUnlock = false;
                    switch (playConfig.getInterruptMode()) {
                        case PlayConfig.INTERRUPT_MODE_RELEASE_CREATE:
                            Log.d(Constants.LOG_TAG, "is KeyGuard Create");
                            openVideo();
                            break;
                        case PlayConfig.INTERRUPT_MODE_PAUSE_RESUME:
                            Log.d(Constants.LOG_TAG, "is KeyGuard Start");
                            stopMusicService();
                            if (!mNeedPauseAfterLeave) {
                                start();
                            } else {
                                Log.d(Constants.LOG_TAG, "POWER_ON PAUSED STATE,Ingored start()");
                                mNeedPauseAfterLeave = false;
                            }
                            break;
                        case PlayConfig.INTERRUPT_MODE_FINISH_OR_ERROR:
                            Log.d(Constants.LOG_TAG, "MediaPlayerTextureView POWER_ON FINISH_OR_ERROR 333 ");
                            mMediaPlayer.setSurface(new Surface(mSurfaceTexture));

                            switch (playConfig.getVideoMode()) {
                                case PlayConfig.SHORT_VIDEO_MODE:
                                    Log.d(Constants.LOG_TAG, "PlayConfig.SHORT_VIDEO_MODE  11111 ");
//                                    playConfig.setInterruptMode(PlayConfig.INTERRUPT_MODE_PAUSE_RESUME);
                                    break;

                                case PlayConfig.LIVE_VIDEO_MODE:
                                    Log.d(Constants.LOG_TAG, "PlayConfig.LIVE_VIDEO_MODE  2222222 ");
//                                    playConfig.setInterruptMode(PlayConfig.INTERRUPT_MODE_RELEASE_CREATE);
                                    break;

                                case PlayConfig.OTHER_MODE:

                                    break;
                            }

                            break;
                    }
                }
                break;
            case Constants.APP_SHOWN:
                isAppShowing = true;
                break;
            case Constants.APP_HIDDEN:
                isAppShowing = false;
                break;
            default:
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected boolean isKeyGuard() {
        km = (KeyguardManager) mContext
                .getSystemService(Context.KEYGUARD_SERVICE);
        if (km.isKeyguardSecure() || km.isKeyguardLocked()) {
            Log.d(Constants.LOG_TAG, "isKeyguardLocked");
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
                                          int height) {
        Log.d(Constants.LOG_TAG, "MediaPlayerTextureVideoView onSurfaceTextureAvailable");
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        mSurfaceTexture = surface;

        switch (playConfig.getInterruptMode()) {
            case PlayConfig.INTERRUPT_MODE_RELEASE_CREATE:
                Log.d(Constants.LOG_TAG, "MediaPlayerTextureVideoView onSurfaceTextureAvailable Create");
                openVideo();
                break;
            case PlayConfig.INTERRUPT_MODE_PAUSE_RESUME:
                if (mMediaPlayer != null) {
                    Log.d(Constants.LOG_TAG, "MediaPlayerTextureVideoView onSurfaceTextureAvailable Start");
                    stopMusicService();
                    mMediaPlayer.setSurface(new Surface(mSurfaceTexture));
                    if (!mNeedPauseAfterLeave) {
                        start();
                    } else {
                        Log.d(Constants.LOG_TAG, "POWER_ON PAUSED STATE,Ingored start()");
                        mNeedPauseAfterLeave = false;
                    }
                } else {
                    openVideo();
                }
                break;
            case PlayConfig.INTERRUPT_MODE_FINISH_OR_ERROR:
                Log.d(Constants.LOG_TAG, "onSurfaceTextureAvailable  INTERRUPT_MODE_STAY_PLAYING");
                mMediaPlayer.setSurface(new Surface(mSurfaceTexture));

                switch (playConfig.getVideoMode()) {
                    case PlayConfig.SHORT_VIDEO_MODE:
//                        playConfig.setInterruptMode(PlayConfig.INTERRUPT_MODE_PAUSE_RESUME);
                        break;

                    case PlayConfig.LIVE_VIDEO_MODE:
//                        playConfig.setInterruptMode(PlayConfig.INTERRUPT_MODE_RELEASE_CREATE);
                        break;

                    case PlayConfig.OTHER_MODE:

                        break;
                }

                break;
        }

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
                                            int height) {
        Log.d(Constants.LOG_TAG, "** onSurfaceTextureSizeChanged");
        Log.d(Constants.LOG_TAG, "onSurfaceTextureSizeChanged :surface width = " + width + ",surface height =" + height);
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        onParentSurfaceTextureSizeChanged(surface, width, height);
    }

    private void onParentSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(Constants.LOG_TAG, "MediaPlayerTextureVideoView onSurfaceTextureDestroyed");
        if (mCurrentState == STATE_PAUSED) {
            mNeedPauseAfterLeave = true;
        }

        switch (playConfig.getInterruptMode()) {
            case PlayConfig.INTERRUPT_MODE_RELEASE_CREATE:
                Log.d(Constants.LOG_TAG, "MediaPlayerTextureVideoView onSurfaceTextureDestroyed Release");
                release(true);
                break;
            case PlayConfig.INTERRUPT_MODE_PAUSE_RESUME:
                Log.d(Constants.LOG_TAG, "MediaPlayerTextureVideoView onSurfaceTextureDestroyed Pause");
                pause();
                break;
            case PlayConfig.INTERRUPT_MODE_FINISH_OR_ERROR:
                Log.d(Constants.LOG_TAG, "onSurfaceTextureDestroyed  INTERRUPT_MODE_STAY_PLAYING");

                break;
        }

        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void getCurrentFrame(Bitmap bitmap) {
        ksyMediaPlayer.getCurrentFrame(bitmap);
    }

}
