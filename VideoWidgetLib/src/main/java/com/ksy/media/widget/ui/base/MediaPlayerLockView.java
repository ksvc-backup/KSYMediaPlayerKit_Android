package com.ksy.media.widget.ui.base;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

public class MediaPlayerLockView extends RelativeLayout {

    private static final int HIDE_TIMEOUT_DEFAULT = 3000;
    private static final int MSG_SHOW = 101;
    private static final int MSG_HIDE = 102;
    
    private ScreenLockCallback mCallback;
    
    public MediaPlayerLockView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MediaPlayerLockView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaPlayerLockView(Context context) {
        super(context);
    }
    
    protected Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {

            switch (msg.what) {
            case MSG_SHOW:
                setVisibility(View.VISIBLE);
                break;
            case MSG_HIDE:
                setVisibility(View.GONE);
                break;
            default:
                break;
            }

        };
    };
    
    private void initViews(){
        
        this.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if(isLockMode()){
                    setLockMode(false);
                    if(mCallback != null) mCallback.onActionLockMode(false);
                }            
                else{
                    setLockMode(true);
                    if(mCallback != null) mCallback.onActionLockMode(true);
                }
            }
        });
        
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initViews();
    }
    
    public void show(){
        show(HIDE_TIMEOUT_DEFAULT);
    }
    
    public void show(int timeout){
        
        mHandler.sendEmptyMessage(MSG_SHOW);
        
        mHandler.removeMessages(MSG_HIDE);
        
        if(timeout > 0){
            Message msgHide = mHandler.obtainMessage(MSG_HIDE);
            mHandler.sendMessageDelayed(msgHide, timeout);
        }
        
    }
    
    public void hide(){
        
        mHandler.sendEmptyMessage(MSG_HIDE);
        
    }
    
    public boolean isShowing(){
        return (getVisibility() == View.VISIBLE ? true : false);
    }
    
    public void setLockMode(boolean lock){
        this.setSelected(lock);
    }
    
    public boolean isLockMode(){
        return this.isSelected();
    }
    
    public void setCallback(ScreenLockCallback callback){
        this.mCallback = callback;
    }
    
    public interface ScreenLockCallback {
        void onActionLockMode(boolean lock);
    }
    
}
