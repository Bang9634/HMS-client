package com.team3.dto.response;

/**
 * [결제 통합 모델]
 * 서버와 데이터를 주고받을 때 사용하는 단일 객체 (DTO)
 * - 기존 PaymentRequest + PaymentResponse 역할 통합
 * @author 김현준
 */
public class Payment {

    // 1. 요청 데이터
    private String guestName;
    private int roomCharge;
    private int foodCharge;
    private String method;
    private String cardNumber;

    // 2. 추가 요청 데이터
    private String reservationId;   // 예약 번호(UI에서 들어옴)

    // 3. 응답 데이터
    private String receiptId;       // 서버가 생성하는 영수증 번호
    private int totalAmount;
    private String details;
    private String paymentTime;

    // 기본 생성자
    public Payment() {}

    // UI에서 POST 전송 시 사용하는 생성자 (예약번호 포함)
    public Payment(String guestName, int roomCharge, int foodCharge, 
                   String method, String cardNumber, String reservationId) {
        this.guestName = guestName;
        this.roomCharge = roomCharge;
        this.foodCharge = foodCharge;
        this.method = method;
        this.cardNumber = cardNumber;
        this.reservationId = reservationId;
        this.totalAmount = roomCharge + foodCharge;
    }

    // Getter (UI, Panel에서 사용)
    public String getGuestName() { return guestName; }
    public int getRoomCharge() { return roomCharge; }
    public int getFoodCharge() { return foodCharge; }
    public String getMethod() { return method; }
    public String getCardNumber() { return cardNumber; }
    public String getReservationId() { return reservationId; }   // ⭐ 추가됨

    public String getReceiptId() { return receiptId; }           // ⭐ 추가됨
    public int getTotalAmount() { return totalAmount; }
    public String getDetails() { return details; }
    public String getPaymentTime() { return paymentTime; }

    @Override
    public String toString() {
        return String.format("[결제] %s님 %d원 (%s)", guestName, totalAmount, paymentTime);
    }
}
