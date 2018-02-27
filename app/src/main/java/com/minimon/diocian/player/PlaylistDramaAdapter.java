package com.minimon.diocian.player;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by GOOD on 2018-02-21.
 */

public class PlaylistDramaAdapter extends RecyclerView.Adapter {

    private List<Drama> items;
    private Context mContext;
    private PlayListItemClickListener listener;
    private String mType;

    public PlaylistDramaAdapter(Context context, List<Drama> modelData, String type){
        this.items = modelData;
        this.mContext = context;
        this.mType = type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        if("info".equals(mType)) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_drama_play, parent, false);
            return new ListItemViewHolder(itemView, viewType);
        }
        else if("list_".equals(mType)) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_playing_playlist, parent, false);
            return new PlayListItemViewHolder(itemView);
        }
        return new ListItemViewHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Drama item = items.get(position);
        if("info".equals(mType)) {
            ListItemViewHolder itemHolder = (ListItemViewHolder) holder;
//        itemHolder.thumbnail.(item.getThumbnailUrl());
            if (!TextUtils.isEmpty(item.getThumbnailUrl()))
                Picasso.with(mContext).load(item.getThumbnailUrl()).into(itemHolder.thumbnail);
            itemHolder.contentTitle.setText(item.getContentTitle());
            itemHolder.channelName.setText(item.getC_title());
            itemHolder.playCount.setText(item.getPlayCount());
            itemHolder.heartCount.setText(item.getHeartCount());
            itemHolder.point.setText(item.getPoint());
        }else{
            PlayListItemViewHolder itemViewHolder = (PlayListItemViewHolder) holder;
            if (!TextUtils.isEmpty(item.getThumbnailUrl()))
                Picasso.with(mContext).load(item.getThumbnailUrl()).into(itemViewHolder.thumbnail);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public void setClickListener(PlayListItemClickListener itemClickLsitener){
        this.listener = itemClickLsitener;
    }
    public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView thumbnail;
        TextView contentTitle;
        TextView channelName;
        TextView playCount;
        TextView heartCount;
        TextView point;

        public ListItemViewHolder(View itemView, int viewType) {
            super(itemView);
            thumbnail = (ImageView) itemView.findViewById(R.id.img_drama_thumbnail);
            contentTitle = (TextView) itemView.findViewById(R.id.tv_drama_playlist_contentTitle);
            channelName = (TextView) itemView.findViewById(R.id.tv_drama_playlist_channelName);
            playCount = (TextView) itemView.findViewById(R.id.tv_drama_playlist_play);
            heartCount = (TextView) itemView.findViewById(R.id.tv_drama_playlist_heart);
            point = (TextView) itemView.findViewById(R.id.tv_drama_playlist_point);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(listener != null) listener.onClick(v, items.get(getAdapterPosition()).getIdx().toString());
        }
    }
    public class PlayListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView thumbnail;

        public PlayListItemViewHolder(View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.img_playlist_thumbnail);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(listener != null) listener.onClick(v,items.get(getAdapterPosition()).getIdx().toString());
        }
    }
}
