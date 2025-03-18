package com.android.tdfruitstore.ui.order;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.tdfruitstore.R;
import com.android.tdfruitstore.data.dao.FirestoreCallback;
import com.android.tdfruitstore.data.dao.OrderDAO;
import com.android.tdfruitstore.data.dao.OrderDetailDAO;
import com.android.tdfruitstore.data.dao.CartItemDAO;
import com.android.tdfruitstore.data.dao.ProductDAO;
import com.android.tdfruitstore.data.entities.CartItem;
import com.android.tdfruitstore.data.entities.Order;
import com.android.tdfruitstore.data.entities.OrderDetail;
import com.android.tdfruitstore.ui.adapter.OrderDetailAdapter;
import com.android.tdfruitstore.ui.login.LoginActivity;
import com.google.firebase.Timestamp;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderActivity extends AppCompatActivity {
    private RecyclerView rvOrderItems;
    private OrderDetailAdapter orderDetailAdapter;
    private List<CartItem> orderItems;
    private TextView tvTotalPrice, tvVoucherCode;
    private Button btnConfirmOrder, btnCancelOrder;
    private OrderDAO orderDAO;
    private OrderDetailDAO orderDetailDAO;
    private CartItemDAO cartItemDAO;
    private ProductDAO productDAO;
    private String userId;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        rvOrderItems = findViewById(R.id.rvOrderItems);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));

        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvVoucherCode = findViewById(R.id.tvVoucherCode);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);

        orderDAO = new OrderDAO();
        orderDetailDAO = new OrderDetailDAO();
        cartItemDAO = new CartItemDAO();
        productDAO = new ProductDAO();

        // Lấy userId từ SharedPreferences
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = preferences.getString("userId", null);

        if (userId.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để đặt hàng!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Nhận dữ liệu từ Intent
        orderItems = (List<CartItem>) getIntent().getSerializableExtra("order_items");
        tvTotalPrice.setText(getIntent().getStringExtra("total_price"));
        tvVoucherCode.setText(getIntent().getStringExtra("voucher_code"));

        // Kiểm tra nếu không có sản phẩm
        if (orderItems == null || orderItems.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        createNewOrder();

        // Hiển thị danh sách sản phẩm đặt hàng bằng OrderDetailAdapter
        orderDetailAdapter = new OrderDetailAdapter(this, convertCartToOrderDetail(orderItems));
        rvOrderItems.setAdapter(orderDetailAdapter);

        btnConfirmOrder.setOnClickListener(v -> confirmOrder());
        btnCancelOrder.setOnClickListener(v -> cancelOrder());
    }

    // Chuyển đổi CartItem thành OrderDetail
    private List<OrderDetail> convertCartToOrderDetail(List<CartItem> cartItems) {
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartItem item : cartItems) {
            double subTotal = item.getQuantity() * item.getPrice();
            orderDetails.add((new OrderDetail(item.getProductId(), item.getQuantity(), item.getPrice(), subTotal)));
        }
        return orderDetails;
    }

    // Tạo Order ngay khi mở OrderActivity
    private void createNewOrder() {
        String totalString = tvTotalPrice.getText().toString().trim();
        totalString = totalString.replaceAll("[^\\d.]", ""); // Loại bỏ ký tự không phải số

        double totalPrice = Double.parseDouble(totalString);
        Order newOrder = new Order(userId, Timestamp.now(), totalPrice, "PENDING");

        orderDAO.insertOrder(newOrder, new FirestoreCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if (result != null) {
                    orderId = result; // ✅ Gán orderId ngay khi tạo đơn hàng thành công
                    SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    preferences.edit().putString("orderId", orderId).apply();
                    Log.d("OrderActivity", "✅ Đã tạo đơn hàng với ID: " + orderId);
                } else {
                    Log.e("OrderActivity", "❌ orderId nhận được là null!");
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("OrderActivity", "❌ Lỗi khi tạo đơn hàng", e);
            }
        });
    }

    private void confirmOrder() {
        // ✅ Lưu OrderDetail vào Firestore
        orderDetailDAO.insertOrderDetails(orderId, convertCartToOrderDetail(orderItems), new FirestoreCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean detailResult) {
                if (detailResult) {
                    // ✅ Cập nhật trạng thái Order thành "CONFIRMED"
                    orderDAO.updateOrderStatus(orderId, "DELIVERED", new FirestoreCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            // ✅ Cập nhật tồn kho sản phẩm
                            updateProductStock(orderItems);

                            // ✅ Xóa giỏ hàng sau khi đặt hàng thành công
                            cartItemDAO.clearCart(userId, new FirestoreCallback<Boolean>() {
                                @Override
                                public void onSuccess(Boolean success) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(OrderActivity.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(OrderActivity.this, OrderHistoryActivity.class));
                                        finish();
                                    });
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Log.e("Firestore", "❌ Lỗi khi xóa giỏ hàng", e);
                                }
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e("Firestore", "❌ Lỗi khi cập nhật trạng thái đơn hàng", e);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "❌ Lỗi khi lưu chi tiết đơn hàng", e);
            }
        });
    }

    private void cancelOrder() {
        orderDAO.deleteOrder(orderId, new FirestoreCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                runOnUiThread(() -> {
                    Toast.makeText(OrderActivity.this, "Đã hủy đơn hàng!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "❌ Lỗi khi xóa đơn hàng", e);
            }
        });
    }

    // ✅ Cập nhật tồn kho sản phẩm
    private void updateProductStock(List<CartItem> items) {
        for (CartItem item : items) {
            productDAO.updateProductStock(item.getProductId(), item.getQuantity(), new FirestoreCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    Log.d("Firestore", "✅ Đã cập nhật tồn kho cho sản phẩm: " + item.getProductId());
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("Firestore", "❌ Lỗi khi cập nhật tồn kho", e);
                }
            });
        }
    }
}
