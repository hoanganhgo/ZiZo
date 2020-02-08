package com.example.zizo.object;

public class UserBasic {

    private String avatar;
    private boolean online;
    private String nickName;
    private String email;

    public UserBasic(String avatar, boolean online, String nickName)
    {
        this.avatar=avatar;
        this.online=online;
        this.nickName=nickName;
    }

    public UserBasic(String email, String avatar, String nickName)
    {
        this.email=email;
        this.avatar=avatar;
        this.nickName=nickName;
        this.online=false;
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
