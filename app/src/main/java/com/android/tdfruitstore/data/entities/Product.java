package com.android.tdfruitstore.data.entities;

import android.os.Parcel;
import android.os.Parcelable;

public class Product implements Parcelable {
    private String id; // Đổi từ int sang String
    private String name;
    private String imageUrl;
    private String category;
    private String code;
    private double price;
    private int stock;
    private double rating;

    public Product() {
    }

    public Product(String id, String name, String imageUrl, String category, String code, double price, int stock, double rating) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.category = category;
        this.code = code;
        this.price = price;
        this.stock = stock;
        this.rating = rating;
    }

    public Product(String name, String imageUrl, String category, String code, double price, int stock, double rating) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.category = category;
        this.code = code;
        this.price = price;
        this.stock = stock;
        this.rating = rating;
    }

    protected Product(Parcel in) {
        id = in.readString();
        name = in.readString();
        imageUrl = in.readString();
        category = in.readString();
        code = in.readString();
        price = in.readDouble();
        stock = in.readInt();
        rating = in.readDouble();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(imageUrl);
        dest.writeString(category);
        dest.writeString(code);
        dest.writeDouble(price);
        dest.writeInt(stock);
        dest.writeDouble(rating);
    }

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
}
