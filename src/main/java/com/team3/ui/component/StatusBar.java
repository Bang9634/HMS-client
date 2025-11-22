package com.team3.ui.component;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 상태바 컴포넌트
 */
public class StatusBar extends JLabel {
    
    private static final Logger logger = LoggerFactory.getLogger(StatusBar.class);
    
    public StatusBar(String serverHost, int serverPort) {
        super(" 서버: " + serverHost + ":" + serverPort);
        
        setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }
    
    /**
     * 상태 메시지 업데이트
     */
    public void updateMessage(String message) {
        setText(" " + message);
        logger.debug("상태바 업데이트: {}", message);
    }
}