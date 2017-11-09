package com.wifi_color_light;

class MessageGroup {
    public static final int WIFI = 0;


};

class WifiMessageId {
    public static final int connection = 0;

}

public class Message {
    public int group = 0;
    public int id = 0;
    public Object object = null;

    public Message(int group, int id, Object object) {
        this.group = group;
        this.id = id;
        this.object = object;
    }

    public Message() {

    }
}
