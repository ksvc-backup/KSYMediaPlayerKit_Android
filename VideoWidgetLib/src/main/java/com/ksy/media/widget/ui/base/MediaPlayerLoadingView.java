package com.ksy.media.widget.ui.base;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ksy.mediaPlayer.widget.R;


public class MediaPlayerLoadingView extends RelativeLayout {

    private TextView mLoadingTitleTextView;
    private TextView mLoadingTipTextView;
    
    
    public MediaPlayerLoadingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MediaPlayerLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaPlayerLoadingView(Context context) {
        super(context);
        LayoutInflater.from(getContext()).inflate(R.layout.blue_media_player_loading_view, this);

        initViews();
    }
    
    private void initViews(){
        mLoadingTitleTextView = (TextView) findViewById(R.id.loading_title_text_view);
        mLoadingTipTextView = (TextView) findViewById(R.id.loading_tip_text_view);
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initViews();
    }    
    
    public void setLoadingTitle(String loadingTitle){        
        if(loadingTitle == null) loadingTitle = "";       
        mLoadingTitleTextView.setText(loadingTitle);        
    }
    
    public void setLoadingTip(String loadingTip){
        
        if(loadingTip == null) loadingTip = "";
        
        mLoadingTipTextView.setText(loadingTip);
        
    }
    
    public void show(){
        if(getVisibility() != View.VISIBLE)
            setVisibility(View.VISIBLE);
    }
    
    public void hide(){
        if(getVisibility() == View.VISIBLE)
            setVisibility(View.INVISIBLE);
    }
    
}
