package com.example.akiri.chatapp;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sendbird.android.OpenChannel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by akiri on 08.05.2018.
 */

public class ChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<OpenChannel> mChannelList;
    private Context mContext;
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;

    interface OnItemClickListener {
        void onItemClick(OpenChannel channel);
    }

    interface OnItemLongClickListener {
        void onItemLongPress(OpenChannel channel);
    }

    ChatListAdapter(Context context) {
        mContext = context;
        mChannelList = new ArrayList<>();
    }

    @NonNull
    @Override
    public ChannelHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ChannelHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ChannelHolder)holder).bind(mContext, mChannelList.get(position), position, mItemClickListener, mItemLongClickListener);
    }

    @Override
    public int getItemCount() {
        return mChannelList.size();
    }

    void setOpenChannelList(List<OpenChannel> channelList) {
        mChannelList = channelList;
        notifyDataSetChanged();
    }

    void addLast(OpenChannel channel) {
        mChannelList.add(channel);
        notifyDataSetChanged();
    }

    void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mItemLongClickListener = listener;
    }

    void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    private class ChannelHolder extends RecyclerView.ViewHolder {

        // A list of colors for decorating each list item.
        private String[] colorList = { "#ff2de3e1", "#ff35a3fb", "#ff805aff", "#ffcf47fb", "#ffe248c3" };

        TextView nameText;
        ImageView coloredDecorator;

        ChannelHolder(View itemView) {
            super(itemView);
            nameText = (TextView) itemView.findViewById(R.id.text_open_channel_list_name);
            coloredDecorator = (ImageView) itemView.findViewById(R.id.image_open_channel_list_decorator);
        }

        void bind(final Context context, final OpenChannel channel, int position, @Nullable final OnItemClickListener clickListener, @Nullable final OnItemLongClickListener longClickListener) {
            nameText.setText(channel.getName());
            coloredDecorator.setBackgroundColor(Color.parseColor(colorList[position % colorList.length]));

            // Set an OnClickListener to this item.
            if (clickListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListener.onItemClick(channel);
                    }
                });
            }

            // Set an OnLongClickListener to this item.
            if (longClickListener != null) {
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        longClickListener.onItemLongPress(channel);

                        // return true if the callback consumed the long click
                        return true;
                    }
                });
            }
        }
    }
}
