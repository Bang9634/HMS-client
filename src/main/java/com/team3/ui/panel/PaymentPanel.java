package com.team3.ui.panel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.team3.client.api.PaymentApi;
import com.team3.dto.response.ApiResponse;
import com.team3.dto.response.Payment;

/**
 * 결제 관리 UI 패널
 * * @author 김현준
 */
public class PaymentPanel extends JPanel {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentPanel.class);
    
    private final PaymentApi paymentApi;
    private final Gson gson = new Gson();
    private final DecimalFormat formatter = new DecimalFormat("###,###"); 
    
    // UI 컴포넌트
    private JTextField guestNameField;
    private JTextField roomChargeField;
    private JTextField foodChargeField;
    private JTextField cardNumField;
    private JComboBox<String> methodCombo;
    private JTextArea resultArea;
    
    private JButton payButton;
    private JButton historyButton;
    private JButton deleteButton; 
    private JButton selectDeleteButton; 
    
    public PaymentPanel(String serverHost, int serverPort){
        this.paymentApi = new PaymentApi(serverHost, serverPort);
        
        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
    }
    
    private void initComponents() {
        // 1. 왼쪽 - 입력 폼 패널
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            "결제 정보 입력",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("맑은 고딕", Font.BOLD, 14)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // 입력 필드들 배치
        addFormField(formPanel, gbc, 0, "고객명:", guestNameField = new JTextField(15));
        
        roomChargeField = new JTextField("0", 15);
        foodChargeField = new JTextField("0", 15);
        addFormField(formPanel, gbc, 1, "객실료(원):", roomChargeField);
        addFormField(formPanel, gbc, 2, "식음료료(원):", foodChargeField);

        String[] methods = {"CARD", "CASH"};
        methodCombo = new JComboBox<>(methods);
        methodCombo.setBackground(Color.WHITE);
        methodCombo.addActionListener(e -> toggleCardField()); 
        addFormField(formPanel, gbc, 3, "결제 수단:", methodCombo);

        cardNumField = new JTextField(15);
        addFormField(formPanel, gbc, 4, "카드 번호:", cardNumField);

        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        payButton = createStyledButton("결제 승인", new Color(52, 152, 219)); // 파란색
        historyButton = createStyledButton("매출/내역", new Color(46, 204, 113)); // 초록색
        deleteButton = createStyledButton("전체 초기화", new Color(231, 76, 60)); // 빨간색
        selectDeleteButton = createStyledButton("선택 삭제", new Color(243, 156, 18)); // 주황색

        payButton.addActionListener(this::handlePayment);
        historyButton.addActionListener(this::handleHistory);
        deleteButton.addActionListener(this::handleDelete);
        selectDeleteButton.addActionListener(this::handleSelectDelete);

        buttonPanel.add(payButton);
        buttonPanel.add(historyButton);
        buttonPanel.add(selectDeleteButton); 
        buttonPanel.add(deleteButton);

        // 버튼 패널 배치
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);


        // 2. 오른쪽 - 결과 로그 영역
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBackground(Color.WHITE);
        resultPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            "영수증 출력 / 매출 현황",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("맑은 고딕", Font.BOLD, 14)
        ));

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12)); 
        resultPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        // 3. 메인 배치
        add(formPanel, BorderLayout.WEST);
        add(resultPanel, BorderLayout.CENTER);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridy = row;
        gbc.gridx = 0; gbc.weightx = 0.3;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        panel.add(label, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(field, gbc);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(95, 35));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        btn.setFocusPainted(false);
        return btn;
    }

    private void toggleCardField() {
        String selected = (String) methodCombo.getSelectedItem();
        cardNumField.setEnabled("CARD".equals(selected));
        if (!cardNumField.isEnabled()) {
            cardNumField.setText("");
        }
    }

    // --- 로직 처리 ---

    /** 1. 결제 승인 처리 */
    private void handlePayment(ActionEvent e) {
        String name = guestNameField.getText().trim();
        String method = (String) methodCombo.getSelectedItem();
        String cardNum = cardNumField.getText().trim();
        String sRoom = roomChargeField.getText().trim();
        String sFood = foodChargeField.getText().trim();

        if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "고객명을 입력해주세요."); return; }

        int tempRoom = 0, tempFood = 0;
        try { 
            tempRoom = Integer.parseInt(sRoom); 
            tempFood = Integer.parseInt(sFood); 
        } catch (NumberFormatException ex) { 
            JOptionPane.showMessageDialog(this, "금액은 숫자만 입력 가능합니다."); 
            return; 
        }
        
        final int roomPrice = tempRoom;
        final int foodPrice = tempFood;
        
        // 영수증에 찍을 '결제 내용'을 여기서 미리 결정합니다.
        String tempDetails = "RoomOnly";
        if (roomPrice == 0 && foodPrice > 0) {
            tempDetails = "Walk-in(Food)";
        } else if (roomPrice > 0 && foodPrice > 0) {
            tempDetails = "Room+Food";
        }
        final String details = tempDetails;
        
        Payment request = new Payment(name, roomPrice, foodPrice, method, cardNum);

        setButtonsEnabled(false);
        resultArea.setText(""); 
        resultArea.append(">> 결제 승인 요청 중...\n");

        SwingWorker<ApiResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected ApiResponse doInBackground() {
                return paymentApi.processPayment(request);
            }
            
            @Override
            protected void done(){
                try{
                    ApiResponse response = get();
                    if(response.isSuccess()) { 
                        JOptionPane.showMessageDialog(PaymentPanel.this, "결제 성공!");
                        
                        resultArea.setText(""); 
                        
                        String receipt = makeCurrentReceipt(name, roomPrice + foodPrice, method, cardNum, details);
                        
                        resultArea.append(receipt);
                        
                        // 입력창 초기화
                        guestNameField.setText("");
                        cardNumField.setText("");
                        roomChargeField.setText("0");
                        foodChargeField.setText("0");
                    } else {
                        resultArea.append("[결제 실패] " + response.getBody() + "\n");
                        JOptionPane.showMessageDialog(PaymentPanel.this, "결제 실패: " + response.getBody());
                    }
                } catch (Exception ex) {
                    logger.error("결제 처리 중 오류", ex);
                    resultArea.append("[오류] " + ex.getMessage() + "\n\n");
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };
        worker.execute();
    }

    /** 2. 내역 조회 처리 */
    private void handleHistory(ActionEvent e) {
        setButtonsEnabled(false);
        resultArea.setText(""); 
        resultArea.append(">> 전체 매출 데이터를 불러오는 중...\n");

        SwingWorker<ApiResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected ApiResponse doInBackground() {
                return paymentApi.getPaymentHistory();
            }

            @Override
            protected void done() {
                try {
                    ApiResponse response = get();
                    if (response.isSuccess()) {
                        String jsonBody = response.getBody();
                        Type listType = new TypeToken<List<Payment>>(){}.getType();
                        List<Payment> paymentList = gson.fromJson(jsonBody, listType);

                        resultArea.setText(""); 

                        if (paymentList == null || paymentList.isEmpty()) {
                            resultArea.append(">> 저장된 결제 내역이 없습니다.\n");
                        } else {
                            long grandTotal = 0; 

                            for (Payment p : paymentList) {
                                resultArea.append(formatReceipt(p));
                                resultArea.append("\n"); 
                                grandTotal += p.getTotalAmount(); 
                            }
                            
                            // 총 매출 요약
                            resultArea.append("\n");
                            resultArea.append("##########################################\n");
                            resultArea.append("           [ 총  매  출  현  황 ]          \n");
                            resultArea.append(String.format("   총 결제 건수 : %d 건\n", paymentList.size()));
                            resultArea.append(String.format("   총 매출 합계 : %s 원\n", formatter.format(grandTotal)));
                            resultArea.append("##########################################\n");
                            
                            resultArea.setCaretPosition(resultArea.getDocument().getLength());
                        }

                    } else {
                        resultArea.append("[조회 실패] " + response.getBody() + "\n");
                    }
                } catch (Exception ex) {
                    logger.error("내역 조회 중 오류", ex);
                    resultArea.append("[오류] 데이터 변환 실패: " + ex.getMessage() + "\n");
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };
        worker.execute();
    }

    /** 3. 내역 삭제(전체 초기화) 처리 */
    private void handleDelete(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "정말 모든 결제 내역을 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.", 
            "데이터 초기화 경고", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        setButtonsEnabled(false);
        resultArea.setText("");
        resultArea.append(">> 내역 초기화 중...\n");

        SwingWorker<ApiResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected ApiResponse doInBackground() {
                return paymentApi.deletePaymentHistory();
            }

            @Override
            protected void done() {
                try {
                    ApiResponse response = get();
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(PaymentPanel.this, "모든 내역이 삭제되었습니다.");
                        resultArea.append("[완료] 결제 내역이 초기화되었습니다.\n");
                    } else {
                        resultArea.append("[실패] " + response.getBody() + "\n");
                    }
                } catch (Exception ex) {
                    logger.error("초기화 중 오류", ex);
                    resultArea.append("[오류] " + ex.getMessage() + "\n");
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };
        worker.execute();
    }
    
    /** 4. 선택 삭제 처리 */
    private void handleSelectDelete(ActionEvent e) {
        String nameToDelete = JOptionPane.showInputDialog(this, 
            "삭제할 고객명을 입력하세요:", "선택 삭제", JOptionPane.QUESTION_MESSAGE);

        if (nameToDelete == null || nameToDelete.trim().isEmpty()) {
            return;
        }

        setButtonsEnabled(false);
        resultArea.setText("");
        resultArea.append(">> '" + nameToDelete + "'님의 내역 삭제 중...\n");

        SwingWorker<ApiResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected ApiResponse doInBackground() {
                return paymentApi.deletePaymentByGuestName(nameToDelete);
            }

            @Override
            protected void done() {
                try {
                    ApiResponse response = get();
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(PaymentPanel.this, "삭제 완료되었습니다.");
                        resultArea.append("[완료] " + response.getBody() + "\n");
                        handleHistory(null); // 목록 갱신
                    } else {
                        resultArea.append("[실패] " + response.getBody() + "\n");
                    }
                } catch (Exception ex) {
                    resultArea.append("[오류] " + ex.getMessage() + "\n");
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };
        worker.execute();
    }

    // 영수증 포맷팅 (과거 내역용)
    private String formatReceipt(Payment p) {
        StringBuilder sb = new StringBuilder();
        sb.append("==========================================\n");
        sb.append("               [영  수  증]               \n");
        sb.append("==========================================\n");
        sb.append(String.format(" 승인일시 : %s\n", p.getPaymentTime()));
        sb.append(String.format(" 고 객 명 : %s\n", p.getGuestName()));
        sb.append("------------------------------------------\n");
        sb.append(String.format(" 결제내역 : %s\n", p.getDetails())); 
        sb.append(String.format(" 결제수단 : %s\n", p.getMethod()));
        
        // 카드번호가 "N/A"가 아니면 표시
        if (!"N/A".equals(p.getCardNumber())) {
            sb.append(String.format(" 카드번호 : %s\n", p.getCardNumber()));
        }
        sb.append("------------------------------------------\n");
        sb.append(String.format(" 청구금액 : %24s 원\n", formatter.format(p.getTotalAmount())));
        sb.append("==========================================\n");
        return sb.toString();
    }

    // 즉석 영수증 포맷팅 (방금 결제용)
    private String makeCurrentReceipt(String name, int total, String method, String cardNum, String details) {
        String nowTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        StringBuilder sb = new StringBuilder();
        sb.append("*********** [방금 승인된 결제] ***********\n"); 
        sb.append("==========================================\n");
        sb.append("               [영  수  증]               \n");
        sb.append("==========================================\n");
        sb.append(String.format(" 승인일시 : %s\n", nowTime));
        sb.append(String.format(" 고 객 명 : %s\n", name));
        sb.append("------------------------------------------\n");
        // [수정] 전달받은 details를 출력
        sb.append(String.format(" 결제내역 : %s\n", details)); 
        sb.append(String.format(" 결제수단 : %s\n", method));   
        
        // 카드번호가 입력되었으면 표시
        if (cardNum != null && !cardNum.isEmpty()) {
            sb.append(String.format(" 카드번호 : %s\n", cardNum));
        }
        sb.append("------------------------------------------\n");
        sb.append(String.format(" 결제금액 : %24s 원\n", formatter.format(total)));
        sb.append("==========================================\n");
        sb.append("           이용해 주셔서 감사합니다.          \n");
        return sb.toString();
    }

    private void setButtonsEnabled(boolean enabled) {
        payButton.setEnabled(enabled);
        historyButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);
        selectDeleteButton.setEnabled(enabled);
    }
}