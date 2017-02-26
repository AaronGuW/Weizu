package com.demo.aaronapplication.weizu;

/**
 * Created by Aaron on 2016/3/25.
 */
public class myMessage {
    private String content;
    private User sender;

    public myMessage() {}
    public myMessage(String content, User sender) {
        this.content = content;
        this.sender = sender;
    }

    public String getContent() { return content; }
    public User getSender() { return sender; }
}
