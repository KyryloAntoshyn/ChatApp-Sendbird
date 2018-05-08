package com.example.akiri.chatapp;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.akiri.chatapp.utils.PreferenceUtils;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.OpenChannelListQuery;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;

import java.util.ArrayList;
import java.util.List;

public class ChatsListActivity extends AppCompatActivity {

    private ChatListAdapter mChannelListAdapter;
    private SwipeRefreshLayout mSwipeRefresh;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    private static final int CHANNEL_LIST_LIMIT = 15;
    private static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_OPEN_CHANNEL_LIST";

    private OpenChannelListQuery mChannelListQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats_list);
        // находим список
        mRecyclerView = (RecyclerView) findViewById(R.id.ChatListView);
        mRecyclerView.setHasFixedSize(true); // Optimise if size would not be changed
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        // создаем адаптер
        mChannelListAdapter = new ChatListAdapter(this);
        mRecyclerView.setAdapter(mChannelListAdapter);


        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_layout_open_channel_list);

        // Swipe down to refresh channel list.
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefresh.setRefreshing(true);
                refresh();
            }
        });



        setUpRecyclerView();
        setUpChannelListAdapter();
    }

    @Override
    public void onResume() {
        super.onResume();

        ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID, new ConnectionManager.ConnectionManagementHandler() {
            @Override
            public void onConnected(boolean reconnect) {
                refresh();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        ConnectionManager.removeConnectionManagementHandler(CONNECTION_HANDLER_ID);
    }

    void setUpRecyclerView() {
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mChannelListAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // If user scrolls to bottom of the list, loads more channels.
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (mLayoutManager.findLastVisibleItemPosition() == mChannelListAdapter.getItemCount() - 1) {
                    loadNextChannelList();
                }
            }
        });
    }

    // Set touch listeners to RecyclerView items
    private void setUpChannelListAdapter() {
        mChannelListAdapter.setOnItemClickListener(new ChatListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(OpenChannel channel) {
                // todo open activity with chat
                String channelUrl = channel.getUrl();
                Intent intent = new Intent(ChatsListActivity.this, ChatActivity.class);
                intent.putExtra("channelurl", channelUrl);
                startActivity(intent);
                finish();
            }
        });

        mChannelListAdapter.setOnItemLongClickListener(new ChatListAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongPress(OpenChannel channel) {
            }
        });
    }

    private void refresh() {
        refreshChannelList(CHANNEL_LIST_LIMIT);
    }

    /**
     * Creates a new query to get the list of the user's Open Channels,
     * then replaces the existing dataset.
     *
     * @param numChannels   The number of channels to load.
     */
    void refreshChannelList(int numChannels) {
        mChannelListQuery = OpenChannel.createOpenChannelListQuery();
        mChannelListQuery.setLimit(numChannels);
        mChannelListQuery.next(new OpenChannelListQuery.OpenChannelListQueryResultHandler() {
            @Override
            public void onResult(List<OpenChannel> list, SendBirdException e) {
                if (e != null) {
                    e.printStackTrace();
                    return;
                }

                mChannelListAdapter.setOpenChannelList(list);

                if (mSwipeRefresh.isRefreshing()) {
                    mSwipeRefresh.setRefreshing(false);
                }
            }
        });
    }

    /**
     * Loads the next channels from the current query instance.
     */
    void loadNextChannelList() {
        if (mChannelListQuery != null) {
            mChannelListQuery.next(new OpenChannelListQuery.OpenChannelListQueryResultHandler() {
                @Override
                public void onResult(List<OpenChannel> list, SendBirdException e) {
                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }

                    for (OpenChannel channel : list) {
                        mChannelListAdapter.addLast(channel);
                    }
                }
            });
        }
    }

    public void disconnect() {
        ConnectionManager.logout(new SendBird.DisconnectHandler() {
            @Override
            public void onDisconnected() {
                PreferenceUtils.setConnected(false);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_main:
                disconnect();
                return true;
        }
        return false;
    }
}
