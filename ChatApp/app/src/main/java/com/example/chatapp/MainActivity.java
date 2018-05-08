package com.example.chatapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;//kak exception
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

public class MainActivity extends AppCompatActivity {

    private android.widget.Button logInButton;
    private EditText mLogin, mNickname;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ///////////////////////////////////
        mLogin = findViewById(R.id.login);
        mNickname = findViewById(R.id.nickname);
        ///////////////////////////////////
        logInButton = (android.widget.Button) findViewById(R.id.logInB);
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChatListActivity();
            }
        });
        SendBird.init("7707571A-FC03-4B73-95D8-01D6B061AD96", getApplicationContext());//чтоб получать все чаты, которые у нас в сб
        }


        void showChatListActivity()
        {
            String userID = mLogin.getText().toString();
            final String nickName = mNickname.getText().toString();
            SendBird.connect(userID, new SendBird.ConnectHandler() {
                @Override
                public void onConnected(User user, SendBirdException e) {

                    if (e != null) { //получается ли коннектиться или нет
                        // Error!
                        Toast.makeText(
                                MainActivity.this, "" + e.getCode() + ": " + e.getMessage(),
                                Toast.LENGTH_SHORT)
                                .show();

                        // Show login failure snackbar
                        return;
                    }

                    SendBird.updateCurrentUserInfo(nickName, null, new SendBird.UserInfoUpdateHandler() {
                        @Override
                        public void onUpdated(SendBirdException e) {
                            if (e != null) {
                                // Error!
                                Toast.makeText(
                                        MainActivity.this, "" + e.getCode() + ":" + e.getMessage(),
                                        Toast.LENGTH_SHORT)
                                        .show();

                                return;
                            }

                        }
                    });

                    Intent intent = new Intent(MainActivity.this, Main2Activity.class);//intent - то, с помощью чего мы вызываем нашу активити
                    startActivity(intent);
                    finish();
                }
            });
        }
    }
