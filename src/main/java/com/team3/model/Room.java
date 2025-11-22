package com.team3.model;

import java.io.Serializable;

public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    private int roomNumber;
    private int price;
    private boolean guaranteed;

    public Room(int roomNumber, int price, boolean guaranteed) {
        this.roomNumber = roomNumber;
        this.price = price;
        this.guaranteed = guaranteed;
    }

    public Room(int roomNumber, int price) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public int getRoomNumber() { return roomNumber; }
    public int getPrice() { return price; }
    public boolean isGuaranteed() { return guaranteed; }

    public void setGuaranteed(boolean guaranteed) { this.guaranteed = guaranteed; }

    @Override
    public String toString() {
        return roomNumber + "호 [" + price + "원] 보증:" + (guaranteed ? "O" : "X");
    }
}