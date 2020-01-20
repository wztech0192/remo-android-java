package com.wztechs.remo.service.broadcast;

public interface BroadCastListener {
    void OnReceivedMessage(String msg, String address);
    void OnError(Exception e);
}
