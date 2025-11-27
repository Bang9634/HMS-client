package com.team3.dto.request;

public class DeleteUserRequest {
    private String userId;

    public DeleteUserRequest(String userId) {
        this.userId = userId;
    }

    public String getUserId() { return userId; }
}
