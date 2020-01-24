package com.example.zizo.object;

public class ChatBox {

    private String avatar;
    private boolean online;
    private String nickName;
    private String message;

    public ChatBox(String avatar, boolean online, String nickName, String message)
    {
        this.avatar=avatar;
        this.online=online;
        this.nickName=nickName;
        this.message=message;
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
}
