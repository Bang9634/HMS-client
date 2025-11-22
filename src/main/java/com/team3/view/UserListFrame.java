package com.team3.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.net.Socket;
import java.util.List;
import com.team3.model.Protocol;
import com.team3.model.User;

public class UserListFrame extends javax.swing.JFrame {

    public UserListFrame() {
        initComponents(); // 디자인 초기화
        setTitle("전체 사용자 목록");
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        
        // 1. 표의 제목줄(컬럼) 설정
        String[] colNames = {"아이디", "비밀번호", "이름", "권한"};
        DefaultTableModel model = new DefaultTableModel(colNames, 0);
        tblUsers.setModel(model);
        
        // 2. 창이 열리자마자 서버에서 데이터 가져오기
        loadUserData();
    }
    
    // 서버와 통신해서 목록을 가져오는 핵심 함수
    private void loadUserData() {
        try {
            Socket socket = new Socket("localhost", 3000);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // 서버에게 "목록 내놔(203번)" 요청
            Protocol request = new Protocol(Protocol.PT_USER_LIST_REQ, null);
            out.writeObject(request);
            out.flush();

            // 응답 받기
            Protocol response = (Protocol) in.readObject();

            if (response.getType() == Protocol.PT_SUCCESS) {
                List<User> users = (List<User>) response.getData();
                
                // 표 내용 초기화 (기존 데이터 삭제)
                DefaultTableModel model = (DefaultTableModel) tblUsers.getModel();
                model.setRowCount(0); 

                // 가져온 데이터를 한 줄씩 표에 추가
                for (User u : users) {
                    Object[] row = { u.getId(), u.getPw(), u.getName(), u.getRole() };
                    model.addRow(row);
                }
            } else {
                JOptionPane.showMessageDialog(this, "목록을 불러오지 못했습니다.");
            }
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "서버 연결 실패!");
        }
    }

    // NetBeans 자동 생성 코드 (화면 디자인)
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        tblUsers = new javax.swing.JTable();
        btnRefresh = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jScrollPane1.setViewportView(tblUsers);

        btnRefresh.setText("새로고침");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadUserData(); // 버튼 누르면 다시 불러오기
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                    .addComponent(btnRefresh, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
    }// </editor-fold>                        

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new UserListFrame().setVisible(true));
    }
    
    // 변수 선언
    private javax.swing.JButton btnRefresh;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblUsers;                 
}