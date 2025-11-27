package com.team3.dto.request;

/**
 * [결제 요청 DTO]
 * 클라이언트가 서버에게 결제 요청
 */
public class PaymentRequest {
    
    // 1. 필요한 데이터 필드 정의
    private String guestName;       // 결제하는 사람 이름
    private int roomCharge;         // 객실 이용료
    private int foodCharge;         // 식음료 이용료
    private String method;          // 결제 수단 ("CARD" 또는 "CASH")
    private String cardNumber;      // 카드 번호 (현금이면 null 가능)

    /**
     * 생성자
     * - 화면에서 입력받은 값들을 여기에 넣음
     */
    public PaymentRequest(String guestName, int roomCharge, int foodCharge, String method, String cardNumber) {
        this.guestName = guestName;
        this.roomCharge = roomCharge;
        this.foodCharge = foodCharge;
        this.method = method;
        this.cardNumber = cardNumber;
    }

    // Getter 메서드
    // 이 객체를 JSON으로 변환할 때
    // 이 Getter들을 통해 값을 꺼내서 JSON 문자열을 만듦
    public String getGuestName() { return guestName; }
    public int getRoomCharge() { return roomCharge; }
    public int getFoodCharge() { return foodCharge; }
    public String getMethod() { return method; }
    public String getCardNumber() { return cardNumber; }
}