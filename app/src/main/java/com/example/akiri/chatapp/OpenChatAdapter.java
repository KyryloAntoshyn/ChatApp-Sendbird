package com.example.akiri.chatapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.akiri.chatapp.utils.DateUtils;
import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.SendBird;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by akiri on 08.05.2018.
 */

public class OpenChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER_MESSAGE = 10;
    private static final int VIEW_TYPE_ADMIN_MESSAGE = 30;

    private Context mContext;
    private List<BaseMessage> mMessageList;
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;

    /**
     * An interface to implement item click callbacks in the activity or fragment that
     * uses this adapter.
     */
    interface OnItemClickListener {
        void onUserMessageItemClick(UserMessage message);
        void onAdminMessageItemClick(AdminMessage message);
    }

    interface OnItemLongClickListener {
        void onBaseMessageLongClick(BaseMessage message, int position);
    }

    public OpenChatAdapter(Context context) {
        mMessageList = new ArrayList<>();
        mContext = context;
    }

    void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mItemLongClickListener = listener;
    }

    void setMessageList(List<BaseMessage> messages) {
        mMessageList = messages;
        notifyDataSetChanged();
    }

    void addFirst(BaseMessage message) {
        mMessageList.add(0, message);
        notifyDataSetChanged();
    }

    void addLast(BaseMessage message) {
        mMessageList.add(message);
        notifyDataSetChanged();
    }

    void delete(long msgId) {
        for(BaseMessage msg : mMessageList) {
            if(msg.getMessageId() == msgId) {
                mMessageList.remove(msg);
                notifyDataSetChanged();
                break;
            }
        }
    }

    void update(BaseMessage message) {
        BaseMessage baseMessage;
        for (int index = 0; index < mMessageList.size(); index++) {
            baseMessage = mMessageList.get(index);
            if(message.getMessageId() == baseMessage.getMessageId()) {
                mMessageList.remove(index);
                mMessageList.add(index, message);
                notifyDataSetChanged();
                break;
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER_MESSAGE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_open_chat_user, parent, false);
            return new UserMessageHolder(view);

        } else if (viewType == VIEW_TYPE_ADMIN_MESSAGE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_open_chat_admin, parent, false);
            return new AdminMessageHolder(view);
        }

        // Theoretically shouldn't happen.
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (mMessageList.get(position) instanceof UserMessage) {
            return VIEW_TYPE_USER_MESSAGE;
        } else if (mMessageList.get(position) instanceof AdminMessage) {
            return VIEW_TYPE_ADMIN_MESSAGE;
        }

        // Unhandled message type.
        return -1;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BaseMessage message = mMessageList.get(position);

        boolean isNewDay = false;

        // If there is at least one item preceding the current one, check the previous message.
        if (position < mMessageList.size() - 1) {
            BaseMessage prevMessage = mMessageList.get(position + 1);

            // If the date of the previous message is different, display the date before the message,
            // and also set isContinuous to false to show information such as the sender's nickname
            // and profile image.
            if (!DateUtils.hasSameDate(message.getCreatedAt(), prevMessage.getCreatedAt())) {
                isNewDay = true;
            }

        } else if (position == mMessageList.size() - 1) {
            isNewDay = true;
        }

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_USER_MESSAGE:
                ((UserMessageHolder) holder).bind(mContext, (UserMessage) message, isNewDay,
                        mItemClickListener, mItemLongClickListener, position);
                break;
            case VIEW_TYPE_ADMIN_MESSAGE:
                ((AdminMessageHolder) holder).bind((AdminMessage) message, isNewDay,
                        mItemClickListener);
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    private class UserMessageHolder extends RecyclerView.ViewHolder {
        TextView nicknameText, messageText, editedText, timeText, dateText;
        //ImageView profileImage;

        UserMessageHolder(View itemView) {
            super(itemView);

            nicknameText = (TextView) itemView.findViewById(R.id.text_open_chat_nickname);
            messageText = (TextView) itemView.findViewById(R.id.text_open_chat_message);
            editedText = (TextView) itemView.findViewById(R.id.text_open_chat_edited);
            timeText = (TextView) itemView.findViewById(R.id.text_open_chat_time);
            //profileImage = (ImageView) itemView.findViewById(R.id.image_open_chat_profile);
            dateText = (TextView) itemView.findViewById(R.id.text_open_chat_date);
        }

        // Binds message details to ViewHolder item
        void bind(Context context, final UserMessage message, boolean isNewDay,
                  @Nullable final OnItemClickListener clickListener,
                  @Nullable final OnItemLongClickListener longClickListener, final int postion) {

            User sender = message.getSender();

            // If current user sent the message, display name in different color
            if (sender.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                nicknameText.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            } else {
                nicknameText.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
            }

            // Show the date if the message was sent on a different date than the previous one.
            if (isNewDay) {
                dateText.setVisibility(View.VISIBLE);
                dateText.setText(DateUtils.formatDate(message.getCreatedAt()));
            } else {
                dateText.setVisibility(View.GONE);
            }

            nicknameText.setText(message.getSender().getNickname());
            messageText.setText(message.getMessage());
            timeText.setText(DateUtils.formatTime(message.getCreatedAt()));

            if (message.getUpdatedAt() > 0) {
                editedText.setVisibility(View.VISIBLE);
            } else {
                editedText.setVisibility(View.GONE);
            }

            // Get profile image and display it
            //ImageUtils.displayRoundImageFromUrl(context, message.getSender().getProfileUrl(), profileImage);

            if (clickListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListener.onUserMessageItemClick(message);
                    }
                });
            }

            if (longClickListener != null) {
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        longClickListener.onBaseMessageLongClick(message, postion);
                        return true;
                    }
                });
            }
        }
    }

    private class AdminMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, dateText;

        AdminMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_open_chat_message);
            dateText = (TextView) itemView.findViewById(R.id.text_open_chat_date);
        }

        void bind(final AdminMessage message, boolean isNewDay, final OnItemClickListener listener) {
            messageText.setText(message.getMessage());

            // Show the date if the message was sent on a different date than the previous one.
            if (isNewDay) {
                dateText.setVisibility(View.VISIBLE);
                dateText.setText(DateUtils.formatDate(message.getCreatedAt()));
            } else {
                dateText.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onAdminMessageItemClick(message);
                }
            });
        }
    }
}

