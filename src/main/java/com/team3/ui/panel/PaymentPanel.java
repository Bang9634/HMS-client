package com.team3.ui.panel;

import java.awt.*;
import java.awt.event.ActionEvent;

import java.lang.reflect.Type;
import java.text.DecimalFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.team3.dto.response.Payment;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.team3.client.api.PaymentApi;
import com.team3.dto.request.PaymentRequest;
import com.team3.dto.response.ApiResponse;

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
    
    public PaymentPanel(String serverHost, int serverPort){
        this.paymentApi = new PaymentApi(serverHost, serverPort);
        
        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20)); // 전체 여백
        
        initComponents();
    }
    
    private void initComponents() {
        // 1. 왼쪽 - 입력 폼
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
        gbc.insets = new Insets(10, 10, 10, 10); // 간격
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // 입력 필드 배치
        addFormField(formPanel, gbc, 0, "고객명:", guestNameField = new JTextField(15));
        
        // 금액 필드 (기본값 0)
        roomChargeField = new JTextField("0", 15);
        foodChargeField = new JTextField("0", 15);
        addFormField(formPanel, gbc, 1, "객실료(원):", roomChargeField);
        addFormField(formPanel, gbc, 2, "식음료료(원):", foodChargeField);

        // 결제 수단
        String[] methods = {"CARD", "CASH"};
        methodCombo = new JComboBox<>(methods);
        methodCombo.setBackground(Color.WHITE);
        methodCombo.addActionListener(e -> toggleCardField()); // 카드일 때만 번호 입력 활성화
        addFormField(formPanel, gbc, 3, "결제 수단:", methodCombo);

        // 카드 번호
        cardNumField = new JTextField(15);
        addFormField(formPanel, gbc, 4, "카드 번호:", cardNumField);


        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        payButton = createStyledButton("결제 승인", new Color(52, 152, 219)); // 파란색
        historyButton = createStyledButton("내역 조회", new Color(46, 204, 113)); // 초록색 (구분을 위해)

        payButton.addActionListener(this::handlePayment);
        historyButton.addActionListener(this::handleHistory);

        buttonPanel.add(payButton);
        buttonPanel.add(historyButton);

        // 폼 패널 하단에 버튼 추가
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);


        // 2. 오른쪽 - 결과 로그 영역
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBackground(Color.WHITE);
        resultPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            "처리 결과 / 조회 내역",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("맑은 고딕", Font.BOLD, 14)
        ));

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);


        // 3. 메인 패널에 배치 (왼쪽: 폼, 중앙: 결과)
        // 화면 비율을 맞추기 위해 
        // BorderLayout 사용 (왼쪽에 폼 고정)
        add(formPanel, BorderLayout.WEST);
        add(resultPanel, BorderLayout.CENTER);
    }

    // GridBagLayout 입력을 도와주는 헬퍼 메서드
    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridy = row;
        
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(field, gbc);
    }

    // 버튼 생성기
    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(100, 35));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btn.setFocusPainted(false);
        return btn;
    }

    // 카드 선택 시에만 카드번호 입력 활성화
    private void toggleCardField() {
        String selected = (String) methodCombo.getSelectedItem();
        cardNumField.setEnabled("CARD".equals(selected));
        if (!cardNumField.isEnabled()) {
            cardNumField.setText("");
        }
    }

    // 결제 승인 시 -> 방금 결제한 영수증만 보여줌 - SwingWorker
    private void handlePayment(ActionEvent e) {
        // 입력값 가져오기
        String name = guestNameField.getText().trim();
        String method = (String) methodCombo.getSelectedItem();
        String cardNum = cardNumField.getText().trim();
        String sRoom = roomChargeField.getText().trim();
        String sFood = foodChargeField.getText().trim();

        if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "고객명을 입력해주세요."); return; }

        // 임시 변수 사용
        int tempRoom = 0;
        int tempFood = 0;
        
        try { 
            tempRoom = Integer.parseInt(sRoom); 
            tempFood = Integer.parseInt(sFood); 
        } catch (NumberFormatException ex) { 
            JOptionPane.showMessageDialog(this, "금액은 숫자만 입력 가능합니다."); 
            return; 
        }
        
        final int roomPrice = tempRoom;
        final int foodPrice = tempFood;
        
        PaymentRequest request = new PaymentRequest(name, roomPrice, foodPrice, method, cardNum);

        setButtonsEnabled(false);
        // 결과창 깨끗하게 비우기
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
                        
                        // 화면을 싹 비우고 방금 결제한 내용으로 영수증을 만듦
                        resultArea.setText(""); 
                        String receipt = makeCurrentReceipt(name, roomPrice + foodPrice, method);
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
                    logger.error("오류", ex);
                    resultArea.append("[오류] " + ex.getMessage() + "\n");
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };
        worker.execute();
    }

   // 내역 조회 -> 전체 리스트 + 총 매출 보여줌
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

                        resultArea.setText(""); // 화면 비우기

                        if (paymentList == null || paymentList.isEmpty()) {
                            resultArea.append(">> 저장된 결제 내역이 없습니다.\n");
                        } else {
                            long grandTotal = 0; // 총 매출 계산용 변수

                            // 영수증 출력
                            for (Payment p : paymentList) {
                                resultArea.append(formatReceipt(p));
                                resultArea.append("\n"); 
                                grandTotal += p.getTotalAmount(); // 금액 누적
                            }
                            
                            // 맨 마지막에 총 매출 요약 보여주기
                            resultArea.append("\n");
                            resultArea.append("##########################################\n");
                            resultArea.append("           [ 총  매  출  현  황 ]          \n");
                            resultArea.append(String.format("   총 결제 건수 : %d 건\n", paymentList.size()));
                            resultArea.append(String.format("   총 매출 합계 : %s 원\n", formatter.format(grandTotal)));
                            resultArea.append("##########################################\n");
                            
                            // 스크롤 맨 아래로
                            resultArea.setCaretPosition(resultArea.getDocument().getLength());
                        }

                    } else {
                        resultArea.append("[조회 실패] " + response.getBody() + "\n");
                    }
                } catch (Exception ex) {
                    logger.error("오류", ex);
                    resultArea.append("[오류] 데이터 변환 실패: " + ex.getMessage() + "\n");
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };
        worker.execute();
    }
    
 // 1. 서버에서 받은 과거 내역용 영수증 포맷
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
        // 카드번호가 N/A가 아니면 표시
        if(!"N/A".equals(p.getCardNumber())){
            sb.append(String.format("카드번호 : $s\n", p.getCardNumber()));
        }
        sb.append("------------------------------------------\n");
        sb.append(String.format(" 청구금액 : %24s 원\n", formatter.format(p.getTotalAmount())));
        sb.append("==========================================\n");
        return sb.toString();
    }

    // 2. 방금 결제한 영수증
    // (서버가 200 OK만 주고 상세 데이터를 안 줄 때, 입력했던 정보로 만듦)
    private String makeCurrentReceipt(String name, int total, String method) {
        String nowTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        StringBuilder sb = new StringBuilder();
        sb.append("********** [방금 승인된 결제] **********\n"); 
        sb.append("==========================================\n");
        sb.append("               [영  수  증]               \n");
        sb.append("==========================================\n");
        sb.append(String.format(" 승인일시 : %s\n", nowTime));
        sb.append(String.format(" 고 객 명 : %s\n", name));
        sb.append("------------------------------------------\n");
        sb.append(String.format(" 결제수단 : %s\n", method));   
        if(!"N/A".equals(p.getCardNumber())){
            sb.append(String.format("카드번호 : $s\n", p.getCardNumber()));
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
    }
}