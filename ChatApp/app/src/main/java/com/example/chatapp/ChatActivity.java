package com.example.chatapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.UserMessage;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private Button sendButton;
    private EditText editText;
    private MessageAdapter messageAdapter;
    private String urlChannel;
    private LinearLayoutManager linearLayoutManager;
    private boolean isMessLoaded = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        String channelName = getIntent().getExtras().getString("CHANNEL_NAME");
        setTitle(channelName);//задаем вверху имя чата
        recyclerView = findViewById(R.id.reyclerview_message_list);
        sendButton = findViewById(R.id.button_chatbox_send);
        editText = findViewById(R.id.edittext_chatbox);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);//добавили лайнеар на ресайкл, чтоб сообщения отображались в нужном порядке

        urlChannel = getIntent().getExtras().getString("CHANNEL_URL");
        OpenChannel.getChannel(urlChannel, new OpenChannel.OpenChannelGetHandler() {
            @Override
            public void onResult(final OpenChannel openChannel, SendBirdException e) {
                if (e != null)
                {
                    isMessLoaded =false;
                    return;
                }
                openChannel.enter(new OpenChannel.OpenChannelEnterHandler() {
                    @Override
                    public void onResult(SendBirdException e) {
                        if (e != null)
                        {
                            isMessLoaded =false;
                            return;
                        }
                        messageAdapter = new MessageAdapter(getContext(),openChannel);
                        recyclerView.setAdapter(messageAdapter);
                        isMessLoaded = true;
                    }
                });
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageAdapter.sendMessage(editText.getText().toString());
                editText.setText("");
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {//если долистали до верха, прогружаем 30 смс
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (isMessLoaded && linearLayoutManager.findLastVisibleItemPosition() == messageAdapter.getItemCount()-1)
                {
                    messageAdapter.loadPreviousMessages();
                }
            }
        });
    }

    @Override
    protected  void  onResume() //следит за присылаемыми смс
    {
        super.onResume();
        SendBird.addChannelHandler("CHANNEL_HANDLER", new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                if (baseChannel .getUrl().equals(urlChannel) && baseMessage instanceof UserMessage)
                {
                    messageAdapter.appendMessage((UserMessage) baseMessage);
                }
            }
        });
    }
    Context getContext()
    {
        return this;
    }
}