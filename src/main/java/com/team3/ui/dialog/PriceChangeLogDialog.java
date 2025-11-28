package com.team3.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Window;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.team3.client.api.RoomApi;
import com.team3.dto.request.RoomIdRequest;
import com.team3.dto.response.ApiResponse;


/**
 * 금액변경로그 다이얼로그
 */
public class PriceChangeLogDialog extends JDialog {
    private static final Logger logger = LoggerFactory.getLogger(LoginDialog.class);
    private final RoomApi roomApi;
    private final int roomId;
    private JTable logTable;
    private DefaultTableModel logTableModel;
    private JLabel statusLabel;

    private static final String[] LOG_COLUMNS = {
        "변경일시", "이전금액", "변경금액", "변경사유"
    };

    public PriceChangeLogDialog(Window parent, RoomApi roomApi, int roomId) {
        super(parent, "금액변경로그 - 객실 " + roomId, ModalityType.APPLICATION_MODAL);
        this.roomApi = roomApi;
        this.roomId = roomId;
        initComponents();
        setupLayout();
        loadPriceChangeLogs();
        setSize(500, 350);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        logTableModel = new DefaultTableModel(LOG_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        logTable = new JTable(logTableModel);
        logTable.setRowHeight(28);
        logTable.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        logTable.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 13));
        statusLabel = new JLabel("금액변경로그 조회 중...");
        statusLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(statusLabel, BorderLayout.WEST);

        JScrollPane scrollPane = new JScrollPane(logTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            "금액변경로그",
            0,
            0,
            new Font("맑은 고딕", Font.BOLD, 12)
        ));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }


    private void loadPriceChangeLogs() {
        statusLabel.setText("금액변경로그 조회 중...");
        logTableModel.setRowCount(0);

        SwingWorker<ApiResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected ApiResponse doInBackground() {
                return roomApi.getPriceChangeLogs(new RoomIdRequest(roomId));
            }

            @Override
            protected void done() {
                try {
                    ApiResponse response = get();
                    logger.debug("ApiResponse boy: {}", response.getBody());
                    if (response.isSuccess()) {
                        List<PriceChangeLogRow> logs = parseLogList(response.getBody());
                        for (PriceChangeLogRow log : logs) {
                            logTableModel.addRow(new Object[]{
                                log.changedAt,
                                log.oldPrice,
                                log.newPrice,
                                log.reason
                            });
                        }
                        logger.debug("로그 개수: {}", logs.size());
                        statusLabel.setText("✓ 로그 " + logs.size() + "건");
                        statusLabel.setForeground(new Color(39, 174, 96));
                    } else {
                        statusLabel.setText("✗ 로그 조회 실패");
                        statusLabel.setForeground(new Color(231, 76, 60));
                    }
                } catch (Exception e) {
                    statusLabel.setText("✗ 오류: " + e.getMessage());
                    statusLabel.setForeground(new Color(231, 76, 60));
                }
            }
        };
        worker.execute();
    }

    private List<PriceChangeLogRow> parseLogList(String json) {
        Gson gson = new Gson();
        try {
            com.google.gson.JsonObject obj = gson.fromJson(json, com.google.gson.JsonObject.class);
            if (obj.has("logs")) {
                com.google.gson.JsonElement logsElem = obj.get("logs");
                java.lang.reflect.Type listType = new TypeToken<List<PriceChangeLogRow>>(){}.getType();
                return gson.fromJson(logsElem, listType);
            }
        } catch (JsonSyntaxException e) {
            logger.error("금액변경로그 파싱 실패", e);
        }
        return java.util.Collections.emptyList();
    }

    private static class PriceChangeLogRow {
        String changedAt;
        int oldPrice;
        int newPrice;
        String reason;
    }
}