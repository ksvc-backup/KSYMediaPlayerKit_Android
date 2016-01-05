package com.ksy.media.widget.ui.livereplay;

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

public class LiveReplayMediaPlayerEventActionView extends RelativeLayout {

	public static final int EVENT_ACTION_VIEW_MODE_COMPLETE = 0X00;
	public static final int EVENT_ACTION_VIEW_MODE_WAIT = 0X01;
	public static final int EVENT_ACTION_VIEW_MODE_ERROR = 0X02;

	private ImageView closeTextView;
	private ImageView reportTextView;
	private Button  replayButton;

	private RelativeLayout mxCompleteLayout;

	private LinearLayout mErrorLayout;
	private LinearLayout mErrorReplayLayout;
	private TextView mErrorTextView;

	private EventActionViewCallback mCallback;

	public LiveReplayMediaPlayerEventActionView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public LiveReplayMediaPlayerEventActionView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LiveReplayMediaPlayerEventActionView(Context context) {
		super(context);
		LayoutInflater.from(getContext()).inflate(R.layout.live_replay_finish_event_action_view, this);
		
		initViews();
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
	}

	private void initViews() {

		closeTextView = (ImageView) findViewById(R.id.title_text_close);
		reportTextView = (ImageView) findViewById(R.id.title_text_report);
		replayButton = (Button)findViewById(R.id.button_replay);

		mxCompleteLayout = (RelativeLayout) findViewById(R.id.layout_live_replay_finish);

		mErrorLayout = (LinearLayout) findViewById(R.id.live_replay_error_layout);
		mErrorReplayLayout = (LinearLayout) findViewById(R.id.live_error_replay_layout);
		mErrorTextView = (TextView) findViewById(R.id.error_info_title_text_view);

		closeTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				System.exit(0);
			}
		});

		replayButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				/*if (mCallback != null) {
					mCallback.onActionReplay();
				}*/

				System.exit(0);
			}
		});

		mErrorReplayLayout.setOnClickListener(new OnClickListener() {
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
//			mWaitLayout.setVisibility(View.GONE);
			mErrorLayout.setVisibility(View.GONE);
			break;
		case EVENT_ACTION_VIEW_MODE_WAIT:
//			mWaitLayout.setVisibility(View.VISIBLE);
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
//			mWaitLayout.setVisibility(View.GONE);
			mxCompleteLayout.setVisibility(View.GONE);
			break;
		default:
			break;
		}
		show();
	}

	public void updateVideoTitle(String title) {
		if (!TextUtils.isEmpty(title)) {

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