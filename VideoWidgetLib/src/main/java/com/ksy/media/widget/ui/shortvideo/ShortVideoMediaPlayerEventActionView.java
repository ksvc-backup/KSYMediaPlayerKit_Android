package com.ksy.media.widget.ui.shortvideo;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ksy.mediaPlayer.widget.R;

public class ShortVideoMediaPlayerEventActionView extends RelativeLayout {

    public static final int EVENT_ACTION_VIEW_MODE_COMPLETE = 0X00;
    public static final int EVENT_ACTION_VIEW_MODE_WAIT = 0X01;
    public static final int EVENT_ACTION_VIEW_MODE_ERROR = 0X02;

    private RelativeLayout mRootView;
    private ImageView mBackImageView;
    private RelativeLayout mWaitLayout;
    private RelativeLayout mxCompleteLayout;
//    private LinearLayout mCompeteReplayLayout;

    private Button mCompletaReplayButton;
    private LinearLayout mErrorLayout;
    private Button mErrorReplayButton;

    private TextView mErrorTextView;

    private EventActionViewCallback mCallback;

    public ShortVideoMediaPlayerEventActionView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
    }

    public ShortVideoMediaPlayerEventActionView(Context context, AttributeSet attrs) {

        super(context, attrs);
    }

    public ShortVideoMediaPlayerEventActionView(Context context) {
        super(context);
        LayoutInflater.from(getContext()).inflate(R.layout.short_video_media_player_event_action_view, this);

        initViews();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {

        super.onWindowFocusChanged(hasWindowFocus);
    }

    private void initViews() {

        mRootView = (RelativeLayout) findViewById(R.id.event_action_layout);
        mWaitLayout = (RelativeLayout) findViewById(R.id.wait_layout);

        mxCompleteLayout = (RelativeLayout) findViewById(R.id.complete_layout);
//        mCompeteReplayLayout = (LinearLayout) findViewById(R.id.complete_replay_layout);

        mCompletaReplayButton = (Button)findViewById(R.id.complete_replay_bt);
        mErrorLayout = (LinearLayout) findViewById(R.id.error_layout);
        mErrorReplayButton = (Button) findViewById(R.id.short_error_replay_bt);
        mErrorTextView = (TextView) findViewById(R.id.error_info_title_text_view);

        mWaitLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mCallback != null)
                    mCallback.onActionPlay();
            }
        });

        mCompletaReplayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mCallback != null)
                    mCallback.onActionReplay();
            }
        });

        mErrorReplayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mCallback != null)
                    mCallback.onActionError();
            }
        });

    }

    @Override
    protected void onFinishInflate() {

        super.onFinishInflate();
        initViews();
    }

    public void updateEventMode(int coverViewMode, String extraMessage) {

        switch (coverViewMode) {
            case EVENT_ACTION_VIEW_MODE_COMPLETE:
                mxCompleteLayout.setVisibility(View.VISIBLE);
                mWaitLayout.setVisibility(View.GONE);
                mErrorLayout.setVisibility(View.GONE);
                break;
            case EVENT_ACTION_VIEW_MODE_WAIT:
                mWaitLayout.setVisibility(View.VISIBLE);
                mxCompleteLayout.setVisibility(View.GONE);
                mErrorLayout.setVisibility(View.GONE);
                break;
            case EVENT_ACTION_VIEW_MODE_ERROR:
                mErrorLayout.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(extraMessage)) {
                    mErrorTextView.setText(getResources().getString(R.string.player_error) + ",错误码（" + extraMessage + "）");
                } else {
                    mErrorTextView.setText(getResources().getString(R.string.player_error));
                }
                mWaitLayout.setVisibility(View.GONE);
                mxCompleteLayout.setVisibility(View.GONE);
                break;
            default:
                break;
        }
        show();
    }

    public void updateVideoTitle(String title) {

        if (!TextUtils.isEmpty(title)) {
//            mTitleTextView.setText(title);
        }
    }

    public void show() {

        if (!isShowing()) {
            setVisibility(View.VISIBLE);
        }
    }

    public void hide() {

        if (isShowing()) {
            setVisibility(View.GONE);
        }
    }

    public boolean isShowing() {

        return (getVisibility() == View.VISIBLE ? true : false);
    }

    public void setCallback(EventActionViewCallback callback) {

        this.mCallback = callback;
    }

    public interface EventActionViewCallback {

        void onActionPlay();

        void onActionReplay();

        void onActionBack();

        void onActionError();
    }

}
