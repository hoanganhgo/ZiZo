package com.example.zizo.object;

public class ChatBox {

    private String avatar;
    private boolean online;
    private String nickName;
    private String message;
    private boolean isNew;
    private long timeOfMessage;

    public ChatBox(String avatar, boolean online, String nickName, String message, boolean isNew, long timeOfMessage)
    {
        this.avatar=avatar;
        this.online=online;
        this.nickName=nickName;
        this.message=message;
        this.isNew = isNew;
        this.timeOfMessage = timeOfMessage;
    }


    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public long getTimeOfMessage() {
        return timeOfMessage;
    }

    public void setTimeOfMessage(long timeOfMessage) {
        this.timeOfMessage = timeOfMessage;
    }
}
