package com.android.tdfruitstore.data.dao;

import android.util.Log;
import com.android.tdfruitstore.data.entities.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserDAO {
    private final FirebaseFirestore db;

    public UserDAO() {
        this.db = FirebaseFirestore.getInstance();
    }

    // 🔹 Thêm user vào Firestore
    public void insertUser(User user, FirestoreCallback<Boolean> callback) {
        db.collection("users").document(user.getEmail()).set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ User added: " + user.getEmail());
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Failed to add user", e);
                    callback.onFailure(e);
                });
    }

    // 🔹 Lấy user theo email từ Firestore
    public void getUserByEmail(String email, FirestoreCallback<User> callback) {
        db.collection("users").document(email)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            callback.onSuccess(user);
                        } else {
                            Log.e("Firestore", "❌ Lỗi: Không thể deserialize user.");
                            callback.onFailure(new Exception("Lỗi deserialize user"));
                        }
                    } else {
                        Log.e("Firestore", "❌ Không tìm thấy user với email: " + email);
                        callback.onFailure(new Exception("Không tìm thấy user"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    // 🔹 Lấy user ID từ Firestore theo email
    public void getUserIdByEmail(String email, FirestoreCallback<String> callback) {
        db.collection("users").document(email)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("id")) {
                        String userId = documentSnapshot.getString("id");
                        callback.onSuccess(userId);
                    } else {
                        Log.e("Firestore", "❌ Không tìm thấy user ID với email: " + email);
                        callback.onFailure(new Exception("User ID không tồn tại"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    // 🔹 Lấy user theo ID từ Firestore (Sửa `int userId` thành `String userId`)
    public void getUserById(String userId, FirestoreCallback<User> callback) {
        db.collection("users").whereEqualTo("id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            callback.onSuccess(user);
                        } else {
                            Log.e("Firestore", "❌ Lỗi deserialize User từ Firestore.");
                            callback.onFailure(new Exception("Lỗi deserialize User"));
                        }
                    } else {
                        Log.e("Firestore", "❌ Không tìm thấy user với ID: " + userId);
                        callback.onFailure(new Exception("Không tìm thấy user"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    // 🔹 Xóa user khỏi Firestore
    public void deleteUser(String email, FirestoreCallback<Boolean> callback) {
        db.collection("users").document(email)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ User deleted: " + email);
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Failed to delete user", e);
                    callback.onFailure(e);
                });
    }

    // 🔹 Cập nhật thông tin user trong Firestore
    public void updateUser(User user, FirestoreCallback<Boolean> callback) {
        db.collection("users").document(user.getEmail())
                .update(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "name", user.getName(),
                        "password", user.getPassword(),
                        "avatarUrl", user.getAvatarUrl()
                )
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ User updated: " + user.getEmail());
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Failed to update user", e);
                    callback.onFailure(e);
                });
    }
}
