package com.team3.dto.request;

public class UpdateRoomRequest {
    private final int roomId;
    private final int basePrice;
    private final boolean isAvailable;
    private final int maxOccupancy;
    private final String reason;

    public UpdateRoomRequest(int roomId, int basePrice, boolean isAvailable, int maxOccupancy, String reason) {
        this.roomId = roomId;
        this.basePrice = basePrice;
        this.isAvailable = isAvailable;
        this.maxOccupancy = maxOccupancy;
        this.reason = reason;
    }

    public int getRoomId() { return roomId; }
    public int getBasePrice() { return basePrice; }
    public boolean getIsAvailable() { return isAvailable; }
    public int getMaxOccupancy() { return maxOccupancy; }
    public String getReason() { return reason; }
}
