package com.team3.ui.dialog;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

import com.team3.client.api.FnbApi;
import com.team3.dto.request.AddFnbRequest;
import com.team3.dto.response.ApiResponse;
import com.team3.model.FnbItem.*; 

public class AddFnbDialog extends JDialog {

    private JComboBox<ServiceType> serviceTypeCombo;
    private JTextField roomIdField = new JTextField(15);
    private JTextField customerNameField = new JTextField(15);
    
    private JPanel roomServicePanel;
    private Map<String, Integer> menuData;
    private Map<String, JSpinner> menuSpinners = new LinkedHashMap<>();

    private JPanel restaurantPanel;
    private JComboBox<MealType> restaurantMealTypeCombo;
    private JSpinner timeSpinner;

    private JComboBox<PaymentMethod> paymentMethodCombo = new JComboBox<>(PaymentMethod.values());
    private JButton okButton;
    private JButton cancelButton;
    private JLabel statusLabel;

    private JPanel centerCardPanel;
    private CardLayout cardLayout;

    private final FnbApi fnbApi;

    public AddFnbDialog(Window owner, FnbApi fnbApi) {
        super(owner, "식음료 서비스 주문", ModalityType.APPLICATION_MODAL);
        this.fnbApi = fnbApi;
        initMenuData();
        initUI();
        setSize(500, 680); 
        setLocationRelativeTo(owner);
    }

    private void initMenuData() {
        menuData = new LinkedHashMap<>();
        menuData.put("Club Sandwich (클럽 샌드위치)", 25000);
        menuData.put("Caesar Salad (시저 샐러드)", 20000);
        menuData.put("Tomato Pasta (토마토 파스타)", 28000);
        menuData.put("Beef Steak (안심 스테이크)", 55000);
        menuData.put("Fried Chicken (치킨)", 35000);
        menuData.put("Coke / Sprite (탄산음료)", 5000);
        menuData.put("Draft Beer (생맥주)", 9000);
        menuData.put("House Wine (하우스 와인)", 12000);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // [상단 패널] - GridBagLayout 적용 (AddUserDialog 스타일)
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("기본 정보"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        serviceTypeCombo = new JComboBox<>(ServiceType.values());
        serviceTypeCombo.addActionListener(e -> changeView());

        gbc.gridx = 0; gbc.gridy = 0; topPanel.add(new JLabel("서비스 유형:"), gbc);
        gbc.gridx = 1; topPanel.add(serviceTypeCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; topPanel.add(new JLabel("객실 번호:"), gbc);
        gbc.gridx = 1; topPanel.add(roomIdField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; topPanel.add(new JLabel("고객명:"), gbc);
        gbc.gridx = 1; topPanel.add(customerNameField, gbc);

        add(topPanel, BorderLayout.NORTH);

        // [중앙 패널]
        cardLayout = new CardLayout();
        centerCardPanel = new JPanel(cardLayout);
        centerCardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        createRoomServicePanel();
        centerCardPanel.add(roomServicePanel, "ROOM_SERVICE");

        createRestaurantPanel();
        centerCardPanel.add(restaurantPanel, "RESTAURANT");

        add(centerCardPanel, BorderLayout.CENTER);

        // [하단 패널]
        JPanel bottomContainer = new JPanel(new BorderLayout());
        
        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        paymentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 20));
        paymentPanel.add(new JLabel("결제 수단: "));
        paymentPanel.add(paymentMethodCombo);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10)); // 간격 조정
        
        okButton = new JButton("확인");
        okButton.setBackground(new Color(39, 174, 96)); // 녹색
        okButton.setForeground(Color.BLACK); // 검정 글씨
        okButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        okButton.addActionListener(e -> handleSubmit());
        
        cancelButton = new JButton("취소");
        cancelButton.setBackground(new Color(192, 57, 43)); // 빨간색
        cancelButton.setForeground(Color.BLACK); // 검정 글씨
        cancelButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        cancelButton.addActionListener(e -> dispose());
        
