package com.example.akiri.chatapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.PreviousMessageListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.UserMessage;

import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ImageButton mMessageSendButton;
    private EditText mMessageEditText;

    private InputMethodManager mIMM;

    private String channelUrl;
    private OpenChannel mChannel;
    private OpenChatAdapter mChatAdapter;
    private LinearLayoutManager mLayoutManager;

    private static final int STATE_NORMAL = 0;
    private static final int STATE_EDIT = 1;

    private static final int CHANNEL_LIST_LIMIT = 30;
    private static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_OPEN_CHAT";
    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_OPEN_CHAT";

    private PreviousMessageListQuery mPrevMessageListQuery;
    private int mCurrentState = STATE_NORMAL;
    private BaseMessage mEditingMessage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mIMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        this.channelUrl = getIntent().getStringExtra("channelurl");

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_open_channel_chat);
        setUpChatAdapter();
        setUpRecyclerView();
        // Set up chat box
        mMessageSendButton = (ImageButton) findViewById(R.id.imageButton);
        mMessageEditText = (EditText) findViewById(R.id.edittext_chat_message);

        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    mMessageSendButton.setEnabled(true);
                } else {
                    mMessageSendButton.setEnabled(false);
                }
            }
        });

        mMessageSendButton.setEnabled(false);
        mMessageSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentState == STATE_EDIT) {
                    String userInput = mMessageEditText.getText().toString();
                    if (userInput.length() > 0) {
                        if (mEditingMessage != null) {
                            editMessage(mEditingMessage, mMessageEditText.getText().toString());
                        }
                    }
                    setState(STATE_NORMAL, null, -1);
                } else {
                    String userInput = mMessageEditText.getText().toString();
                    if (userInput.length() > 0) {
                        sendUserMessage(userInput);
                        mMessageEditText.setText("");
                    }
                }
            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Set this as true to restart auto-background detection.
        // This means that you will be automatically disconnected from SendBird when your
        // app enters the background.
        SendBird.setAutoBackgroundDetection(true);
    }

    @Override
    public void onBackPressed() {

        if (mCurrentState == STATE_EDIT) {
            setState(STATE_NORMAL, null, -1);
            mIMM.hideSoftInputFromWindow(mMessageEditText.getWindowToken(), 0);
        }
        else {
            moveTaskToBack(true);
            Intent intent = new Intent(ChatActivity.this, ChatsListActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID, new ConnectionManager.ConnectionManagementHandler() {
            @Override
            public void onConnected(boolean reconnect) {
                if (reconnect) {
                    refresh();
                } else {
                    refreshFirst();
                }
            }
        });

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                // Add new message to view
                if (baseChannel.getUrl().equals(channelUrl)) {
                    mChatAdapter.addFirst(baseMessage);
                }
            }

            @Override
            public void onMessageDeleted(BaseChannel baseChannel, long msgId) {
                super.onMessageDeleted(baseChannel, msgId);
                if (baseChannel.getUrl().equals(channelUrl)) {
                    mChatAdapter.delete(msgId);
                }
            }

            @Override
            public void onMessageUpdated(BaseChannel channel, BaseMessage message) {
                super.onMessageUpdated(channel, message);
                if (channel.getUrl().equals(channelUrl)) {
                    mChatAdapter.update(message);
                }
            }
        });
        /*
        OpenChannel.getChannel(channelUrl, new OpenChannel.OpenChannelGetHandler() {
            @Override
            public void onResult(OpenChannel openChannel, SendBirdException e) {
                if (e != null) {
                    return;
                }

                mChannel = openChannel;

                openChannel.enter(new OpenChannel.OpenChannelEnterHandler() {
                    @Override
                    public void onResult(SendBirdException e) {
                        if (e != null) {
                            return;
                        }
                    }
                });


            }
        });*/
    }

    @Override
    public void onPause() {
        ConnectionManager.removeConnectionManagementHandler(CONNECTION_HANDLER_ID);
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
        super.onPause();
        /*OpenChannel.getChannel(channelUrl, new OpenChannel.OpenChannelGetHandler() {
            @Override
            public void onResult(OpenChannel openChannel, SendBirdException e) {
                if (e != null) {
                    // Error.
                    return;
                }

                openChannel.exit(new OpenChannel.OpenChannelExitHandler() {
                    @Override
                    public void onResult(SendBirdException e) {
                        if (e != null) {
                            // Error.
                            return;
                        }
                    }
                });
            }
        });*/
    }

    @Override
    public void onDestroy() {
        if (mChannel != null) {
            mChannel.exit(new OpenChannel.OpenChannelExitHandler() {
                @Override
                public void onResult(SendBirdException e) {
                    if (e != null) {
                        // Error!
                        e.printStackTrace();
                        return;
                    }
                }
            });
        }

        super.onDestroy();
    }

    private void setUpChatAdapter() {
        mChatAdapter = new OpenChatAdapter(this);
        mChatAdapter.setOnItemClickListener(new OpenChatAdapter.OnItemClickListener() {
            @Override
            public void onUserMessageItemClick(UserMessage message) {
            }

            @Override
            public void onAdminMessageItemClick(AdminMessage message) {
            }
        });

        mChatAdapter.setOnItemLongClickListener(new OpenChatAdapter.OnItemLongClickListener() {
            @Override
            public void onBaseMessageLongClick(final BaseMessage message, int position) {
                showMessageOptionsDialog(message, position);
            }
        });
    }

    private void showMessageOptionsDialog(final BaseMessage message, final int position) {
        String[] options = new String[] { "Edit message", "Delete message" };

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this); // getActivity() -> Chat.this
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    setState(STATE_EDIT, message, position);
                } else if (which == 1) {
                    deleteMessage(message);
                }
            }
        });
        builder.create().show();
    }

    private void setState(int state, BaseMessage editingMessage, final int position) {
        switch (state) {
            case STATE_NORMAL:
                mCurrentState = STATE_NORMAL;
                mEditingMessage = null;

                mMessageEditText.setText("");
                break;

            case STATE_EDIT:
                mCurrentState = STATE_EDIT;
                mEditingMessage = editingMessage;

                String messageString = ((UserMessage)editingMessage).getMessage();
                if (messageString == null) {
                    messageString = "";
                }
                mMessageEditText.setText(messageString);
                if (messageString.length() > 0) {
                    mMessageEditText.setSelection(0, messageString.length());
                }

                mMessageEditText.requestFocus();
                mMessageEditText.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIMM.showSoftInput(mMessageEditText, 0);

                        mRecyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mRecyclerView.scrollToPosition(position);
                            }
                        }, 500);
                    }
                }, 100);
                break;
        }
    }

    private void setUpRecyclerView() {
        mLayoutManager = new LinearLayoutManager(ChatActivity.this);
        mLayoutManager.setReverseLayout(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mChatAdapter);

        // Load more messages when user reaches the top of the current message list.
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                if (mLayoutManager.findLastVisibleItemPosition() == mChatAdapter.getItemCount() - 1) {
                    loadNextMessageList(CHANNEL_LIST_LIMIT);
                }
                Log.v("[setUpRecyclerView]", "onScrollStateChanged");
            }
        });
    }

    private void refreshFirst() {
        enterChannel(channelUrl);
    }

    /**
     * Enters an Open Channel.
     * <p>
     * A user must successfully enter a channel before being able to load or send messages
     * within the channel.
     *
     * @param channelUrl The URL of the channel to enter.
     */
    private void enterChannel(String channelUrl) {
        OpenChannel.getChannel(channelUrl, new OpenChannel.OpenChannelGetHandler() {
            @Override
            public void onResult(final OpenChannel openChannel, SendBirdException e) {
                if (e != null) {
                    // Error!
                    e.printStackTrace();
                    return;
                }

                // Enter the channel
                openChannel.enter(new OpenChannel.OpenChannelEnterHandler() {
                    @Override
                    public void onResult(SendBirdException e) {
                        if (e != null) {
                            // Error!
                            e.printStackTrace();
                            return;
                        }

                        mChannel = openChannel;

                        if (ChatActivity.this != null) {
                            // Set action bar title to name of channel
                            //setActionBarTitle(mChannel.getName());
                            getSupportActionBar().setTitle(mChannel.getName());
                        }

                        refresh();
                    }
                });
            }
        });
    }

    private void sendUserMessage(String text) {
        mChannel.sendUserMessage(text, new BaseChannel.SendUserMessageHandler() {
            @Override
            public void onSent(UserMessage userMessage, SendBirdException e) {
                if (e != null) {
                    // Error!
                    Log.e("[sendUserMessage]", e.toString());
                    Toast.makeText(
                            ChatActivity.this,
                            "Send failed with error " + e.getCode() + ": " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                // Display sent message to RecyclerView
                mChatAdapter.addFirst(userMessage);
            }
        });
    }

    private void refresh() {
        loadInitialMessageList(CHANNEL_LIST_LIMIT);
    }

    /**
     * Replaces current message list with new list.
     * Should be used only on initial load.
     */
    private void loadInitialMessageList(int numMessages) {

        mPrevMessageListQuery = mChannel.createPreviousMessageListQuery();
        mPrevMessageListQuery.load(numMessages, true, new PreviousMessageListQuery.MessageListQueryResult() {
            @Override
            public void onResult(List<BaseMessage> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    e.printStackTrace();
                    return;
                }

                mChatAdapter.setMessageList(list);
            }
        });

    }

    /**
     * Loads messages and adds them to current message list.
     * <p>
     * A PreviousMessageListQuery must have been already initialized through {@link #loadInitialMessageList(int)}
     */
    private void loadNextMessageList(int numMessages) throws NullPointerException {

        if (mChannel == null) {
            throw new NullPointerException("Current channel instance is null.");
        }

        if (mPrevMessageListQuery == null) {
            throw new NullPointerException("Current query instance is null.");
        }

        mPrevMessageListQuery.load(numMessages, true, new PreviousMessageListQuery.MessageListQueryResult() {
            @Override
            public void onResult(List<BaseMessage> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    e.printStackTrace();
                    return;
                }

                for (BaseMessage message : list) {
                    mChatAdapter.addLast((message));
                }
            }
        });
    }

    private void editMessage(final BaseMessage message, String editedMessage) {
        mChannel.updateUserMessage(message.getMessageId(), editedMessage, null, null, new BaseChannel.UpdateUserMessageHandler() {
            @Override
            public void onUpdated(UserMessage userMessage, SendBirdException e) {
                if (e != null) {
                    // Error!
                    Toast.makeText(ChatActivity.this, "Error " + e.getCode() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                refresh();
            }
        });
    }

    /**
     * Deletes a message within the channel.
     * Note that users can only delete messages sent by oneself.
     *
     * @param message The message to delete.
     */
    private void deleteMessage(final BaseMessage message) {
        mChannel.deleteMessage(message, new BaseChannel.DeleteMessageHandler() {
            @Override
            public void onResult(SendBirdException e) {
                if (e != null) {
                    // Error!
                    Toast.makeText(ChatActivity.this, "Error " + e.getCode() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                refresh();
            }
        });
    }
}
