package com.android.tdfruitstore.data.dao;

import android.util.Log;
import com.android.tdfruitstore.data.entities.OrderDetail;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class OrderDetailDAO {
    private final CollectionReference orderDetailsRef;

    public OrderDetailDAO() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.orderDetailsRef = db.collection("order_details");
    }

    // 🔥 Thêm order detail vào Firestore
    public void insertOrderDetail(OrderDetail orderDetail, FirestoreCallback<Boolean> callback) {
        String orderDetailId = orderDetailsRef.document().getId();
        orderDetail.setId(orderDetailId); // Tạo ID Firestore

        orderDetailsRef.document(orderDetailId)
                .set(orderDetail)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ OrderDetail added: " + orderDetailId);
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Failed to add OrderDetail", e);
                    callback.onFailure(e);
                });
    }

    public void insertOrderDetails(String orderId, List<OrderDetail> orderDetails, FirestoreCallback<Boolean> callback) {
        for (OrderDetail orderDetail : orderDetails) {
            String orderDetailId = orderDetailsRef.document().getId();
            orderDetail.setId(orderDetailId);
            orderDetail.setOrderId(orderId);

            orderDetailsRef.document(orderDetailId)
                    .set(orderDetail)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "✅ OrderDetail added: " + orderDetailId);
                        callback.onSuccess(true);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "❌ Failed to add OrderDetail", e);
                        callback.onFailure(e);
                    });
        }
    }

    // 🔥 Cập nhật order detail trên Firestore
    public void updateOrderDetail(OrderDetail orderDetail, FirestoreCallback<Boolean> callback) {
        orderDetailsRef.document(orderDetail.getId())
                .set(orderDetail)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ OrderDetail updated: " + orderDetail.getId());
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Failed to update OrderDetail", e);
                    callback.onFailure(e);
                });
    }

    // 🔥 Xóa order detail khỏi Firestore
    public void deleteOrderDetail(String orderDetailId, FirestoreCallback<Boolean> callback) {
        orderDetailsRef.document(orderDetailId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ OrderDetail deleted: " + orderDetailId);
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Failed to delete OrderDetail", e);
                    callback.onFailure(e);
                });
    }

    // 🔥 Lấy danh sách order details theo orderId
    public void getOrderDetailsByOrderId(String orderId, FirestoreCallback<List<OrderDetail>> callback) {
        orderDetailsRef.whereEqualTo("orderId", orderId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<OrderDetail> orderDetails = queryDocumentSnapshots.toObjects(OrderDetail.class);
                    Log.d("Firestore", "✅ Retrieved OrderDetails by Order ID: " + orderId);
                    callback.onSuccess(orderDetails);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Failed to fetch OrderDetails by Order ID", e);
                    callback.onFailure(e);
                });
    }

    // 🔥 Lấy tất cả order details
    public void getAllOrderDetails(FirestoreCallback<List<OrderDetail>> callback) {
        orderDetailsRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<OrderDetail> orderDetails = queryDocumentSnapshots.toObjects(OrderDetail.class);
                    Log.d("Firestore", "✅ Retrieved all OrderDetails");
                    callback.onSuccess(orderDetails);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Failed to fetch all OrderDetails", e);
                    callback.onFailure(e);
                });
    }
}
