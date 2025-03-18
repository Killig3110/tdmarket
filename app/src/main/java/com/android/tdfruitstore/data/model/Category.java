package com.android.tdfruitstore.data.model;

public class Category {
    private String name;
    private String tag; // ID danh mục từ OpenFoodFacts API
    private int imageResource;

    public Category(String name, String tag, int imageResource) {
        this.name = name;
        this.tag = tag;
        this.imageResource = imageResource;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public int getImageResource() {
        return imageResource;
    }
}


