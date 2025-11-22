package com.team3.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.team3.client.api.HealthApi;
import com.team3.dto.response.ApiResponse;

/**
 * Health Check 패널
 */
public class HealthCheckPanel extends JPanel {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckPanel.class);
    
    private final HealthApi healthApi;
    
    private JButton checkButton;
    private JTextArea resultArea;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    
    public HealthCheckPanel(String serverHost, int serverPort) {
        this.healthApi = new HealthApi(serverHost, serverPort);
        
        initComponents();
        setupLayout();
        
        logger.info("HealthCheckPanel 초기화 완료");
    }
    
    private void initComponents() {
        setBackground(Color.WHITE);
        
        // 버튼
        checkButton = new JButton("Health Check 요청");
        checkButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        checkButton.setPreferredSize(new Dimension(200, 40));
        checkButton.setBackground(new Color(52, 152, 219));
        checkButton.setForeground(Color.WHITE);
        checkButton.setFocusPainted(false);
        checkButton.addActionListener(e -> handleHealthCheck());
        
        // 결과 표시 영역
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        resultArea.setMargin(new Insets(10, 10, 10, 10));
        resultArea.setBackground(new Color(248, 249, 250));
        
        // 상태 레이블
        statusLabel = new JLabel("대기 중...");
        statusLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        
        // 프로그레스 바
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 상단 패널 (버튼)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(checkButton);
        
        add(topPanel, BorderLayout.NORTH);
        
        // 중앙 패널 (결과 표시)
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            "응답 결과",
            0,
            0,
            new Font("맑은 고딕", Font.BOLD, 12)
        ));
        centerPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // 하단 패널 (상태 + 프로그레스)
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(progressBar, BorderLayout.CENTER);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void handleHealthCheck() {
        logger.info("Health Check 버튼 클릭");
        
        checkButton.setEnabled(false);
        statusLabel.setText("서버에 요청 중...");
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        resultArea.setText("요청 중...\n");
        
        SwingWorker<ApiResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected ApiResponse doInBackground() {
                return healthApi.checkHealth();
            }
            
            @Override
            protected void done() {
                try {
                    ApiResponse response = get();
                    displayResult(response);
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Health Check 실패", e);
                    displayError(e);
                } finally {
                    checkButton.setEnabled(true);
                    progressBar.setVisible(false);
                    progressBar.setIndeterminate(false);
                }
            }
        };
        
        worker.execute();
    }
    
    private void displayResult(ApiResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════\n");
        sb.append("  응답 결과\n");
        sb.append("═══════════════════════════════════════\n\n");
        
        sb.append("상태 코드: ").append(response.getStatusCode()).append("\n");
        sb.append("성공 여부: ").append(response.isSuccess() ? "✓ 성공" : "✗ 실패").append("\n\n");
        
        sb.append("응답 본문:\n");
        sb.append("───────────────────────────────────────\n");
        sb.append(response.getBody()).append("\n");
        sb.append("───────────────────────────────────────\n");
        
        resultArea.setText(sb.toString());
        
        if (response.isSuccess()) {
            statusLabel.setText("✓ 서버가 정상적으로 응답했습니다");
            statusLabel.setForeground(new Color(39, 174, 96));
            logger.info("Health Check 성공");
        } else {
            statusLabel.setText("✗ 서버 응답 오류 (상태 코드: " + response.getStatusCode() + ")");
            statusLabel.setForeground(new Color(231, 76, 60));
            logger.warn("Health Check 실패: {}", response.getStatusCode());
        }
    }
    
    private void displayError(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════\n");
        sb.append("  오류 발생\n");
        sb.append("═══════════════════════════════════════\n\n");
        
        sb.append("오류 메시지:\n");
        sb.append(e.getMessage()).append("\n\n");
        
        sb.append("오류 유형:\n");
        sb.append(e.getClass().getSimpleName()).append("\n");
        
        resultArea.setText(sb.toString());
        
        statusLabel.setText("✗ 오류 발생: " + e.getMessage());
        statusLabel.setForeground(new Color(231, 76, 60));
    }
}