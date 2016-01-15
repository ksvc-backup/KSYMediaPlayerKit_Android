package com.ksy.media.widget.ui.base;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

public class MediaPlayerVideoSeekBar extends SeekBar {

    public MediaPlayerVideoSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MediaPlayerVideoSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaPlayerVideoSeekBar(Context context) {
        super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
    
}
