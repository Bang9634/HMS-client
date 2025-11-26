package com.team3.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.team3.session.SessionManager;

/**
 * HMS 서버와 HTTP 통신을 담당하는 기본 클라이언트 클래스
 * <p>
 * Java 11+ HttpClient API를 사용하여 HMS 서버와 통신한다.
 * 모든 API 클래스(HealthApi, UserApi 등)의 부모 클래스로 사용되며,
 * GET, POST 요청을 위한 공통 메서드를 제공한다.
 * </p>
 * 
 * <h3>사용 예시:</h3>
 * <pre>{@code
 * // 직접 사용하지 않고 상속받아 사용
 * public class UserApi extends HmsClient {
 *     public UserApi(String host, int port) {
 *         super(host, port);
 *     }
 *     
 *     public ApiResponse login(LoginRequest request) {
 *         HttpResponse<String> response = sendPost("/api/user/login", request);
 *         // ...
 *     }
 * }
 * }</pre>
 * 
 * @author bang9634
 * @since 2025-11-22
 */
public class HmsClient {
    private static final Logger logger = LoggerFactory.getLogger(HmsClient.class);
    
    /** 서버의 기본 URL (예: http://localhost:8080) */
    private final String serverUrl;

    /** Java 11+ HttpClient 인스턴스 */
    private final HttpClient httpClient;

    /** JSON 직렬화/역직렬화를 위한 Gson 인스턴스 */
    private final Gson gson;
    
    /**
     * HMS 클라이언트 생성자
     * <p>
     * 서버 주소를 설정하고 HTTP 클라이언트를 초기화한다.
     * 연결 타임아웃은 10초로 설정된다.
     * </p>
     * 
     * @param serverHost 서버 호스트 (예: "localhost", "192.168.1.100")
     * @param serverPort 서버 포트 (예: 8080)
     */
    public HmsClient(String serverHost, int serverPort) {
        this.serverUrl = String.format("http://%s:%d", serverHost, serverPort);
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.gson = new Gson();
        
        logger.info("HmsClient 초기화: {}", serverUrl);
    }

    /**
     * HTTP GET 요청을 서버에 전송한다
     * <p>
     * 지정된 엔드포인트로 GET 요청을 보내고 응답을 반환한다.
     * 응답은 UTF-8로 인코딩된 문자열로 받는다.
     * </p>
     * 
     * <h4>사용 예시:</h4>
     * <pre>{@code
     * HttpResponse<String> response = sendGet("/api/health");
     * int statusCode = response.statusCode();      // 200
     * String body = response.body();               // "OK"
     * }</pre>
     * 
     * @param endpoint API 엔드포인트 경로 (예: "/api/health", "/api/user/list")
     *                 반드시 '/'로 시작해야 함
     * @return HTTP 응답 객체 (상태 코드, 헤더, 본문 포함)
     * 
     * @throws IOException 네트워크 오류 발생 시
     *                     <ul>
     *                       <li>서버 연결 실패</li>
     *                       <li>네트워크 타임아웃</li>
     *                       <li>DNS 해석 실패</li>
     *                     </ul>
     * @throws InterruptedException 요청 중 스레드가 인터럽트된 경우
     * @throws IllegalArgumentException endpoint가 null이거나 '/'로 시작하지 않는 경우
     */
    protected HttpResponse<String> sendGet(String endpoint) 
            throws IOException, InterruptedException {
        String url = serverUrl + endpoint;
        logger.debug("GET 요청: {}", url);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .header("Authorization", SessionManager.getInstance().getAuthorizationHeader())
            .GET()
            .build();
        
        return httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    /**
     * HTTP POST 요청을 서버에 전송한다
     * <p>
     * 지정된 엔드포인트로 JSON 형식의 데이터를 POST 요청으로 보내고 응답을 반환한다.
     * 요청 본문은 자동으로 JSON으로 직렬화되며, Content-Type은 application/json으로 설정된다.
     * </p>
     * 
     * <h4>사용 예시:</h4>
     * <pre>{@code
     * LoginRequest loginReq = new LoginRequest("admin", "password");
     * HttpResponse<String> response = sendPost("/api/user/login", loginReq);
     * 
     * // 전송되는 JSON:
     * // {"userId":"admin","password":"password"}
     * }</pre>
     * 
     * @param endpoint API 엔드포인트 경로 (예: "/api/user/login")
     *                 반드시 '/'로 시작해야 함
     * @param requestBody JSON으로 변환할 요청 본문 객체
     *                    <ul>
     *                      <li>DTO 객체 (LoginRequest, SignupRequest 등)</li>
     *                      <li>Map, List 등 JSON 변환 가능한 모든 객체</li>
     *                      <li>null인 경우 빈 객체 {}가 전송됨</li>
     *                    </ul>
     * @return HTTP 응답 객체 (상태 코드, 헤더, 본문 포함)
     * 
     * @throws IOException 네트워크 오류 발생 시
     *                     <ul>
     *                       <li>서버 연결 실패</li>
     *                       <li>네트워크 타임아웃</li>
     *                       <li>요청 전송 중 오류</li>
     *                     </ul>
     * @throws InterruptedException 요청 중 스레드가 인터럽트된 경우
     * @throws IllegalArgumentException endpoint가 null이거나 '/'로 시작하지 않는 경우
     * @throws com.google.gson.JsonSyntaxException requestBody를 JSON으로 변환할 수 없는 경우
     */
    protected HttpResponse<String> sendPost(String endpoint, Object requestBody) 
            throws IOException, InterruptedException {
        String url = serverUrl + endpoint;
        String jsonBody = gson.toJson(requestBody);
        logger.debug("POST 요청: {} - Body: {}", url, jsonBody);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .header("Authorization", SessionManager.getInstance().getAuthorizationHeader())
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
        
        return httpClient.send(request,
            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    /**
     * 서버의 기본 URL을 반환한다
     * <p>
     * 생성자에서 설정된 서버 URL (http://host:port 형식)을 반환한다.
     * </p>
     * 
     * @return 서버 URL (예: "http://localhost:8080")
     */
    public String getServerUrl() { return serverUrl; }
}