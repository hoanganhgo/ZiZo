package com.example.zizo.object;

import java.util.ArrayList;

public class Status {

    private String email;
    private String content;
    private String image;
    private long dateTime;
    private ArrayList<String> likes;
    private ArrayList<Comment> comments;

    public Status(String email, String content, String image, long dateTime, ArrayList<String> likes, ArrayList<Comment> comments) {
        this.email = email;
        this.content = content;
        this.image = image;
        this.dateTime = dateTime;
        this.likes = likes;
        this.comments = comments;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public ArrayList<String> getLikes() {
        return likes;
    }

    public void setLikes(ArrayList<String> likes) {
        this.likes = likes;
    }

    public void addLike(String email)
    {
        this.likes.add(email);
    }

    public void removeLike(String email)
    {
        this.likes.remove(email);
    }

    public void initLikes()
    {
        this.likes=new ArrayList<String>();
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }
}
