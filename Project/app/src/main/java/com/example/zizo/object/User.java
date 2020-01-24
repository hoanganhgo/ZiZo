package com.example.zizo.object;

import java.util.ArrayList;
import java.util.Date;

public class User {
    private String email;
    private String nickName;
    private String avatar;
    private String dateOfBirth;
    private String sex;
    private long realTime;
    private ArrayList<String> friends;
    private ArrayList<String> follows;

    private ArrayList<String> invitation;    //Lời mời kết bạn dành cho bạn
    private ArrayList<String> chatBox;
    private ArrayList<String> status;

    public User(String email, String nickName, String avatar, String dateOfBirth, String sex, long realTime, ArrayList<String> friends, ArrayList<String> follows, ArrayList<String> invitation) {
        this.email = email;
        this.nickName = nickName;
        this.avatar = avatar;
        this.dateOfBirth = dateOfBirth;
        this.sex = sex;
        this.realTime=realTime;
        this.friends = friends;
        this.follows = follows;
        this.invitation=invitation;
    }

    public String getEmail(){return email;}
    public void setEmail(String email){this.email=email;}

    public String getNickName(){return this.nickName;}
    public void setNickName(String nickName){this.nickName=nickName;}

    public String getAvatar(){return this.avatar;}
    public void setAvatar(String avatar){this.avatar=avatar;}

    public String getDateOfBirth(){return this.dateOfBirth;}
    public void setDateOfBirth(String dateOfBirth){this.dateOfBirth=dateOfBirth;}

    public String getSex(){return this.sex;}
    public void setSex(String sex){this.sex=sex;}

    public long getRealTime(){return this.realTime;}
    public void setRealTime(long realTime){this.realTime=realTime;}

    public ArrayList<String> getFriends(){return this.friends;}
    public void setFriends(ArrayList<String> friends){this.friends=friends;}

    public ArrayList<String> getFollows(){return this.follows;}
    public void setFollows(ArrayList<String> follows){this.follows=follows;}

    public ArrayList<String> getInvitation(){return this.invitation;}
    public void setInvitation(ArrayList<String> invitation){this.invitation=invitation;}

    public void addFriend(String email)
    {
        this.friends.add(email);
    }
    public void removeFriend(String email)
    {
        this.friends.remove(email);
    }

    public void addFollow(String email)
    {
        this.follows.add(email);
    }
    public void removeFollow(String email)
    {
        this.follows.remove(email);
    }

    public void addInvitation(String email)
    {
        this.invitation.add(email);
    }
    public void removeInvitation(String email)
    {
        this.invitation.remove(email);
    }

    public ArrayList<String> getChatBox() {
        return chatBox;
    }

    public void setChatBox(ArrayList<String> chatBox) {
        this.chatBox = chatBox;
    }

    public ArrayList<String> getStatus() {
        return status;
    }

    public void setStatus(ArrayList<String> status) {
        this.status = status;
    }
}
