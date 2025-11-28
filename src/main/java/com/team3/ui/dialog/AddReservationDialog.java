package com.team3.ui.dialog;

import java.awt.*;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

import com.team3.client.api.ReservationApi;
import com.team3.dto.request.AddReservationRequest;
import com.team3.dto.request.UpdateReservationRequest;
import com.team3.dto.response.ApiResponse;
import com.team3.model.Reservation;

public class AddReservationDialog extends JDialog {

    private JTextField roomIdField;
    private JTextField guestNameField;
    private JTextField phoneField;
    private JTextField checkInField;
    private JTextField checkOutField;
    private JTextField guestCountField;
    
    private JButton okButton;
    private JButton cancelButton;
    private JLabel statusLabel;

    private final ReservationApi reservationApi;
    
    // 수정 모드 관련 변수
    private Reservation existingReservation = null; 
    private boolean isEditMode = false;

    // 1. 기본 생성자 (추가용)
    public AddReservationDialog(Window parent, ReservationApi reservationApi) {
        this(parent, reservationApi, null);
    }

    // 2. [수정] 통합 생성자 (수정용 데이터 받음)
    public AddReservationDialog(Window parent, ReservationApi reservationApi, Reservation reservation) {
        super(parent, reservation == null ? "예약 추가" : "예약 수정", ModalityType.APPLICATION_MODAL);
        this.reservationApi = reservationApi;
        this.existingReservation = reservation;
        this.isEditMode = (reservation != null);
        
        initComponents();
        setupLayout();
        
        // 수정 모드면 데이터 채우기
        if (isEditMode) {
            fillData();
        }
        
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        roomIdField = new JTextField(15);
        guestNameField = new JTextField(15);
        phoneField = new JTextField(15);
        checkInField = new JTextField(15);
        checkOutField = new JTextField(15);
        guestCountField = new JTextField("2", 15);

        // 버튼 텍스트 변경 (예약하기 / 수정완료)
        okButton = new JButton(isEditMode ? "수정완료" : "예약하기");
        okButton.setBackground(new Color(39, 174, 96));
        okButton.setForeground(Color.WHITE);
        okButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        okButton.addActionListener(e -> handleSubmit());

        cancelButton = new JButton("취소");
        cancelButton.setBackground(new Color(192, 57, 43));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        cancelButton.addActionListener(e -> dispose());

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(52, 73, 94));
    }

    private void setupLayout() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        addFormField(formPanel, gbc, 0, "객실 번호:", roomIdField);
        addFormField(formPanel, gbc, 1, "예약자명:", guestNameField);
        addFormField(formPanel, gbc, 2, "전화번호:", phoneField);
        addFormField(formPanel, gbc, 3, "체크인:", checkInField);
        addFormField(formPanel, gbc, 4, "체크아웃:", checkOutField);
        addFormField(formPanel, gbc, 5, "인원 수:", guestCountField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(formPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        getContentPane().add(statusPanel, BorderLayout.NORTH);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(comp, gbc);
    }

    // [추가] 기존 데이터를 입력창에 채워넣는 메서드
    private void fillData() {
        roomIdField.setText(existingReservation.getRoomId());
        guestNameField.setText(existingReservation.getGuestName());
        phoneField.setText(existingReservation.getPhone());
        checkInField.setText(existingReservation.getCheckInDate());
        checkOutField.setText(existingReservation.getCheckOutDate());
        guestCountField.setText(String.valueOf(existingReservation.getGuestCount()));
    }

    // [수정] 추가/수정 분기 처리
    private void handleSubmit() {
        try {
            String roomId = roomIdField.getText().trim();
            String guestName = guestNameField.getText().trim();
            String phone = phoneField.getText().trim();
            String checkIn = checkInField.getText().trim();
            String checkOut = checkOutField.getText().trim();
            int guestCount = Integer.parseInt(guestCountField.getText().trim());

            if (roomId.isEmpty() || guestName.isEmpty()) {
                statusLabel.setText("필수 정보를 입력하세요.");
                return;
            }

            okButton.setEnabled(false);
            statusLabel.setText("처리 중...");

            SwingWorker<ApiResponse, Void> worker = new SwingWorker<>() {
                @Override protected ApiResponse doInBackground() {
                    if (isEditMode) {
                        // [수정 요청]
                        UpdateReservationRequest req = new UpdateReservationRequest(
                            existingReservation.getId(), // ID 필수!
                            existingReservation.getUserId(),
                            roomId, guestName, phone, checkIn, checkOut, guestCount
                        );
                        return reservationApi.updateReservation(req);
                    } else {
                        // [생성 요청]
                        AddReservationRequest req = new AddReservationRequest(
                            roomId, guestName, phone, checkIn, checkOut, guestCount
                        );
                        return reservationApi.createReservation(req);
                    }
                }

                @Override protected void done() {
                    okButton.setEnabled(true);
                    try {
                        ApiResponse response = get();
                        if (response.isSuccess()) {
                            JOptionPane.showMessageDialog(AddReservationDialog.this, isEditMode ? "수정되었습니다." : "예약되었습니다.");
                            dispose();
                        } else {
                            statusLabel.setText("실패: " + response.getBody());
                        }
                    } catch (Exception e) {
                        statusLabel.setText("오류: " + e.getMessage());
                    }
                }
            };
            worker.execute();

        } catch (NumberFormatException e) {
            statusLabel.setText("인원 수는 숫자여야 합니다.");
        }
    }
}