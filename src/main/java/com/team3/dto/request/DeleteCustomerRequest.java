package com.team3.dto.request;

public class DeleteCustomerRequest {
    private String id;

    public DeleteCustomerRequest(String id) {
        this.id = id;
    }

    public String getId() { return id; }
}
