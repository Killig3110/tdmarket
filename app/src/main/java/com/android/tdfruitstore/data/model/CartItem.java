package com.android.tdfruitstore.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CartItem implements Parcelable {
    private String name;
    private String imageUrl;
    private double price;
    private int quantity;

    // 🔹 Constructor không tham số (CẦN THIẾT CHO FIREBASE)
    public CartItem() {
    }

    public CartItem(String name, String imageUrl, double price, int quantity) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
    }

    protected CartItem(Parcel in) {
        name = in.readString();
        imageUrl = in.readString();
        price = in.readDouble();
        quantity = in.readInt();
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(imageUrl);
        dest.writeDouble(price);
        dest.writeInt(quantity);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // 🔹 Getters và Setters để Firebase có thể đọc và ghi dữ liệu
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
