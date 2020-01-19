package com.example.zizo;

import java.util.ArrayList;
import java.util.Date;

public class Class_User {
    private String email;
    private String nickName;
    private String avatar;
    private String dateOfBirth;
    private String sex;
    private ArrayList<String> friends;
    private ArrayList<String> follows;

    public Class_User(String email, String nickName, String avatar, String dateOfBirth, String sex, ArrayList<String> friends, ArrayList<String> follows) {
        this.email = email;
        this.nickName = nickName;
        this.avatar = avatar;
        this.dateOfBirth = dateOfBirth;
        this.sex = sex;
        this.friends = friends;
        this.follows = follows;
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

    public ArrayList<String> getFriends(){return this.friends;}
    public void setFriends(ArrayList<String> friends){this.friends=friends;}

    public ArrayList<String> getFollows(){return this.follows;}
    public void setFollows(ArrayList<String> follows){this.follows=follows;}

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
}
