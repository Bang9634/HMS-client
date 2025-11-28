package com.team3.dto.request;

public class UpdateReservationRequest {
    private final String id; // 수정할 예약의 ID (필수)
    private final String userId;
    private final String roomId;
    private final String guestName;
    private final String phone;
    private final String checkInDate;
    private final String checkOutDate;
    private final int guestCount;

    public UpdateReservationRequest(String id, String userId, String roomId, String guestName, String phone, String checkInDate, String checkOutDate, int guestCount) {
        this.id = id;
        this.userId = userId;
        this.roomId = roomId;
        this.guestName = guestName;
        this.phone = phone;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestCount = guestCount;
    }
    
    // Getters (Gson 직렬화용)
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getRoomId() { return roomId; }
    public String getGuestName() { return guestName; }
    public String getPhone() { return phone; }
    public String getCheckInDate() { return checkInDate; }
    public String getCheckOutDate() { return checkOutDate; }
    public int getGuestCount() { return guestCount; }
}