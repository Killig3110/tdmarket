package com.android.tdfruitstore.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.tdfruitstore.R;
import com.android.tdfruitstore.data.dao.FirestoreCallback;
import com.android.tdfruitstore.data.dao.ProductDAO;
import com.android.tdfruitstore.data.entities.OrderDetail;
import com.android.tdfruitstore.data.entities.Product;
import com.android.tdfruitstore.ui.home.ProductDetailActivity;
import com.bumptech.glide.Glide;

import java.util.List;


public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder> {
    private Context context;
    private List<OrderDetail> orderDetailList;
    private ProductDAO productDAO;

    public OrderDetailAdapter(Context context, List<OrderDetail> orderDetailList) {
        this.context = context;
        this.orderDetailList = orderDetailList;
        this.productDAO = new ProductDAO();
    }

    @NonNull
    @Override
    public OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_detail, parent, false);
        return new OrderDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailViewHolder holder, int position) {
        if (orderDetailList == null || position >= orderDetailList.size()) {
            Log.e("DEBUG", "orderDetailList trống hoặc vị trí không hợp lệ: " + position);
            return;
        }

        OrderDetail orderDetail = orderDetailList.get(position);
        if (orderDetail == null) {
            Log.e("DEBUG", "OrderDetail tại vị trí " + position + " là NULL!");
            return;
        }

        // 🔥 Lấy thông tin sản phẩm từ Firestore
        productDAO.getProductById(orderDetail.getProductId(), new FirestoreCallback<Product>() {
            @Override
            public void onSuccess(Product product) {
                if (product != null) {
                    holder.tvProductName.setText(product.getName() != null ? product.getName() : "Unknown Product");
                    holder.tvQuantity.setText("Số lượng: " + orderDetail.getQuantity());
                    holder.tvPrice.setText("Đơn giá: $" + String.format("%.2f", orderDetail.getPriceAtTime()));
                    holder.tvSubTotal.setText("Tổng tiền: $" + String.format("%.2f", orderDetail.getSubTotal()));

                    if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                        Glide.with(context)
                                .load(product.getImageUrl())
                                .placeholder(R.drawable.ic_category_placeholder) // Placeholder khi tải ảnh
                                .into(holder.ivProductImage);
                    } else {
                        holder.ivProductImage.setImageResource(R.drawable.ic_category_placeholder);
                    }
                } else {
                    Log.e("Firestore", "Không tìm thấy sản phẩm với ID: " + orderDetail.getProductId());
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "❌ Lỗi khi lấy thông tin sản phẩm", e);
            }
        });

        // 🔥 Xử lý click mở chi tiết sản phẩm
        holder.itemView.setOnClickListener(v -> {
            productDAO.getProductById(orderDetail.getProductId(), new FirestoreCallback<Product>() {
                @Override
                public void onSuccess(Product product) {
                    if (product != null) {
                        Intent intent = new Intent(context, ProductDetailActivity.class);
                        intent.putExtra("productId", product.getId());
                        intent.putExtra("name", product.getName());
                        intent.putExtra("imageUrl", product.getImageUrl());
                        intent.putExtra("category", product.getCategory());
                        intent.putExtra("code", product.getCode());
                        intent.putExtra("price", product.getPrice());
                        context.startActivity(intent);
                    } else {
                        Log.e("Firestore", "Không tìm thấy sản phẩm với ID: " + orderDetail.getProductId());
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("Firestore", "❌ Lỗi khi lấy thông tin sản phẩm để mở chi tiết", e);
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return (orderDetailList != null) ? orderDetailList.size() : 0;
    }

    public static class OrderDetailViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvQuantity, tvPrice, tvSubTotal;
        ImageView ivProductImage;

        public OrderDetailViewHolder(@NonNull View itemView) {
            super(itemView);

            tvProductName = itemView.findViewById(R.id.tvName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvSubTotal = itemView.findViewById(R.id.tvTotalPrice);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
        }
    }
}



