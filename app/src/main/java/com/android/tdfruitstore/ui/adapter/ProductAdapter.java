package com.android.tdfruitstore.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.tdfruitstore.R;
import com.android.tdfruitstore.data.dao.FirestoreCallback;
import com.android.tdfruitstore.data.dao.UserDAO;
import com.android.tdfruitstore.data.entities.CartItem;
import com.android.tdfruitstore.data.entities.Product;
import com.android.tdfruitstore.ui.home.ProductDetailActivity;
import com.android.tdfruitstore.ui.order.OrderActivity;
import com.bumptech.glide.Glide;
import com.android.tdfruitstore.data.entities.User;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private Context context;
    private List<Product> productList;
    private boolean useSquareLayout;
    private UserDAO userDAO;

    public ProductAdapter(Context context, List<Product> productList, boolean useSquareLayout) {
        this.context = context;
        this.productList = productList;
        this.useSquareLayout = useSquareLayout;
        this.userDAO = new UserDAO();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (useSquareLayout) {
            view = LayoutInflater.from(context).inflate(R.layout.item_product_square, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        // Gán dữ liệu cho UI
        holder.tvName.setText(product.getName());

        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_placeholder) // Ảnh chờ
                .into(holder.ivProductImage);

        holder.tvCategory.setText(product.getCategory());
        holder.tvPrice.setText(String.format("$ %.2f", product.getPrice()));
        // Xử lý sự kiện click vào sản phẩm
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("productId", product.getId());
            intent.putExtra("name", product.getName());
            intent.putExtra("imageUrl", product.getImageUrl());
            intent.putExtra("category", product.getCategory());
            intent.putExtra("code", product.getCode());
            intent.putExtra("price", product.getPrice());
            context.startActivity(intent);
        });

        // ✅ Xử lý thêm vào giỏ hàng (Dùng Firestore để lấy `userId`)
        holder.btnAddToCart.setOnClickListener(v -> {
            String userEmail = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    .getString("email", null);

            if (userEmail != null) {
                userDAO.getUserByEmail(userEmail, new FirestoreCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        if (user != null) {
                            String cartId = java.util.UUID.randomUUID().toString();

                            // 🔥 Thêm sản phẩm vào giỏ hàng với cartId
                            List<CartItem> orderItems = new ArrayList<>();
                            orderItems.add(new CartItem(cartId, user.getId(), product.getId(), product.getImageUrl(), 1, product.getPrice()));

                            Intent intent = new Intent(context, OrderActivity.class);
                            intent.putParcelableArrayListExtra("order_items", new ArrayList<>(orderItems));
                            intent.putExtra("total_price", String.format("Total: $%.2f", product.getPrice()));
                            intent.putExtra("voucher_code", "No Voucher");
                            context.startActivity(intent);
                        } else {
                            Toast.makeText(context, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("Firestore", "❌ Lỗi khi lấy thông tin người dùng", e);
                        Toast.makeText(context, "Lỗi kết nối đến Firestore!", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(context, "Vui lòng đăng nhập trước khi mua hàng!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvName, tvCategory, tvPrice;
        Button btnAddToCart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
    public void updateList(List<Product> newProducts) {
        if (newProducts != null && !newProducts.isEmpty()) {
            productList.clear();
            productList.addAll(newProducts);
            notifyDataSetChanged();
        } else {
            Toast.makeText(context, "Không có sản phẩm nào!", Toast.LENGTH_SHORT).show();
        }
    }

}
