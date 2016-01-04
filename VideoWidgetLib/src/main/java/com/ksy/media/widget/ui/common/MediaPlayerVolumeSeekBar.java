package com.ksy.media.widget.ui.common;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.SeekBar;

import com.ksy.media.widget.util.Constants;

public class MediaPlayerVolumeSeekBar extends SeekBar {

	public interface onScreenShowListener {
		public void onScreenShow();
	}

	private onScreenShowListener mOnShowListener;

	public MediaPlayerVolumeSeekBar(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public MediaPlayerVolumeSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MediaPlayerVolumeSeekBar(Context context) {
		super(context);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(h, w, oldh, oldw);
	}

	@Override
	protected synchronized void onMeasure(int widthMeasureSpec,
			int heightMeasureSpec) {
		super.onMeasure(heightMeasureSpec, widthMeasureSpec);
		setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		canvas.rotate(-90);
		canvas.translate(-getHeight(), 0);
		super.onDraw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.d(Constants.LOG_TAG, "touch in Volume");

		if (!isEnabled()) {
			return false;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
		case MotionEvent.ACTION_UP:
			setProgress(getMax()
					- (int) (getMax() * event.getY() / getHeight()));
			if (mOnShowListener != null) {
				mOnShowListener.onScreenShow();
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			break;
		}

		return true;
	}

	@Override
	public synchronized void setProgress(int progress) {
		super.setProgress(progress);
		onSizeChanged(getWidth(), getHeight(), 0, 0);
	}

	public void setOnScreenShowListener(onScreenShowListener listener) {
		this.mOnShowListener = listener;
	}

}
