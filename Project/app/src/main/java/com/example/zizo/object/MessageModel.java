package com.example.zizo.object;

public class MessageModel {
    private int sender;   //user1: sender=1   user2=: sender=2
    private long time;
    private String content;

    public MessageModel(int sender, long time, String content)
    {
        this.sender=sender;
        this.time=time;
        this.content=content;
    }

    public MessageModel()
    {

    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }
}
