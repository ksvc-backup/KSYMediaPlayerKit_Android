package com.ksy.media.demo.main;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by eflakemac on 15/12/7.
 */
public class DemoListItemSpaceDecoration extends RecyclerView.ItemDecoration {
    private final int space;

    public DemoListItemSpaceDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.bottom = space;
        outRect.right = space;
//        if (parent.getChildPosition(view) == 0) {
//            outRect.top = space;
//        }

    }
}
