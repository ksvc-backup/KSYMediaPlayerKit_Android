package com.ksy.media.widget.controller.livereplay;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.ksy.media.widget.ui.base.LiveAnchorDialog;
import com.ksy.media.widget.ui.base.MediaPlayerVideoSeekBar;
import com.ksy.media.widget.ui.base.HeartLayout;
import com.ksy.media.widget.ui.base.HorizontalListView;
import com.ksy.media.widget.ui.base.LiveExitDialog;
import com.ksy.media.widget.ui.base.LivePersonDialog;
import com.ksy.media.widget.ui.livereplay.LiveReplayChatAdapter;
import com.ksy.media.widget.ui.livereplay.LiveReplayHeadListAdapter;
import com.ksy.media.widget.util.MediaPlayerUtils;
import com.ksy.mediaPlayer.widget.R;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.graphics.Color;
import android.widget.Toast;


public class LiveReplayMediaPlayerControllerView extends FrameLayout implements View.OnClickListener {

    private ImageView liveReplayHead;
    private ImageView loadingImage;
    private TextView netErrorTextView;
    private ImageView closeImage;
    private ImageView reportImage;

    private ListView liveReplayListView;
    private TextView noticeTextView;
    private List<Map<String, Object>> data;
    private LiveReplayChatAdapter adapter;
    Map<String, Object> map;

    private TextView personCountTextView;
    private ImageView liveReplayPerson;
    private TextView praiseCountTextView;
    private HorizontalListView mHorizontalList;
    private LiveReplayHeadListAdapter headListAdapter;
    private int praiseCount;

    private ImageView switchButton;
    private ImageView shareButton;
    private MediaPlayerVideoSeekBar mSeekBar;
    private TextView currentTimeTextView;
    private TextView lineTextView;
    private TextView totalTimeTextView;
    private ImageView mPlaybackImageView;

    private Context mContext;
    private Random mRandom = new Random();

    private Timer refreshTimerLiveReplay = null;
    private Timer mTimer = null;
    private Timer mAudienceComeTimer = null;
    private Timer mAudienceComeTimerGoneTimer = null;
    private Timer seekTimer = null;

    private HeartLayout mHeartLayout;
    private ImageView heartImageView;
    private boolean isSwitch;
    private boolean isListVisible;

    private Handler mHandler = new Handler();
    protected LayoutInflater mLiveReplayLayoutInflater;
    protected static final int LIVEREPLAY_MAX_VIDEO_PROGRESS = 1000;
    protected volatile boolean mVideoProgressTrackingTouch = false;
    protected ILiveReplayController mLiveReplayMediaPlayerController;

    private volatile boolean mSeekStarted = false;
    private Animation showAudienceAnimation;
    private Animation hideAudienceAnimation;
    private boolean isReplayRemoveData;

    public LiveReplayMediaPlayerControllerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    public LiveReplayMediaPlayerControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public LiveReplayMediaPlayerControllerView(Context context) {
        super(context);
        mContext = context;

        mLiveReplayLayoutInflater = LayoutInflater.from(getContext());
        mLiveReplayLayoutInflater.inflate(R.layout.blue_media_player_controller_live_replay, this);

        initViews();
        initListeners();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        initViews();
        initListeners();
    }

