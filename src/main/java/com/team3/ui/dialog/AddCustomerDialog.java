package com.team3.ui.dialog;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;

import com.team3.client.api.CustomerApi;
import com.team3.dto.request.AddCustomerRequest;
import com.team3.dto.response.ApiResponse;

/**
 * 고객 추가 다이얼로그
 * <p>SFR-701, 702, 704 정보를 입력받습니다.</p>
 */
public class AddCustomerDialog extends JDialog {

    private JTextField nameField;
    private JTextField phoneField;
    private JTextField roomNumberField;
    private JTextArea feedbackArea;
    
    private JButton okButton;
    private JButton cancelButton;
    private JLabel statusLabel;

    private final CustomerApi customerApi;

    public AddCustomerDialog(Window owner, CustomerApi customerApi) {
        super(owner, "고객 등록", ModalityType.APPLICATION_MODAL);
        this.customerApi = customerApi;
        initComponents();
        setupLayout();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        nameField = new JTextField(15);
        
        phoneField = new JTextField(15);
        // 전화번호 자동 하이픈 기능 연결
        addAutoHyphenListener(phoneField);

        roomNumberField = new JTextField(15);
        
        feedbackArea = new JTextArea(4, 15);
        feedbackArea.setLineWrap(true);
        feedbackArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        okButton = new JButton("등록");
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
        statusLabel.setForeground(new Color(52, 73, 94));
    }
    
    // 전화번호 자동 하이픈 리스너
    private void addAutoHyphenListener(JTextField textField) {
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // 백스페이스나 삭제 키일 때는 처리하지 않음
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) {
                    return;
                }

                String text = textField.getText();
                // 숫자만 남기고 모두 제거
                String numbers = text.replaceAll("[^0-9]", "");
                
                String formatted = "";
                if (numbers.length() < 4) {
                    formatted = numbers;
                } else if (numbers.length() < 7) {
                    // 010-123
                    formatted = numbers.substring(0, 3) + "-" + numbers.substring(3);
                } else if (numbers.length() < 11) {
                    // 010-123-456
                    formatted = numbers.substring(0, 3) + "-" + numbers.substring(3, 6) + "-" + numbers.substring(6);
                } else {
                    // 010-1234-5678 (11자리 제한)
                    if (numbers.length() > 11) numbers = numbers.substring(0, 11);
                    formatted = numbers.substring(0, 3) + "-" + numbers.substring(3, 7) + "-" + numbers.substring(7);
                }

                // 변환된 텍스트가 다를 경우에만 반영 (커서 튐 방지)
                if (!text.equals(formatted)) {
                    textField.setText(formatted);
                }
            }
            
            @Override
            public void keyTyped(KeyEvent e) {
                // 숫자와 백스페이스만 입력 가능하게 제한
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                    e.consume(); // 입력 무시
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

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("고객명:"), gbc);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("전화번호:"), gbc);
        gbc.gridx = 1;
        formPanel.add(phoneField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("객실 번호:"), gbc);
        gbc.gridx = 1;
        formPanel.add(roomNumberField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("피드백:"), gbc);
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(feedbackArea), gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(okButton);
        btnPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.NORTH);
    }

    private void handleSubmit() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String room = roomNumberField.getText().trim();
        String feedback = feedbackArea.getText().trim();

        // 빈 값 체크
        if (name.isEmpty() || phone.isEmpty()) {
            statusLabel.setText("이름과 전화번호는 필수입니다.");
            statusLabel.setForeground(Color.RED);
            return;
        }
        
        // 한글 완성형(가-힣) 또는 영문 대소문자(a-zA-Z), 공백(\s)만 허용
        String namePattern = "^[가-힣a-zA-Z\\s]+$";
        
        if (!name.matches(namePattern) || name.length() < 2) {
            JOptionPane.showMessageDialog(this, 
                "이름 형식이 올바르지 않습니다.\n(한글 또는 영문만 입력 가능, 2글자 이상)", 
                "입력 오류", 
                JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }

        // 전화번호 형식 검증
        // 예: 010-1234-5678 또는 02-123-4567 형식만 허용
        String phonePattern = "^\\d{2,3}-\\d{3,4}-\\d{4}$";
        if (!phone.matches(phonePattern)) {
            JOptionPane.showMessageDialog(this, 
                "전화번호 형식이 올바르지 않습니다.\n(예: 010-1234-5678)", 
                "입력 오류", 
                JOptionPane.WARNING_MESSAGE);
            phoneField.requestFocus(); // 다시 입력하도록 포커스 이동
            return;
        }

        okButton.setEnabled(false);
        AddCustomerRequest req = new AddCustomerRequest(name, phone, room, feedback);

        new SwingWorker<ApiResponse, Void>() {
            @Override protected ApiResponse doInBackground() {
                return customerApi.addCustomer(req);
            }
            @Override protected void done() {
                okButton.setEnabled(true);
                try {
                    ApiResponse res = get();
                    if (res.isSuccess()) {
                        JOptionPane.showMessageDialog(null, "등록되었습니다.");
                        dispose();
                    } else {
                        statusLabel.setText("실패: " + res.getBody());
                        statusLabel.setForeground(Color.RED);
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }
}