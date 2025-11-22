package com.team3.model;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String pw;
    private String name;
    private String role; // "ADMIN" or "USER"

    // 전체 생성자
    public User(String id, String pw, String name, String role) {
        this.id = id;
        this.pw = pw;
        this.name = name;
        this.role = role;
    }
    
    // 로그인용 간편 생성자
    public User(String id, String pw) {
        this(id, pw, null, null);
    }

    public String getId() { return id; }
    public String getPw() { return pw; }
    public String getName() { return name; }
    public String getRole() { return role; }
    
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return name + "(" + id + ") - " + role;
    }
}