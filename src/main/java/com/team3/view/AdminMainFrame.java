package com.team3.view;

import javax.swing.*;
import java.awt.*;

public class AdminMainFrame extends JFrame {

    public AdminMainFrame() {
        // 1. 기본 창 설정
        setTitle("관리자 메뉴 (Admin Mode)");
        setSize(400, 600); // 버튼이 늘어나서 세로 길이를 좀 늘렸습니다
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setLayout(new BorderLayout()); 

        // 2. 제목
        JLabel titleLabel = new JLabel("관리자 시스템", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // 3. 버튼 패널
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new GridLayout(7, 1, 10, 10)); // 버튼 7개로 늘림
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 20, 50));

        // 버튼 생성
        JButton addUser = new JButton("사용자 추가 (Add User)");
        
        // ★ [여기에 버튼 추가했습니다!] ★
        JButton listUser = new JButton("사용자 목록 조회 (User List)"); 
        
        JButton delUser = new JButton("사용자 삭제 (Delete User)");
        JButton roomInfo = new JButton("객실 정보 (Room Info)");
        JButton payment = new JButton("결제 관리 (Payment)");
        JButton report = new JButton("보고서 (Report)");
        JButton logout = new JButton("로그아웃 (Logout)");

        // 패널에 붙이기
        btnPanel.add(addUser);
        btnPanel.add(listUser); // ★ 패널에도 추가!
        btnPanel.add(delUser);
        btnPanel.add(roomInfo);
        btnPanel.add(payment);
        btnPanel.add(report);
        btnPanel.add(logout);

        add(btnPanel, BorderLayout.CENTER);

        // 4. 이벤트 연결 (버튼 누르면 이동)
        
        // 사용자 추가
        addUser.addActionListener(e -> {
            new UserAddFrame().setVisible(true);
        });
        
        // ★ [사용자 목록 조회 연결!] ★
        listUser.addActionListener(e -> {
            // 목록창을 엽니다. (UserListFrame 파일이 있어야 함)
            new UserListFrame().setVisible(true); 
        });
        
        // 사용자 삭제
        delUser.addActionListener(e -> {
            new UserDeleteFrame().setVisible(true);
        });
        
        // 객실 정보
        roomInfo.addActionListener(e -> {
            new RoomInfoFrame().setVisible(true); 
        });
        
        // 결제 관리
        payment.addActionListener(e -> {
            new PaymentFrame().setVisible(true);
        });
        
        // 보고서
        report.addActionListener(e -> {
            new ReportFrame().setVisible(true);
        });

        // 로그아웃
        logout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, "로그아웃 하시겠습니까?", "로그아웃", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                new LoginFrame().setVisible(true); 
                this.dispose(); 
            }
        });

        // 화면 중앙 배치
        setLocationRelativeTo(null); 
        setVisible(true);
    }

    public static void main(String[] args) {
        new AdminMainFrame();
    }
}