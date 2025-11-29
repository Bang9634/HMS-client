package com.team3.dto.request;

public class AddCustomerRequest {
    private String name;
    private String phoneNumber;
    private String roomNumber;
    private String feedback;

    public AddCustomerRequest(String name, String phoneNumber, String roomNumber, String feedback) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.roomNumber = roomNumber;
        this.feedback = feedback;
    }

    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getRoomNumber() { return roomNumber; }
    public String getFeedback() { return feedback; }
}
