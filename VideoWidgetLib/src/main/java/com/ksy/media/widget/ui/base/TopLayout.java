package com.ksy.media.widget.ui.base;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.ksy.media.widget.util.Constants;

public class TopLayout extends RelativeLayout {

    private final Context mContext;
    private DisplayMetrics displayMetrics;

    public TopLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        displayMetrics = getResources().getDisplayMetrics();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(Constants.LOG_TAG, "TopLayout---top parent width = " + w + ",top parent height = " + h
                + "oldwidth = " + oldw + ",oldheight = " + oldh);
    }

    private void setWidthAndHeight(int oldw, int oldh) {
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.width = oldw;
        lp.height = oldh;
        setLayoutParams(lp);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec((int) FixKeyBoardDistance(displayMetrics.heightPixels), MeasureSpec.AT_MOST));
    }

    private float FixKeyBoardDistance(int defaultDistance) {
        Rect frame = new Rect();
        ((Activity) mContext).getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        float keyboardDistance = ((float) defaultDistance - (float) statusBarHeight);
        return keyboardDistance;
    }


}
