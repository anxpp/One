package com.anxpp.one.tinyim.client.event;

public interface ChatBaseEvent {
    void onLoginMessage(int paramInt1, int paramInt2);

    void onLinkCloseMessage(int paramInt);
}