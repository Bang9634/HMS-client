package com.team3.model;

/**
 * 예약 도메인 모델 (클라이언트용)
 * <p>
 * 서버 응답(JSON)을 받기 위해 날짜 필드를 String으로 설정합니다.
 * </p>
 */
public class Reservation {
    private String id;
    private String userId;
    private String roomId;
    private String guestName;
    private String phone;
    private String checkInDate;
    private String checkOutDate;
    private int guestCount;
    private boolean isCheckedIn;
    
    // JSON 파싱 오류 방지를 위해 String으로 변경
    private String createdAt;
    private String updatedAt;

    public Reservation() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    
    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getCheckInDate() { return checkInDate; }
    public void setCheckInDate(String checkInDate) { this.checkInDate = checkInDate; }
    
    public String getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(String checkOutDate) { this.checkOutDate = checkOutDate; }
    
    public int getGuestCount() { return guestCount; }
    public void setGuestCount(int guestCount) { this.guestCount = guestCount; }
    
    // [수정] String 타입으로 Getter/Setter 변경
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public boolean isCheckedIn() { return isCheckedIn; }
    public void setIsCheckedIn(boolean isCheckedIn) {this.isCheckedIn = isCheckedIn; }
    
    
    @Override
    public String toString() {
        return guestName + " (" + roomId + ")";
    }
}