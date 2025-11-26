package com.team3.dto.request;

public class AddUserRequest {
    private final String userId;
    private final String password;
    private final String userName;
    private final String role;

    public AddUserRequest(String userId, String password, String userName, String role) {
        this.userId = userId;
        this.password = password;
        this.userName = userName;
        this.role = role;
    }
    
    public String getUserId() { return userId; }
    public String getPassword() { return password; }
    public String getUserName() {return userName; }
    public String getRole() {return role;}
}
