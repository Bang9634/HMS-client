package com.team3;

import com.team3.client.api.PaymentApi;
import com.team3.dto.request.PaymentRequest;
import com.team3.dto.response.ApiResponse;

/**
 * [결제 기능 테스트용 가짜 메인 화면]
 */
public class PaymentTestMain {

    public static void main(String[] args) {
        System.out.println("====== [테스트 시작] 결제 시스템 점검 ======");

        // 1. API 도구 준비 (서버 주소: localhost, 포트: 8080)
        PaymentApi paymentApi = new PaymentApi("localhost", 8080);

        // 2. 가상의 결제 데이터 만들기
        PaymentRequest testRequest = new PaymentRequest(
            "테스터",      // 고객명
            50000,         // 객실료
            10000,         // 식음료
            "CARD",        // 결제수단
            "1234-1234-1234-1234" // 카드번호
        );

        System.out.println(">> 서버로 결제 요청을 보냅니다...");

        // 3. 결제 승인 요청 (API 호출)
        ApiResponse response = paymentApi.processPayment(testRequest);

        // 4. 결과 확인
        System.out.println("====== [결과 확인] ======");
        
        // getStatus() -> getStatusCode() 로 변경 (추정)
        // 만약 여기서도 빨간줄이 뜨면 getStatus() 대신 getCode() 인지 확인 필요
        System.out.println("상태 코드: " + response.getStatusCode()); 
        
        // getData() -> getBody() 로 변경 (추정)
        // 만약 여기서도 빨간줄이 뜨면 getMessage() 인지 확인 필요
        System.out.println("서버 메시지: " + response.getBody()); 
        
        System.out.println("=========================");

        
        // 5. 잘 저장됐는지 내역 조회도 한번 해보기
        System.out.println("\n>> 전체 내역 조회를 시도합니다...");
        ApiResponse historyResponse = paymentApi.getPaymentHistory();
        
        // getData() -> getBody() 로 변경
        System.out.println("내역 조회 결과: " + historyResponse.getBody());
    }
}