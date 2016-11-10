package com.appvisor_event.master;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by zhanghongbo on 16/11/2.
 */
public class RecycleAdapter extends RecyclerView.Adapter{
    private Context mContext;
    private ArrayList<ArrayList<ImageItem>> mList;
    private int checked = 0;

    public RecycleAdapter(Context context, ArrayList<ArrayList<ImageItem>> list){
        this.mContext=context;
        this.mList=list;
    }
    public interface RecycleImgListener{
        void RecycleClick(int position);
    }
    public RecycleImgListener recycleListener;
    public void setOnMenuListener(RecycleImgListener recycleImgListenerlistener){
        this.recycleListener=recycleImgListenerlistener;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageViewHolder viewHolder=new ImageViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_recycle,parent,false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        ImageViewHolder viewHolder= (ImageViewHolder) holder;
        viewHolder.imageView.addImage(mList.get(position));
        viewHolder.imageView.setChecked(checked == position);
        viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checked = position;
                notifyDataSetChanged();
             recycleListener.RecycleClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
    class ImageViewHolder extends RecyclerView.ViewHolder{
        CustomImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView= (CustomImageView) itemView.findViewById(R.id.image_item);
        }
    }
}
