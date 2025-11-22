package com.team3.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

// 우리가 만든 서버 모델 가져오기
import com.team3.model.User;
import com.team3.model.Protocol;

public class LoginFrame extends JFrame {

    public LoginFrame() {
        // 1. 화면 설정
        setTitle("Login");
        setSize(300, 200);
        setLayout(new GridLayout(3, 2));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 닫으면 프로그램 종료

        // 2. 컴포넌트 생성
        JLabel idLabel = new JLabel("ID");
        JTextField idField = new JTextField();

        JLabel pwLabel = new JLabel("PW");
        JPasswordField pwField = new JPasswordField();

        JButton loginBtn = new JButton("Login");

        // 3. 화면에 붙이기
        add(idLabel); add(idField);
        add(pwLabel); add(pwField);
        add(loginBtn);

        // 4. 로그인 버튼 이벤트 (서버 통신 로직)
        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String id = idField.getText();
                String pw = new String(pwField.getPassword());

                if (id.isEmpty() || pw.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "아이디와 비번을 입력하세요.");
                    return;
                }

                try {
                    // --- [서버 통신 시작] ---
                    Socket socket = new Socket("localhost", 3000);
                    
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                    // 문자열 대신 Protocol 객체로 포장해서 보냄
                    User loginUser = new User(id, pw);
                    Protocol request = new Protocol(Protocol.PT_LOGIN_REQ, loginUser);

                    out.writeObject(request);
                    out.flush();

                    // 응답 받기
                    Protocol response = (Protocol) in.readObject();

                    if (response.getType() == Protocol.PT_LOGIN_RES) {
                        User myInfo = (User) response.getData();
                        JOptionPane.showMessageDialog(null, myInfo.getName() + "님 환영합니다!");

                        // 화면 분기
                        if ("ADMIN".equals(myInfo.getRole())) {
                             new AdminMainFrame().setVisible(true); // 파일 있으면 주석 해제
                             System.out.println("관리자 화면으로 이동");
                        } else {
                             new UserMainFrame().setVisible(true); // 파일 있으면 주석 해제
                             System.out.println("사용자 화면으로 이동");
                        }
                        dispose(); // 로그인 창 닫기
                        
                    } else {
                        JOptionPane.showMessageDialog(null, "로그인 실패!");
                    }
                    
                    socket.close();
                    // --- [서버 통신 끝] ---

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "서버 연결 오류: " + ex.getMessage());
                }
            }
        });

        setVisible(true); // 화면 표시
    }
    
    // 테스트용 실행 메인
    public static void main(String[] args) {
        new LoginFrame();
    }
}