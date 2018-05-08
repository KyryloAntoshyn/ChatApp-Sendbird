package com.example.akiri.chatapp;

import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.akiri.chatapp.utils.PreferenceUtils;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button loginButton;
    private TextView textViewLogin, textViewPassword;
    private ContentLoadingProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplicationContext();

        setContentView(R.layout.activity_main);

        textViewLogin = (TextView) findViewById(R.id.textViewLogin);
        textViewPassword = (TextView) findViewById(R.id.textViewPassword);

        loginButton = (Button) findViewById(R.id.buttonLogIn);
        loginButton.setOnClickListener(this);

        progressBar = (ContentLoadingProgressBar) findViewById(R.id.progressBar);
        changeProgressBarState(false); // Hide loading
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.buttonLogIn:
                String userId = textViewLogin.getText().toString();
                String token = textViewPassword.getText().toString();
                PreferenceUtils.setUserId(userId);
                PreferenceUtils.setNickname(userId);
                PreferenceUtils.setToken(token);

                connectToSendBird(userId, token);
                break;
            default:
                // Other buttons to add
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(PreferenceUtils.getConnected()) {
            connectToSendBird(PreferenceUtils.getUserId(), PreferenceUtils.getToken());
        }
    }

    private void showChatListActivity(){
        Intent intent = new Intent(this, ChatsListActivity.class);
        startActivity(intent);
    }

    private void changeProgressBarState(boolean isActive){
        if (isActive)
            progressBar.show();
        else
            progressBar.hide();
    }

    private void connectToSendBird(String userId, String token){
        changeProgressBarState(true); // Show loading

        ConnectionManager.login(userId, token, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                // Callback received
                changeProgressBarState(false); // Hide loading

                if (e != null){
                    // Error
                    Toast.makeText(MainActivity.this, "" + e.getCode() + ": " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    loginButton.setEnabled(true);
                    PreferenceUtils.setConnected(false);
                    return;
                }

                PreferenceUtils.setNickname(user.getNickname());
                PreferenceUtils.setConnected(true);

                showChatListActivity();
                finish();
            }
        });
    }
}
