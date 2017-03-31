package com.anxpp.one.tinyim.event;

import android.util.Log;

import com.anxpp.one.activity.MainActivity;
import com.anxpp.one.tinyim.client.event.ChatTransDataEvent;

public class ChatTransDataEventImpl implements ChatTransDataEvent {
    private final static String TAG = ChatTransDataEventImpl.class.getSimpleName();

    private MainActivity mainGUI = null;

    @Override
    public void onTransBuffer(String fingerPrintOfProtocal, int dwUserid, String dataContent) {
        Log.i(TAG,"onTransBuffer");
    }

    public ChatTransDataEventImpl setMainGUI(MainActivity mainGUI) {
        this.mainGUI = mainGUI;
        return this;
    }

    @Override
    public void onErrorResponse(int errorCode, String errorMsg) {
        Log.i(TAG,"onErrorResponse");
    }
}
