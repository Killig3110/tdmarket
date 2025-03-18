package com.android.tdfruitstore.ui.order;

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
import com.android.tdfruitstore.data.entities.OrderDetail;
import com.android.tdfruitstore.ui.adapter.OrderDetailAdapter;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailActivity extends AppCompatActivity {
    private RecyclerView rvOrderDetails;
    private OrderDetailAdapter orderDetailAdapter;
    private TextView tvOrderId;
    private Button btnBack;
    private OrderDetailDAO orderDetailDAO;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        rvOrderDetails = findViewById(R.id.rvOrderDetails);
        rvOrderDetails.setLayoutManager(new LinearLayoutManager(this));

        tvOrderId = findViewById(R.id.tvOrderId);

        orderDetailDAO = new OrderDetailDAO();

        orderId = getIntent().getStringExtra("orderId");

        if (orderId == null) {
            Log.e("Firestore", "❌ OrderId không được truyền vào!");
            Toast.makeText(this, "Không tìm thấy OrderId!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvOrderId.setText("Chi tiết đơn hàng #" + orderId);
        loadOrderDetails(orderId);

        btnBack = findViewById(R.id.btnCancelOrder);
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadOrderDetails(String orderId) {
        orderDetailDAO.getOrderDetailsByOrderId(orderId, new FirestoreCallback<List<OrderDetail>>() {
            @Override
            public void onSuccess(List<OrderDetail> orderDetails) {
                if (orderDetails == null || orderDetails.isEmpty()) {
                    Log.e("Firestore", "Không có OrderDetail nào cho OrderId: " + orderId);
                    Toast.makeText(OrderDetailActivity.this, "Không tìm thấy chi tiết đơn hàng!", Toast.LENGTH_SHORT).show();
                    return;
                }

                orderDetailAdapter = new OrderDetailAdapter(OrderDetailActivity.this, orderDetails);
                rvOrderDetails.setAdapter(orderDetailAdapter);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "❌ Lỗi khi tải chi tiết đơn hàng!", e);
                Toast.makeText(OrderDetailActivity.this, "Lỗi khi tải chi tiết đơn hàng!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
