package com.team3.ui.panel;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.team3.client.api.CustomerApi;
import com.team3.dto.request.DeleteCustomerRequest;
import com.team3.dto.response.ApiResponse;
import com.team3.model.Customer;
import com.team3.ui.dialog.AddCustomerDialog;
import java.util.concurrent.ExecutionException;

public class CustomerManagePanel extends JPanel {

    private final CustomerApi api;
    private JTable table;
    private DefaultTableModel model;
    
    // 검색 컴포넌트
    private JComboBox<String> searchTypeCombo;
    private JTextField searchField;

    public CustomerManagePanel(String host, int port) {
        this.api = new CustomerApi(host, port);
        setLayout(new BorderLayout());

        // [1] 상단: 검색 및 버튼 패널
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // 검색 조건 (SFR-703: 이름/객실번호)
        String[] searchTypes = {"이름 (Name)", "객실 번호 (Room)"};
        searchTypeCombo = new JComboBox<>(searchTypes);
        searchField = new JTextField(15);
        JButton searchButton = new JButton("검색");
        
        // 기능 버튼
        JButton refreshButton = new JButton("전체 목록");
        JButton addButton = new JButton("고객 등록");

        // 스타일링
        searchButton.setBackground(new Color(52, 152, 219)); searchButton.setForeground(Color.BLACK);
        refreshButton.setBackground(new Color(241, 196, 15)); refreshButton.setForeground(Color.BLACK);
        addButton.setBackground(new Color(39, 174, 96)); addButton.setForeground(Color.BLACK);

        topPanel.add(new JLabel("검색 조건:"));
        topPanel.add(searchTypeCombo);
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(Box.createHorizontalStrut(20)); // 간격
        topPanel.add(refreshButton);
        topPanel.add(addButton);

        add(topPanel, BorderLayout.NORTH);

        // [2] 테이블
        String[] cols = {"ID", "이름", "전화번호", "객실", "피드백", "등록일", "삭제"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 6; }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        
        // 삭제 버튼
        table.getColumn("삭제").setCellRenderer(new ButtonRenderer());
        table.getColumn("삭제").setCellEditor(new ButtonEditor(this::deleteItem));
        
        // ID 숨김
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // [3] 이벤트 연결
        searchButton.addActionListener(e -> searchData());
        searchField.addActionListener(e -> searchData()); // 엔터키 처리
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadData();
        });
        addButton.addActionListener(e -> {
            new AddCustomerDialog(SwingUtilities.getWindowAncestor(this), api).setVisible(true);
            loadData(); // 창 닫히면 갱신
        });

        loadData(); // 초기 로드
    }

    private void searchData() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadData(); // 검색어 없으면 전체 조회
            return;
        }

        // 콤보박스 선택값: "이름 (Name)" -> "NAME", "객실 번호 (Room)" -> "ROOM"
        String type = searchTypeCombo.getSelectedIndex() == 0 ? "NAME" : "ROOM";

        new SwingWorker<ApiResponse, Void>() {
            @Override protected ApiResponse doInBackground() {
                return api.searchCustomers(type, keyword);
            }
            @Override protected void done() {
                try {
                    processResponse(get());
                } catch (InterruptedException ex) {
                    System.getLogger(CustomerManagePanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                } catch (ExecutionException ex) {
                    System.getLogger(CustomerManagePanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            }
        }.execute();
    }

    private void loadData() {
        new SwingWorker<ApiResponse, Void>() {
            @Override protected ApiResponse doInBackground() {
                return api.getCustomerList();
            }
            @Override protected void done() {
                try {
                    processResponse(get());
                } catch (InterruptedException ex) {
                    System.getLogger(CustomerManagePanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                } catch (ExecutionException ex) {
                    System.getLogger(CustomerManagePanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            }
        }.execute();
    }

    // 응답 처리 공통 메서드
    private void processResponse(ApiResponse res) {
        try {
            if (res != null && res.isSuccess()) {
                com.google.gson.JsonObject json = new Gson().fromJson(res.getBody(), com.google.gson.JsonObject.class);
                if (json.has("data")) {
                    List<Customer> list = new Gson().fromJson(json.get("data"), new TypeToken<List<Customer>>(){}.getType());
                    model.setRowCount(0);
                    for (Customer c : list) {
                        model.addRow(new Object[]{
                            c.getId(), c.getName(), c.getPhoneNumber(), c.getRoomNumber(), 
                            c.getFeedback(), c.getCreatedAt(), "삭제"
                        });
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "데이터 로드 실패");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void deleteItem(int row) {
        String id = (String) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION) == 0) {
            new SwingWorker<ApiResponse, Void>() {
                @Override protected ApiResponse doInBackground() {
                    return api.deleteCustomer(new DeleteCustomerRequest(id));
                }
                @Override protected void done() { loadData(); }
            }.execute();
        }
    }

    // --- 내부 클래스 (버튼 UI) ---
    static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() { setText("삭제"); setBackground(new Color(231, 76, 60)); setForeground(Color.WHITE); }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) { return this; }
    }
    static class ButtonEditor extends DefaultCellEditor {
        JButton btn; int r; java.util.function.IntConsumer act;
        public ButtonEditor(java.util.function.IntConsumer a) { 
            super(new JTextField()); act = a; btn = new JButton("삭제");
            btn.setBackground(new Color(231, 76, 60)); btn.setForeground(Color.WHITE);
            btn.addActionListener(e -> { fireEditingStopped(); act.accept(r); });
        }
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { this.r = r; return btn; }
    }
}