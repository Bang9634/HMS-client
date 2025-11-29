package com.team3.dto.response;

/**
 * [결제 통합 모델]
 * 서버와 데이터를 주고받을 때 사용하는 단일 객체 (DTO)
 * - 기존의 PaymentRequest와 PaymentResponse 역할을 모두 수행함.
 * * @author 김현준
 */
public class Payment {
    // 1. 보낼 때 필요한 데이터 (요청)
    private String guestName;
    private int roomCharge;
    private int foodCharge;
    private String method;
    private String cardNumber;

    // 2. 받을 때 필요한 데이터 (응답)
    private int totalAmount;
    private String details;
    private String paymentTime;

    // 기본 생성자
    public Payment() {}

    // UI에서 데이터를 담아서 보낼 때 쓰는 생성자
    public Payment(String guestName, int roomCharge, int foodCharge, String method, String cardNumber) {
        this.guestName = guestName;
        this.roomCharge = roomCharge;
        this.foodCharge = foodCharge;
        this.method = method;
        this.cardNumber = cardNumber;
        // 보내는 시점에 합계를 임시로 계산해둘 수도 있음
        this.totalAmount = roomCharge + foodCharge;
    }

    // Getter
    public String getGuestName() { return guestName; }
    public int getRoomCharge() { return roomCharge; }
    public int getFoodCharge() { return foodCharge; }
    public String getMethod() { return method; }
    public String getCardNumber() { return cardNumber; }
    public int getTotalAmount() { return totalAmount; }
    public String getDetails() { return details; }
    public String getPaymentTime() { return paymentTime; }
    
    @Override
    public String toString() {
        return String.format("[결제] %s님 %d원 (%s)", guestName, totalAmount, paymentTime);
    }
}