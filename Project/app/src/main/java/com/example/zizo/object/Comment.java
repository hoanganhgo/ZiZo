package com.example.zizo.object;

public class Comment {

    private String email;
    private long time;
    private String content;


    public Comment(String email, long time, String content) {
        this.email = email;
        this.time=time;
        this.content = content;
    }

    public Comment(){
        this.email=null;
        this.time=0;
        this.content=null;
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

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
