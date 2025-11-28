package com.team3.dto.request;

public class AddRoomRequest {
    private final int roomId;
    private final int basePrice;
    private final boolean isAvailable;
    private final int maxOccupancy;

    public AddRoomRequest(int roomId, int basePrice, boolean isAvailable, int maxOccupancy) {
        this.roomId = roomId;
        this.basePrice = basePrice;
        this.isAvailable = isAvailable;
        this.maxOccupancy = maxOccupancy;
    }

    public int getRoomId() { return roomId; }
    public int getBasePrice() { return basePrice; }
    public boolean getIsAvailable() { return isAvailable; }
    public int getMaxOccupancy() { return maxOccupancy; }
}
