package com.sashafilth.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RedisAOFReceiver implements RedisReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisAOFReceiver.class);
    public static List<String> messageList = new ArrayList<String>();

    @Override
    public void receiveMessage(String message) {
        messageList.add(message);
    }
}
