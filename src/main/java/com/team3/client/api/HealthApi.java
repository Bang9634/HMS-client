package com.team3.client.api;

import java.io.IOException;
import java.net.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.team3.client.HmsClient;
import com.team3.dto.response.ApiResponse;


/**
 * HMS 서버 헬스 체크 API 클라이언트
 * <p>
 * 서버의 상태를 확인하는 헬스 체크 기능을 제공한다.
 * {@link HmsClient}를 상속받아 HTTP 통신 기능을 사용하며,
 * /health 엔드포인트를 통해 서버의 가용성을 검사한다.
 * </p>

 * <h3>사용 예시:</h3>
 * <pre>{@code
 * // HealthApi 인스턴스 생성
 * HealthApi healthApi = new HealthApi("localhost", 8080);
 * 
 * // 헬스 체크 수행
 * ApiResponse response = healthApi.checkHealth();
 * 
 * if (response.isSuccess()) {
 *     System.out.println("서버 정상 동작");
 * } else {
 *     System.out.println("서버 오류: " + response.getBody());
 * }
 * }</pre>
 * 
 * @author bang9634
 * @since 2025-11-22
 * @see com.team3.client.HmsClient
 * @see com.team3.dto.response.ApiResponse
 */
public class HealthApi extends HmsClient {
    private static final Logger logger = LoggerFactory.getLogger(HealthApi.class);
    
    /**
     * HealthApi 생성자
     * <p>
     * 부모 클래스인 {@link HmsClient}의 생성자를 호출하여
     * HTTP 클라이언트를 초기화한다.
     * </p>
     * 
     * @param serverHost 서버 호스트 주소
     * @param serverPort 서버 포트 번호 (일반적으로 8080)
     */
    public HealthApi(String serverHost, int serverPort) {
        super(serverHost, serverPort);
    }
    

    /**
     * 서버 헬스 체크를 수행한다
     * <p>
     * GET /health 엔드포인트에 요청을 보내 서버의 상태를 확인한다.
     * 정상적인 경우 HTTP 200 OK 응답을 받으며, 이는 서버가 정상 작동 중임을 의미한다.
     * </p>
     * 
     * <h4>응답 처리:</h4>
     * <ul>
     *   <li><strong>성공 (200 OK):</strong> 서버 정상, isSuccess() = true</li>
     *   <li><strong>실패 (200이 아닌 상태코드):</strong> 서버 오류, isSuccess() = false</li>
     * </ul>
     * 
     * <h4>사용 예시:</h4>
     * <pre>{@code
     * ApiResponse response = healthApi.checkHealth();
     * }</pre>
     * 
     * <h4>에러 처리:</h4>
     * <ul>
     *   <li><strong>IOException:</strong> 서버 연결 실패, 네트워크 타임아웃 등</li>
     *   <li><strong>InterruptedException:</strong> 요청 중 스레드 인터럽트</li>
     * </ul>
     * 
     * @return API 응답 객체
     *         <ul>
     *           <li><strong>성공 시:</strong>
     *               <ul>
     *                 <li>statusCode: 200</li>
     *                 <li>body: 서버 응답 메시지 (예: "OK")</li>
     *                 <li>success: true</li>
     *               </ul>
     *           </li>
     *           <li><strong>실패 시:</strong>
     *               <ul>
     *                 <li>statusCode: 실제 상태 코드 (4xx, 5xx)</li>
     *                 <li>body: 에러 메시지</li>
     *                 <li>success: false</li>
     *               </ul>
     *           </li>
     *           <li><strong>네트워크 오류 시:</strong>
     *               <ul>
     *                 <li>statusCode: 0</li>
     *                 <li>body: "서버 연결 실패: [오류 메시지]"</li>
     *                 <li>success: false</li>
     *               </ul>
     *           </li>
     *         </ul>
     * 
     * @apiNote 이 메서드는 예외를 던지지 않고 항상 ApiResponse를 반환한다.
     *          모든 예외는 내부에서 처리되어 에러 응답으로 변환된다.
     * 
     * @see com.team3.dto.response.ApiResponse
     * @see com.team3.client.HmsClient#sendGet(String)
     */
    public ApiResponse checkHealth() {
        try {
            logger.info("헬스 체크 요청");
            HttpResponse<String> response = sendGet("/health");
            logger.info("헬스 체크 응답: statusCode={}", response.statusCode());
            
            return new ApiResponse(
                response.statusCode(),
                response.body()
            );
            
        } catch (IOException e) {
            logger.error("네트워크 오류", e);
            return ApiResponse.error("서버 연결 실패: " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("요청 중단", e);
            Thread.currentThread().interrupt();
            return ApiResponse.error("요청이 중단되었습니다");
        }
    }
}