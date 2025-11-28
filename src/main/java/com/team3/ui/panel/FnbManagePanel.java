package com.team3.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.team3.client.api.FnbApi;
import com.team3.dto.request.DeleteFnbRequest;
import com.team3.dto.response.ApiResponse;
import com.team3.model.FnbItem;
import com.team3.ui.dialog.AddFnbDialog;

public class FnbManagePanel extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(FnbManagePanel.class);

    private final FnbApi fnbApi;
    private JButton refreshButton;
    private JButton addFnbButton;
    private JTable fnbTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JProgressBar progressBar;

    // 컬럼 정의 (9:결제원본, 8:메뉴원본 은 숨김)
    private static final String[] COLUMN_NAMES = {
        "ID", "일시", "구분", "객실", "고객", "메뉴 / 결제", "금액", "주문취소", "메뉴원본", "결제원본"
    };

    public FnbManagePanel(String serverHost, int serverPort) {
        this.fnbApi = new FnbApi(serverHost, serverPort);
        initComponents();
        setupLayout();
        loadFnbList();
        logger.info("FnbManagePanel 초기화 완료");
    }

    private void initComponents() {
        setBackground(Color.WHITE);

        refreshButton = new JButton("새로고침");
        refreshButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        refreshButton.setBackground(new Color(52, 152, 219));
        refreshButton.setForeground(Color.BLACK);
        refreshButton.setFocusPainted(false);
        refreshButton.setPreferredSize(new Dimension(120, 36));
        refreshButton.addActionListener(e -> loadFnbList());

        addFnbButton = new JButton("주문/예약 추가");
        addFnbButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        addFnbButton.setBackground(new Color(39, 174, 96));
        addFnbButton.setForeground(Color.BLACK);
        addFnbButton.setFocusPainted(false);
        addFnbButton.setPreferredSize(new Dimension(140, 36)); // 글자 길이 고려
        addFnbButton.addActionListener(e -> handleAddFnb());

        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // 삭제 버튼
            }
        };
        fnbTable = new JTable(tableModel);
        fnbTable.setRowHeight(32);
        fnbTable.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        fnbTable.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 13));
        fnbTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 주문취소 버튼 스타일
        fnbTable.getColumn("주문취소").setCellRenderer(new ButtonRenderer("주문취소", new Color(231, 76, 60)));
        fnbTable.getColumn("주문취소").setCellEditor(new ButtonEditor("주문취소", this::handleDeleteFnb));

        // 컬럼 숨김 (ID, 원본 데이터)
        hideColumn(0);
        hideColumn(8);
        hideColumn(9);

        // 더블 클릭 이벤트
        fnbTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = fnbTable.getSelectedRow();
                    if (row != -1) {
                        String type = fnbTable.getValueAt(row, 2).toString();
                        String amount = (String) fnbTable.getValueAt(row, 6);
                        String realMenu = (String) fnbTable.getValueAt(row, 8);
                        String realPayment = (String) fnbTable.getValueAt(row, 9);
                        showMenuDetail(type, realMenu, realPayment, amount);
                    }
                }
            }
        });

        statusLabel = new JLabel("대기 중...");
        statusLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(refreshButton);
        topPanel.add(addFnbButton);
        add(topPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(fnbTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            "식음료 주문 내역 (더블 클릭 시 상세 보기)",
            0, 0,
            new Font("맑은 고딕", Font.BOLD, 12)
        ));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(progressBar, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void hideColumn(int index) {
        fnbTable.getColumnModel().getColumn(index).setMinWidth(0);
        fnbTable.getColumnModel().getColumn(index).setMaxWidth(0);
        fnbTable.getColumnModel().getColumn(index).setWidth(0);
    }

    private void showMenuDetail(String type, String menu, String payment, String amount) {
        String formattedMenu = "- " + menu.replace(", ", "\n- ");
        StringBuilder sb = new StringBuilder();
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("       ").append(type).append(" 상세 내역       \n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        sb.append("[주문 메뉴]\n").append(formattedMenu).append("\n\n");
        sb.append("────────────────────────\n");
        sb.append(" 결제 수단:  ").append(payment).append("\n");
        sb.append(" 총 결제 금액:  ").append(amount).append("\n");
        sb.append("────────────────────────");

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("나눔고딕", Font.PLAIN, 14));
        textArea.setMargin(new Insets(20, 20, 20, 20));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 450));
        JOptionPane.showMessageDialog(this, scrollPane, "주문 상세 정보", JOptionPane.PLAIN_MESSAGE);
    }

    private void loadFnbList() {
        statusLabel.setText("조회 중...");
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        tableModel.setRowCount(0);

        SwingWorker<ApiResponse, Void> worker = new SwingWorker<>() {
            @Override protected ApiResponse doInBackground() { return fnbApi.getFnbList(); }
            @Override protected void done() {
                try {
                    ApiResponse res = get();
                    if (res.isSuccess()) {
                        com.google.gson.JsonObject json = new Gson().fromJson(res.getBody(), com.google.gson.JsonObject.class);
                        if (json.has("data")) {
                            List<FnbItem> list = new Gson().fromJson(json.get("data"), new TypeToken<List<FnbItem>>(){}.getType());
                            for (FnbItem i : list) {
                                String timeStr = i.getOrderTime();
                                if(timeStr != null) {
                                    timeStr = timeStr.replace("T", " ");
                                    if(timeStr.contains(".")) timeStr = timeStr.substring(0, timeStr.indexOf("."));
                                } else { timeStr = "-"; }

                                String combined = i.getMenuName() + " / " + i.getPaymentMethod();
                                tableModel.addRow(new Object[]{
                                    i.getId(), timeStr, i.getServiceType(), i.getRoomId(), i.getCustomerName(),
                                    combined, String.format("%,d원", i.getTotalAmount()), "삭제",
                                    i.getMenuName(), i.getPaymentMethod().toString()
                                });
                            }
                            statusLabel.setText("✓ 조회 성공 (" + list.size() + "건)");
                            statusLabel.setForeground(new Color(39, 174, 96));
                        }
                    } else {
                        statusLabel.setText("✗ 조회 실패: " + res.getBody());
                        statusLabel.setForeground(new Color(231, 76, 60));
                    }
                } catch (Exception e) {
                    statusLabel.setText("✗ 오류: " + e.getMessage());
                    statusLabel.setForeground(new Color(231, 76, 60));
                } finally {
                    progressBar.setVisible(false);
                    progressBar.setIndeterminate(false);
                }
            }
        };
        worker.execute();
    }

    private void handleAddFnb() {
        AddFnbDialog dialog = new AddFnbDialog(SwingUtilities.getWindowAncestor(this), fnbApi);
        dialog.setVisible(true);
        loadFnbList();
    }

    private void handleDeleteFnb(int row) {
        String id = (String) tableModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "주문을 취소하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION) == 0) {
            SwingWorker<ApiResponse, Void> worker = new SwingWorker<>() {
                @Override protected ApiResponse doInBackground() {
                    return fnbApi.deleteFnbItem(new DeleteFnbRequest(id));
                }
                @Override protected void done() { loadFnbList(); }
            };
            worker.execute();
        }
    }

    // --- 내부 클래스 (RoomManagePanel과 동일) ---
    private static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer(String text, Color bg) {
            setText(text);
            setFont(new Font("맑은 고딕", Font.BOLD, 12));
            setBackground(bg);
            setForeground(Color.BLACK);
            setFocusPainted(false);
        }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) { return this; }
    }

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
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { this.row = r; return button; }
        @Override public Object getCellEditorValue() { return button.getText(); }
    }
}