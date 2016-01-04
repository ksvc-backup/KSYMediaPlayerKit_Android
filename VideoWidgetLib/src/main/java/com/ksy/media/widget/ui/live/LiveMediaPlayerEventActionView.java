package com.ksy.media.widget.ui.live;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ksy.media.widget.util.Constants;
import com.ksy.mediaPlayer.widget.R;

public class LiveMediaPlayerEventActionView extends RelativeLayout implements View.OnClickListener{

	public static final int EVENT_ACTION_VIEW_MODE_COMPLETE = 0X00;
	public static final int EVENT_ACTION_VIEW_MODE_WAIT = 0X01;
	public static final int EVENT_ACTION_VIEW_MODE_ERROR = 0X02;

	private TextView closeTextView;
	private TextView reportTextView;
	private Button liveNotice;
	private Button liveNoticePerson;
	private RelativeLayout mxCompleteLayout;
	private EventActionViewCallback mCallback;

	private LinearLayout mErrorLayout;
	private LinearLayout mErrorReplayLayout;
	private TextView mErrorTextView;


	public LiveMediaPlayerEventActionView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public LiveMediaPlayerEventActionView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LiveMediaPlayerEventActionView(Context context) {
		super(context);
		LayoutInflater.from(getContext()).inflate(R.layout.live_finish_event_action_view, this);
		
		initViews();
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
	}

	private void initViews() {

		closeTextView = (TextView) findViewById(R.id.title_text_close);
		reportTextView = (TextView) findViewById(R.id.title_text_report);
		liveNotice = (Button)findViewById(R.id.live_notice_button);
		liveNoticePerson = (Button)findViewById(R.id.live_notice_person_button);

		mxCompleteLayout = (RelativeLayout) findViewById(R.id.layout_live_finish);
		mErrorLayout = (LinearLayout) findViewById(R.id.error_layout);
		mErrorReplayLayout = (LinearLayout) findViewById(R.id.error_replay_layout);
		mErrorTextView = (TextView) findViewById(R.id.error_info_title_text_view);

		closeTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
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
		Log.d(Constants.LOG_TAG, "coverViewMode =" + coverViewMode);
		switch (coverViewMode) {
		case EVENT_ACTION_VIEW_MODE_COMPLETE:
			mxCompleteLayout.setVisibility(View.VISIBLE);
			mErrorLayout.setVisibility(View.GONE);
			break;
		case EVENT_ACTION_VIEW_MODE_WAIT:
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

	@Override
	public void onClick(View view) {
		int id = view.getId();

		if (id == liveNoticePerson.getId()) {
			System.exit(0);
		}

	}

	public interface EventActionViewCallback {

		void onActionPlay();

		void onActionReplay();

		void onActionBack();

		void onActionError();
	}

}
