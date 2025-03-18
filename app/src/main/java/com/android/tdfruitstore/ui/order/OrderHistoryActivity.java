package com.android.tdfruitstore.ui.order;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.tdfruitstore.R;
import com.android.tdfruitstore.data.dao.FirestoreCallback;
import com.android.tdfruitstore.data.dao.OrderDAO;
import com.android.tdfruitstore.data.dao.OrderDetailDAO;
import com.android.tdfruitstore.data.entities.Order;
import com.android.tdfruitstore.ui.adapter.OrderAdapter;
import com.android.tdfruitstore.ui.home.HomeActivity;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {
    private RecyclerView rvOrders;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private OrderDAO orderDAO;
    private Button btnBack;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        rvOrders = findViewById(R.id.rvOrderHistory);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));

        orderDAO = new OrderDAO();

        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(this, orderList);
        rvOrders.setAdapter(orderAdapter);

        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            // Back to home
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        });

        // Lấy userId từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);

        if (userId == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        loadOrdersFromFirestore();
    }

    private void loadOrdersFromFirestore() {
        orderDAO.getOrdersByUser(userId, new FirestoreCallback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> orders) {
                if (orders == null || orders.isEmpty()) {
                    Toast.makeText(OrderHistoryActivity.this, "Bạn chưa có đơn hàng nào!", Toast.LENGTH_SHORT).show();
                } else {
                    orderList.clear();
                    orderList.addAll(orders);
                    orderAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(OrderHistoryActivity.this, "❌ Lỗi khi tải đơn hàng!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
