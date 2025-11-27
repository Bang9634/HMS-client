package com.team3.dto.request;

public class AddRoomRequest {
    private int roomId;
    private int basePrice;
    private boolean isAvailable;
    private int maxOccupancy;
    private String priceChangeReason;

    public AddRoomRequest(
        int roomId, int basePrice, boolean isAvailable, int maxOccupancy, String priceChangeReason) {
        this.roomId = roomId;
        this.basePrice = basePrice;
        this.isAvailable = isAvailable;
        this.maxOccupancy = maxOccupancy;
        this.priceChangeReason = priceChangeReason;
    }

    public int getRoomId() { return roomId; }
    public int getBasePrice() { return basePrice; }
    public boolean getIsAvailable() { return isAvailable; }
    public int getMaxOccupancy() { return maxOccupancy; }
    public String getPriceChangeReason() { return priceChangeReason; }
}
