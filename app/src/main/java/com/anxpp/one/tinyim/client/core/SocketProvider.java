package com.anxpp.one.tinyim.client.core;

import android.util.Log;

import com.anxpp.one.tinyim.client.ClientCoreSDK;
import com.anxpp.one.tinyim.client.conf.ConfigEntity;

import java.net.DatagramSocket;

public class SocketProvider {
    private static final String TAG = SocketProvider.class.getSimpleName();
    private static SocketProvider instance = null;
    private DatagramSocket localUDPSocket = null;

    public static SocketProvider getInstance() {
        if (instance == null)
            instance = new SocketProvider();
        return instance;
    }

    private DatagramSocket resetLocalUDPSocket() {
        try {
            closeLocalUDPSocket();
            if (ClientCoreSDK.DEBUG)
                Log.d(TAG, "【IMCORE】new DatagramSocket()中...");
            this.localUDPSocket = (ConfigEntity.localUDPPort == 0 ?
                    new DatagramSocket() : new DatagramSocket(ConfigEntity.localUDPPort));
            this.localUDPSocket.setReuseAddress(true);
            if (ClientCoreSDK.DEBUG) {
                Log.d(TAG, "【IMCORE】new DatagramSocket()已成功完成.");
            }

            return this.localUDPSocket;
        } catch (Exception e) {
            Log.w(TAG, "【IMCORE】localUDPSocket创建时出错，原因是：" + e.getMessage(), e);

            closeLocalUDPSocket();
            return null;
        }
    }

    private boolean isLocalUDPSocketReady() {
        return (this.localUDPSocket != null) && (!this.localUDPSocket.isClosed());
    }

    DatagramSocket getLocalUDPSocket() {
        if (isLocalUDPSocketReady()) {
            if (ClientCoreSDK.DEBUG)
                Log.d(TAG, "【IMCORE】isLocalUDPSocketReady()==true，直接返回本地socket引用哦。");
            return this.localUDPSocket;
        }

        if (ClientCoreSDK.DEBUG)
            Log.d(TAG, "【IMCORE】isLocalUDPSocketReady()==false，需要先resetLocalUDPSocket()...");
        return resetLocalUDPSocket();
    }

    public void closeLocalUDPSocket() {
        try {
            if (ClientCoreSDK.DEBUG)
                Log.d(TAG, "【IMCORE】正在closeLocalUDPSocket()...");
            if (this.localUDPSocket != null) {
                this.localUDPSocket.close();
                this.localUDPSocket = null;
            } else {
                Log.d(TAG, "【IMCORE】Socket处于未初化状态（可能是您还未登陆），无需关闭。");
            }
        } catch (Exception e) {
            Log.w(TAG, "【IMCORE】lcloseLocalUDPSocket时出错，原因是：" + e.getMessage(), e);
        }
    }
}