    protected void initViews() {

        showAudienceAnimation = AnimationUtils.loadAnimation(mContext, R.anim.live_audience_show);
        hideAudienceAnimation = AnimationUtils.loadAnimation(mContext, R.anim.live_audience_hide);

        liveReplayHead = (ImageView) findViewById(R.id.image_live_replay_head);
        loadingImage = (ImageView) findViewById(R.id.text_live_replay);
        netErrorTextView = (TextView) findViewById(R.id.textViewNetError);
        closeImage = (ImageView) findViewById(R.id.live_replay_image_close);
        reportImage = (ImageView) findViewById(R.id.live_replay_image_report);
        praiseCountTextView = (TextView) findViewById(R.id.praise_count_text);
        personCountTextView = (TextView) findViewById(R.id.person_count_textview);

        liveReplayListView = (ListView) findViewById(R.id.live_replay_list);
        noticeTextView = (TextView) findViewById(R.id.live_replay_notice_text);
        data = getData();
        adapter = new LiveReplayChatAdapter(mContext, data);
        liveReplayListView.setAdapter(adapter);

        mHorizontalList = (HorizontalListView) findViewById(R.id.live_replay_horizon);
        headListAdapter = new LiveReplayHeadListAdapter(mContext);
        mHorizontalList.setAdapter(headListAdapter);

        mHorizontalList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LivePersonDialog dialogPerson = new LivePersonDialog(mContext);

                WindowManager.LayoutParams lp = dialogPerson.getWindow().getAttributes();
                lp.alpha = 0.8f;
                dialogPerson.getWindow().setAttributes(lp);

                dialogPerson.show();
            }
        });

        liveReplayPerson = (ImageView) findViewById(R.id.live_replay_person_image);
        switchButton = (ImageView) findViewById(R.id.live_replay_information_switch);
        mHeartLayout = (HeartLayout) findViewById(R.id.live_replay_layout_heart);
        heartImageView = (ImageView) findViewById(R.id.live_replay_image_heart);
        shareButton = (ImageView) findViewById(R.id.live_replay_share_bt);

        mSeekBar = (MediaPlayerVideoSeekBar) findViewById(R.id.seekbar_video_progress);
        mPlaybackImageView = (ImageView) findViewById(R.id.video_playback_image_view);
        currentTimeTextView = (TextView) findViewById(R.id.textViewCurrentTime);
        lineTextView = (TextView) findViewById(R.id.textViewLine);
        totalTimeTextView = (TextView) findViewById(R.id.textViewTotalTime);
        mSeekBar.setMax(LIVEREPLAY_MAX_VIDEO_PROGRESS);
        mSeekBar.setProgress(0);

    }

    Runnable seekRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            onTimerTicker();
        }
    };

    Runnable mAudienceComeRunnable = new Runnable() {
        @Override
        public void run() {
            noticeTextView.startAnimation(showAudienceAnimation);
        }
    };

    Runnable mAudienceComeGoneRunnable = new Runnable() {
        @Override
        public void run() {
            if (noticeTextView.isShown()) {
                noticeTextView.startAnimation(hideAudienceAnimation);
                noticeTextView.setVisibility(INVISIBLE);
            }
        }
    };

    public void seekRefresh() {
        seekTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mHandler.postDelayed(seekRefreshRunnable, 100);
            }
        }, 200, 1000);
    }

    public void heartRefresh() {
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mHeartLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        mHeartLayout.addHeart(randomColor());
                        praiseCount++;
                        praiseCountTextView.setText(String.valueOf(praiseCount));
                    }
                });
            }
        }, 500, 1000);
    }

    public void audienceComeTimer() {
        mAudienceComeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mHandler.postDelayed(mAudienceComeRunnable, 100);
            }
        }, 100, 6000);
    }

    public void audienceComeTimerGoneTimer() {
        mAudienceComeTimerGoneTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mHandler.postDelayed(mAudienceComeGoneRunnable, 100);
            }
        }, 100, 8000);
    }

    private void chatListControlLiveReplay() {
        refreshTimerLiveReplay.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (data.size() >= 8) {
                            data.remove(0);
                            isReplayRemoveData = false;
                        } else {
                            data.add(map);
                            isReplayRemoveData = true;
                        }
                        if (isReplayRemoveData) {
                            liveReplayListView.requestLayout();
                            adapter.notifyDataSetChanged();
                        } else {
                            liveReplayListView.requestLayout();
                            adapter.notifyDataSetChanged();
                            data.add(7, map);
                            liveReplayListView.requestLayout();
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        }, 200, 2000);

    }

    private List<Map<String, Object>> getData() {

        if (data != null && data.size() > 0) {
            return data;
        }

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map;

        map = new HashMap<String, Object>();
        map.put("img", R.drawable.live_dialog_list_item);
        map.put("title", "用户名");
        map.put("info", "评论内容评论内容");

        this.map = map;
        list.add(map);

        return list;
    }

    public void startLiveReplayTimer() {

        if (mTimer == null || mAudienceComeTimer == null || mAudienceComeTimerGoneTimer == null || seekTimer == null || refreshTimerLiveReplay == null) {
            mTimer = new Timer();
            mAudienceComeTimer = new Timer();
            mAudienceComeTimerGoneTimer = new Timer();
            seekTimer = new Timer();
            refreshTimerLiveReplay = new Timer();
        }

        chatListControlLiveReplay();

        seekRefresh();

        heartRefresh();

        audienceComeTimer();

        audienceComeTimerGoneTimer();

        initListeners();
    }

    public void stopLiveReplayTimer() {
        if (null != seekTimer) {
            seekTimer.cancel();
            seekTimer.purge();
            seekTimer = null;
        }

        if (null != mTimer) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }

        if (null != mAudienceComeTimer) {
            mAudienceComeTimer.cancel();
            mAudienceComeTimer.purge();
            mAudienceComeTimer = null;
        }

        if (null != mAudienceComeTimerGoneTimer) {
            mAudienceComeTimerGoneTimer.cancel();
            mAudienceComeTimerGoneTimer.purge();
            mAudienceComeTimerGoneTimer = null;
        }

        if (null != refreshTimerLiveReplay) {
            refreshTimerLiveReplay.cancel();
            refreshTimerLiveReplay.purge();
            refreshTimerLiveReplay = null;
        }

        currentTimeTextView.setText("00:00:00");
        totalTimeTextView.setText("00:00:00");
    }

    protected void initListeners() {

        liveReplayHead.setOnClickListener(this);
        closeImage.setOnClickListener(this);
        reportImage.setOnClickListener(this);

        liveReplayPerson.setOnClickListener(this);
        switchButton.setOnClickListener(this);
        shareButton.setOnClickListener(this);
        mPlaybackImageView.setOnClickListener(this);

        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mVideoProgressTrackingTouch = false;

                Log.d("lixp", "mSeekBar  -----------------");
                int curProgress = seekBar.getProgress();
                int maxProgress = seekBar.getMax();

                if (curProgress >= 0 && curProgress <= maxProgress) {
                    float percentage = ((float) curProgress) / maxProgress;
                    int position = (int) (mLiveReplayMediaPlayerController.getDuration() * percentage);
                    mLiveReplayMediaPlayerController.seekTo(position);
                    // mMediaPlayerController.start();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mVideoProgressTrackingTouch = true;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                /*if (fromUser) {
					if (isShowing()) {
						show();
					}
				}*/
            }
        });

    }

    public boolean isShowing() {
        if (getVisibility() == View.VISIBLE) {
            return true;
        }
        return false;
    }

    private void onTimerTicker() {

        long currentTime = mLiveReplayMediaPlayerController.getCurrentPosition();
        long durationTime = mLiveReplayMediaPlayerController.getDuration();

        if (durationTime > 0 && currentTime <= durationTime) {
            float percentage = ((float) currentTime) / durationTime;
            updateVideoProgress(percentage);
        }
    }

    public void updateVideoTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
        }
    }

    public void updateVideoProgress(float percentage) {

        if (percentage >= 0 && percentage <= 1) {
            int progress = (int) (percentage * mSeekBar.getMax());
            if (!mVideoProgressTrackingTouch) {
                mSeekBar.setProgress(progress);
            }

            long curTime = mLiveReplayMediaPlayerController.getCurrentPosition();
            long durTime = mLiveReplayMediaPlayerController.getDuration();

            if (durTime > 0 && curTime <= durTime) {
                currentTimeTextView.setText(MediaPlayerUtils
                        .getVideoDisplayTime(curTime));
                totalTimeTextView.setText(MediaPlayerUtils.getVideoDisplayTime(durTime));
            }
        }
    }

    public void updateVideoPlaybackState(boolean isStart) {
        // 播放中
        if (isStart) {
            mPlaybackImageView.setImageResource(R.drawable.live_replay_pause);

            if (mLiveReplayMediaPlayerController.canPause()) {
                mPlaybackImageView.setEnabled(true);
            } else {
                mPlaybackImageView.setEnabled(false);
            }
        }
        // 未播放
        else {
            mPlaybackImageView.setImageResource(R.drawable.live_replay_play);
            if (mLiveReplayMediaPlayerController.canStart()) {
                mPlaybackImageView.setEnabled(true);
            } else {
                mPlaybackImageView.setEnabled(false);
            }
        }
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == liveReplayHead.getId()) {
            LiveAnchorDialog dialogPerson = new LiveAnchorDialog(mContext);

            WindowManager.LayoutParams lp = dialogPerson.getWindow().getAttributes();
            lp.alpha = 0.8f;
            dialogPerson.getWindow().setAttributes(lp);

            dialogPerson.show();

        } else if (id == mPlaybackImageView.getId()) {
            if (mLiveReplayMediaPlayerController.isPlaying()) {
                mLiveReplayMediaPlayerController.pause();
            } else if (!mLiveReplayMediaPlayerController.isPlaying()) {
                mLiveReplayMediaPlayerController.start();
            }
        } else if (id == closeImage.getId()) {
            LiveExitDialog dialog = new LiveExitDialog(mContext, "确定关闭该直播？");
            dialog.show();

        } else if (id == reportImage.getId()) {
            LiveExitDialog dialog = new LiveExitDialog(mContext, "确定举报该直播？");
            dialog.show();

        } else if (id == liveReplayPerson.getId()) {
            //person list button
            if (isListVisible) {
                mHorizontalList.setVisibility(VISIBLE);
                isListVisible = false;
            } else {
                mHorizontalList.setVisibility(GONE);
                isListVisible = true;
            }

        } else if (id == shareButton.getId()) {
            Toast.makeText(mContext, "I am share", Toast.LENGTH_SHORT).show();

        } else if (id == switchButton.getId()) {
            if (isSwitch) {
                currentTimeTextView.setVisibility(VISIBLE);
                lineTextView.setVisibility(VISIBLE);
                totalTimeTextView.setVisibility(VISIBLE);
                liveReplayPerson.setVisibility(VISIBLE);
                mHeartLayout.setVisibility(VISIBLE);
                mHorizontalList.setVisibility(VISIBLE);
                mPlaybackImageView.setVisibility(VISIBLE);
                mSeekBar.setVisibility(VISIBLE);
                shareButton.setVisibility(VISIBLE);
                heartImageView.setVisibility(VISIBLE);
                personCountTextView.setVisibility(VISIBLE);
                praiseCountTextView.setVisibility(VISIBLE);
                liveReplayListView.setVisibility(VISIBLE);
                switchButton.setImageResource(R.drawable.live_model_image);
                isSwitch = false;

            } else {
                currentTimeTextView.setVisibility(GONE);
                lineTextView.setVisibility(GONE);
                totalTimeTextView.setVisibility(GONE);
                liveReplayPerson.setVisibility(GONE);
                mHeartLayout.setVisibility(GONE);
                mHorizontalList.setVisibility(GONE);
                mPlaybackImageView.setVisibility(GONE);
                mSeekBar.setVisibility(GONE);
                shareButton.setVisibility(GONE);
                heartImageView.setVisibility(GONE);
                personCountTextView.setVisibility(GONE);
                praiseCountTextView.setVisibility(GONE);
                liveReplayListView.setVisibility(GONE);
                switchButton.setImageResource(R.drawable.live_quiet_model_image);
                isSwitch = true;
            }
        } else if (id == heartImageView.getId()) {
            //TODO
            praiseCount++;
            praiseCountTextView.setText(String.valueOf(praiseCount));
        }

    }

    public void setMediaPlayerController(ILiveReplayController mediaPlayerController) {
        mLiveReplayMediaPlayerController = mediaPlayerController;
    }

    private int randomColor() {
        return Color.rgb(mRandom.nextInt(255), mRandom.nextInt(255), mRandom.nextInt(255));
    }

}
