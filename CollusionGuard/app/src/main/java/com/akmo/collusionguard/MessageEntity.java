package com.akmo.collusionguard;

import java.util.ArrayList;

public class MessageEntity {

    public final String sender;
    public final String receiver;
    public ArrayList<String> dataList;

    MessageEntity(String sender, String receiver) {
        this.sender = sender;
        this.receiver = receiver;
        dataList = new ArrayList<>();
    }

    public int hashCode() {
        return sender.concat(receiver).hashCode();
    }
}
