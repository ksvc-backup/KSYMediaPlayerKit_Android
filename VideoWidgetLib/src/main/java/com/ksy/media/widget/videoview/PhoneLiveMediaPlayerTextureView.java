package com.ksy.media.widget.videoview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;

import com.ksy.media.widget.ui.live.LiveMediaPlayerView;
import com.ksy.media.widget.util.Constants;

/**
 * Created by eflakemac on 16/1/11.
 */
public class PhoneLiveMediaPlayerTextureView extends MediaPlayerTextureView {
    public int mTargetOrientaion;
    public int mLastOrientaion;
    public int mCurrentPushMode = PhoneLiveMediaPlayerTextureView.PUSH_MODE_PORTRAIT;
    public static final int PUSH_MODE_PORTRAIT = 0;
    public static final int PUSH_MODE_LANDSCAPE = 1;
    private boolean needMatrixTransform;

    public PhoneLiveMediaPlayerTextureView(Context context) {
        super(context);
    }

    public PhoneLiveMediaPlayerTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhoneLiveMediaPlayerTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onParentVideoSizeChanged() {
        if (mVideoWidth > mVideoHeight) {
            setCurrentPushMode(PhoneLiveMediaPlayerTextureView.PUSH_MODE_LANDSCAPE);
        } else {
            setCurrentPushMode(PhoneLiveMediaPlayerTextureView.PUSH_MODE_PORTRAIT);
        }
        if (mCurrentPushMode == PhoneLiveMediaPlayerTextureView.PUSH_MODE_PORTRAIT) {
            fixPreviewFrame(mVideoWidth, mVideoHeight, mSurfaceWidth,
                    mSurfaceHeight);
        } else {
            doMatrixChange(LiveMediaPlayerView.ORIENTATION_PORTRAIT_NORMAL);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        super.onSurfaceTextureSizeChanged(surface, width, height);
        if (needMatrixTransform) {
            Log.d(Constants.LOG_TAG, "needMatrixTransform");
            if (mCurrentPushMode == PUSH_MODE_PORTRAIT) {
                if (mTargetOrientaion == LiveMediaPlayerView.ORIENTATION_LANDSCAPE_REVERSED) {
                    doMatrixChange(mTargetOrientaion);
                    setLastOrientation(mTargetOrientaion);
                } else if (mTargetOrientaion == LiveMediaPlayerView.ORIENTATION_LANDSCAPE_NORMAL) {
                    doMatrixChange(mTargetOrientaion);
                    setLastOrientation(mTargetOrientaion);
                } else if (mTargetOrientaion == LiveMediaPlayerView.ORIENTATION_PORTRAIT_NORMAL) {
                    resetMatrix();
                    setLastOrientation(mTargetOrientaion);
                } else if (mTargetOrientaion == LiveMediaPlayerView.ORIENTATION_PORTRAIT_REVERSED) {
                    resetMatrix();
                    setLastOrientation(mTargetOrientaion);
                }
                setTargetOrientation(LiveMediaPlayerView.ORIENTATION_NONE);
            } else {
                if (mTargetOrientaion == LiveMediaPlayerView.ORIENTATION_LANDSCAPE_REVERSED) {
                    resetMatrix();
                    setLastOrientation(mTargetOrientaion);
                } else if (mTargetOrientaion == LiveMediaPlayerView.ORIENTATION_LANDSCAPE_NORMAL) {
                    resetMatrix();
                    setLastOrientation(mTargetOrientaion);
                } else if (mTargetOrientaion == LiveMediaPlayerView.ORIENTATION_PORTRAIT_NORMAL) {
                    doMatrixChange(mTargetOrientaion);
                    setLastOrientation(mTargetOrientaion);
                } else if (mTargetOrientaion == LiveMediaPlayerView.ORIENTATION_PORTRAIT_REVERSED) {
                }
                setTargetOrientation(LiveMediaPlayerView.ORIENTATION_NONE);
            }
            needMatrixTransform = false;
        }
    }

    private void fixPreviewFrame(int videoWidth, int videoHeight,
                                 int surfaceWidth, int surfaceHeight) {
        if (videoWidth == 0 || videoHeight == 0 || surfaceWidth == 0
                || surfaceHeight == 0) {
            return;
        }
        Matrix matrix = new Matrix();
        getTransform(matrix);
        float scaleWid = (float) surfaceWidth / videoWidth;
        float scaleHei = (float) surfaceHeight / videoHeight;
        float fixFactor = 1f;
        if (scaleWid < scaleHei) {
            fixFactor = scaleHei;
        } else {
            fixFactor = scaleWid;
        }
        matrix.setScale(1f / scaleWid * fixFactor, 1f / scaleHei * fixFactor,
                videoWidth / 2, videoHeight / 2);

        setTransform(matrix);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    public void doMatrixChange(int mTargetOrientaion) {
        Matrix matrix = new Matrix();
        getTransform(matrix);
        if (mCurrentPushMode == PhoneLiveMediaPlayerTextureView.PUSH_MODE_PORTRAIT) {
            if (mTargetOrientaion == LiveMediaPlayerView.ORIENTATION_LANDSCAPE_REVERSED) {
                float scaleW = (float) mSurfaceHeight / mSurfaceWidth;
                float scaleH = 1 / scaleW;
                matrix.setScale(scaleW, scaleH, mSurfaceWidth / 2, mSurfaceHeight / 2);
                matrix.postRotate(90, mSurfaceWidth / 2, mSurfaceHeight / 2);
            } else {
                float scaleW = (float) mSurfaceHeight / mSurfaceWidth;
                float scaleH = 1 / scaleW;
                matrix.setScale(scaleW, scaleH, mSurfaceWidth / 2, mSurfaceHeight / 2);
                matrix.postRotate(-90, mSurfaceWidth / 2, mSurfaceHeight / 2);//
            }

        } else {
            if (mTargetOrientaion == LiveMediaPlayerView.ORIENTATION_PORTRAIT_NORMAL) {
                float scaleW = (float) mSurfaceHeight / mSurfaceWidth;
                float scaleH = 1 / scaleW;
                matrix.setScale(scaleW, scaleH, mSurfaceWidth / 2, mSurfaceHeight / 2);
                matrix.postRotate(90, mSurfaceWidth / 2, mSurfaceHeight / 2);
            } else if (mTargetOrientaion == LiveMediaPlayerView.ORIENTATION_LANDSCAPE_REVERSED) {
            }
        }
        setTransform(matrix);
    }

    public void resetMatrix() {
        Matrix matrix = new Matrix();
        getTransform(matrix);
        matrix.reset();
        setTransform(matrix);
    }

    public void setTargetOrientation(int targetOrientation) {
        this.mTargetOrientaion = targetOrientation;
    }

    public void setLastOrientation(int mLastOrientation) {
        this.mLastOrientaion = mLastOrientation;
    }

    public void setCurrentPushMode(int mCurrentPushMode) {
        this.mCurrentPushMode = mCurrentPushMode;
    }

    public void setNeedMatrixTransform(boolean needMartrixTransform) {
        this.needMatrixTransform = needMartrixTransform;
    }
}
