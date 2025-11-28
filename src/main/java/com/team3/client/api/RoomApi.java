package com.team3.client.api;

import java.io.IOException;
import java.net.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.team3.client.HmsClient;
import com.team3.dto.request.AddRoomRequest;
import com.team3.dto.request.RoomIdRequest;
import com.team3.dto.response.ApiResponse;

public class RoomApi extends HmsClient {
    private static final Logger logger = LoggerFactory.getLogger(UserApi.class);

    public RoomApi(String serverHost, int serverPort) {
        super(serverHost, serverPort);
    }


    public ApiResponse getRoomList() {
        try {
            // GET /api/users/get-users 요청 전송
            logger.info("객실 목록 조회 시도");
            HttpResponse<String> response = sendGet("/api/rooms/get-rooms");
            logger.info("객실 목록 조회 응답: statusCode={}", response.statusCode());
            logger.debug("응답 본문: {}", response.body());
            // 응답을 ApiResponse로 변환
            return new ApiResponse(
                response.statusCode(),
                response.body()
            );
            
        } catch (IOException e) {
            logger.error("네트워크 오류", e);
            return ApiResponse.error("서버 연결 실패: " + e.getMessage());
        } catch (InterruptedException e) {
            // 요청 중 스레드가 인터럽트됨
            logger.error("요청 중단", e);
            // 인터럽트 상태 복원
            // 다른 코드가 인터럽트를 감지할 수 있도록 함
            Thread.currentThread().interrupt();
            return ApiResponse.error("요청이 중단되었습니다");
        }
    }

    public ApiResponse addRoom(AddRoomRequest request) {
        try {
            // 입력 검증 (클라이언트 측 빠른 피드백)
            if (request.getRoomId() <= 0) {
                logger.warn("검증 실패: 객실 ID 부적절");
                return ApiResponse.error("객실 ID를 적절한 객실 번호를 입력해주세요.");
            }
     
            // POST /api/users/login 요청 전송
            logger.info("객실 추가 요청: roomId={}", request.getRoomId());
            HttpResponse<String> response = sendPost("/api/rooms/add-room", request);
            logger.info("객실 추가 응답: statusCode={}", response.statusCode());

            
            // 응답을 ApiResponse로 변환
            return new ApiResponse(
                response.statusCode(),
                response.body()
            );
            
        } catch (IOException e) {
            logger.error("네트워크 오류", e);
            return ApiResponse.error("서버 연결 실패: " + e.getMessage());
        } catch (InterruptedException e) {
            // 요청 중 스레드가 인터럽트됨
            logger.error("요청 중단", e);
            // 인터럽트 상태 복원
            // 다른 코드가 인터럽트를 감지할 수 있도록 함
            Thread.currentThread().interrupt();
            return ApiResponse.error("요청이 중단되었습니다");
        }
    }

    public ApiResponse deleteRoom(RoomIdRequest request) {
        try {
            // 입력 검증 (클라이언트 측 빠른 피드백)
            if (request.getRoomId() <= 0) {
                logger.warn("검증 실패: 객실 ID 부적절");
                return ApiResponse.error("객실 ID를 적절한 객실 번호를 입력해주세요.");
            }


            logger.info("객실 삭제 요청: roomId={}", request.getRoomId());
            HttpResponse<String> response = sendPost("/api/rooms/delete-room", request);
            logger.info("객실 삭제 응답: statusCode={}", response.statusCode());

            // 응답을 ApiResponse로 변환
            return new ApiResponse(
                response.statusCode(),
                response.body()
            );
            
        } catch (IOException e) {
            logger.error("네트워크 오류", e);
            return ApiResponse.error("서버 연결 실패: " + e.getMessage());
        } catch (InterruptedException e) {
            // 요청 중 스레드가 인터럽트됨
            logger.error("요청 중단", e);
            // 인터럽트 상태 복원
            // 다른 코드가 인터럽트를 감지할 수 있도록 함
            Thread.currentThread().interrupt();
            return ApiResponse.error("요청이 중단되었습니다");
        }
    }


    
    public ApiResponse getPriceChangeLogs(RoomIdRequest request) {
        try {
            // GET /api/users/get-users 요청 전송
            logger.info("금액변경로그 조회 시도: userId={}", request.getRoomId());
            HttpResponse<String> response = sendPost("/api/rooms/get-price-change-logs", request);
            logger.info("금액변경로그 조회 응답: statusCode={}", response.statusCode());
            logger.debug("응답 본문: {}", response.body());
            // 응답을 ApiResponse로 변환
            return new ApiResponse(
                response.statusCode(),
                response.body()
            );
            
        } catch (IOException e) {
            logger.error("네트워크 오류", e);
            return ApiResponse.error("서버 연결 실패: " + e.getMessage());
        } catch (InterruptedException e) {
            // 요청 중 스레드가 인터럽트됨
            logger.error("요청 중단", e);
            // 인터럽트 상태 복원
            // 다른 코드가 인터럽트를 감지할 수 있도록 함
            Thread.currentThread().interrupt();
            return ApiResponse.error("요청이 중단되었습니다");
        }
    }
}
