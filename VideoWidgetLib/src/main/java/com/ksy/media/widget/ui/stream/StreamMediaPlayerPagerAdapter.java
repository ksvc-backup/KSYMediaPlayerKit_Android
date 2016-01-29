package com.ksy.media.widget.ui.stream;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ksy.media.widget.ui.base.fragment.CommentListFragment;
import com.ksy.media.widget.ui.base.fragment.DetailFragment;
import com.ksy.media.widget.ui.base.fragment.RecommendListFragment;
import com.ksy.mediaPlayer.widget.R;

public class StreamMediaPlayerPagerAdapter extends FragmentPagerAdapter {

    public static final int PAGER_COUNT = 3;
    private final Context mContext;
    public CommentListFragment mFragment;

    public StreamMediaPlayerPagerAdapter(Context context,FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                mFragment = CommentListFragment.getInstance(position + "", "");
                return mFragment;
            case 1:
                return DetailFragment.newInstance(position + "", "");
            case 2:
                return RecommendListFragment.newInstance(position + "", "");
        }
        return null;
    }

    @Override
    public int getCount() {
        return PAGER_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getStringArray(R.array.pager_title)[position];
    }
}
