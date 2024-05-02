package com.android.wifirc.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.wifirc.event.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Objects;

public class WifiCommunicationService extends Service {
    private static final String TAG = WifiCommunicationService.class.getSimpleName();
    public static final String NULL_CONNECTION = "NULL_CONNECTION";
    public static final String CONNECTION_SUCCEED = "CONNECTION_SUCCEED";
    public static final String CONNECTION_FAILED = "CONNECTION_FAILED";
    public static final String NOT_CONNECTED = "NOT_CONNECTED";
    public static final String SENDING_SUCCEED = "SEND_SUCCEED";
    public static final String CLOSED = "CLOSED";

    private final IBinder iBinder = new MyBinder();
    private Socket socket;
    private String host;
    private int port;

    public WifiCommunicationService() {
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
        // 注册EventBus
        EventBus.getDefault().register(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return iBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG, "onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
        // 反注册EventBus
        EventBus.getDefault().unregister(this);
    }

    public class MyBinder extends Binder {
        public WifiCommunicationService getService() {
            return WifiCommunicationService.this;
        }
    }

    public void connect(String host, int port, boolean keepAlive) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(host, port);
                    socket.setKeepAlive(keepAlive);
                    EventBus.getDefault().post(new MessageEvent(CONNECTION_SUCCEED));
                } catch (IOException e) {
                    EventBus.getDefault().post(new MessageEvent(CONNECTION_FAILED, e.getMessage()));
                }
            }
        }).start();
    }

    public void sendData(String data) {
        if (socket == null) {
            EventBus.getDefault().post(new MessageEvent(NULL_CONNECTION));
        } else if (socket.isClosed() || socket.isOutputShutdown()) {
            try {
                socket = new Socket(host, port);
            } catch (IOException e) {
                EventBus.getDefault().post(new MessageEvent(CONNECTION_SUCCEED));
            }
        } else {
            try {
                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                dataOutputStream.write(data.getBytes());
                dataOutputStream.flush();
            } catch (IOException e) {
                EventBus.getDefault().post(new MessageEvent("IOException", e.getMessage()));
            }
        }
    }

    public void sendData(String host, int port, String data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(host, port);
                    EventBus.getDefault().post(new MessageEvent(CONNECTION_SUCCEED));
                    OutputStream outputStream = socket.getOutputStream();
                    DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                    dataOutputStream.write(data.getBytes());
                    dataOutputStream.flush();
                    EventBus.getDefault().post(new MessageEvent(SENDING_SUCCEED));
                    socket.close();
                    EventBus.getDefault().post(new MessageEvent(CLOSED));
                } catch (IOException e) {
                    EventBus.getDefault().post(new MessageEvent(CONNECTION_FAILED, e.getMessage()));
                }
            }
        }).start();
    }

    @Nullable
    public String receiveData(int length) {
        if (socket != null && !socket.isClosed() && !socket.isInputShutdown()) {
            try {
                InputStream inputStream = socket.getInputStream();
                byte[] buffer = new byte[length];
                int bytesRead = inputStream.read(buffer);
                return new String(buffer, 0, bytesRead);
            } catch (IOException e) {
                Log.i(TAG, Objects.requireNonNull(e.getMessage()));
            }
        }
        return null;
    }

    public void close() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // 处理EventBus事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleEvent(MessageEvent messageEvent) {
    }
}