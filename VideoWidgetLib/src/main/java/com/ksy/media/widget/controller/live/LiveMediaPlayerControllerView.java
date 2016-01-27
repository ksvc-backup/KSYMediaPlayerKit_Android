package com.ksy.media.widget.controller.live;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ksy.media.widget.ui.base.HeartLayout;
import com.ksy.media.widget.ui.base.HorizontalListView;
import com.ksy.media.widget.ui.base.LiveAnchorDialog;
import com.ksy.media.widget.ui.base.LiveExitDialog;
import com.ksy.media.widget.ui.base.LivePersonDialog;
import com.ksy.media.widget.ui.live.LiveChatAdapter;
import com.ksy.media.widget.ui.live.LiveHeadListAdapter;
import com.ksy.mediaPlayer.widget.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class LiveMediaPlayerControllerView extends FrameLayout implements View.OnClickListener {

    private ImageView liveHead;
    private ImageView liveStateImage;
    private TextView timeTextView;
    private ImageView liveCloseImage;
    private ImageView liveReportImage;

    private ListView liveListView;
    private TextView noticeTextViewLive;
    private List<Map<String, Object>> data;
    private Timer refreshTimer = new Timer();
    private LiveChatAdapter adapter;
    Map<String, Object> map;

    private ImageView livePerson;
    private HorizontalListView liveHorizontalList;
    private LiveHeadListAdapter liveHeadListAdapter;

    private ImageView liveSwitchButton;
    private ImageView liveShareButton;
    private EditText liveEditText;

    private Context mContext;
    private Random mRandom = new Random();
    private Timer mLiveTimer = new Timer();
    private HeartLayout liveHeartLayout;
    private ImageView liveImageView;
    private boolean isSwitch;
    private boolean isLiveListVisible;
    private TextView livePraiseCountTextView;
    private TextView livePersonCountTextView;
    private int livePraiseCount;

    private Handler liveHandler = new Handler();
    protected LayoutInflater mLiveLayoutInflater;
    private Animation showLiveAudienceAnimation;
    private Animation hideLiveAudienceAnimation;
    private Timer mLiveAudienceComeTimer = new Timer();
    private Timer mLiveAudienceComeTimerGoneTimer = new Timer();
    private boolean isRemoveData;

    public LiveMediaPlayerControllerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    public LiveMediaPlayerControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public LiveMediaPlayerControllerView(Context context) {
        super(context);
        mContext = context;

        mLiveLayoutInflater = LayoutInflater.from(getContext());
        mLiveLayoutInflater.inflate(R.layout.blue_media_player_controller_live, this);

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
        showLiveAudienceAnimation = AnimationUtils.loadAnimation(mContext, R.anim.live_audience_show);
        hideLiveAudienceAnimation = AnimationUtils.loadAnimation(mContext, R.anim.live_audience_hide);

        liveEditText = (EditText) findViewById(R.id.video_comment_text);
        liveHead = (ImageView) findViewById(R.id.image_live_head);
        timeTextView = (TextView) findViewById(R.id.textViewTime);
        liveCloseImage = (ImageView) findViewById(R.id.live_image_close);
        liveReportImage = (ImageView) findViewById(R.id.live_image_report);
        livePersonCountTextView = (TextView) findViewById(R.id.live_person_count_textview);
        livePraiseCountTextView = (TextView) findViewById(R.id.live_praise_count_text);

        liveListView = (ListView) findViewById(R.id.live_list);
        data = getData();
        adapter = new LiveChatAdapter(mContext, data);
        liveListView.setAdapter(adapter);
        noticeTextViewLive = (TextView) findViewById(R.id.notice_text_live);

        liveHorizontalList = (HorizontalListView) findViewById(R.id.live_horizon);
        liveHeadListAdapter = new LiveHeadListAdapter(mContext);
        liveHorizontalList.setAdapter(liveHeadListAdapter);

        livePerson = (ImageView) findViewById(R.id.live_person_image);
        liveSwitchButton = (ImageView) findViewById(R.id.live_information_switch_bt);
        liveHeartLayout = (HeartLayout) findViewById(R.id.live_image_heart);
        liveImageView = (ImageView) findViewById(R.id.live_image_heart_bt);
        liveShareButton = (ImageView) findViewById(R.id.live_share_bt);

        liveHorizontalList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LivePersonDialog dialogPerson = new LivePersonDialog(mContext);

                WindowManager.LayoutParams lp = dialogPerson.getWindow().getAttributes();
                lp.alpha = 0.8f;
                dialogPerson.getWindow().setAttributes(lp);

                dialogPerson.show();
            }
        });

    }


    private void chatListControl() {
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                liveHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (data.size() >= 8) {
                            data.remove(0);
                            isRemoveData = false;
                        } else {
                            data.add(map);
                            isRemoveData = true;
                        }
                        if (isRemoveData) {
                            liveListView.requestLayout();
                            adapter.notifyDataSetChanged();
                        } else {
                            liveListView.requestLayout();
                            adapter.notifyDataSetChanged();
                            data.add(7, map);
                            liveListView.requestLayout();
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        }, 200, 2000);
    }

    public void heartLayoutTimer() {
        mLiveTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                liveHeartLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        liveHeartLayout.addHeart(randomColor());
                        livePraiseCount++;
                        livePraiseCountTextView.setText(String.valueOf(livePraiseCount));
                    }
                });
            }
        }, 500, 1000);
    }

    public void liveAudienceComeTimer() {
        mLiveAudienceComeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                liveHandler.postDelayed(mAudienceComeRunnable, 100);
            }
        }, 100, 6000);
    }

    public void liveAudienceComeTimerGoneTimer() {
        mLiveAudienceComeTimerGoneTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                liveHandler.postDelayed(mAudienceComeGoneRunnable, 100);
            }
        }, 100, 8000);
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

    public void startLiveTimer() {

        if (mLiveTimer == null || mLiveAudienceComeTimer == null || mLiveAudienceComeTimerGoneTimer == null || refreshTimer == null) {
            mLiveTimer = new Timer();
            mLiveAudienceComeTimer = new Timer();
            mLiveAudienceComeTimerGoneTimer = new Timer();
            refreshTimer = new Timer();
        }

        chatListControl();
        heartLayoutTimer();
        liveAudienceComeTimer();
        liveAudienceComeTimerGoneTimer();
        initListeners();
    }

    public void stopLiveTimer() {
        if (null != mLiveTimer) {
            mLiveTimer.cancel();
            mLiveTimer = null;
        }

        if (null != mLiveAudienceComeTimer) {
            mLiveAudienceComeTimer.cancel();
            mLiveAudienceComeTimer = null;
        }

        if (null != mLiveAudienceComeTimerGoneTimer) {
            mLiveAudienceComeTimerGoneTimer.cancel();
            mLiveAudienceComeTimerGoneTimer = null;
        }

        if (null != refreshTimer) {
            refreshTimer.cancel();
            refreshTimer = null;
        }
    }

    Runnable mAudienceComeRunnable = new Runnable() {
        @Override
        public void run() {
            noticeTextViewLive.startAnimation(showLiveAudienceAnimation);
        }
    };

    Runnable mAudienceComeGoneRunnable = new Runnable() {
        @Override
        public void run() {
            if (noticeTextViewLive.isShown()) {
                noticeTextViewLive.startAnimation(hideLiveAudienceAnimation);
                noticeTextViewLive.setVisibility(INVISIBLE);
            }
        }
    };

    protected void initListeners() {
        liveHead.setOnClickListener(this);
        liveCloseImage.setOnClickListener(this);
        liveReportImage.setOnClickListener(this);
        livePerson.setOnClickListener(this);
        liveSwitchButton.setOnClickListener(this);
        liveShareButton.setOnClickListener(this);
    }

    public void updateVideoTitle(String title) {
        if (!TextUtils.isEmpty(title)) {

        }
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == liveHead.getId()) {
            LiveAnchorDialog dialogPerson = new LiveAnchorDialog(mContext);

            WindowManager.LayoutParams lp = dialogPerson.getWindow().getAttributes();
            lp.alpha = 0.8f;
            dialogPerson.getWindow().setAttributes(lp);

            dialogPerson.show();

        } else if (id == liveCloseImage.getId()) {
            LiveExitDialog dialog = new LiveExitDialog(mContext, "确定关闭该直播？");
            dialog.show();

        } else if (id == liveReportImage.getId()) {
            LiveExitDialog dialog = new LiveExitDialog(mContext, "确定举报该直播？");
            dialog.show();

        } else if (id == livePerson.getId()) {
            //person list button
            if (isLiveListVisible) {
                liveHorizontalList.setVisibility(VISIBLE);
                isLiveListVisible = false;
            } else {
                liveHorizontalList.setVisibility(GONE);
                isLiveListVisible = true;
            }

        } else if (id == liveShareButton.getId()) {

            Toast.makeText(mContext, "I am share", Toast.LENGTH_SHORT).show();

        } else if (id == liveSwitchButton.getId()) {
            if (isSwitch) {
                livePerson.setVisibility(VISIBLE);
                liveHeartLayout.setVisibility(VISIBLE);
                liveHorizontalList.setVisibility(VISIBLE);
                liveShareButton.setVisibility(VISIBLE);
                liveEditText.setVisibility(VISIBLE);
                liveImageView.setVisibility(VISIBLE);
//				liveSwitchButton.setText(getResources().getString(R.string.live_info_switch));
                liveSwitchButton.setImageResource(R.drawable.live_model_image);
                livePersonCountTextView.setVisibility(VISIBLE);
                livePraiseCountTextView.setVisibility(VISIBLE);
                liveListView.setVisibility(VISIBLE);

                isSwitch = false;

            } else {
                livePerson.setVisibility(GONE);
                liveHeartLayout.setVisibility(GONE);
                liveHorizontalList.setVisibility(GONE);
                liveShareButton.setVisibility(GONE);
                liveEditText.setVisibility(GONE);
                liveImageView.setVisibility(GONE);
                liveSwitchButton.setImageResource(R.drawable.live_quiet_model_image);
                livePersonCountTextView.setVisibility(GONE);
                livePraiseCountTextView.setVisibility(GONE);
                liveListView.setVisibility(GONE);

                isSwitch = true;
            }
        } else if (id == liveImageView.getId()) {

            livePraiseCount++;
            livePraiseCountTextView.setText(String.valueOf(livePraiseCount));
        }

    }

    private int randomColor() {
        return Color.rgb(mRandom.nextInt(255), mRandom.nextInt(255), mRandom.nextInt(255));
    }

}
