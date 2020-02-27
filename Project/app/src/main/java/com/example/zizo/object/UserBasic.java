package com.example.zizo.object;

public class UserBasic {

    private String avatar;
    private long time;
    private String nickName;
    private String email;

    public UserBasic(String email, String avatar, long time, String nickName)
    {
        this.avatar=avatar;
        this.time=time;
        this.nickName=nickName;
        this.email=email;
    }

    public UserBasic(String email, String avatar, String nickName)
    {
        this.email=email;
        this.avatar=avatar;
        this.nickName=nickName;
        this.time=0;
    }


    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
