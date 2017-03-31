package com.anxpp.one.tinyim.event;

import android.util.Log;

import com.anxpp.one.activity.MainActivity;
import com.anxpp.one.tinyim.client.event.ChatBaseEvent;

import java.util.Observer;

public class ChatBaseEventImpl implements ChatBaseEvent {
    private final static String TAG = ChatBaseEventImpl.class.getSimpleName();

    private MainActivity mainGUI = null;

    // 本Observer目前仅用于登陆时（因为登陆与收到服务端的登陆验证结果
    // 是异步的，所以有此观察者来完成收到验证后的处理）
    private Observer loginOkForLaunchObserver = null;

    @Override
    public void onLoginMessage(int dwUserId, int dwErrorCode) {
        if (dwErrorCode == 0) {
                Log.i(TAG,"onLoginMessage:success");
        } else {
            Log.i(TAG,"onLoginMessage:failed:"+dwErrorCode);
        }

        // 此观察者只有开启程序首次使用登陆界面时有用
        if (loginOkForLaunchObserver != null) {
            loginOkForLaunchObserver.update(null, dwErrorCode);
            loginOkForLaunchObserver = null;
        }
    }

    @Override
    public void onLinkCloseMessage(int dwErrorCode) {
        Log.i(TAG,"onLinkCloseMessage::"+dwErrorCode);
    }

    public void setLoginOkForLaunchObserver(Observer loginOkForLaunchObserver) {
        this.loginOkForLaunchObserver = loginOkForLaunchObserver;
    }

    public ChatBaseEventImpl setMainGUI(MainActivity mainGUI) {
        this.mainGUI = mainGUI;
        return this;
    }
}
