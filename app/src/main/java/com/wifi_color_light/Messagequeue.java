package com.wifi_color_light;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by chenguihui on 2017/11/8.
 * Email: 494723324@qq.com
 */

public class Messagequeue extends Message {
    private static List<Message> messageList = new LinkedList<Message>();

    public static Message readMessage(int index) {
        Message message = messageList.get(index);
        return message;
    }

    public static boolean addMessage(Message message) {
        return messageList.add(message);
    }

    public static Message removeMessage(int index) {
        return messageList.remove(index);
    }
    public static int messagequeueSize(){
        return messageList.size();
    }
    private Messagequeue() {

    }

}

