package com.example.zizo.object;

public class Comment {

    private String email;
    private String content;


    public Comment(String email, String content) {
        this.email = email;
        this.content = content;
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
}
