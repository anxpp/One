package com.anxpp.one.tinyim.client.event;


import com.anxpp.one.tinyim.client.message.Message;

import java.util.ArrayList;

public interface MessageQoSEvent {
    void messagesLost(ArrayList<Message> paramArrayList);

    void messagesBeReceived(String paramString);
}