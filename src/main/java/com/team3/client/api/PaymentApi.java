package com.team3.client.api;

import java.io.IOException;
import java.net.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.team3.client.HmsClient;
import com.team3.dto.response.ApiResponse;
import com.team3.dto.response.Payment;

/**
 * 결제 관련 API 호출 클래스
 * * @author 김현준
 */
public class PaymentApi extends HmsClient {

    private static final Logger logger = LoggerFactory.getLogger(PaymentApi.class);

    public PaymentApi(String serverHost, int serverPort) {
        super(serverHost, serverPort);
    }

    // 파라미터 PaymentRequest -> Payment 로 변경됨
    public ApiResponse processPayment(Payment request) {
        try {
            if (request.getGuestName() == null || request.getGuestName().trim().isEmpty()) {
                return ApiResponse.error("고객명을 입력해주세요.");
            }

            logger.info("결제 승인 요청 전송: 고객={}", request.getGuestName());
            
            // Payment 객체를 그대로 서버로 전송 (서버도 Payment로 받음)
            HttpResponse<String> response = sendPost("/api/payments/process", request);
            
            return new ApiResponse(response.statusCode(), response.body());

        } catch (IOException e) {
            logger.error("통신 오류", e);
            return ApiResponse.error("서버 연결 실패: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ApiResponse.error("요청이 중단되었습니다.");
        }
    }

    public ApiResponse getPaymentHistory() {
        try {
            HttpResponse<String> response = sendGet("/api/payments/history");
            return new ApiResponse(response.statusCode(), response.body());
        } catch (Exception e) {
            return ApiResponse.error("조회 실패");
        }
    }

    public ApiResponse deletePaymentHistory() {
        try {
            HttpResponse<String> response = sendDelete("/api/payments/history");
            return new ApiResponse(response.statusCode(), response.body());
        } catch (Exception e) {
            return ApiResponse.error("삭제 실패");
        }
    }
    
    public ApiResponse deletePaymentByGuestName(String guestName) {
        try {
            // 한글 이름을 URL에 넣으려면 인코딩 필요
            String encodedName = java.net.URLEncoder.encode(guestName, "UTF-8");
            String url = "/api/payments/history?guestName=" + encodedName;
        
            logger.info("개별 내역 삭제 요청: {}", guestName);
            HttpResponse<String> response = sendDelete(url);
            return new ApiResponse(response.statusCode(), response.body());
        } catch (Exception e) {
            logger.error("개별 삭제 중 오류", e);
            return ApiResponse.error("삭제 실패");
        }
    }
    
}