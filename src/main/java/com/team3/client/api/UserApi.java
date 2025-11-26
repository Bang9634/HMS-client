package com.team3.client.api;

import java.io.IOException;
import java.net.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.team3.client.HmsClient;
import com.team3.dto.request.LoginRequest;
import com.team3.dto.response.ApiResponse;

/**
 * 사용자 관련 API 호출을 담당하는 클라이언트 클래스
 * <p>
 * HMS 서버의 사용자 인증 및 관리 기능을 제공한다.
 * {@link HmsClient}를 상속받아 HTTP 통신 기능을 사용하며,
 * /api/users 엔드포인트를 통해 로그인 등의 사용자 관련 기능을 수행한다.
 * </p>
 * 
 * @author bang9634
 * @since 2025-11-22
 * @see com.team3.client.HmsClient
 * @see com.team3.dto.request.LoginRequest
 * @see com.team3.dto.response.ApiResponse
 */
public class UserApi extends HmsClient {
    private static final Logger logger = LoggerFactory.getLogger(UserApi.class);
    
    /**
     * UserApi 생성자
     * <p>
     * 부모 클래스인 {@link HmsClient}의 생성자를 호출하여
     * HTTP 클라이언트를 초기화한다.
     * </p>
     * 
     * @param serverHost 서버 호스트 주소
     * @param serverPort 서버 포트 번호 (일반적으로 8080)
     */
    public UserApi(String serverHost, int serverPort) {
        super(serverHost, serverPort);
    }
   
    /**
     * 사용자 로그인을 수행한다
     * <p>
     * POST /api/users/login 엔드포인트에 사용자 ID와 평문 비밀번호를 전송하여
     * 로그인을 시도한다.
     * </p>
     * 
     * <h4>요청 프로세스:</h4>
     * <ol>
     *   <li>LoginRequest 객체를 JSON으로 직렬화</li>
     *   <li>POST /api/users/login 요청 전송</li>
     *   <li>서버 응답 수신 및 처리</li>
     *   <li>ApiResponse 객체로 변환하여 반환</li>
     * </ol>
     * 
     * @param request 로그인 요청 객체
     * @return API 응답 객체
     * 
     * @throws NullPointerException request가 null인 경우 (sendPost 내부에서 발생)
     * 
     * @apiNote 
     * <ul>
     *   <li>이 메서드는 예외를 던지지 않고 항상 ApiResponse를 반환함</li>
     *   <li>모든 예외는 내부에서 처리되어 에러 응답으로 변환됨</li>
     * </ul>
     * 
     * @see com.team3.dto.request.LoginRequest
     * @see com.team3.dto.response.ApiResponse
     * @see com.team3.util.PasswordUtil#hash(String)
     * @see com.team3.client.HmsClient#sendPost(String, Object)
     */
    public ApiResponse login(LoginRequest request) {
        try {
            // 입력 검증 (클라이언트 측 빠른 피드백)
            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                logger.warn("검증 실패: 사용자 ID 없음");
                return ApiResponse.error("사용자 ID를 입력해주세요");
            }
            
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                logger.warn("검증 실패: 비밀번호 없음");
                return ApiResponse.error("비밀번호를 입력해주세요");
            }
     
            // POST /api/users/login 요청 전송
            logger.info("로그인 요청: userId={}", request.getUserId());
            HttpResponse<String> response = sendPost("/api/users/login", 
                new LoginRequest(request.getUserId(), request.getPassword()));
            logger.info("로그인 응답: statusCode={}", response.statusCode());

            
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

    /**
     * 사용자 목록 전체 조회 시도
     * 
     * @return 사용자 목록 전체 정보가 담긴 응답
     */
public ApiResponse getUserList() {
        try {
            // GET /api/users/get-users 요청 전송
            logger.info("사용자 목록 조회 시도");
            HttpResponse<String> response = sendGet("/api/users/get-users");
            logger.info("사용자 목록 조회 응답: statusCode={}", response.statusCode());
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
