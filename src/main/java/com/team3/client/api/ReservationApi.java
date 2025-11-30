package com.team3.client.api;

import java.io.IOException;
import java.net.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.team3.client.HmsClient;
import com.team3.dto.request.AddReservationRequest;
import com.team3.dto.request.CheckIntOutRequest;
import com.team3.dto.request.DeleteReservationRequest;
import com.team3.dto.request.UpdateReservationRequest;
import com.team3.dto.response.ApiResponse;

/**
 * 예약 관련 API 호출을 담당하는 클라이언트 클래스
 * <p>
 * HMS 서버의 예약 관리 기능을 제공한다.
 * {@link HmsClient}를 상속받아 HTTP 통신 기능을 사용하며,
 * /api/reservation 엔드포인트를 통해 조회, 생성, 삭제 기능을 수행한다.
 * </p>
 *
 * @author bang9634
 * @since 2025-11-28
 * @see com.team3.client.HmsClient
 * @see com.team3.dto.request.AddReservationRequest
 * @see com.team3.dto.response.ApiResponse
 */
public class ReservationApi extends HmsClient {
    private static final Logger logger = LoggerFactory.getLogger(ReservationApi.class);

    /**
     * ReservationApi 생성자
     * <p>
     * 부모 클래스인 {@link HmsClient}의 생성자를 호출하여 HTTP 클라이언트를 초기화한다.
     * </p>
     *
     * @param serverHost 서버 호스트 주소
     * @param serverPort 서버 포트 번호
     */
    public ReservationApi(String serverHost, int serverPort) {
        super(serverHost, serverPort);
    }

    /**
     * 예약 목록 조회 (GET /api/reservation/list)
     * <p>
     * 서버에 저장된 모든 예약 목록을 조회한다.
     * </p>
     *
     * @return 예약 목록이 담긴 API 응답 객체
     */
    public ApiResponse getReservations() {
        try {
            logger.info("예약 목록 조회 요청");
            
            // GET 요청 전송 (토큰은 HmsClient 내부에서 SessionManager를 통해 자동 추가됨)
            HttpResponse<String> response = sendGet("/api/reservation/list");
            
            logger.info("예약 목록 조회 응답: statusCode={}", response.statusCode());
            logger.debug("응답 본문: {}", response.body());
            
            return new ApiResponse(response.statusCode(), response.body());
            
        } catch (IOException e) {
            logger.error("네트워크 오류", e);
            return ApiResponse.error("서버 연결 실패: " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("요청 중단", e);
            Thread.currentThread().interrupt();
            return ApiResponse.error("요청이 중단되었습니다.");
        }
    }

    /**
     * 예약 생성 (POST /api/reservation/create)
     * <p>
     * 새로운 예약을 생성한다.
     * </p>
     *
     * @param request 예약 생성 요청 DTO
     * @return 생성 결과 API 응답 객체
     */
    public ApiResponse createReservation(AddReservationRequest request) {
        try {
            // 입력값 검증 (간단한 클라이언트 측 검증)
            if (request == null) {
                logger.warn("검증 실패: 요청 객체가 null입니다.");
                return ApiResponse.error("잘못된 요청입니다.");
            }
            if (request.getRoomId() == null || request.getRoomId().trim().isEmpty()) {
                logger.warn("검증 실패: 객실 번호 없음");
                return ApiResponse.error("객실 번호를 입력해주세요.");
            }

            logger.info("예약 생성 요청: roomId={}, guest={}", request.getRoomId(), request.getGuestName());
            
            // POST 요청 전송
            HttpResponse<String> response = sendPost("/api/reservation/create", request);
            
            logger.info("예약 생성 응답: statusCode={}", response.statusCode());
            
            return new ApiResponse(response.statusCode(), response.body());
            
        } catch (IOException e) {
            logger.error("네트워크 오류", e);
            return ApiResponse.error("서버 연결 실패: " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("요청 중단", e);
            Thread.currentThread().interrupt();
            return ApiResponse.error("요청이 중단되었습니다.");
        }
    }

    /**
     * 예약 취소 (POST /api/reservation/delete)
     * <p>
     * 특정 예약을 취소(삭제)한다.
     * </p>
     *
     * @param request 예약 취소 요청 DTO (삭제할 예약 ID 포함)
     * @return 취소 결과 API 응답 객체
     */
    public ApiResponse cancelReservation(DeleteReservationRequest request) {
        try {
            if (request == null || request.getId() == null || request.getId().trim().isEmpty()) {
                logger.warn("검증 실패: 예약 ID 없음");
                return ApiResponse.error("예약 ID가 필요합니다.");
            }

            logger.info("예약 취소 요청: id={}", request.getId());
            
            // POST 요청 전송 (삭제도 POST 메서드로 처리하도록 서버가 구현됨)
            HttpResponse<String> response = sendPost("/api/reservation/delete", request);
            
            logger.info("예약 취소 응답: statusCode={}", response.statusCode());
            
            return new ApiResponse(response.statusCode(), response.body());
            
        } catch (IOException e) {
            logger.error("네트워크 오류", e);
            return ApiResponse.error("서버 연결 실패: " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("요청 중단", e);
            Thread.currentThread().interrupt();
            return ApiResponse.error("요청이 중단되었습니다.");
        }
    } 
    /**
     * [추가] 예약 수정 (POST /api/reservation/update)
     */
    public ApiResponse updateReservation(UpdateReservationRequest request) {
        try {
            logger.info("예약 수정 요청: id={}", request.getId());
            // POST 요청 전송
            HttpResponse<String> response = sendPost("/api/reservation/update", request);
            return new ApiResponse(response.statusCode(), response.body());
        } catch (Exception e) {
            logger.error("예약 수정 실패", e);
            return ApiResponse.error("요청 실패: " + e.getMessage());
        }
    }

    public ApiResponse checkInOut(CheckIntOutRequest request) {
        try {
            HttpResponse<String> response;
            if (!request.isCheckedIn()) {
                logger.info("체크인 요청: roomId={}", request.getRoomId());
                response= sendPost("/api/reservation/checkin", request);
            } else {
                logger.info("체크아웃 요청: roomId={}", request.getRoomId());
                response = sendPost("/api/reservation/checkout", request);
            }
            
            return new ApiResponse(response.statusCode(), response.body());
        } catch (Exception e) {
            logger.error("체크인/아웃 수정 실패", e);
            return ApiResponse.error("요청 실패: " + e.getMessage());
        }
    }
}