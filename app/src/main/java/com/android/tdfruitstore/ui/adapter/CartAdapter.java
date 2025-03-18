package com.android.tdfruitstore.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.tdfruitstore.R;
import com.android.tdfruitstore.data.dao.CartItemDAO;
import com.android.tdfruitstore.data.dao.FirestoreCallback;
import com.android.tdfruitstore.data.dao.ProductDAO;
import com.android.tdfruitstore.data.entities.CartItem;
import com.android.tdfruitstore.data.entities.Product;
import com.android.tdfruitstore.ui.cart.CartActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private Context context;
    private List<CartItem> cartItemList;
    private ProductDAO productDAO;
    private CartItemDAO cartItemDAO;
    private FirebaseUser currentUser;

    public CartAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.cartItemDAO = new CartItemDAO();
        this.productDAO = new ProductDAO();
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartItemList.get(position);

        // Lấy thông tin sản phẩm từ Firestore
        productDAO.getProductById(item.getProductId(), new FirestoreCallback<Product>() {
            @Override
            public void onSuccess(Product product) {
                if (product != null) {
                    Glide.with(context).load(product.getImageUrl()).into(holder.ivProductImage);
                    holder.tvProductName.setText(product.getName());
                    holder.tvPrice.setText(String.format("%.2f $", product.getPrice()));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "❌ Lỗi khi lấy sản phẩm", e);
            }
        });

        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        // Tăng số lượng sản phẩm
        holder.btnIncrease.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() + 1;
            item.setQuantity(newQuantity);
            updateCartItem(item);
            holder.tvQuantity.setText(String.valueOf(newQuantity));
        });

        // Giảm số lượng sản phẩm
        holder.btnDecrease.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                int newQuantity = item.getQuantity() - 1;
                item.setQuantity(newQuantity);
                updateCartItem(item);
                holder.tvQuantity.setText(String.valueOf(newQuantity));
            } else {
                // Xóa sản phẩm khỏi giỏ hàng
                removeCartItem(position, item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }


    // 🔥 Cập nhật số lượng sản phẩm trong giỏ hàng
    private void updateCartItem(CartItem cartItem) {
        if (currentUser != null) {
            cartItemDAO.updateCartItem(cartItem, new FirestoreCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    Log.d("Firestore", "✅ Cập nhật số lượng sản phẩm thành công!");
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("Firestore", "❌ Lỗi khi cập nhật số lượng sản phẩm", e);
                }
            });
        }
    }

    // 🔥 Xóa sản phẩm khỏi giỏ hàng
    private void removeCartItem(int position, CartItem item) {
        if (currentUser != null) {
            cartItemDAO.deleteCartItem(item.getId(), new FirestoreCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    if (result) {
                        ((Activity) context).runOnUiThread(() -> {
                            cartItemList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, cartItemList.size());

                            // Cập nhật tổng tiền
                            if (context instanceof CartActivity) {
                                ((CartActivity) context).updateTotalPrice();
                            }
                        });

                        Log.d("DEBUG", "✅ Sản phẩm đã bị xóa khỏi giỏ hàng: " + item.getProductId());
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("Firestore", "❌ Lỗi khi xóa sản phẩm khỏi giỏ hàng", e);
                }
            });
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvPrice, tvQuantity, tvTotalPrice;
        ImageView ivProductImage;
       CircleImageView btnDecrease, btnIncrease;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
        }
    }
}
