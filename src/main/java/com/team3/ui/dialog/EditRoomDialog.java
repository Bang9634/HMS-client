package com.team3.ui.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.team3.client.api.RoomApi;
import com.team3.dto.request.UpdateRoomRequest;
import com.team3.dto.response.ApiResponse;
import com.team3.util.JsonUtil;

/**
 * 객실 수정 다이얼로그
 * 
 * @author bang9634
 * @since 2025-11-27
 */
public class EditRoomDialog extends JDialog {

    private static final Logger logger = LoggerFactory.getLogger(EditRoomDialog.class);

    private final RoomApi roomApi;
    private final int roomId;

    private JTextField basePriceField;
    private JComboBox<String> isAvailableComboBox;
    private JTextField maxOccupancyField;
    private JTextField priceChangeReasonField;
    private JButton editButton;
    private JButton cancelButton;

    public EditRoomDialog(Window parent, RoomApi roomApi, int roomId, int basePrice, boolean isAvailable, int maxOccupancy, String priceChangeReason) {
        super(parent, "객실 정보 수정", ModalityType.APPLICATION_MODAL);
        this.roomApi = roomApi;
        this.roomId = roomId;
        initComponents(basePrice, isAvailable, maxOccupancy, priceChangeReason);
        setupLayout();

        setSize(400, 340);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleCancel();
            }
        });

        logger.info("EditRoomDialog 초기화 완료");
    }

    private void initComponents(int basePrice, boolean isAvailable, int maxOccupancy, String priceChangeReason) {
        basePriceField = new JTextField(String.valueOf(basePrice), 16);
        isAvailableComboBox = new JComboBox<>(new String[] { "비어있음", "점유중" });
        isAvailableComboBox.setSelectedItem(isAvailable ? "비어있음" : "점유중");
        maxOccupancyField = new JTextField(String.valueOf(maxOccupancy), 16);
        priceChangeReasonField = new JTextField(priceChangeReason == null ? "" : priceChangeReason, 16);

        editButton = new JButton("수정");
        editButton.setPreferredSize(new Dimension(100, 35));
        editButton.setBackground(new Color(241, 196, 15));
        editButton.setForeground(Color.BLACK);
        editButton.setFocusPainted(false);
        editButton.addActionListener(e -> handleEditRoom());

        cancelButton = new JButton("취소");
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.setBackground(new Color(192, 57, 43));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(e -> handleCancel());

        getRootPane().setDefaultButton(editButton);
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 제목
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("객실 정보 수정", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        mainPanel.add(titleLabel, gbc);

        gbc.gridy++;
        JSeparator separator = new JSeparator();
        mainPanel.add(separator, gbc);

        gbc.gridwidth = 1;

        // 객실번호(읽기전용)
        gbc.gridx = 0; gbc.gridy++;
        JLabel roomNumberLabel = new JLabel("객실 번호:");
        roomNumberLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        mainPanel.add(roomNumberLabel, gbc);
        gbc.gridx = 1;
        JTextField roomNumberField = new JTextField(String.valueOf(roomId), 16);
        roomNumberField.setEditable(false);
        mainPanel.add(roomNumberField, gbc);

        // 기본금액
        gbc.gridx = 0; gbc.gridy++;
        JLabel basePriceLabel = new JLabel("기본 금액:");
        basePriceLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        mainPanel.add(basePriceLabel, gbc);
        gbc.gridx = 1;
        mainPanel.add(basePriceField, gbc);

        // 점유상태
        gbc.gridx = 0; gbc.gridy++;
        JLabel isAvailableLabel = new JLabel("점유 상태:");
        isAvailableLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        mainPanel.add(isAvailableLabel, gbc);
        gbc.gridx = 1;
        mainPanel.add(isAvailableComboBox, gbc);

        // 최대인원
        gbc.gridx = 0; gbc.gridy++;
        JLabel maxOccupancyLabel = new JLabel("최대 인원:");
        maxOccupancyLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        mainPanel.add(maxOccupancyLabel, gbc);
        gbc.gridx = 1;
        mainPanel.add(maxOccupancyField, gbc);

        // 금액변경사유
        gbc.gridx = 0; gbc.gridy++;
        JLabel priceChangeReasonLabel = new JLabel("금액 변경 사유:");
        priceChangeReasonLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        mainPanel.add(priceChangeReasonLabel, gbc);
        gbc.gridx = 1;
        mainPanel.add(priceChangeReasonField, gbc);

        // 버튼 패널
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(editButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, gbc);

        setContentPane(mainPanel);
    }

    /**
     * 객실 수정 처리
     */
    private void handleEditRoom() {
        String basePriceText = basePriceField.getText().trim();
        String isAvailableText = (String) isAvailableComboBox.getSelectedItem();
        String maxOccupancyText = maxOccupancyField.getText().trim();
        String priceChangeReason = priceChangeReasonField.getText().trim();

        if (basePriceText.isEmpty() || maxOccupancyText.isEmpty()) {
            showError("모든 필수 항목을 입력하세요.");
            return;
        }

        int basePrice, maxOccupancy;
        boolean isAvailable = "비어있음".equals(isAvailableText);

        try {
            basePrice = Integer.parseInt(basePriceText);
            maxOccupancy = Integer.parseInt(maxOccupancyText);
        } catch (NumberFormatException e) {
            showError("기본금액, 최대인원은 숫자로 입력하세요.");
            return;
        }

        editButton.setEnabled(false);

        SwingWorker<ApiResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected ApiResponse doInBackground() {
                UpdateRoomRequest request = new UpdateRoomRequest(roomId, basePrice, isAvailable, maxOccupancy, priceChangeReason);
                return roomApi.updateRoom(request);
            }

            @Override
            protected void done() {
                editButton.setEnabled(true);
                try {
                    ApiResponse response = get();
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(EditRoomDialog.this, "객실 정보 수정 성공", "알림", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        showError("수정 실패: " + JsonUtil.extract(response.getBody(), "message"));
                    }
                } catch (InterruptedException | ExecutionException e) {
                    showError("오류: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * 취소 처리
     */
    private void handleCancel() {
        dispose();
    }

    /**
     * 에러 메시지 표시
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "오류",
            JOptionPane.ERROR_MESSAGE
        );
    }
}