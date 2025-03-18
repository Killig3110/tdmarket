package com.android.tdfruitstore.data.entities;

public class Category {
    private String id;  // 🔥 ID Firestore dưới dạng String
    private String categoryName;
    private String tag;
    private int imageResId;

    // 🔹 Constructor mặc định (Firestore yêu cầu)
    public Category() {
    }

    // 🔹 Constructor đầy đủ
    public Category(String id, String categoryName, String tag, int imageResId) {
        this.id = id;
        this.categoryName = categoryName;
        this.tag = tag;
        this.imageResId = imageResId;
    }

    // 🔹 Getters và Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }
}
