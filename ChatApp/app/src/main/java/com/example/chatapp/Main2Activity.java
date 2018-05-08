package com.example.chatapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ArrayAdapter;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.OpenChannelListQuery;
import com.sendbird.android.SendBirdException;

import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends AppCompatActivity {


    List <String> names = new ArrayList<>();//список имен чатов
    private ListView lvMain;//список чатов
    private ArrayAdapter<String> adapter;
    private List <BaseChannel> listChannels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {//создание активити
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        lvMain = (ListView) findViewById(R.id.chats);//находим наш лист вью по айди

        // создаем адаптер
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, names);

        // присваиваем адаптер списку
        lvMain.setAdapter(adapter);

        OpenChannelListQuery channelListQuery = OpenChannel.createOpenChannelListQuery();//список наших открытых каналов
        channelListQuery.next(new OpenChannelListQuery.OpenChannelListQueryResultHandler() {
            @Override
            public void onResult(List<OpenChannel> channels, SendBirdException e) {
                if (e != null) {
                    // Error.
                    return;
                }

                listChannels.addAll(channels);//получаем названия
                names.addAll(getChannelsNames(channels));//наших чатов из сэндберда (браузер)
                lvMain.setAdapter(adapter);
            }
        });
        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {//нажатие на любой чат (канал)
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                openChat(i);
            }
        });

    }

    private void openChat(int i) {//при нажатии на любой чат
        Intent intent = new Intent(this, ChatActivity.class);//открывает третью активити
        intent.putExtra("CHANNEL_URL", listChannels.get(i).getUrl());
        intent.putExtra("CHANNEL_NAME", listChannels.get(i).getName());//по нему определяем название нашего чата, это нам надо для последующего поиска нашего чата на других активити (это как ключ, по котор. осуществл. поиск)
        startActivity(intent);

    }


    private List <String> getChannelsNames(List<OpenChannel> channels) {
        List <String> names_ = new ArrayList<>();
        for (OpenChannel channel: channels){
            names_.add(channel.getName());
        }

        return names_;
    }


}
