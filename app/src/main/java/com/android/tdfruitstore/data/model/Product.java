package com.android.tdfruitstore.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Product implements Parcelable {
    private String productId; // ID sản phẩm
    private String name;      // Tên sản phẩm
    private String imageUrl;  // Ảnh sản phẩm (URL)
    private String category;  // Danh mục sản phẩm
    private String code;      // Mã barcode sản phẩm
    private String price;     // Giá sản phẩm
    private int stock;        // Số lượng tồn kho
    private double rating;    // Xếp hạng sản phẩm

    // Constructor có đầy đủ thuộc tính
    public Product(String productId, String name, String imageUrl, String category, String code, String price, int stock, double rating) {
        this.productId = productId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.category = category;
        this.code = code;
        this.price = price;
        this.stock = stock;
        this.rating = rating;
    }

    // Constructor rỗng để Firebase đọc dữ liệu
    public Product() {}

    // Getter & Setter
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    // Parcelable implementation để truyền Product giữa các Activity
    protected Product(Parcel in) {
        productId = in.readString();
        name = in.readString();
        imageUrl = in.readString();
        category = in.readString();
        code = in.readString();
        price = in.readString();
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
        dest.writeString(productId);
        dest.writeString(name);
        dest.writeString(imageUrl);
        dest.writeString(category);
        dest.writeString(code);
        dest.writeString(price);
        dest.writeInt(stock);
        dest.writeDouble(rating);
    }
}
