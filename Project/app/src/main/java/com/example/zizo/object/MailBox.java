package com.example.zizo.object;

public class MailBox {
    private String id;
    private String finalMessage;
    private long timeOfFinalMessage;
    private int user1Viewed;
    private int user2Viewed;

    public MailBox(){

    }

    public MailBox(String id, String finalMessage, long timeOfFinalMessage, int user1Viewed, int user2Viewed) {
        this.id = id;
        this.finalMessage = finalMessage;
        this.timeOfFinalMessage = timeOfFinalMessage;
        this.user1Viewed = user1Viewed;
        this.user2Viewed = user2Viewed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFinalMessage() {
        return finalMessage;
    }

    public void setFinalMessage(String finalMessage) {
        this.finalMessage = finalMessage;
    }

    public long getTimeOfFinalMessage() {
        return timeOfFinalMessage;
    }

    public void setTimeOfFinalMessage(long timeOfFinalMessage) {
        this.timeOfFinalMessage = timeOfFinalMessage;
    }

    public int getUser1Viewed() {
        return user1Viewed;
    }

    public void setUser1Viewed(int user1Viewed) {
        this.user1Viewed = user1Viewed;
    }

    public int getUser2Viewed() {
        return user2Viewed;
    }

    public void setUser2Viewed(int user2Viewed) {
        this.user2Viewed = user2Viewed;
    }
}
