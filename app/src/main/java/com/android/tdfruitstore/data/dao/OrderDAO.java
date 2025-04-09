package com.android.tdfruitstore.data.dao;

import android.util.Log;
import com.android.tdfruitstore.data.entities.Order;
import com.android.tdfruitstore.data.entities.OrderDetail;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    private static final String COLLECTION_NAME = "orders";
    private FirebaseFirestore db;

    public OrderDAO() {
        db = FirebaseFirestore.getInstance();
    }

    // 🔹 Thêm đơn hàng vào Firestore
    public void insertOrder(Order order, FirestoreCallback<String> callback) {
        db.collection(COLLECTION_NAME)
                .add(order)
                .addOnSuccessListener(documentReference -> {
                    String orderId = documentReference.getId();
                    order.setId(orderId); // 🔥 Lưu ID vào order object
                    updateOrder(orderId, order, new FirestoreCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Log.d("Firestore", "✅ Order added with ID: " + orderId);
                            callback.onSuccess(orderId);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            callback.onFailure(e);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Lỗi khi thêm đơn hàng: " + e.getMessage());
                    callback.onFailure(e);
                });
    }

    // 🔹 Cập nhật đơn hàng
    public void updateOrder(String orderId, Order order, FirestoreCallback<Void> callback) {
        db.collection(COLLECTION_NAME)
                .document(orderId)
                .set(order)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ Order updated successfully!");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Lỗi khi cập nhật đơn hàng: " + e.getMessage());
                    callback.onFailure(e);
                });
    }

    // 🔹 Xóa đơn hàng
    public void deleteOrder(String orderId, FirestoreCallback<Boolean> callback) {
        db.collection(COLLECTION_NAME)
                .document(orderId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ Order deleted successfully!");
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Lỗi khi xóa đơn hàng: " + e.getMessage());
                    callback.onFailure(e);
                });
    }

    public void updateOrderStatus(String orderId, String status, FirestoreCallback<Boolean> callback) {
        db.collection(COLLECTION_NAME)
                .document(orderId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ Order status updated to: " + status);
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Lỗi khi cập nhật trạng thái đơn hàng: " + e.getMessage());
                    callback.onFailure(e);
                });
    }

    public void getOrdersByUser(String userId, FirestoreCallback<List<Order>> callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Order> orders = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = doc.toObject(Order.class);
                        order.setId(doc.getId());
                        orders.add(order);
                    }
                    Log.d("Firestore", "✅ Fetched orders for userId: " + userId);
                    callback.onSuccess(orders);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Lỗi khi lấy đơn hàng của userId: " + e.getMessage());
                    callback.onFailure(e);
                });
    }

    public void canUserCommentOnProduct(String userId, String productId, FirestoreCallback<Boolean> callback) {
        db.collection("orders")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "DELIVERED") // 🔥 Chỉ lấy đơn đã giao
                .get()
                .addOnSuccessListener(orderSnapshots -> {
                    if (orderSnapshots.isEmpty()) {
                        Log.d("Firestore", "❌ Không tìm thấy đơn hàng nào đã giao cho user: " + userId);
                        callback.onSuccess(false);
                        return;
                    }

                    List<String> deliveredOrderIds = new ArrayList<>();
                    for (QueryDocumentSnapshot orderDoc : orderSnapshots) {
                        deliveredOrderIds.add(orderDoc.getId());
                    }

                    // 🔥 Kiểm tra trong OrderDetail xem sản phẩm có trong đơn hàng đã giao không
                    db.collection("order_details")
                            .whereIn("orderId", deliveredOrderIds) // 🔥 Lọc theo danh sách orderId đã giao
                            .whereEqualTo("productId", productId)
                            .get()
                            .addOnSuccessListener(orderDetailSnapshots -> {
                                if (orderDetailSnapshots.isEmpty()) {
                                    Log.d("Firestore", "❌ Không có đơn hàng nào đã giao chứa sản phẩm " + productId);
                                    callback.onSuccess(false);
                                    return;
                                }

                                // 🔥 Kiểm tra xem sản phẩm đã có bình luận chưa
                                CommentDAO commentDAO = new CommentDAO();
                                commentDAO.hasUserCommented(productId, userId, new FirestoreCallback<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean hasCommented) {
                                        Log.d("HasCommented", "hasCommented: " + hasCommented);
                                        if (!hasCommented) {
                                            Log.d("Firestore", "✅ Người dùng có thể bình luận về sản phẩm " + productId);
                                            callback.onSuccess(true);
                                        } else {
                                            Log.d("Firestore", "❌ Người dùng đã bình luận về sản phẩm " + productId);
                                            callback.onSuccess(false);
                                        }
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Log.e("Firestore", "❌ Lỗi khi kiểm tra bình luận", e);
                                        callback.onFailure(e);
                                    }
                                });

                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "❌ Lỗi khi kiểm tra order_details: " + e.getMessage(), e);
                                callback.onFailure(e);
                            });

                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Lỗi khi kiểm tra đơn hàng: " + e.getMessage(), e);
                    callback.onFailure(e);
                });
    }
}
