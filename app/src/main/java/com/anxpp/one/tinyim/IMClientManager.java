package com.anxpp.one.tinyim;

import android.content.Context;

import com.anxpp.one.tinyim.client.ClientCoreSDK;
import com.anxpp.one.tinyim.client.conf.ConfigEntity;
import com.anxpp.one.tinyim.event.ChatBaseEventImpl;
import com.anxpp.one.tinyim.event.ChatTransDataEventImpl;
import com.anxpp.one.tinyim.event.MessageQoSEventImpl;

public class IMClientManager {
    private static String TAG = IMClientManager.class.getSimpleName();

    private static IMClientManager instance = null;

    /**
     * MobileIMSDK是否已被初始化. true表示已初化完成，否则未初始化.
     */
    private boolean init = false;

    //
    private ChatBaseEventImpl baseEventListener = null;
    //
    private ChatTransDataEventImpl transDataListener = null;
    //
    private MessageQoSEventImpl messageQoSListener = null;

    private Context context = null;

    private IMClientManager(Context context) {
        this.context = context;
        initMobileIMSDK();
    }

    public static IMClientManager getInstance(Context context) {
        if (instance == null)
            instance = new IMClientManager(context);
        return instance;
    }

    public void initMobileIMSDK() {
        if (!init) {
            // 设置AppKey
            ConfigEntity.appKey = "tinyim";

            // 设置服务器ip和服务器端口
            ConfigEntity.serverIP = "10.0.2.2";
            ConfigEntity.serverUDPPort = 1114;

            // MobileIMSDK核心IM框架的敏感度模式设置
//			ConfigEntity.setSenseMode(SenseMode.MODE_10S);

            // 开启/关闭DEBUG信息输出
            ClientCoreSDK.DEBUG = false;

            // 【特别注意】请确保首先进行核心库的初始化（这是不同于iOS和Java端的地方)
            ClientCoreSDK.getInstance().init(this.context);

            // 设置事件回调
            baseEventListener = new ChatBaseEventImpl();
            transDataListener = new ChatTransDataEventImpl();
            messageQoSListener = new MessageQoSEventImpl();
            ClientCoreSDK.getInstance().setChatBaseEvent(baseEventListener);
            ClientCoreSDK.getInstance().setChatTransDataEvent(transDataListener);
            ClientCoreSDK.getInstance().setMessageQoSEvent(messageQoSListener);

            init = true;
        }
    }

    public void release() {
        ClientCoreSDK.getInstance().release();
    }

    public ChatTransDataEventImpl getTransDataListener() {
        return transDataListener;
    }

    public ChatBaseEventImpl getBaseEventListener() {
        return baseEventListener;
    }

    public MessageQoSEventImpl getMessageQoSListener() {
        return messageQoSListener;
    }
}
