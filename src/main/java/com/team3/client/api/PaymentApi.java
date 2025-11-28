package com.team3.client.api;

import com.team3.client.HmsClient;
import com.team3.dto.request.PaymentRequest;
import com.team3.dto.response.ApiResponse;
import java.io.IOException;
import java.net.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * [결제 API 클라이언트]
 * - 역할: 서버의 결제 관련 URL(/api/payments/...)로 요청을 보내고 응답을 받음.
 * - HmsClient를 상속 -> sendPost, sendGet 기능 사용
 * @author 김현준
 */
public class PaymentApi extends HmsClient {

    // 로그 찍기 (System.out.println 대신 사용)
    private static final Logger logger = LoggerFactory.getLogger(PaymentApi.class);

    // 생성자
    // HmsClient에게 서버 IP와 포트를 넘김 -> 통신 준비
    public PaymentApi(String serverHost, int serverPort) {
        super(serverHost, serverPort);
    }

    /**
     * 1. 결제 승인 요청 (POST 방식).
     * - 화면에서 입력한 정보를 받아서 서버로 쏘는 메서드.
     */
    public ApiResponse processPayment(PaymentRequest request) {
        try {
            // [1단계] 클라이언트 검증 (서버로 보내기 전에 미리 검사)
            // -ㅜ굳이 잘못된 데이터를 서버까지 보내서 트래픽을 낭비할 필요가 없기 때문
            if (request.getGuestName() == null || request.getGuestName().trim().isEmpty()) {
                logger.warn("검증 실패: 고객명 없음");
                return ApiResponse.error("고객명을 입력해주세요.");
            }
            // 카드를 선택했는데 번호가 없으면 에러
            if ("CARD".equals(request.getMethod()) && (request.getCardNumber() == null || request.getCardNumber().isEmpty())) {
                logger.warn("검증 실패: 카드번호 없음");
                return ApiResponse.error("카드 결제 시 카드번호는 필수입니다.");
            }

            // [2단계] 서버로 전송
            logger.info("결제 요청 시작: 고객명={}", request.getGuestName());
            
            // sendPost: HmsClient에 있는 메서드. 
            // - 첫 번째 파라미터: 주소 (서버 Main.java에 등록된 주소랑 같아야 함)
            // - 두 번째 파라미터: 보낼 데이터 객체
            HttpResponse<String> response = sendPost("/api/payments/process", request);
            
            logger.info("결제 응답 도착: 상태코드={}", response.statusCode());

            // [3단계] 결과 반환 (성공이든 실패든 ApiResponse에 담아서 리턴)
            return new ApiResponse(response.statusCode(), response.body());

        } catch (IOException e) {
            // 인터넷 선이 뽑혔거나 서버가 꺼져있을 때
            logger.error("통신 오류 발생", e);
            return ApiResponse.error("서버와 연결할 수 없습니다.");
        } catch (InterruptedException e) {
            // 통신 중 강제로 종료되었을 때
            Thread.currentThread().interrupt();
            return ApiResponse.error("통신 중 오류가 발생했습니다.");
        }
    }

    /**
     * 2. 결제 내역 조회 (GET 방식).
     * - 서버에 저장된 모든 결제 기록을 요청하는 메서드.
     */
    public ApiResponse getPaymentHistory() {
        try {
            logger.info("전체 결제 내역 조회 요청");
            
            // sendGet: 데이터를 보낼 필요 없이 주소만 찌르면 됨.
            HttpResponse<String> response = sendGet("/api/payments/history");
            
            logger.info("조회 완료: 상태코드={}", response.statusCode());
            
            return new ApiResponse(response.statusCode(), response.body());

        } catch (IOException e) {
            logger.error("통신 오류", e);
            return ApiResponse.error("서버 연결 실패");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ApiResponse.error("오류 발생");
        }
    }
    
    /**
     * 3. 모든 결제 내역 삭제 (DELETE)
     * @return 처리 결과 응답
     */
    public ApiResponse deletePaymentHistory() {
        try {
            logger.info("결제 내역 초기화 요청");
            // HmsClient에 추가한 sendDelete 사용
            HttpResponse<String> response = sendDelete("/api/payments/history");
            return new ApiResponse(response.statusCode(), response.body());

        } catch (IOException e) {
            logger.error("통신 오류", e);
            return ApiResponse.error("서버 연결 실패");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ApiResponse.error("요청 중단됨");
        }
    }
    
}