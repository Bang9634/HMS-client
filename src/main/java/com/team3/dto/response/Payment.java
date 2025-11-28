package com.team3.dto.response;

/**
 * [결제 응답 모델]
 * 서버에서 "결제 내역 조회"를 했을 때 돌아오는 JSON 데이터를 담는 클래스
 * - 서버가 보내주는 JSON 키값(key)과 변수명이 일치해야 함
 * @author 김현준
 */
public class Payment {
    
    // 서버의 Payment 모델과 변수명이 똑같아야 합니다.
    private String guestName;       // 고객명
    private int totalAmount;        // 합계 금액
    private String details;         // 상세 내역 (예: Room+Food)
    private String method;          // 결제 수단
    private String cardNumber;      // 카드 번호
    private String paymentTime;     // 결제 시간 (서버가 찍어줌)

    // Getter
    public String getGuestName() { return guestName; }
    public int getTotalAmount() { return totalAmount; }
    public String getDetails() { return details; }
    public String getMethod() { return method; }
    public String getCardNumber() { return cardNumber; }
    public String getPaymentTime() { return paymentTime; }

    /**
     * toString(): 콘솔에서 로그 
     * 객체 주소값 대신 내용을 예쁘게 보기 위해 재정의
     */
    @Override
    public String toString() {
        return String.format("[%s] %s님 | %d원 (%s) | %s", paymentTime, guestName, totalAmount, details, method);
    }
}