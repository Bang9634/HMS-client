package com.team3.ui.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.team3.client.api.UserApi;
import com.team3.dto.request.LoginRequest;
import com.team3.dto.response.ApiResponse;
import com.team3.session.SessionManager;
import com.team3.util.JsonUtil;

/**
 * 로그인 다이얼로그
 * 모달 방식으로 로그인 성공 전까지 메인 프레임 접근 불가
 */
public class LoginDialog extends JDialog {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginDialog.class);
    
    private final UserApi userApi;
    
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;
    
    public LoginDialog(Frame parent, String serverHost, int serverPort) {
        super(parent, "HMS 로그인", true); // true = 모달
        this.userApi = new UserApi(serverHost, serverPort);
        
        initComponents();
        setupLayout();
        
        // 다이얼로그 설정
        setSize(400, 250);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);
        
        // X 버튼 클릭 시 처리
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleCancel();
            }
        });
        
        logger.info("LoginDialog 초기화 완료");
    }
    
    private void initComponents() {
        userIdField = new JTextField(20);
        passwordField = new JPasswordField(20);
        
        loginButton = new JButton("로그인");
        loginButton.setPreferredSize(new Dimension(100, 35));
        loginButton.setBackground(new Color(52, 152, 219));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> handleLogin());

        
        cancelButton = new JButton("취소");
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.addActionListener(e -> handleCancel());
        
        // Enter 키로 로그인
        getRootPane().setDefaultButton(loginButton);
    }
    
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 제목
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("HMS 호텔 관리 시스템", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        mainPanel.add(titleLabel, gbc);
        
        gbc.gridy++;
        JSeparator separator = new JSeparator();
        mainPanel.add(separator, gbc);
        
        // 사용자 ID
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy++;
        JLabel userIdLabel = new JLabel("사용자 ID:");
        userIdLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        mainPanel.add(userIdLabel, gbc);
        
        gbc.gridx = 1;
        mainPanel.add(userIdField, gbc);
        
        // 비밀번호
        gbc.gridx = 0; gbc.gridy++;
        JLabel passwordLabel = new JLabel("비밀번호:");
        passwordLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        mainPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        mainPanel.add(passwordField, gbc);
        
        // 버튼 패널
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, gbc);
        
        setContentPane(mainPanel);
    }
    
    /**
     * 로그인 처리
     */
    private void handleLogin() {
        String userId = userIdField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (userId.isEmpty()) {
            showError("사용자 ID를 입력해주세요");
            userIdField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showError("비밀번호를 입력해주세요");
            passwordField.requestFocus();
            return;
        }
        
        // 버튼 비활성화
        setButtonsEnabled(false);
        
        // 백그라운드에서 로그인 처리
        SwingWorker<ApiResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected ApiResponse doInBackground() {
                LoginRequest request = new LoginRequest(userId, password);
                return userApi.login(request);
            }
            
            @Override
            protected void done() {
                try {
                    ApiResponse response = get();
                    
                    if (response.isSuccess()) {
                        logger.info("로그인 성공: {}", userId);
                        String responseBody = response.getBody();
                        SessionManager.getInstance().login(
                            JsonUtil.extract(responseBody, "token"),
                            JsonUtil.extract(responseBody, "userId"),
                            JsonUtil.extract(responseBody, "userName"),
                            JsonUtil.extract(responseBody, "role")
                        );
                        JOptionPane.showMessageDialog(
                            LoginDialog.this,
                            "로그인 성공!\n환영합니다, " + SessionManager.getInstance().getUserName() + "님",
                            "로그인 성공",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        
                        dispose(); // 다이얼로그 닫기
                        
                    } else {
                        logger.warn("로그인 실패: {}", response.getBody());
                        showError("로그인 실패\n" + response.getBody());
                        passwordField.setText("");
                        passwordField.requestFocus();
                    }
                    
                } catch (HeadlessException | InterruptedException | ExecutionException e) {
                    logger.error("로그인 중 오류", e);
                    showError("로그인 중 오류가 발생했습니다\n" + e.getMessage());
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };
        
        worker.execute();
    }

    /**
     * 취소 처리 (프로그램 종료)
     */
    private void handleCancel() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "로그인하지 않고 종료하시겠습니까?",
            "종료 확인",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            logger.info("사용자가 로그인 취소");
            dispose();
        }
    }
    
    /**
     * 버튼 활성화/비활성화
     */
    private void setButtonsEnabled(boolean enabled) {
        loginButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
        userIdField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
    }
    
    /**
     * 에러 메시지 표시
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "오류",
            JOptionPane.ERROR_MESSAGE
        );
    }
}