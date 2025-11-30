package com.team3.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingWorker;

import com.team3.client.api.ReservationApi;
import com.team3.dto.request.AddReservationRequest;
import com.team3.dto.request.UpdateReservationRequest;
import com.team3.dto.response.ApiResponse;
import com.team3.model.Reservation;

public class AddReservationDialog extends JDialog {

    private JTextField roomIdField;
    private JTextField guestNameField;
    private JTextField phoneField;
    private JSpinner checkInSpinner;
    private JSpinner checkOutSpinner;
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
        // ▼▼▼ [추가 1] 전화번호 자동 하이픈 리스너 연결 ▼▼▼
        addAutoHyphenListener(phoneField);

        
        Date todayDate = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

        checkInSpinner = new JSpinner(new SpinnerDateModel(todayDate, null, null, Calendar.DAY_OF_MONTH));
        checkInSpinner.setEditor(new JSpinner.DateEditor(checkInSpinner, "yyyy-MM-dd"));
        checkOutSpinner = new JSpinner(new SpinnerDateModel(todayDate, null, null, Calendar.DAY_OF_MONTH));
        checkOutSpinner.setEditor(new JSpinner.DateEditor(checkOutSpinner, "yyyy-MM-dd"));
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

    // ▼▼▼ [추가 2] 자동 하이픈 로직 (AddCustomerDialog와 동일) ▼▼▼
    private void addAutoHyphenListener(JTextField textField) {
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) {
                    return;
                }
                String text = textField.getText();
                String numbers = text.replaceAll("[^0-9]", "");
                
                String formatted = "";
                if (numbers.length() < 4) {
                    formatted = numbers;
                } else if (numbers.length() < 7) {
                    formatted = numbers.substring(0, 3) + "-" + numbers.substring(3);
                } else if (numbers.length() < 11) {
                    formatted = numbers.substring(0, 3) + "-" + numbers.substring(3, 6) + "-" + numbers.substring(6);
                } else {
                    if (numbers.length() > 11) numbers = numbers.substring(0, 11);
                    formatted = numbers.substring(0, 3) + "-" + numbers.substring(3, 7) + "-" + numbers.substring(7);
                }

                if (!text.equals(formatted)) {
                    textField.setText(formatted);
                }
            }
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                    e.consume();
                }
            }
        });
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
        addFormField(formPanel, gbc, 3, "체크인:", checkInSpinner);
        addFormField(formPanel, gbc, 4, "체크아웃:", checkOutSpinner);
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
        LocalDate checkInDate = LocalDate.parse(existingReservation.getCheckInDate());
        LocalDate checkOutDate = LocalDate.parse(existingReservation.getCheckOutDate());
        checkInSpinner.setValue(Date.from(checkInDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        checkOutSpinner.setValue(Date.from(checkOutDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        guestCountField.setText(String.valueOf(existingReservation.getGuestCount()));
    }

    private void handleSubmit() {
        String roomId = roomIdField.getText().trim();
        String guestName = guestNameField.getText().trim();
        String phone = phoneField.getText().trim();

        Date checkInDate = (Date) checkInSpinner.getValue();
        Date checkOutDate = (Date) checkOutSpinner.getValue();
        LocalDate checkIn = checkInDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate checkOut = checkOutDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        String guestCountStr = guestCountField.getText().trim();

        // 1. 필수 값 체크
        if (roomId.isEmpty() || guestName.isEmpty() || phone.isEmpty()) {
            statusLabel.setText("필수 정보를 입력하세요.");
            statusLabel.setForeground(new Color(192, 57, 43));
            return;
        }

        // ▼▼▼ [추가 3] 이름 형식 검증 ▼▼▼
        String namePattern = "^[가-힣a-zA-Z\\s]+$";
        if (!guestName.matches(namePattern) || guestName.length() < 2) {
            JOptionPane.showMessageDialog(this, 
                "예약자명 형식이 올바르지 않습니다.\n(한글 또는 영문만 입력 가능, 2글자 이상)", 
                "입력 오류", 
                JOptionPane.WARNING_MESSAGE);
            guestNameField.requestFocus();
            return;
        }

        // ▼▼▼ [추가 4] 전화번호 형식 검증 ▼▼▼
        String phonePattern = "^\\d{2,3}-\\d{3,4}-\\d{4}$";
        if (!phone.matches(phonePattern)) {
            JOptionPane.showMessageDialog(this, 
                "전화번호 형식이 올바르지 않습니다.\n(예: 010-1234-5678)", 
                "입력 오류", 
                JOptionPane.WARNING_MESSAGE);
            phoneField.requestFocus();
            return;
        }

        // 인원 수 숫자 변환
        int guestCount;
        try {
            guestCount = Integer.parseInt(guestCountStr);
        } catch (NumberFormatException e) {
            statusLabel.setText("인원 수는 숫자여야 합니다.");
            statusLabel.setForeground(new Color(192, 57, 43));
            return;
        }

        okButton.setEnabled(false);
        statusLabel.setText("처리 중...");

        SwingWorker<ApiResponse, Void> worker = new SwingWorker<>() {
            @Override protected ApiResponse doInBackground() {
                if (isEditMode) {
                    UpdateReservationRequest req = new UpdateReservationRequest(
                        existingReservation.getId(), existingReservation.getUserId(),
                        roomId, guestName, phone, checkIn.toString(), checkOut.toString(), guestCount
                    );
                    return reservationApi.updateReservation(req);
                } else {
                    AddReservationRequest req = new AddReservationRequest(
                        roomId, guestName, phone, checkIn.toString(), checkOut.toString(), guestCount
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
                        statusLabel.setText("실패: " + response.getMessage());
                        statusLabel.setForeground(new Color(192, 57, 43));
                    }
                } catch (Exception e) {
                    statusLabel.setText("오류: " + e.getMessage());
                    statusLabel.setForeground(new Color(192, 57, 43));
                }
            }
        };
        worker.execute();
    }
}