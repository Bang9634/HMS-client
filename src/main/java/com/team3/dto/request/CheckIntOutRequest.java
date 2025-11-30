package com.team3.dto.request;

public class CheckIntOutRequest {
    private final String id;
    private final int roomId;
    private final boolean isCheckedIn;

    public CheckIntOutRequest(String id, int roomId, boolean isCheckedIn) {
        this.id = id;
        this.roomId = roomId;
        this.isCheckedIn = isCheckedIn;
    }

    public String getId() { return id; }

    public int getRoomId() { return roomId; }

    public boolean isCheckedIn() { return isCheckedIn; }
}