package com.team3.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.team3.client.api.ReservationApi;
import com.team3.dto.request.DeleteReservationRequest;
import com.team3.dto.response.ApiResponse;
import com.team3.model.Reservation;
import com.team3.ui.dialog.AddReservationDialog;

public class ReservationPanel extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(ReservationPanel.class);

    private final ReservationApi reservationApi;
    private JButton refreshButton;
    private JButton addReservationButton;
    private JTable reservationTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JProgressBar progressBar;

    private static final String[] COLUMN_NAMES = {
        "예약ID", "객실번호", "예약자명", "전화번호", "체크인", "체크아웃", "인원", "수정", "취소"
    };

    public ReservationPanel(String serverHost, int serverPort) {
        this.reservationApi = new ReservationApi(serverHost, serverPort);
        initComponents();
        setupLayout();
        loadReservationList();
        logger.info("ReservationPanel 초기화 완료");
    }

    private void initComponents() {
        setBackground(Color.WHITE);

        refreshButton = new JButton("새로고침");
        refreshButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        refreshButton.setBackground(new Color(52, 152, 219)); // 파란색
        refreshButton.setForeground(Color.BLACK);
        refreshButton.setFocusPainted(false);
        refreshButton.setPreferredSize(new Dimension(120, 36));
        refreshButton.addActionListener(e -> loadReservationList());

        addReservationButton = new JButton("예약 추가");
        addReservationButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        addReservationButton.setBackground(new Color(39, 174, 96)); // 녹색
        addReservationButton.setForeground(Color.BLACK);
        addReservationButton.setFocusPainted(false);
        addReservationButton.setPreferredSize(new Dimension(120, 36));
        addReservationButton.addActionListener(e -> handleAddReservation());

        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7 || column == 8; // 수정(7), 취소(8)
            }
        };
        reservationTable = new JTable(tableModel);
        reservationTable.setRowHeight(32);
        reservationTable.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        reservationTable.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 13));
        reservationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 버튼 설정 (RoomManagePanel 스타일)
        reservationTable.getColumn("수정").setCellRenderer(new ButtonRenderer("수정", new Color(241, 196, 15)));
        reservationTable.getColumn("수정").setCellEditor(new ButtonEditor("수정", this::handleEditReservation));
        
        reservationTable.getColumn("취소").setCellRenderer(new ButtonRenderer("취소", new Color(231, 76, 60)));
        reservationTable.getColumn("취소").setCellEditor(new ButtonEditor("취소", this::handleCancelReservation));

        // ID 컬럼 숨김
        reservationTable.getColumnModel().getColumn(0).setMinWidth(0);
        reservationTable.getColumnModel().getColumn(0).setMaxWidth(0);
        reservationTable.getColumnModel().getColumn(0).setWidth(0);

        statusLabel = new JLabel("대기 중...");
        statusLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(refreshButton);
        topPanel.add(addReservationButton);
        add(topPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(reservationTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            "예약 목록",
            0, 0,
            new Font("맑은 고딕", Font.BOLD, 12)
        ));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(progressBar, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadReservationList() {
        statusLabel.setText("예약 목록 조회 중...");
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        tableModel.setRowCount(0);

        SwingWorker<ApiResponse, Void> worker = new SwingWorker<>() {
            @Override protected ApiResponse doInBackground() {
                return reservationApi.getReservations();
            }
            @Override protected void done() {
                try {
                    ApiResponse response = get();
                    if (response.isSuccess()) {
                        List<Reservation> list = parseReservationList(response.getBody());
                        updateTable(list);
                        statusLabel.setText("✓ 예약 목록 조회 성공 (" + list.size() + "건)");
                        statusLabel.setForeground(new Color(39, 174, 96));
                    } else {
                        statusLabel.setText("✗ 조회 실패: " + response.getBody());
                        statusLabel.setForeground(new Color(231, 76, 60));
                    }
                } catch (Exception e) {
                    statusLabel.setText("✗ 오류: " + e.getMessage());
                    statusLabel.setForeground(new Color(231, 76, 60));
                } finally {
                    progressBar.setVisible(false);
                    progressBar.setIndeterminate(false);
                }
            }
        };
        worker.execute();
    }

    private List<Reservation> parseReservationList(String jsonBody) {
        Gson gson = new Gson();
        try {
            com.google.gson.JsonObject jsonObject = gson.fromJson(jsonBody, com.google.gson.JsonObject.class);
            if (jsonObject.has("reservations")) {
                Type listType = new TypeToken<List<Reservation>>(){}.getType();
                return gson.fromJson(jsonObject.get("reservations"), listType);
            }
        } catch (JsonSyntaxException e) {
            logger.error("JSON 파싱 실패", e);
        }
        return java.util.Collections.emptyList();
    }

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
            row.add("수정");
            row.add("취소");
            tableModel.addRow(row);
        }
    }

    private void handleAddReservation() {
        AddReservationDialog dialog = new AddReservationDialog(
            SwingUtilities.getWindowAncestor(this), reservationApi
        );
        dialog.setVisible(true);
        loadReservationList();
    }

    private void handleEditReservation(int row) {
        Reservation res = new Reservation();
        res.setId((String) tableModel.getValueAt(row, 0));
        res.setRoomId((String) tableModel.getValueAt(row, 1));
        res.setGuestName((String) tableModel.getValueAt(row, 2));
        res.setPhone((String) tableModel.getValueAt(row, 3));
        res.setCheckInDate((String) tableModel.getValueAt(row, 4));
        res.setCheckOutDate((String) tableModel.getValueAt(row, 5));
        res.setGuestCount((Integer) tableModel.getValueAt(row, 6));

        AddReservationDialog dialog = new AddReservationDialog(
            SwingUtilities.getWindowAncestor(this), reservationApi, res
        );
        dialog.setVisible(true);
        loadReservationList();
    }

    private void handleCancelReservation(int row) {
        String id = (String) tableModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "취소하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION) == 0) {
            SwingWorker<ApiResponse, Void> worker = new SwingWorker<>() {
                @Override protected ApiResponse doInBackground() {
                    return reservationApi.cancelReservation(new DeleteReservationRequest(id));
                }
                @Override protected void done() { loadReservationList(); }
            };
            worker.execute();
        }
    }

    // --- 내부 클래스 (RoomManagePanel과 동일) ---
    private static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer(String text, Color bg) {
            setText(text);
            setFont(new Font("맑은 고딕", Font.BOLD, 12));
            setBackground(bg);
            setForeground(Color.BLACK);
            setFocusPainted(false);
        }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) { return this; }
    }

    private static class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private final java.util.function.IntConsumer action;
        private int row;

        public ButtonEditor(String text, java.util.function.IntConsumer action) {
            super(new JTextField());
            this.action = action;
            button = new JButton(text);
            button.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            button.setBackground(text.equals("취소") ? new Color(231, 76, 60) : new Color(241, 196, 15));
            button.setForeground(Color.BLACK);
            button.setFocusPainted(false);
            button.addActionListener(e -> action.accept(row));
        }
        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { this.row = r; return button; }
        @Override public Object getCellEditorValue() { return button.getText(); }
    }
}