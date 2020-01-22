package com.example.zizo.adapter;

public class UserBasic {

    private String avatar;
    private boolean online;
    private String nickName;

    public UserBasic(String avatar, boolean online, String nickName)
    {
        this.avatar=avatar;
        this.online=online;
        this.nickName=nickName;
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
}
