package com.team3.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.team3.session.SessionManager;
import com.team3.ui.component.AppMenuBar;
import com.team3.ui.component.HeaderPanel;
import com.team3.ui.component.StatusBar;
import com.team3.ui.panel.HealthCheckPanel;
import com.team3.ui.panel.UserManagePanel;
import com.team3.ui.util.IconUtil;

/**
 * HMS 클라이언트 메인 프레임
 * <p>
 * 컴포넌트를 조립하는 역할만 수행
 * </p>
 * 
 * @author bang9634
 * @since 2025-11-22
 */
public class MainFrame extends JFrame {
    
    private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);
    
    private final String serverHost;
    private final int serverPort;
    
    private JTabbedPane tabbedPane;
    private StatusBar statusBar;
    
    public MainFrame(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        
        logger.info("MainFrame 초기화: {}:{}", serverHost, serverPort);
        
        initComponents();
        setupLayout();
    }
    
    /**
     * 컴포넌트 초기화
     */
    private void initComponents() {
        setTitle("HMS 클라이언트");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        
        // 탭 패널 생성
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        
        // 탭 추가
        // 관리자 권한일 경우만
        if (SessionManager.getInstance().getRole().equals("ADMIN")) {
            addTab("서버 상태", "🏥", new HealthCheckPanel(serverHost, serverPort), "서버 상태 확인");
            addTab("사용자 관리", "👤", new UserManagePanel(serverHost, serverPort), "로그인 및 회원가입");
        }
        addTab("객실 관리", "🏨", IconUtil.createPlaceholderPanel("객실 관리"), "객실 조회 및 관리");
        addTab("예약 관리", "📅", IconUtil.createPlaceholderPanel("예약 관리"), "예약 조회 및 관리");
        
        // 메뉴바 생성
        AppMenuBar menuBar = new AppMenuBar(this, tabbedPane, serverHost, serverPort);
        setJMenuBar(menuBar);
        
        // 상태바 생성
        statusBar = new StatusBar(serverHost, serverPort);
    }
    
    /**
     * 레이아웃 설정
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 헤더
        add(new HeaderPanel(serverHost, serverPort), BorderLayout.NORTH);
        
        // 탭 패널
        add(tabbedPane, BorderLayout.CENTER);
        
        // 상태바
        add(statusBar, BorderLayout.SOUTH);
    }
    
    /**
     * 탭 추가 헬퍼 메서드
     * 
     * @param title 탭 제목 
     * @param emoji 제목에 들어갈 아이콘(이모지 기반)
     * @param component 탭을 누르면 출력할 패널 객체
     * @param tooltip 탭 설명
     */
    private void addTab(String title, String emoji, Component component, String tooltip) {
        tabbedPane.addTab(title, IconUtil.createEmojiIcon(emoji), component, tooltip);
    }
    
    /**
     * 상태바 메시지 업데이트
     * <p>
     * 인자로 전달한 문자열을 상태바에 업데이트한다.
     * </p>
     * 
     * @param message 상태바에 띄울 문자열
     */
    public void updateStatusBar(String message) {
        statusBar.updateMessage(message);
    }
}