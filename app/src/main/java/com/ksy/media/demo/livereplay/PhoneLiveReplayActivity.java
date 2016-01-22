package com.ksy.media.demo.livereplay;

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
import com.ksy.media.widget.ui.livereplay.LiveReplayMediaPlayerView;
import com.ksy.media.widget.util.Constants;
import com.ksy.media.widget.util.PlayConfig;

public class  PhoneLiveReplayActivity extends AppCompatActivity implements LiveReplayMediaPlayerView.PlayerViewCallback{

    LiveReplayMediaPlayerView playerViewLiveReplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_phone_live_replay);

        playerViewLiveReplay = (LiveReplayMediaPlayerView) findViewById(R.id.player_view_live_replay);
        playerViewLiveReplay.setPlayConfig(false, PlayConfig.INTERRUPT_MODE_RELEASE_CREATE, PlayConfig.LIVE_VIDEO_MODE);
        playerViewLiveReplay.setPlayerViewCallback(this);

        final View dialogView = LayoutInflater.from(this).inflate(
                R.layout.livereplay_dialog_input, null);
        final EditText editInput = (EditText) dialogView
                .findViewById(R.id.input);
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
                            Toast.makeText(PhoneLiveReplayActivity.this,
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
        playerViewLiveReplay.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        playerViewLiveReplay.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(Constants.LOG_TAG, "VideoPlayerActivity ....onDestroy()......");
        super.onDestroy();
        playerViewLiveReplay.onDestroy();
    }

    private void startPlayer(String url) {
        Log.d(Constants.LOG_TAG, "input url = " + url);
        playerViewLiveReplay.play(url);
    }

    @Override
    public void onPrepared() {

    }
    
    @Override
    public void onFinish(int playMode) {
        Log.i(Constants.LOG_TAG, "activity on finish ===========");
        this.finish();
    }

    @Override
    public void onError(int errorCode, String errorMsg) {

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        playerViewLiveReplay.dispatchKeyEvent(event);
        return super.dispatchKeyEvent(event);
    }

}
