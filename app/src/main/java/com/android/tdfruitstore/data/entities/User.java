package com.android.tdfruitstore.data.entities;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;

public class User implements Serializable {
    private String id;         // 🔥 Chuyển ID từ int -> String
    private String email;
    private String name;
    private String password;
    private String avatarUrl;

    // 🔹 Constructor không tham số (cần thiết cho Firestore)
    public User() {
    }

    // 🔹 Constructor đầy đủ tham số
    public User(String id, String email, String name, String password, String avatarUrl) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.avatarUrl = avatarUrl;
    }

    // 🔹 Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
