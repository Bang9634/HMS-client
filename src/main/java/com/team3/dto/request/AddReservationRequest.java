package com.team3.dto.request;

/**
 * 예약 생성 요청 DTO
 * <p>
 * 클라이언트에서 예약 생성 시 필요한 데이터를 서버로 전송하기 위한 객체.
 * </p>
 * @author bang9634
 * @since 2025-11-28
 */
public class AddReservationRequest {
    private final String roomId;
    private final String guestName;
    private final String phone;
    private final String checkInDate;
    private final String checkOutDate;
    private final int guestCount;

    public AddReservationRequest(String roomId, String guestName, String phone, String checkInDate, String checkOutDate, int guestCount) {
        this.roomId = roomId;
        this.guestName = guestName;
        this.phone = phone;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestCount = guestCount;
    }

    public String getRoomId() { return roomId; }
    public String getGuestName() { return guestName; }
    public String getPhone() { return phone; }
    public String getCheckInDate() { return checkInDate; }
    public String getCheckOutDate() { return checkOutDate; }
    public int getGuestCount() { return guestCount; }
}