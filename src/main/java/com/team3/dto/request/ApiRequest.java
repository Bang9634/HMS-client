package com.team3.dto.request;

import com.team3.session.SessionManager;

public class ApiRequest {
    private final String role;

    
    public ApiRequest() {
        this.role = SessionManager.getInstance().getRole();
    }
    
    public String getRole() { return role; }
}