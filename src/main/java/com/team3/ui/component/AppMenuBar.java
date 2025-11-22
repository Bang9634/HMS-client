package com.team3.ui.component;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 애플리케이션 메뉴바
 */
public class AppMenuBar extends JMenuBar {
    
    private static final Logger logger = LoggerFactory.getLogger(AppMenuBar.class);
    
    private final JFrame parentFrame;
    private final JTabbedPane tabbedPane;
    private final String serverHost;
    private final int serverPort;
    
    public AppMenuBar(JFrame parentFrame, JTabbedPane tabbedPane, String serverHost, int serverPort) {
        this.parentFrame = parentFrame;
        this.tabbedPane = tabbedPane;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        
        initMenus();
    }
    
    private void initMenus() {
        add(createFileMenu());
        add(createViewMenu());
        add(createHelpMenu());
    }
    
    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("파일");
        fileMenu.setMnemonic('F');
        
        JMenuItem refreshItem = new JMenuItem("새로고침");
        refreshItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
        refreshItem.addActionListener(e -> refreshCurrentTab());
        
        JMenuItem exitItem = new JMenuItem("종료");
        exitItem.setAccelerator(KeyStroke.getKeyStroke("alt Q"));
        exitItem.addActionListener(e -> exitApplication());
        
        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        return fileMenu;
    }
    
    private JMenu createViewMenu() {
        JMenu viewMenu = new JMenu("보기");
        viewMenu.setMnemonic('V');
        
        String[] tabNames = {"Health Check", "사용자 관리", "객실 관리", "예약 관리"};
        for (int i = 0; i < tabNames.length; i++) {
            final int index = i;
            JMenuItem item = new JMenuItem(tabNames[i]);
            item.addActionListener(e -> tabbedPane.setSelectedIndex(index));
            viewMenu.add(item);
        }
        
        return viewMenu;
    }
    
    private JMenu createHelpMenu() {
        JMenu helpMenu = new JMenu("도움말");
        helpMenu.setMnemonic('H');
        
        JMenuItem aboutItem = new JMenuItem("프로그램 정보");
        aboutItem.addActionListener(e -> showAboutDialog());
        
        helpMenu.add(aboutItem);
        
        return helpMenu;
    }
    
    private void refreshCurrentTab() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        String tabTitle = tabbedPane.getTitleAt(selectedIndex);
        
        logger.info("탭 새로고침: {}", tabTitle);
        
        JOptionPane.showMessageDialog(
            parentFrame,
            tabTitle + " 탭을 새로고침했습니다.",
            "새로고침",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    private void exitApplication() {
        int option = JOptionPane.showConfirmDialog(
            parentFrame,
            "정말 종료하시겠습니까?",
            "종료 확인",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            logger.info("애플리케이션 종료");
            System.exit(0);
        }
    }
    
    private void showAboutDialog() {
        String message = """
            HMS 클라이언트 v1.0
            
            호텔 관리 시스템 자바 클라이언트
            
            개발: Team 3
            서버: %s:%d
            """.formatted(serverHost, serverPort);
        
        JOptionPane.showMessageDialog(
            parentFrame,
            message,
            "프로그램 정보",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}