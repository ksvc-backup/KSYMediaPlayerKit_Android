package com.ksy.media.demo.main;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;

import com.ksy.media.demo.R;
import com.ksy.media.demo.live.PhoneLiveActivity;
import com.ksy.media.demo.livereplay.PhoneLiveReplayActivity;
import com.ksy.media.demo.shortvideo.ShortVideoActivity;
import com.ksy.media.demo.stream.StreamVideoActivity;
import com.ksy.media.demo.video.OnlineVideoActivity;

import java.util.ArrayList;

public class DemoActivity extends AppCompatActivity implements DemoListAdapter.DemoListClickListener {

    private static final int PHONE_LIVE = 0;
    private static final int PHONE_LIVE_REPLAY = 1;
    private static final int ONLINE_VIDEO = 2;
    private static final int ONLINE_STREAM = 3;
    private static final int SHORT_VIDEO = 4;
    private RecyclerView mRecycleView;
    private ArrayList<DemoContent> demoList;
    private DemoListAdapter mAdapter;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_main);
        setupDemoTitle();
        setupViews();
    }


    private void setupViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }
        mRecycleView = (RecyclerView) findViewById(R.id.demo_list);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecycleView.setLayoutManager(staggeredGridLayoutManager);
        mAdapter = new DemoListAdapter(DemoActivity.this, demoList);
        mRecycleView.setAdapter(mAdapter);
        mRecycleView.addItemDecoration(new DemoListItemSpaceDecoration(getResources().getDimensionPixelSize(R.dimen.demo_card_hori_margin)));
//      mRecycleView.setItemAnimator(new SlideInOutBottomItemAnimator(mRecycleView));
        mAdapter.setDemoListClickListener(this);
    }

    private void setupDemoTitle() {
        demoList = new ArrayList<>();
        demoList.add(new DemoContent("PHONE_LIVE"));
        demoList.add(new DemoContent("PHONE_LIVE_REPLAY"));
        demoList.add(new DemoContent("ONLINE_VIDEO"));
        demoList.add(new DemoContent("ONLINE_STREAM"));
        demoList.add(new DemoContent("SHORT_VIDEO"));
    }

    @Override
    public void onDemoListClicked(int position, DemoContent demoContent) {
        switch (position) {
            case DemoActivity.PHONE_LIVE:
                startActivity(new Intent(DemoActivity.this, PhoneLiveActivity.class));
                break;
            case DemoActivity.PHONE_LIVE_REPLAY:
                startActivity(new Intent(DemoActivity.this, PhoneLiveReplayActivity.class));
                break;
            case DemoActivity.ONLINE_VIDEO:
                startActivity(new Intent(DemoActivity.this, OnlineVideoActivity.class));
                break;
            case DemoActivity.ONLINE_STREAM:
                startActivity(new Intent(DemoActivity.this, StreamVideoActivity.class));
                break;
            case DemoActivity.SHORT_VIDEO:
                startActivity(new Intent(DemoActivity.this, ShortVideoActivity.class));
                break;
        }
    }
}
