package com.team3.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 클라이언트 세션 관리자
 * <p>
 * JWT 토큰과 사용자 정보를 메모리에 저장하고 관리한다.
 * 싱글톤 패턴으로 구현되어 애플리케이션 전역에서 하나의 인스턴스만 존재한다.
 * </p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>로그인 시 토큰 저장</li>
 *   <li>로그아웃 시 토큰 삭제</li>
 *   <li>API 요청 시 Authorization 헤더 생성</li>
 *   <li>토큰 만료 확인</li>
 * </ul>
 * 
 * <h3>사용 예시:</h3>
 * <pre>{@code
 * // 로그인 성공 시
 * SessionManager.getInstance().login(token, userId, userName, role, rememberMe);
 * 
 * // API 요청 시
 * String authHeader = SessionManager.getInstance().getAuthorizationHeader();
 * 
 * // 로그아웃 시
 * SessionManager.getInstance().logout();
 * }</pre>
 * 
 * @author bang9634
 * @since 2025-11-26
 */
public class SessionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private static SessionManager instance;
    
    /** JWT 인증 토큰 */
    private String authToken;
    
    /** 사용자 ID */
    private String userId;
    
    /** 사용자 이름 */
    private String userName;
    
    /** 사용자 역할 (ADMIN, USER 등) */
    private String role;
    
    /**
     * private 생성자 (싱글톤 패턴)
     */
    private SessionManager() {}
    
    /**
     * SessionManager 싱글톤 인스턴스 반환
     * 
     * @return SessionManager 인스턴스
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * 로그인 (세션 생성)
     * <p>
     * 로그인 성공 시 호출하여 토큰과 사용자 정보를 저장한다.
     * </p>
     * 
     * @param token JWT 인증 토큰
     * @param userId 사용자 ID
     * @param userName 사용자 이름
     * @param role 사용자 역할
     */
    public void login(String token, String userId, String userName, String role) {
        this.authToken = token;
        this.userId = userId;
        this.userName = userName;
        this.role = role;
        logger.info("세션 생성: userId={}, role={}", userId, role);
    }
    
    /**
     * 로그아웃 (세션 삭제)
     * <p>
     * 메모리와 파일에 저장된 모든 세션 정보를 삭제한다.
     * </p>
     */
    public void logout() {
        logger.info("세션 종료: userId={}", userId);
        
        // 메모리 삭제
        this.authToken = null;
        this.userId = null;
        this.userName = null;
        this.role = null;
    }
    
    /**
     * 로그인 여부 확인
     * 
     * @return 로그인 상태면 true, 아니면 false
     */
    public boolean isLoggedIn() {
        return authToken != null && !authToken.isEmpty();
    }
    
    /**
     * Authorization 헤더 생성
     * <p>
     * API 요청 시 사용할 인증 헤더를 반환한다.
     * </p>
     * 
     * @return "Bearer {token}" 형식의 문자열, 로그인 안 된 경우 빈 문자열
     */
    public String getAuthorizationHeader() {
        logger.debug("헤더에 심을 인증 토큰 반환 시도...");
        if (!isLoggedIn()) {
            logger.warn("로그인하지 않은 상태: token = {}", "");
            return "";
        }
        logger.debug("로그인 상태: token = {}", authToken);
        return "Bearer " + authToken;
    }
    
    public String getAuthToken() { return authToken; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getRole() { return role; }
}