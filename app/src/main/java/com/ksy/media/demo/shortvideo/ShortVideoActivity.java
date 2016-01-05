package com.ksy.media.demo.shortvideo;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ksy.media.demo.R;
import com.ksy.media.widget.ui.shortvideo.ShortMovieItem;
import com.ksy.media.widget.ui.shortvideo.ShortVideoListAdapter;
import com.ksy.media.widget.ui.shortvideo.ShortVideoMediaPlayerView;
import com.ksy.media.widget.util.Constants;
import com.ksy.media.widget.util.PlayConfig;

import java.util.ArrayList;

public class ShortVideoActivity extends AppCompatActivity implements
        ShortVideoMediaPlayerView.PlayerViewCallback, AbsListView.OnScrollListener, AdapterView.OnItemClickListener, View.OnClickListener {

    private View headView;
    private View commentLayout;
    private RelativeLayout container;
    private Toolbar mToolbar;
    private ListView listView;
    private ArrayList<ShortMovieItem> items;
    private ShortVideoMediaPlayerView playerViewShortMovie;
    private int lastVisibleItemPosition;
    private boolean scrollFlag;
    private int currentState;
    private int lastState;
    private int mHeight;
    private int mWidth;
    private static final int STATE_UP = 1;
    private static final int STATE_DOWN = 0;
    private ImageView short_video_watch_icon;
    private ImageView short_video_comment_icon;
    private ImageView short_video_favourate_icon;
    private ImageView short_video_share_icon;
    private View pop_comment_btn;
    private View pop_edittext_et;
    private TextView short_video_add_focus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_short_movie);
        setupScreenSize();
        setupViews();
    }

    private void setupViews() {
        container = (RelativeLayout) findViewById(R.id.container);
        headView = LayoutInflater.from(ShortVideoActivity.this).inflate(R.layout.short_movie_head_view, null);
        listView = (ListView) findViewById(R.id.short_video_list);
        commentLayout = LayoutInflater.from(ShortVideoActivity.this).inflate(
                R.layout.short_video_pop_layout, null);
        pop_edittext_et = commentLayout.findViewById(R.id.pop_edittext_et);
        pop_comment_btn = commentLayout.findViewById(R.id.pop_comment_btn);
        pop_comment_btn.setOnClickListener(this);
        setupFunctionIcon();
        setupCommentList();
        setupDialog();
        setupAnimation();
        setupToolbar();
    }

    private void setupFunctionIcon() {
        short_video_watch_icon = (ImageView) headView.findViewById(R.id.short_video_watch_icon);
        short_video_comment_icon = (ImageView) headView.findViewById(R.id.short_video_comment_icon);
        short_video_favourate_icon = (ImageView) headView.findViewById(R.id.short_video_favourate_icon);
        short_video_share_icon = (ImageView) headView.findViewById(R.id.short_video_share_icon);
        short_video_watch_icon.setOnClickListener(this);
        short_video_comment_icon.setOnClickListener(this);
        short_video_favourate_icon.setOnClickListener(this);
        short_video_share_icon.setOnClickListener(this);
    }

    private void setupCommentList() {
        makeContents();
        ShortVideoListAdapter adapter = new ShortVideoListAdapter(ShortVideoActivity.this, items);
        listView.addHeaderView(headView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(this);
        playerViewShortMovie = (ShortVideoMediaPlayerView) headView.findViewById(R.id.player_view_short_movie);
        playerViewShortMovie.setPlayConfig(false, PlayConfig.INTERRUPT_MODE_PAUSE_RESUME);
        playerViewShortMovie.setPlayerViewCallback(this);
        playerViewShortMovie.setTextureViewVisible(true);
        short_video_add_focus = (TextView) headView.findViewById(R.id.short_video_add_focus);
        short_video_add_focus.setOnClickListener(this);
    }

    private void setupAnimation() {
        LayoutTransition transition = new LayoutTransition();
        container.setLayoutTransition(transition);
        ObjectAnimator enter_animator = ObjectAnimator.ofInt(commentLayout, "y", mHeight, mHeight - commentLayout.getHeight());
        ObjectAnimator exit_animator = ObjectAnimator.ofInt(commentLayout, "y", mHeight - commentLayout.getHeight(), mHeight);
        transition.setAnimator(LayoutTransition.APPEARING, enter_animator);
        transition.setAnimator(LayoutTransition.DISAPPEARING, exit_animator);
    }

    private void setupScreenSize() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        mHeight = dm.heightPixels;
        mWidth = dm.widthPixels;
    }

    private void makeContents() {
        items = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            ShortMovieItem item = new ShortMovieItem();
            item.setComment(getString(R.string.short_video_item_comment));
            item.setFav(getString(R.string.short_video_item_fav));
            item.setInfo(getString(R.string.short_video_item_info));
            items.add(item);
        }
    }

    private void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            mToolbar.setTitle(getResources().getString(R.string.short_video_title));
