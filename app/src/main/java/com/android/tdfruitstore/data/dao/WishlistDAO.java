package com.android.tdfruitstore.data.dao;

import android.util.Log;
import com.android.tdfruitstore.data.entities.Wishlist;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class WishlistDAO {
    private final FirebaseFirestore db;

    public WishlistDAO() {
        this.db = FirebaseFirestore.getInstance();
    }

    // 🔹 Thêm sản phẩm vào Wishlist
    public void insertWishlist(Wishlist wishlist, FirestoreCallback<Boolean> callback) {
        db.collection("wishlist")
                .document(wishlist.getUserId() + "_" + wishlist.getProductId())  // Tạo document ID duy nhất
                .set(wishlist)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ Wishlist item added successfully");
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Failed to add wishlist item", e);
                    callback.onFailure(e);
                });
    }

    // 🔹 Cập nhật Wishlist item
    public void updateWishlistItem(Wishlist wishlist, FirestoreCallback<Boolean> callback) {
        db.collection("wishlist")
                .document(wishlist.getUserId() + "_" + wishlist.getProductId())
                .update(
                        "addedAt", wishlist.getAddedAt(),
                        "isBought", wishlist.isBought()
                )
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ Wishlist item updated successfully");
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Failed to update wishlist item", e);
                    callback.onFailure(e);
                });
    }

    // 🔹 Xóa sản phẩm khỏi Wishlist
    public void deleteWishlistItem(String userId, String productId, FirestoreCallback<Boolean> callback) {
        db.collection("wishlist")
                .document(userId + "_" + productId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ Wishlist item deleted successfully");
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Failed to delete wishlist item", e);
                    callback.onFailure(e);
                });
    }

    // 🔹 Lấy danh sách Wishlist theo User ID
    public void getWishlistByUserId(String userId, FirestoreCallback<List<Wishlist>> callback) {
        db.collection("wishlist")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Wishlist> wishlistItems = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Wishlist wishlist = document.toObject(Wishlist.class);
                        wishlistItems.add(wishlist);
                    }
                    callback.onSuccess(wishlistItems);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Failed to get wishlist items", e);
                    callback.onFailure(e);
                });
    }

    // 🔹 Kiểm tra sản phẩm có trong Wishlist không
    public void isProductInWishlist(String userId, String productId, FirestoreCallback<Boolean> callback) {
        db.collection("wishlist")
                .document(userId + "_" + productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> callback.onSuccess(documentSnapshot.exists()))
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Failed to check if product is in wishlist", e);
                    callback.onFailure(e);
                });
    }

    // 🔹 Xóa Wishlist theo userId và productId
    public void removeFromWishlist(String userId, String productId, FirestoreCallback<Boolean> callback) {
        db.collection("wishlist")
                .document(userId + "_" + productId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ Wishlist item removed successfully");
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Failed to remove wishlist item", e);
                    callback.onFailure(e);
                });
    }

    // 🔹 Lấy toàn bộ Wishlist (tất cả user)
    public void getAllWishlistItems(FirestoreCallback<List<Wishlist>> callback) {
        db.collection("wishlist")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Wishlist> wishlistItems = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Wishlist wishlist = document.toObject(Wishlist.class);
                        wishlistItems.add(wishlist);
                    }
                    callback.onSuccess(wishlistItems);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Failed to get all wishlist items", e);
                    callback.onFailure(e);
                });
    }
}