        btnPanel.add(okButton);
        btnPanel.add(cancelButton);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(52, 73, 94));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        bottomContainer.add(paymentPanel, BorderLayout.NORTH);
        bottomContainer.add(statusLabel, BorderLayout.CENTER);
        bottomContainer.add(btnPanel, BorderLayout.SOUTH);

        add(bottomContainer, BorderLayout.SOUTH);
        
        changeView();
    }

    private void changeView() {
        ServiceType selected = (ServiceType) serviceTypeCombo.getSelectedItem();
        if (selected == ServiceType.ROOM_SERVICE) {
            cardLayout.show(centerCardPanel, "ROOM_SERVICE");
            okButton.setText("주문하기");
        } else {
            cardLayout.show(centerCardPanel, "RESTAURANT");
            okButton.setText("예약하기");
        }
    }

    private void createRoomServicePanel() {
        roomServicePanel = new JPanel(new BorderLayout());
        roomServicePanel.setBorder(BorderFactory.createTitledBorder("메뉴 선택"));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        for (Map.Entry<String, Integer> entry : menuData.entrySet()) {
            String menuName = entry.getKey();
            int price = entry.getValue();

            JPanel row = new JPanel(new BorderLayout());
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            row.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JLabel nameLabel = new JLabel(String.format("%s (%,d원)", menuName, price));
            nameLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
            
            row.add(nameLabel, BorderLayout.CENTER);
            row.add(spinner, BorderLayout.EAST);
            
            listPanel.add(row);
            menuSpinners.put(menuName, spinner);
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        roomServicePanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void createRestaurantPanel() {
        restaurantPanel = new JPanel(new GridBagLayout());
        restaurantPanel.setBorder(BorderFactory.createTitledBorder("예약 정보"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        restaurantMealTypeCombo = new JComboBox<>(new MealType[]{
            MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER
        });

        timeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.setValue(new Date());

        gbc.gridx = 0; gbc.gridy = 0;
        restaurantPanel.add(new JLabel("식사 유형:"), gbc);
        gbc.gridx = 1;
        restaurantPanel.add(restaurantMealTypeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        restaurantPanel.add(new JLabel("방문 시간:"), gbc);
        gbc.gridx = 1;
        restaurantPanel.add(timeSpinner, gbc);
    }

    private void handleSubmit() {
        ServiceType sType = (ServiceType) serviceTypeCombo.getSelectedItem();
        String room = roomIdField.getText().trim();
        String customer = customerNameField.getText().trim();
        PaymentMethod pMethod = (PaymentMethod) paymentMethodCombo.getSelectedItem();

        if (pMethod == PaymentMethod.ROOM_CHARGE && room.isEmpty()) {
            statusLabel.setText("객실 청구 시 객실 번호는 필수입니다.");
            statusLabel.setForeground(new Color(192, 57, 43));
            return;
        }

        AddFnbRequest request = null;

        if (sType == ServiceType.ROOM_SERVICE) {
            StringBuilder menuBuilder = new StringBuilder();
            int totalPrice = 0;
            int totalCount = 0;

            for (Map.Entry<String, JSpinner> entry : menuSpinners.entrySet()) {
                int count = (int) entry.getValue().getValue();
                if (count > 0) {
                    String name = entry.getKey(); 
                    int price = menuData.get(entry.getKey());
                    int itemTotal = price * count;
                    
                    if (menuBuilder.length() > 0) menuBuilder.append(", ");
                    menuBuilder.append(name)
                               .append(" x").append(count)
                               .append(" (").append(String.format("%,d원", itemTotal)).append(")");
                    
                    totalPrice += itemTotal;
                    totalCount += count;
                }
            }

            if (totalCount == 0) {
                statusLabel.setText("메뉴를 하나 이상 선택해주세요.");
                statusLabel.setForeground(new Color(192, 57, 43));
                return;
            }

            request = new AddFnbRequest(
                room, customer, sType, MealType.SNACK,
                menuBuilder.toString(), totalPrice, 1, pMethod
            );

        } else {
            MealType mType = (MealType) restaurantMealTypeCombo.getSelectedItem();
            Date time = (Date) timeSpinner.getValue();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            String timeStr = sdf.format(time);
            String menuName = "Restaurant Reservation (레스토랑 예약) " + timeStr;
            
            request = new AddFnbRequest(
                room, customer, sType, mType,
                menuName, 0, 1, pMethod
            );
        }

        okButton.setEnabled(false);
        statusLabel.setText("처리 중...");
        
        final AddFnbRequest finalReq = request;
        new SwingWorker<ApiResponse, Void>() {
            @Override protected ApiResponse doInBackground() {
                return fnbApi.addFnbItem(finalReq);
            }
            @Override protected void done() {
                okButton.setEnabled(true);
                try {
                    ApiResponse res = get();
                    if (res.isSuccess()) {
                        JOptionPane.showMessageDialog(null, "완료되었습니다.");
                        dispose();
                    } else {
                        statusLabel.setText("실패: " + res.getBody());
                        statusLabel.setForeground(new Color(192, 57, 43));
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }
}