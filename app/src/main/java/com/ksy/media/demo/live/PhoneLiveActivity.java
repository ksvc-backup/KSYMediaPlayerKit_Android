package com.ksy.media.demo.live;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.ksy.media.demo.R;
import com.ksy.media.widget.ui.live.LiveMediaPlayerView;
import com.ksy.media.widget.util.Constants;
import com.ksy.media.widget.util.PlayConfig;

public class PhoneLiveActivity extends AppCompatActivity implements LiveMediaPlayerView.PlayerViewCallback {

    LiveMediaPlayerView playerViewLive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_phone_live);

        playerViewLive = (LiveMediaPlayerView) findViewById(R.id.player_view_live);
        playerViewLive.setPlayConfig(true, PlayConfig.INTERRUPT_MODE_RELEASE_CREATE);
        playerViewLive.setPlayerViewCallback(this);

        final View dialogView = LayoutInflater.from(this).inflate(
                R.layout.dialog_input_live, null);
        final EditText editInput = (EditText) dialogView
                .findViewById(R.id.input_live);
        // startPlayer("");
        new AlertDialog.Builder(this).setTitle("User Input")
                .setView(dialogView)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputString = editInput.getText().toString();
                        if (!TextUtils.isEmpty(inputString)) {
                            startPlayer(inputString);
                        } else {
                            Toast.makeText(PhoneLiveActivity.this,
                                    "Paht or URL can not be null",
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
        playerViewLive.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        playerViewLive.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(Constants.LOG_TAG, "VideoPlayerActivity ....onDestroy()......");
        super.onDestroy();
        playerViewLive.onDestroy();
    }

    private void startPlayer(String url) {
        Log.d(Constants.LOG_TAG, "input url = " + url);
        playerViewLive.play(url);
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
        playerViewLive.dispatchKeyEvent(event);
        return super.dispatchKeyEvent(event);
    }
}
