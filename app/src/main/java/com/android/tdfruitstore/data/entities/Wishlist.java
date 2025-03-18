package com.android.tdfruitstore.data.entities;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

public class Wishlist {
    @Exclude // 🔥 Không lưu `id` trong Firestore, dùng document ID làm ID
    private String id;

    @PropertyName("userId") // 🔥 Đặt tên trường Firestore
    private String userId;

    @PropertyName("productId")
    private String productId;

    @PropertyName("addedAt")
    private Timestamp addedAt;

    @PropertyName("isBought")
    private boolean isBought;

    // ✅ Constructor mặc định (Firestore yêu cầu)
    public Wishlist() {}

    public Wishlist(String userId, String productId, Timestamp addedAt, boolean isBought) {
        this.userId = userId;
        this.productId = productId;
        this.addedAt = addedAt;
        this.isBought = isBought;
    }

    // ✅ Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public Timestamp getAddedAt() { return addedAt; }
    public void setAddedAt(Timestamp addedAt) { this.addedAt = addedAt; }

    public boolean isBought() { return isBought; }
    public void setBought(boolean bought) { isBought = bought; }
}
