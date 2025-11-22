package com.team3.view;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.List;
import com.team3.model.Protocol;

public class ReportFrame extends JFrame {

    private final JTextArea txtLog; // 로그 보여줄 화면
    private final JButton btnClose; // 닫기 버튼

    public ReportFrame() {
        // 1. 화면 디자인 (GUI)
        setTitle("요금 변경 및 예외 보고서");
        setSize(500, 600);
        setLayout(new BorderLayout());

        // 제목
        JLabel title = new JLabel("=== 시스템 로그 리포트 ===", SwingConstants.CENTER);
        title.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(title, BorderLayout.NORTH);

        // 내용 (스크롤 가능한 텍스트 영역)
        txtLog = new JTextArea();
        txtLog.setEditable(false); // 읽기 전용
        txtLog.setFont(new Font("Monospaced", Font.PLAIN, 14)); // 고정폭 글꼴
        add(new JScrollPane(txtLog), BorderLayout.CENTER);

        // 하단 버튼
        JPanel btnPanel = new JPanel();
        btnClose = new JButton("닫기");
        btnClose.addActionListener(e -> dispose());
        btnPanel.add(btnClose);
        add(btnPanel, BorderLayout.SOUTH);

        // 2. 서버에서 데이터 가져오기
        loadReportData();
        
        setVisible(true);
    }

    // 서버와 통신하여 로그를 가져오는 메소드
    private void loadReportData() {
        try {
            txtLog.setText("데이터를 불러오는 중...\n");

            try (Socket socket = new Socket("localhost", 3000)) {
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                
                // 보고서 요청 (명령어: 301 - 아래 Protocol 수정 필요)
                Protocol request = new Protocol(301, null);
                out.writeObject(request);
                out.flush();
                
                // 응답 받기
                Protocol response = (Protocol) in.readObject();
                
                if (response.getType() == Protocol.PT_SUCCESS) {
                    // 성공 시 리스트를 받아서 한 줄씩 출력
                    List<String> logs = (List<String>) response.getData();
                    txtLog.setText(""); // 초기화
                    
                    if (logs.isEmpty()) {
                        txtLog.append("기록된 로그가 없습니다.\n");
                    } else {
                        for (String line : logs) {
                            txtLog.append(line + "\n");
                        }
                    }
                } else {
                    txtLog.setText("불러오기 실패: " + response.getData());
                }
            }

        } catch (Exception e) {
            txtLog.setText("서버 연결 실패!\nServerMain을 확인하세요.");
            e.printStackTrace();
        }
    }
    
    // 테스트용 메인
    public static void main(String[] args) {
        new ReportFrame();
    }
}