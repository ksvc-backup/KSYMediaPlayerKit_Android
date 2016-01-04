package com.ksy.media.demo.main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ksy.media.demo.R;

import java.util.ArrayList;

/**
 * Created by eflakemac on 15/12/7.
 */
public class DemoListAdapter extends RecyclerView.Adapter<DemoContentViewHolder> {
    private final ArrayList<DemoContent> mDemoList;
    private final Context mContext;
    private final LayoutInflater mInflater;
    private DemoListClickListener mListener;

    public DemoListAdapter(Context context, ArrayList<DemoContent> demoList) {
        this.mContext = context;
        this.mDemoList = demoList;
        mInflater = LayoutInflater.from(context);
    }

    public void setDemoListClickListener(DemoListClickListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public DemoContentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        DemoContentViewHolder holder = null;
        View view = null;
        switch (viewType) {
            case 0:
                view = mInflater.inflate(R.layout.recycler_view_item_demo_list, parent, false);
                holder = new DemoContentViewHolder(view);
                break;
        }
        // Set Data
        holder.name = (TextView) view.findViewById(R.id.tv_demo_name);
        return holder;
    }

    @Override
    public void onBindViewHolder(DemoContentViewHolder holder, final int position) {
        final DemoContent demoContent = mDemoList.get(position);
        holder.name.setText(demoContent.name);
        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener!=null){
                    mListener.onDemoListClicked(position,demoContent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDemoList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    public interface DemoListClickListener {
        void onDemoListClicked(int position, DemoContent demoContent);
    }
}
