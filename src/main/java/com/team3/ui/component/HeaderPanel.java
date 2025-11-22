package com.team3.ui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * 헤더 패널 컴포넌트
 */
public class HeaderPanel extends JPanel {
    
    public HeaderPanel(String serverHost, int serverPort) {
        initComponents(serverHost, serverPort);
    }
    
    private void initComponents(String serverHost, int serverPort) {
        setLayout(new BorderLayout());
        setBackground(new Color(41, 128, 185));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // 타이틀
        JLabel titleLabel = new JLabel("HMS 호텔 관리 시스템");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        // 서버 정보
        JLabel serverLabel = new JLabel("Server: " + serverHost + ":" + serverPort);
        serverLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        serverLabel.setForeground(new Color(236, 240, 241));
        
        // 수직 박스로 배치
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(serverLabel);
        
        add(textPanel, BorderLayout.WEST);
    }
}