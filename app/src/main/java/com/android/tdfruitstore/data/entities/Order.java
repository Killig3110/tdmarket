package com.android.tdfruitstore.data.entities;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.util.Date;

public class Order {
    private String id; // Firestore document ID
    private String userId; // User ID dưới dạng String
    private Timestamp orderDate;
    private double totalPrice;
    private String status;

    public Order() {}

    public Order(String userId, Timestamp orderDate, double totalPrice, String status) {
        this.userId = userId;
        this.orderDate = orderDate;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    // 🔥 Firestore tự tạo document ID, nên cần phương thức để lưu ID sau khi thêm vào Firestore
    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Timestamp orderDate) {
        this.orderDate = orderDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
