package com.android.tdfruitstore.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.tdfruitstore.R;
import com.android.tdfruitstore.data.entities.Order;
import com.android.tdfruitstore.ui.order.OrderDetailActivity;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private Context context;
    private List<Order> orderList;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        if (orderList.isEmpty()) {
            Toast.makeText(context, "Bạn chưa có đơn hàng nào!", Toast.LENGTH_SHORT).show();
        }


        if (order.getOrderDate() instanceof com.google.firebase.Timestamp) {
            com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) order.getOrderDate();
            Date date = timestamp.toDate(); // ✅ Chuyển Timestamp thành Date
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvOrderDate.setText(dateFormat.format(date));
        } else {
            holder.tvOrderDate.setText("Ngày đặt: Không xác định");
        }

        holder.tvOrderId.setText("Mã đơn hàng: #" + order.getId());
        holder.tvOrderStatus.setText("Trạng thái: " + order.getStatus());
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        holder.tvOrderTotal.setText("Tổng tiền: " + formatter.format(order.getTotalPrice()));

        // Khi bấm vào đơn hàng, mở OrderDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("orderId", order.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvOrderStatus, tvOrderTotal;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderTotal = itemView.findViewById(R.id.tvTotalPrice);
        }
    }
}
