package com.team3;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.team3.session.SessionManager;
import com.team3.ui.MainFrame;
import com.team3.ui.dialog.LoginDialog;

/**
 * HMS 클라이언트 메인 클래스
 * <p>
 * HMS 클라이언트 프로그램의 시작점 역할을 담당한다.
 * 실행 시 연결할 서버를 설정하고, GUI를 출력한다.
 * </p>
 * 
 * <h3>실행 방법:</h3>
 * <pre>{@code
 * # 기본 설정으로 실행 (localhost, 8080)
 * java -jar hms-client.jar
 * 
 * # 커스텀 포트로 실행
 * java -jar hms-client.jar [호스트주소] [포트번호]
 * }</pre>
 * @author bang9634
 * @since 2025-11-22
 */
public class Main {
    /** SLF4J 로거 인스턴스 - 메인 애플리케이션 시작 및 설정 로깅 */
    private static final Logger logger = LoggerFactory.getLogger(Main.class);


    /**
     * 애플리케이션의 메인 엔트리 포인트
     * <p>
     * 연결할 호스트 주소와 포트 주소를 설정하고
     * 로그인 프레임을 먼저 띄운 후, 로그인이 성공하면 메인 프레임을 출력한다.
     * </p>
     * 
     * <h3>커맨드라인 인수:</h3>
     * <pre>{@code
     * java -jar hms-client.jar # 기본 설정 (localhost, 8080)
     * java -jar hms-client.jar [호스트주소] [포트번호] # 커스텀 설정
     * }</pre>
     * @param args 커맨드라인 인수 배열
     */
    public static void main(String[] args) {
        // 서버 주소 설정 (커맨드라인 인수 또는 기본값)
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 8080;
        
        logger.info("HMS 클라이언트 애플리케이션 시작");
        logger.info("연결 대상: {}:{}", host, port);
    
        FlatMacLightLaf.setup();
        SwingUtilities.invokeLater(() -> {

            // 로그인 다이얼로그 표시
            LoginDialog loginDialog = new LoginDialog(new JFrame(), host, port);
            loginDialog.setVisible(true);
            
            
            // 로그인 성공 시에만 메인 프레임 표시
            if (SessionManager.getInstance().isLoggedIn()) {
                logger.info("로그인 성공, 메인 프레임 표시");
                MainFrame mainFrame = new MainFrame(host, port);
                mainFrame.setVisible(true);
                mainFrame.updateStatusBar("로그인 사용자: " + SessionManager.getInstance().getUserName());
            } else {
                logger.info("로그인 취소, 애플리케이션 종료");
                System.exit(0);
            }
        });
        
        logger.info("HMS 클라이언트 애플리케이션 종료");
    }
}