package com.ksy.media.widget.ui.base;
import com.ksy.mediaPlayer.widget.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class MediaPlayerBufferingView extends RelativeLayout {

    private ProgressBar mProgressBar;
    private TextView mTextView;
    
    public MediaPlayerBufferingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MediaPlayerBufferingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaPlayerBufferingView(Context context) {
        super(context);

        LayoutInflater.from(getContext()).inflate(R.layout.blue_media_player_buffering_view, this);
        
        initViews();
    }
    
    private void initViews(){
        mProgressBar = (ProgressBar) findViewById(R.id.pb_buffering);
        mTextView = (TextView) findViewById(R.id.tv_buffering);
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initViews();
    }
    
    public void setBufferingProgress(int progress){
        
        if(progress < 0 || progress > 100){
            return;
        }
        mTextView.setText(progress + "%");
        
    }
    
    public void show(){
        if(getVisibility() != View.VISIBLE)
            setVisibility(View.VISIBLE);
    }
    
    public void hide(){
        if(getVisibility() == View.VISIBLE)
            setVisibility(View.GONE);
    }
    
}
