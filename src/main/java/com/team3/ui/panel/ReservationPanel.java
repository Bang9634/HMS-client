package com.team3.ui.panel;

import java.awt.*;
import java.util.List;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.team3.client.api.ReservationApi;
import com.team3.dto.request.DeleteReservationRequest;
import com.team3.dto.response.ApiResponse;
import com.team3.model.Reservation;
import com.team3.ui.dialog.AddReservationDialog;

public class ReservationPanel extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(ReservationPanel.class);
    private final ReservationApi reservationApi;

    private JTable reservationTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JProgressBar progressBar;

    // [수정] 컬럼에 "수정" 추가
    private static final String[] COLUMN_NAMES = {
        "예약ID", "객실번호", "예약자명", "전화번호", "체크인", "체크아웃", "인원", "수정", "취소"
    };

    public ReservationPanel(String serverHost, int serverPort) {
        this.reservationApi = new ReservationApi(serverHost, serverPort);
        initComponents();
        setupLayout();
        loadReservationList();
    }

    private void initComponents() {
        setBackground(Color.WHITE);

        JButton refreshButton = new JButton("새로고침");
        refreshButton.addActionListener(e -> loadReservationList());
        
        JButton addButton = new JButton("예약 추가");
        addButton.setBackground(new Color(39, 174, 96));
        addButton.setForeground(Color.BLACK);
        addButton.addActionListener(e -> handleAddReservation());

        // [수정] 테이블 모델 (수정, 취소 버튼 클릭 가능하게 설정)
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7 || column == 8; // 7:수정, 8:취소
            }
        };

        reservationTable = new JTable(tableModel);
        reservationTable.setRowHeight(32);
        
        // [추가] "수정" 버튼 렌더러/에디터 (노란색)
        reservationTable.getColumn("수정").setCellRenderer(new ButtonRenderer("수정", new Color(241, 196, 15)));
        reservationTable.getColumn("수정").setCellEditor(new ButtonEditor("수정", this::handleEditReservation));

        // [기존] "취소" 버튼 렌더러/에디터 (빨간색)
        reservationTable.getColumn("취소").setCellRenderer(new ButtonRenderer("취소", new Color(231, 76, 60)));
        reservationTable.getColumn("취소").setCellEditor(new ButtonEditor("취소", this::handleCancelReservation));

        // ID 컬럼 숨기기
        reservationTable.getColumnModel().getColumn(0).setMinWidth(0);
        reservationTable.getColumnModel().getColumn(0).setMaxWidth(0);
        reservationTable.getColumnModel().getColumn(0).setWidth(0);

        statusLabel = new JLabel("대기 중...");
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        
        // (상단 버튼 패널 구성은 생략 - 이전과 동일하게 배치하면 됨)
        // ...
    }
    
    // setupLayout()은 이전과 동일 (생략 가능)
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("새로고침");
        refresh.addActionListener(e -> loadReservationList());
        JButton add = new JButton("예약 추가");
        add.addActionListener(e -> handleAddReservation());
        top.add(refresh); top.add(add);
        add(top, BorderLayout.NORTH);
        
        add(new JScrollPane(reservationTable), BorderLayout.CENTER);
        
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(statusLabel, BorderLayout.WEST);
        bottom.add(progressBar, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    // loadReservationList()는 이전과 동일 (생략)
    private void loadReservationList() {
        // ... (API 호출 및 updateTable 호출 로직)
        new SwingWorker<ApiResponse, Void>() {
            @Override protected ApiResponse doInBackground() { return reservationApi.getReservations(); }
            @Override protected void done() {
                try {
                    ApiResponse res = get();
                    if(res.isSuccess()) {
                        // JSON 파싱 후 updateTable 호출
                        java.lang.reflect.Type listType = new TypeToken<List<Reservation>>(){}.getType();
                        com.google.gson.JsonObject json = new Gson().fromJson(res.getBody(), com.google.gson.JsonObject.class);
                        List<Reservation> list = new Gson().fromJson(json.get("reservations"), listType);
                        updateTable(list);
                        statusLabel.setText("조회 성공");
                    }
                } catch(Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    // [수정] 테이블 데이터 채우기 (컬럼 수 맞춤)
    private void updateTable(List<Reservation> list) {
        tableModel.setRowCount(0);
        for (Reservation r : list) {
            Vector<Object> row = new Vector<>();
            row.add(r.getId());
            row.add(r.getRoomId());
            row.add(r.getGuestName());
            row.add(r.getPhone());
            row.add(r.getCheckInDate());
            row.add(r.getCheckOutDate());
            row.add(r.getGuestCount());
            row.add("수정"); // 버튼
            row.add("취소"); // 버튼
            tableModel.addRow(row);
        }
    }

    // [기존] 추가 핸들러
    private void handleAddReservation() {
        AddReservationDialog dialog = new AddReservationDialog(
            SwingUtilities.getWindowAncestor(this), reservationApi
        );
        dialog.setVisible(true);
        loadReservationList();
    }

    // [추가] 수정 핸들러 (SFR-304)
    private void handleEditReservation(int row) {
        // 선택된 행의 데이터를 모아서 Reservation 객체로 복원
        Reservation res = new Reservation();
        res.setId((String) tableModel.getValueAt(row, 0));
        res.setRoomId((String) tableModel.getValueAt(row, 1));
        res.setGuestName((String) tableModel.getValueAt(row, 2));
        res.setPhone((String) tableModel.getValueAt(row, 3));
        res.setCheckInDate((String) tableModel.getValueAt(row, 4));
        res.setCheckOutDate((String) tableModel.getValueAt(row, 5));
        res.setGuestCount((Integer) tableModel.getValueAt(row, 6));
        // userId는 화면에 없으므로, 필요하다면 전체 목록 리스트에서 id로 찾아오는 게 더 정확함.
        // 여기서는 편의상 생략하거나, API에서 처리하도록 함.

        // 다이얼로그에 기존 데이터(res) 전달 -> 수정 모드 자동 활성화
        AddReservationDialog dialog = new AddReservationDialog(
            SwingUtilities.getWindowAncestor(this), 
            reservationApi, 
            res 
        );
        dialog.setVisible(true);
        loadReservationList();
    }

    // [기존] 취소 핸들러
    private void handleCancelReservation(int row) {
        String id = (String) tableModel.getValueAt(row, 0);
        if(JOptionPane.showConfirmDialog(this, "취소하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION) == 0) {
            new SwingWorker<ApiResponse, Void>() {
                @Override protected ApiResponse doInBackground() {
                    return reservationApi.cancelReservation(new DeleteReservationRequest(id));
                }
                @Override protected void done() { loadReservationList(); }
            }.execute();
        }
    }

    // --- 내부 클래스 (버튼 렌더러/에디터) - UserManagePanel과 동일 ---
    private static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer(String text, Color bg) {
            setText(text); setBackground(bg); setForeground(Color.BLACK);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) { return this; }
    }
    
    private static class ButtonEditor extends DefaultCellEditor {
        JButton btn; int r; java.util.function.IntConsumer act;
        public ButtonEditor(String t, java.util.function.IntConsumer a) {
            super(new JTextField()); act = a; btn = new JButton(t);
            btn.addActionListener(e -> { fireEditingStopped(); act.accept(r); });
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { this.r = r; return btn; }
    }
}