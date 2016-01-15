package com.ksy.media.widget.ui.base.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ksy.mediaPlayer.widget.R;

import java.util.ArrayList;

public class RecommendListFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private OnFragmentInteractionListener mListener;

    private String mParam1;
    private String mParam2;
    private ListView recommendList;
    private ArrayList<RecommendItem> items;
    private RecommendListAdapter mRecommendListAdapter;


    public RecommendListFragment() {
        // Required empty public constructor
    }


    public static RecommendListFragment newInstance(String param1, String param2) {
        RecommendListFragment fragment = new RecommendListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        items = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            RecommendItem item = new RecommendItem();
            item.setTime(getString(R.string.video_recommend_item_time));
            item.setTitle(getString(R.string.video_recommend_item_title));
            item.setWatch(getString(R.string.video_recommend_item_watch));
            items.add(item);
        }
        mRecommendListAdapter = new RecommendListAdapter(getActivity(), items);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_recommend_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        recommendList = (ListView) view.findViewById(R.id.video_recomment_list);
        recommendList.setAdapter(mRecommendListAdapter);
        recommendList.setOnItemClickListener(this);
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            mListener.onRecommendListFragmentInteraction(position + "");
        }
    }

    public interface OnFragmentInteractionListener {
        public void onRecommendListFragmentInteraction(String id);
    }
}
