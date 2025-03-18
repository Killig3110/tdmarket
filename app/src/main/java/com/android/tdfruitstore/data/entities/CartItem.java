package com.android.tdfruitstore.data.entities;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;

public class CartItem implements Parcelable, Serializable {
    private String id;           // 🔥 Chuyển ID từ int -> String
    private String userId;       // 🔥 Chuyển userId từ int -> String
    private String productId;    // 🔥 Chuyển productId từ int -> String
    private String productImageUrl;
    private int quantity;
    private double price;

    // ✅ Constructor không tham số (cần thiết cho Firestore)
    public CartItem() {}

    // ✅ Constructor đầy đủ tham số
    public CartItem(String id, String userId, String productId, String productImageUrl, int quantity, double price) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.productImageUrl = productImageUrl;
        this.quantity = quantity;
        this.price = price;
    }

    // ✅ Parcelable
    protected CartItem(Parcel in) {
        id = in.readString();
        userId = in.readString();
        productId = in.readString();
        productImageUrl = in.readString();
        quantity = in.readInt();
        price = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(userId);
        dest.writeString(productId);
        dest.writeString(productImageUrl);
        dest.writeInt(quantity);
        dest.writeDouble(price);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
        @Override
        public CartItem createFromParcel(Parcel in) {
            return new CartItem(in);
        }

        @Override
        public CartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };

    // ✅ Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductImageUrl() { return productImageUrl; }
    public void setProductImageUrl(String productImageUrl) { this.productImageUrl = productImageUrl; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
