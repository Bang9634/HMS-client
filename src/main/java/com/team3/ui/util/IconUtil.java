package com.team3.ui.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * 아이콘 생성 유틸리티
 * 
 * <p>
 * 이모지를 기반으로 아이콘을 생성할 때 사용한다.
 * </p>
 * 
 * @author bang9634
 * @since 2025-11-22
 */
public class IconUtil {
    
    /**
     * 이모지 기반 아이콘 생성
     * 
     * @param emoji 아이콘으로 생성할 이모지
     * @return 아이콘 객체를 반환
     */
    public static Icon createEmojiIcon(String emoji) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
                g2.drawString(emoji, x, y + 14);
            }
            
            @Override
            public int getIconWidth() { return 20; }
            @Override
            public int getIconHeight() { return 20; }
        };
    }
    
    /**
     * 플레이스홀더 패널 생성
     * <p>
     * 플레이스홀더 패널에 출력할 문자열을 입력하고,
     * 문자열 뒤에 (개발 예정) 문자열을 추가한다.
     * </p>
     * 
     * @param message 플레이스홀더에 작성할 메시지
     * @return 플레이스홀더 패널을 반환
     */
    public static JPanel createPlaceholderPanel(String message) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel label = new JLabel(message + " (개발 예정)");
        label.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        label.setForeground(Color.GRAY);
        
        panel.add(label);
        return panel;
    }
}