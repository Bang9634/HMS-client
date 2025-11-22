package com.team3.model;

import java.io.Serializable;

/**
 * 결제 정보 모델 클래스
 */
public class Payment implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String userId;   // 결제한 사용자 ID
    private final int amount;      // 금액
    private final String content;  // 결제 내용 (예: "101호 예약 결제")

    public Payment(String userId, int amount, String content) {
        this.userId = userId;
        this.amount = amount;
        this.content = content;
    }

    public String getUserId() { return userId; }
    public int getAmount() { return amount; }
    public String getContent() { return content; }
    
    @Override
    public String toString() {
        return "[" + userId + "] " + amount + "원 (" + content + ")";
    }
}