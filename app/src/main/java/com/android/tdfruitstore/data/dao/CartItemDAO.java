package com.android.tdfruitstore.data.dao;

import android.util.Log;

import com.android.tdfruitstore.data.entities.CartItem;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class CartItemDAO {
    private final CollectionReference cartItemsRef;

    public CartItemDAO() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        cartItemsRef = db.collection("cartItems");
    }

    // Thêm item vào giỏ hàng
    public void insertCart(CartItem cartItem, FirestoreCallback<Boolean> callback) {
        String cartItemId = cartItem.getId();
        cartItemsRef.document(cartItemId)
                .set(cartItem)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(callback::onFailure);
    }

    // Lấy item giỏ hàng theo ID
    public void getCartItemById(String cartItemId, FirestoreCallback<CartItem> callback) {
        cartItemsRef.document(cartItemId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        CartItem cartItem = documentSnapshot.toObject(CartItem.class);
                        callback.onSuccess(cartItem);
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    // 🔹 Lấy CartItem theo userId và productId
    public void getCartItemByUserIdAndProductId(String userId, String productId, FirestoreCallback<CartItem> callback) {
        cartItemsRef
                .whereEqualTo("userId", userId)
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        CartItem cartItem = queryDocumentSnapshots.getDocuments().get(0).toObject(CartItem.class);
                        callback.onSuccess(cartItem);
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Lỗi khi lấy CartItem", e);
                    callback.onFailure(e);
                });
    }

    // Lấy tất cả sản phẩm trong giỏ hàng theo userId
    public void getCartItemsByUserId(String userId, FirestoreCallback<List<CartItem>> callback) {
        cartItemsRef
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.e("CartItemDAO", "❌ Không tìm thấy giỏ hàng cho user: " + userId);
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }
                    List<CartItem> cartItems = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        CartItem item = doc.toObject(CartItem.class);
                        item.setId(doc.getId()); // 🔥 Đặt ID Firestore cho CartItem
                        cartItems.add(item);
                    }
                    Log.d("CartItemDAO", "✅ Lấy giỏ hàng thành công: " + cartItems.size());
                    callback.onSuccess(cartItems);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Lỗi khi lấy giỏ hàng: " + e.getMessage(), e);
                    callback.onFailure(e);
                });
    }


    // Cập nhật số lượng sản phẩm trong giỏ hàng
    public void updateCartItemQuantity(String cartItemId, int quantity, FirestoreCallback<Boolean> callback) {
        cartItemsRef.document(cartItemId)
                .update("quantity", quantity)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(callback::onFailure);
    }

    // Xóa toàn bộ giỏ hàng theo userId
    public void clearCart(String userId, FirestoreCallback<Boolean> callback) {
        cartItemsRef.whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = cartItemsRef.getFirestore().batch();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        batch.delete(document.getReference());
                    }
                    batch.commit()
                            .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // Cập nhật CartItem
    public void updateCartItem(CartItem cartItem, FirestoreCallback<Boolean> callback) {
        cartItemsRef.document(cartItem.getId())
                .set(cartItem)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(callback::onFailure);
    }

    // Xóa item khỏi giỏ hàng
    public void deleteCartItem(String cartItemId, FirestoreCallback<Boolean> callback) {
        cartItemsRef.document(cartItemId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(callback::onFailure);
    }
}
