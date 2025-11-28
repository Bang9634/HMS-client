package com.team3.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Window;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import com.team3.client.api.ReservationApi;
import com.team3.dto.request.AddReservationRequest;
import com.team3.dto.request.UpdateReservationRequest;
import com.team3.dto.response.ApiResponse;
import com.team3.model.Reservation;
import java.awt.Component;

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
    private Reservation existingReservation = null; 
    private boolean isEditMode = false;

    public AddReservationDialog(Window parent, ReservationApi reservationApi) {
        this(parent, reservationApi, null);
    }

    public AddReservationDialog(Window parent, ReservationApi reservationApi, Reservation reservation) {
        super(parent, reservation == null ? "예약 추가" : "예약 수정", ModalityType.APPLICATION_MODAL);
        this.reservationApi = reservationApi;
        this.existingReservation = reservation;
        this.isEditMode = (reservation != null);
        
        initComponents();
        setupLayout();
        
        if (isEditMode) fillData();
        
        
        
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

        okButton = new JButton(isEditMode ? "수정완료" : "예약하기");
        okButton.setBackground(new Color(39, 174, 96)); // 녹색
        okButton.setForeground(Color.BLACK);
        okButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        okButton.addActionListener(e -> handleSubmit());

        cancelButton = new JButton("취소");
        cancelButton.setBackground(new Color(192, 57, 43)); // 빨간색
        cancelButton.setForeground(Color.BLACK);
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

    private void fillData() {
        roomIdField.setText(existingReservation.getRoomId());
        guestNameField.setText(existingReservation.getGuestName());
        phoneField.setText(existingReservation.getPhone());
        checkInField.setText(existingReservation.getCheckInDate());
        checkOutField.setText(existingReservation.getCheckOutDate());
        guestCountField.setText(String.valueOf(existingReservation.getGuestCount()));
    }

    private void handleSubmit() {
        // ... (기존 로직 유지) ...
        try {
            String roomId = roomIdField.getText().trim();
            String guestName = guestNameField.getText().trim();
            String phone = phoneField.getText().trim();
            String checkIn = checkInField.getText().trim();
            String checkOut = checkOutField.getText().trim();
            String guestCountStr = guestCountField.getText().trim();

            if (roomId.isEmpty() || guestName.isEmpty()) {
                statusLabel.setText("필수 정보를 입력하세요.");
                statusLabel.setForeground(new Color(192, 57, 43));
                return;
            }
            int guestCount = Integer.parseInt(guestCountStr);

            okButton.setEnabled(false);
            statusLabel.setText("처리 중...");

            SwingWorker<ApiResponse, Void> worker = new SwingWorker<>() {
                @Override protected ApiResponse doInBackground() {
                    if (isEditMode) {
                        UpdateReservationRequest req = new UpdateReservationRequest(
                            existingReservation.getId(), existingReservation.getUserId(),
                            roomId, guestName, phone, checkIn, checkOut, guestCount
                        );
                        return reservationApi.updateReservation(req);
                    } else {
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
                            statusLabel.setForeground(new Color(192, 57, 43));
                        }
                    } catch (Exception e) {
                        statusLabel.setText("오류: " + e.getMessage());
                        statusLabel.setForeground(new Color(192, 57, 43));
                    }
                }
            };
            worker.execute();
        } catch (NumberFormatException e) {
            statusLabel.setText("인원 수는 숫자여야 합니다.");
            statusLabel.setForeground(new Color(192, 57, 43));
        }
    }
}