//            mToolbar.setTitleTextColor(Color.BLACK);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }
    }

    private void setupDialog() {
        final View dialogView = LayoutInflater.from(this).inflate(
                R.layout.dialog_input, null);
        final EditText editInput = (EditText) dialogView
                .findViewById(R.id.input);
        new AlertDialog.Builder(this).setTitle("User Input")
                .setView(dialogView)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputString = editInput.getText().toString();
                        if (!TextUtils.isEmpty(inputString)) {

                            startPlayer(inputString);
                        } else {
                            Toast.makeText(ShortVideoActivity.this,
                                    "Path or URL can not be null",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        playerViewShortMovie.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        playerViewShortMovie.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(Constants.LOG_TAG, "ShortVideoActivity ....onDestroy()......");
        super.onDestroy();
        playerViewShortMovie.onDestroy();
    }

    // master
    private void startPlayer(String url) {
        Log.d(Constants.LOG_TAG, "input url = " + url);
        playerViewShortMovie.play(url);
    }

    @Override
    public void hideViews() {

    }

    @Override
    public void restoreViews() {

    }

    @Override
    public void onPrepared() {

    }

    @Override
    public void onQualityChanged() {

    }

    @Override
    public void onFinish(int playMode) {
        this.finish();
    }

    @Override
    public void onError(int errorCode, String errorMsg) {

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        playerViewShortMovie.dispatchKeyEvent(event);
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
            scrollFlag = true;
        } else {
            scrollFlag = false;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (scrollFlag) {
//            Log.d(Constants.LOG_TAG, "firstVisibleItem = " + firstVisibleItem + ",visibleItemCount = " + visibleItemCount + ",totalItemCount = " + totalItemCount);
            if (firstVisibleItem == 0) {
                if (!playerViewShortMovie.isTextureViewVisible()) {
                    playerViewShortMovie.setTextureViewVisible(true);
                    Log.d(Constants.LOG_TAG, "visible");
                }
            } else {
                if (playerViewShortMovie.isTextureViewVisible()) {
                    playerViewShortMovie.setTextureViewVisible(false);
                    Log.d(Constants.LOG_TAG, "invisible");
                }
            }

            if (firstVisibleItem > lastVisibleItemPosition) {
                //Up
                currentState = STATE_UP;
            }
            if (firstVisibleItem < lastVisibleItemPosition) {
                //Down
                currentState = STATE_DOWN;
            }
            if (lastState > currentState) {
                hideCommentLayout();
            } else if (lastState < currentState) {
                showCommentLayout();
            } else {

            }
            if (firstVisibleItem == lastVisibleItemPosition) {
                return;
            }
            lastVisibleItemPosition = firstVisibleItem;
            lastState = currentState;
        }
    }

    private void hideCommentLayout() {
        container.removeView(commentLayout);
    }

    private void showCommentLayout() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.short_movie_comment_distance));
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        container.addView(commentLayout, params);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(ShortVideoActivity.this, "i am comment", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.short_video_watch_icon:
                Toast.makeText(ShortVideoActivity.this, "i am watch count", Toast.LENGTH_SHORT).show();
                break;
            case R.id.short_video_comment_icon:
                Toast.makeText(ShortVideoActivity.this, "i am comment", Toast.LENGTH_SHORT).show();
                break;
            case R.id.short_video_favourate_icon:
                Toast.makeText(ShortVideoActivity.this, "i am favourate", Toast.LENGTH_SHORT).show();
                break;
            case R.id.short_video_share_icon:
                Toast.makeText(ShortVideoActivity.this, "i am share", Toast.LENGTH_SHORT).show();
                break;
            case R.id.pop_comment_btn:
                Toast.makeText(ShortVideoActivity.this, "comment send", Toast.LENGTH_SHORT).show();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                hideCommentLayout();
                break;
            case R.id.short_video_add_focus:
                Toast.makeText(ShortVideoActivity.this, "follow clicked", Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
