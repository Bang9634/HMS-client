package com.team3.model;

/**
 * 식음료 모델 (클라이언트용)
 * <p>
 * Enum의 toString()을 오버라이딩하여 UI에 "English (한글)" 형태로 표시되도록 변경함.
 * </p>
 */
public class FnbItem {

    // 1. 식사 유형 (수정됨)
    public enum MealType {
        BREAKFAST("Breakfast (조식)"), 
        LUNCH("Lunch (점심)"), 
        DINNER("Dinner (석식)"), 
        SNACK("Snack (간식)"), 
        DRINK("Drink (음료)"), 
        ALCOHOL("Alcohol (주류)");

        private final String label;
        MealType(String label) { this.label = label; }
        
        @Override
        public String toString() { return label; } // 콤보박스/테이블에 이 값이 보임
    }

    // 2. 서비스 유형 (수정됨)
    public enum ServiceType {
        RESTAURANT("Restaurant (레스토랑)"), 
        ROOM_SERVICE("Room Service (룸서비스)");

        private final String label;
        ServiceType(String label) { this.label = label; }

        @Override
        public String toString() { return label; }
    }

    // 3. 결제 수단 (수정됨)
    public enum PaymentMethod {
        ROOM_CHARGE("Room Charge (객실 청구)"), 
        CREDIT_CARD("Credit Card (신용카드)"), 
        CASH("Cash (현금)"), 
        CHECK("Check (수표)");

        private final String label;
        PaymentMethod(String label) { this.label = label; }

        @Override
        public String toString() { return label; }
    }

    // --- 필드는 기존과 동일 ---
    private String id;
    private String roomId;
    private String customerName;
    private ServiceType serviceType;
    private MealType mealType;
    private String menuName;
    private int price;
    private int count;
    private int totalAmount;
    private PaymentMethod paymentMethod;
    private String orderTime;

    public FnbItem() {}

    // Getters
    public String getId() { return id; }
    public String getRoomId() { return roomId; }
    public String getCustomerName() { return customerName; }
    public ServiceType getServiceType() { return serviceType; }
    public MealType getMealType() { return mealType; }
    public String getMenuName() { return menuName; }
    public int getPrice() { return price; }
    public int getCount() { return count; }
    public int getTotalAmount() { return totalAmount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public String getOrderTime() { return orderTime; }
}