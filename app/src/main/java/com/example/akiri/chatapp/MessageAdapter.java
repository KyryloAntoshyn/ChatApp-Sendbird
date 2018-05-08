package com.example.akiri.chatapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.akiri.chatapp.utils.ImageUtils;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.UserMessage;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_MESSAGE_SENT = 1; //какого типа сообщение или полученное или отправлен
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private Context mContext; //контекст активити с сообщениями
    private List<BaseMessage> mMessageList; //список сообщений
    private OpenChannel mChannel;

    public MessageAdapter(Context context, OpenChannel channel) {
        mContext = context;
        mMessageList = new ArrayList<>();
        mChannel = channel;

        refresh();
    }

    void refresh() { //получаем 30 пред сообщ (прогружаем по 30) (когда обновляем)

        mChannel.getPreviousMessagesByTimestamp(Long.MAX_VALUE, true, 30, true,
                BaseChannel.MessageTypeFilter.USER, null, new BaseChannel.GetMessagesHandler() {
                    @Override
                    public void onResult(List<BaseMessage> list, SendBirdException e) {
                        if (e != null) {
                            e.printStackTrace();
                            return;
                        }
                        mMessageList = (ArrayList<BaseMessage>) list;

                        notifyDataSetChanged(); //ф-ция адаптера, которая уведомляет что данные изменились
                    }
                });
    }

    void sendMessage(final String message) {
        mChannel.sendUserMessage(message, new BaseChannel.SendUserMessageHandler() {
            @Override
            public void onSent(UserMessage userMessage, SendBirdException e) {
                if (e != null) {
                    e.printStackTrace();
                    Toast.makeText(mContext, "Send status: ERROR!!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                mMessageList.add(0, userMessage);
                notifyDataSetChanged();
                //Toast.makeText(mContext, "Send status: SUCCESS!!!",Toast.LENGTH_SHORT).show();
            }
        });
    }

    void loadPreviousMessages() { //ф-ция, которая считает сколько сообщений нужно прогрузить (когда листаем)
        final long lastTimestamp = mMessageList.get(mMessageList.size() - 1).getCreatedAt();
        mChannel.getPreviousMessagesByTimestamp(lastTimestamp, false, 30, true,
                BaseChannel.MessageTypeFilter.USER, null, new BaseChannel.GetMessagesHandler() {
                    @Override
                    public void onResult(List<BaseMessage> list, SendBirdException e) {
                        if (e != null) {
                            e.printStackTrace();
                            return;
                        }

                        mMessageList.addAll(list);

                        notifyDataSetChanged();//функция адаптера, которая уведомляет,что данные изменились
                    }
                });
    }

    // Appends a new message to the beginning of the message list.
    void appendMessage(UserMessage message) {
        mMessageList.add(0, message);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) { //определяем тип сообщ
        UserMessage message = (UserMessage) mMessageList.get(position);

        if (message.getSender().getUserId().equals(SendBird.getCurrentUser().getUserId())) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { //создаем из хмл файла вью
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) { //привязуем сообщение к вью
        UserMessage message = (UserMessage) mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    public void addFirst(UserMessage userMessage) {
        mMessageList.add(0, userMessage);
        notifyDataSetChanged();
    }


    TextView messageText, timeText;

    private class SentMessageHolder extends RecyclerView.ViewHolder { //привязываем текст\картинки на нужные места

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
            timeText = (TextView) itemView.findViewById(R.id.text_message_time);
        }

        void bind(UserMessage message) {
            messageText.setText(message.getMessage());

            // Format the stored timestamp into a readable String using method.
            timeText.setText(DateUtils.formatDateTime(mContext, message.getCreatedAt(), DateUtils.FORMAT_SHOW_TIME));
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;
        ImageView profileImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
            timeText = (TextView) itemView.findViewById(R.id.text_message_time);
            nameText = (TextView) itemView.findViewById(R.id.text_message_name);
            profileImage = (ImageView) itemView.findViewById(R.id.image_message_profile);
        }

        void bind(UserMessage message) {
            messageText.setText(message.getMessage());

            // Format the stored timestamp into a readable String using method.
            timeText.setText(DateUtils.formatDateTime(mContext, message.getCreatedAt(), DateUtils.FORMAT_SHOW_TIME));

            nameText.setText(message.getSender().getNickname());

            // Insert the profile image from the URL into the ImageView.
            ImageUtils.displayRoundImageFromUrl(mContext, message.getSender().getProfileUrl(), profileImage);//для кеширования картинки пользователя
        }
    }
}
