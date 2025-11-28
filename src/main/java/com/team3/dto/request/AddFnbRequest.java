package com.team3.dto.request;

import com.team3.model.FnbItem.*;

public class AddFnbRequest {
    private final String roomId;
    private final String customerName;
    private final ServiceType serviceType;
    private final MealType mealType;
    private final String menuName;
    private final int price;
    private final int count;
    private final PaymentMethod paymentMethod;

    public AddFnbRequest(String roomId, String customerName, ServiceType serviceType, MealType mealType, String menuName, int price, int count, PaymentMethod paymentMethod) {
        this.roomId = roomId;
        this.customerName = customerName;
        this.serviceType = serviceType;
        this.mealType = mealType;
        this.menuName = menuName;
        this.price = price;
        this.count = count;
        this.paymentMethod = paymentMethod;
    }
}