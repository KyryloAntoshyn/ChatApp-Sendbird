package com.example.akiri.chatapp;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

import com.example.akiri.chatapp.utils.PreferenceUtils;

/**
 * Created by SendBird
 * https://github.com/smilefam/SendBird-Android
 */

public class ConnectionManager {
    public static void login(String userId, String accessToken, final SendBird.ConnectHandler handler) {
        SendBird.connect(userId, accessToken, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                if (handler != null) {
                    handler.onConnected(user, e);
                }
            }
        });
    }

    public static void logout(final SendBird.DisconnectHandler handler) {
        SendBird.disconnect(new SendBird.DisconnectHandler() {
            @Override
            public void onDisconnected() {
                if (handler != null) {
                    handler.onDisconnected();
                }
            }
        });
    }

    public static void addConnectionManagementHandler(String handlerId, final ConnectionManagementHandler handler) {
        SendBird.addConnectionHandler(handlerId, new SendBird.ConnectionHandler() {
            @Override
            public void onReconnectStarted() {
            }

            @Override
            public void onReconnectSucceeded() {
                if (handler != null) {
                    handler.onConnected(true);
                }
            }

            @Override
            public void onReconnectFailed() {
            }
        });

        if (SendBird.getConnectionState() == SendBird.ConnectionState.OPEN) {
            if (handler != null) {
                handler.onConnected(false);
            }
        }
        else if (SendBird.getConnectionState() == SendBird.ConnectionState.CLOSED) { // push notification or system kill
            String userId = PreferenceUtils.getUserId();
            String token = PreferenceUtils.getToken();
            SendBird.connect(userId, token, new SendBird.ConnectHandler() {
                @Override
                public void onConnected(User user, SendBirdException e) {
                    if (e != null) {
                        return;
                    }

                    if (handler != null) {
                        handler.onConnected(false);
                    }
                }
            });
        }
    }

    public static void removeConnectionManagementHandler(String handleId) {
        SendBird.removeConnectionHandler(handleId);
    }

    public interface ConnectionManagementHandler {
        /**
         * A callback for when connected or reconnected to refresh.
         *
         * @param reconnect Set false if connected, true if reconnected.
         */
        void onConnected(boolean reconnect);
    }
}
