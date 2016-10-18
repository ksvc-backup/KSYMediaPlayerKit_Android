package com.ksy.media.widget.ui.base;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.ksy.mediaPlayer.widget.R;


public class LiveAnchorDialog extends Dialog {
    private Context mContext;
    private ImageView closeImageView;
    private Button anchorAttentionButton;

    public LiveAnchorDialog(Context context) {
        super(context,R.style.ExitDialog);
        mContext=context;
    }

    public LiveAnchorDialog(Context context, int theme) {
        super(context, theme);
        mContext=context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_layout_anchor_dialog);

        closeImageView = (ImageView) findViewById(R.id.imageLiveViewClose);
        closeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LiveAnchorDialog.this.dismiss();
            }
        });

        anchorAttentionButton = (Button)findViewById(R.id.live_button);
        anchorAttentionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LiveAnchorDialog.this.dismiss();
                Toast.makeText(mContext, "Attention Succeed", Toast.LENGTH_SHORT).show();
            }
        });

        this.setCanceledOnTouchOutside(false);

    }

}
