package com.team3.model;

public class Customer {
    private String id;
    private String name;
    private String phoneNumber;
    private String roomNumber;
    private String feedback;
    private String createdAt;

    public Customer() {}

    public Customer(String id, String name, String phoneNumber, String roomNumber, String feedback, String createdAt) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.roomNumber = roomNumber;
        this.feedback = feedback;
        this.createdAt = createdAt;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getRoomNumber() { return roomNumber; }
    public String getFeedback() { return feedback; }
    public String getCreatedAt() { return createdAt; }
}