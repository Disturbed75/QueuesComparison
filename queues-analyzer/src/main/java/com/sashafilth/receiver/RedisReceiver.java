package com.sashafilth.receiver;

public interface RedisReceiver {

    void receiveMessage(String message);
}
