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
import com.team3.client.api.UserApi;
import com.team3.dto.request.DeleteUserRequest;
import com.team3.dto.response.ApiResponse;
import com.team3.ui.dialog.AddUserDialog;
import com.team3.util.JsonUtil;

/**
 * 사용자 관리 패널
 * <p>
 * 전체 사용자 목록 조회, 추가/수정/삭제 기능 제공
 * </p>
 * 
 * @author bang9634
 * @since 2025-11-27
 */
public class UserManagePanel extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(UserManagePanel.class);

    private final UserApi userApi;
    private JButton refreshButton;
    private JButton addUserButton;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JProgressBar progressBar;

    private static final String[] COLUMN_NAMES = {
        "아이디", "이름", "비밀번호", "권한", "생성일", "수정일", "삭제"
    };

    public UserManagePanel(String serverHost, int serverPort) {
        this.userApi = new UserApi(serverHost, serverPort);
        initComponents();
        setupLayout();
        loadUserList();
        logger.info("UserManagePanel 초기화 완료");
    }

    private void initComponents() {
        setBackground(Color.WHITE);

        // 새로고침 버튼
        refreshButton = new JButton("새로고침");
        refreshButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        refreshButton.setBackground(new Color(52, 152, 219));
        refreshButton.setForeground(Color.BLACK);
        refreshButton.setFocusPainted(false);
        refreshButton.setPreferredSize(new Dimension(120, 36));
        refreshButton.addActionListener(e -> loadUserList());

        // 사용자 추가 버튼
        addUserButton = new JButton("사용자 추가");
        addUserButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        addUserButton.setBackground(new Color(39, 174, 96));
        addUserButton.setForeground(Color.BLACK);
        addUserButton.setFocusPainted(false);
        addUserButton.setPreferredSize(new Dimension(120, 36));
        addUserButton.addActionListener(e -> handleAddUser());

        // 테이블 모델 및 테이블
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 수정/삭제 버튼만 편집 가능
                return column == 6 || column == 7;
            }
        };
        userTable = new JTable(tableModel);
        userTable.setRowHeight(32);
        userTable.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        userTable.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 13));
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 수정/삭제 버튼 렌더러 및 에디터
        userTable.getColumn("삭제").setCellRenderer(new ButtonRenderer("삭제", new Color(231, 76, 60)));
        userTable.getColumn("삭제").setCellEditor(new ButtonEditor("삭제", this::handleDeleteUser));

        // 상태 레이블
        statusLabel = new JLabel("대기 중...");
        statusLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));

        // 프로그레스 바
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // 상단 패널 (버튼)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(refreshButton);
        topPanel.add(addUserButton);

        add(topPanel, BorderLayout.NORTH);

        // 중앙 패널 (테이블)
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            "사용자 목록",
            0,
            0,
            new Font("맑은 고딕", Font.BOLD, 12)
        ));
        add(scrollPane, BorderLayout.CENTER);

        // 하단 패널 (상태 + 프로그레스)
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(progressBar, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * 사용자 목록 조회 및 테이블 갱신
     */
    private void loadUserList() {
        statusLabel.setText("사용자 목록 조회 중...");
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        tableModel.setRowCount(0);

        SwingWorker<ApiResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected ApiResponse doInBackground() {
                logger.debug("getUserList 호출");
                return userApi.getUserList();
            }

            @Override
            protected void done() {
                try {
                    logger.debug("getUserList 성공");
                    ApiResponse response = get();
                    if (response.isSuccess()) {
                        List<UserRow> users = parseUserList(response.getBody());
                        updateTable(users);
                        statusLabel.setText("✓ 사용자 목록 조회 성공 (" + users.size() + "명)");
                        statusLabel.setForeground(new Color(39, 174, 96));
                    } else {
                        statusLabel.setText("✗ 사용자 목록 조회 실패");
                        statusLabel.setForeground(new Color(231, 76, 60));
                    }
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("사용자 목록 조회 실패", e);
                    statusLabel.setText("✗ 오류 발생: " + e.getMessage());
                    statusLabel.setForeground(new Color(231, 76, 60));
                } finally {
                    progressBar.setVisible(false);
                    progressBar.setIndeterminate(false);
                }
            }
        };
        worker.execute();
    }

    /**
     * 사용자 목록 JSON 파싱
     */
    private List<UserRow> parseUserList(String json) {
        logger.debug("사용자 목록 JSON 파싱 시작");
        Gson gson = new Gson();
        try {
            // 서버 응답이 객체라면 data 필드만 추출
            com.google.gson.JsonObject obj = gson.fromJson(json, com.google.gson.JsonObject.class);
            if (obj.has("users")) {
                com.google.gson.JsonElement usersElem = obj.get("users");
                Type listType = new TypeToken<List<UserRow>>(){}.getType();
                logger.debug("사용자 목록 JSON 파싱 성공");
                return gson.fromJson(usersElem, listType);
            }
        } catch (JsonSyntaxException e) {
            logger.error("사용자 목록 파싱 실패", e);
        }
        // 실패 시 빈 리스트 반환
        logger.debug("빈 리스트 반환");
        return java.util.Collections.emptyList();
    }

    /**
     * 테이블에 사용자 목록 표시
     */
    private void updateTable(List<UserRow> users) {
        tableModel.setRowCount(0);
        for (UserRow user : users) {
            Vector<Object> row = new Vector<>();
            row.add(user.userId);
            row.add(user.userName);
            row.add(user.password);
            row.add(user.role);
            row.add(user.createdAt);
            row.add(user.updatedAt);
            row.add("수정");
            row.add("삭제");
            tableModel.addRow(row);
        }
    }

    /**
     * 사용자 추가 버튼 클릭 처리
     */
    private void handleAddUser() {
        AddUserDialog dialog = new AddUserDialog(SwingUtilities.getWindowAncestor(this), userApi);
        dialog.setVisible(true);
        loadUserList();
    }

    /**
     * 사용자 삭제 버튼 클릭 처리
     */
    private void handleDeleteUser(int row) {
        String userId = (String) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "정말로 사용자 [" + userId + "]를 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            ApiResponse response = userApi.deleteUser(new DeleteUserRequest(userId));
            if (response.getStatusCode() != 200) {
                JOptionPane.showConfirmDialog(this, JsonUtil.extract(response.getBody(),"message"), "삭제 실패", JOptionPane.CLOSED_OPTION);
            }
        }
        loadUserList();
    }

    /**
     * 사용자 정보 DTO (테이블 표시용)
     */
    private static class UserRow {
        String userId;
        String userName;
        String password;
        String role;
        String createdAt;
        String updatedAt;
    }

    /**
     * 테이블 버튼 셀 렌더러
     */
    private static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer(String text, Color bg) {
            setText(text);
            setFont(new Font("맑은 고딕", Font.BOLD, 12));
            setBackground(bg);
            setForeground(Color.BLACK);
            setFocusPainted(false);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    /**
     * 테이블 버튼 셀 에디터
     */
    private static class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private final java.util.function.IntConsumer action;
        private int row;

        public ButtonEditor(String text, java.util.function.IntConsumer action) {
            super(new JTextField());
            this.action = action;
            button = new JButton(text);
            button.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            button.setBackground(text.equals("삭제") ? new Color(231, 76, 60) : new Color(241, 196, 15));
            button.setForeground(Color.BLACK);
            button.setFocusPainted(false);
            button.addActionListener(e -> action.accept(row));
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return button.getText();
        }
    }
}