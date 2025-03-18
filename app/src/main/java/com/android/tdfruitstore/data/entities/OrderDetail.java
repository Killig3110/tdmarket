package com.android.tdfruitstore.data.entities;

public class OrderDetail {
    private String id; // Chuyển từ int -> String
    private String orderId; // Chuyển từ int -> String
    private String productId; // Chuyển từ int -> String
    private int quantity;
    private double priceAtTime;
    private double subTotal;

    public OrderDetail() { }

    public OrderDetail(String id, String orderId, String productId, int quantity, double priceAtTime, double subTotal) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.priceAtTime = priceAtTime;
        this.subTotal = subTotal;
    }

    public OrderDetail(String productId, int quantity, double priceAtTime, double subTotal) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.priceAtTime = priceAtTime;
        this.subTotal = subTotal;
    }

    // Getter và Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPriceAtTime() { return priceAtTime; }
    public void setPriceAtTime(double priceAtTime) { this.priceAtTime = priceAtTime; }

    public double getSubTotal() { return subTotal; }
    public void setSubTotal(double subTotal) { this.subTotal = subTotal; }
}
