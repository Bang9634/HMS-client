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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import com.team3.client.api.UserApi;
import com.team3.dto.request.AddUserRequest;
import com.team3.dto.response.ApiResponse;

/**
 * 사용자 추가 다이얼로그
 * 
 * @author bang9634
 * @since 2025-11-27
 */
public class AddUserDialog extends JDialog {

    private JTextField userIdField;
    private JTextField userNameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton addButton;
    private JButton cancelButton;
    private JLabel statusLabel;

    private final UserApi userApi;

    public AddUserDialog(Window parent, UserApi userApi) {
        super(parent, "사용자 추가", ModalityType.APPLICATION_MODAL);
        this.userApi = userApi;
        initComponents();
        setupLayout();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        userIdField = new JTextField(16);
        userNameField = new JTextField(16);
        passwordField = new JPasswordField(16);
        roleComboBox = new JComboBox<>(new String[] { "ADMIN", "CSR" });

        addButton = new JButton("추가");
        addButton.setBackground(new Color(39, 174, 96));
        addButton.setForeground(Color.BLACK);
        addButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        addButton.addActionListener(e -> handleAddUser());

        cancelButton = new JButton("취소");
        cancelButton.setBackground(new Color(192, 57, 43));
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

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("아이디:"), gbc);
        gbc.gridx = 1;
        formPanel.add(userIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("이름:"), gbc);
        gbc.gridx = 1;
        formPanel.add(userNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("비밀번호:"), gbc);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("권한:"), gbc);
        gbc.gridx = 1;
        formPanel.add(roleComboBox, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(formPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        getContentPane().add(statusPanel, BorderLayout.NORTH);
    }

    private void handleAddUser() {
        String userId = userIdField.getText().trim();
        String userName = userNameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();

        if (userId.isEmpty() || userName.isEmpty() || password.isEmpty()) {
            statusLabel.setText("모든 항목을 입력하세요.");
            statusLabel.setForeground(new Color(192, 57, 43));
            return;
        }

        addButton.setEnabled(false);
        statusLabel.setText("사용자 추가 중...");
        statusLabel.setForeground(new Color(52, 73, 94));

        SwingWorker<ApiResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected ApiResponse doInBackground() {
                AddUserRequest req = new AddUserRequest(userId, password, userName, role);
                return userApi.addUser(req);
            }

            @Override
            protected void done() {
                addButton.setEnabled(true);
                try {
                    ApiResponse response = get();
                    if (response.isSuccess()) {
                        statusLabel.setText("사용자 추가 성공!");
                        statusLabel.setForeground(new Color(39, 174, 96));
                        JOptionPane.showMessageDialog(AddUserDialog.this, "사용자 추가 성공!", "알림", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        statusLabel.setText("추가 실패: " + response.getMessage());
                        statusLabel.setForeground(new Color(192, 57, 43));
                    }
                } catch (HeadlessException | InterruptedException | ExecutionException e) {
                    statusLabel.setText("오류: " + e.getMessage());
                    statusLabel.setForeground(new Color(192, 57, 43));
                }
            }
        };
        worker.execute();
    }
}