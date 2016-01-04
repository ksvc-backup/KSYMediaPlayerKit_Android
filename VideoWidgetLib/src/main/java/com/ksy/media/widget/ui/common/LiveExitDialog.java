package com.ksy.media.widget.ui.common;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ksy.mediaPlayer.widget.R;


public class LiveExitDialog extends Dialog {
    private Context mContext;
    private Button mConfirm;
    private Button mCancel;
    private TextView titleTextView;
    String mTitle;

    public LiveExitDialog(Context context, String title) {
        super(context,R.style.ExitDialog);
        mContext=context;
        mTitle = title;
    }

    public LiveExitDialog(Context context, int theme) {
        super(context, theme);
        mContext=context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_layout_dialog);

        this.setCanceledOnTouchOutside(false);

        titleTextView = (TextView) findViewById(R.id.title);
        titleTextView.setText(mTitle);

        mConfirm= (Button) findViewById(R.id.dialog_confirm);
        mCancel= (Button) findViewById(R.id.dialog_cancel);

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);

            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LiveExitDialog.this.dismiss();
            }
        });
    }

}
