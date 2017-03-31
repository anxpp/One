package com.anxpp.one.tinyim.event;

import android.util.Log;

import com.anxpp.one.activity.MainActivity;
import com.anxpp.one.tinyim.client.event.MessageQoSEvent;
import com.anxpp.one.tinyim.client.message.Message;

import java.util.ArrayList;

public class MessageQoSEventImpl implements MessageQoSEvent {
    private final static String TAG = MessageQoSEventImpl.class.getSimpleName();

    private MainActivity mainGUI = null;

    @Override
    public void messagesLost(ArrayList<Message> lostMessages) {
        Log.i(TAG,"messagesLost");
    }

    @Override
    public void messagesBeReceived(String theFingerPrint) {
        if (theFingerPrint != null) {
            Log.i(TAG,"messagesBeReceived");
        }
    }

    public MessageQoSEventImpl setMainGUI(MainActivity mainGUI) {
        this.mainGUI = mainGUI;
        return this;
    }
}
