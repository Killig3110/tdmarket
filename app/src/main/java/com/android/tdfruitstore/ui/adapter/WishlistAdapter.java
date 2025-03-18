package com.android.tdfruitstore.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.tdfruitstore.R;
import com.android.tdfruitstore.data.dao.CartItemDAO;
import com.android.tdfruitstore.data.dao.FirestoreCallback;
import com.android.tdfruitstore.data.dao.ProductDAO;
import com.android.tdfruitstore.data.dao.WishlistDAO;
import com.android.tdfruitstore.data.entities.CartItem;
import com.android.tdfruitstore.data.entities.Product;
import com.android.tdfruitstore.data.entities.Wishlist;
import com.android.tdfruitstore.ui.home.ProductDetailActivity;
import com.bumptech.glide.Glide;

import java.util.List;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {
    private Context context;
    private List<Wishlist> wishlistItems;
    private WishlistDAO wishlistDAO;
    private ProductDAO productDAO;
    private CartItemDAO cartItemDAO;
    private String userId;

    public WishlistAdapter(Context context, List<Wishlist> wishlistItems, String userId) {
        this.context = context;
        this.wishlistItems = wishlistItems;
        this.userId = userId;
        this.wishlistDAO = new WishlistDAO();
        this.productDAO = new ProductDAO();
        this.cartItemDAO = new CartItemDAO();
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wishlist, parent, false);
        return new WishlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Wishlist wishlistItem = wishlistItems.get(position);

        productDAO.getProductById(wishlistItem.getProductId(), new FirestoreCallback<Product>() {
            @Override
            public void onSuccess(Product product) {
                if (product != null) {
                    Glide.with(context)
                            .load(product.getImageUrl())
                            .placeholder(R.drawable.ic_placeholder)
                            .into(holder.ivProductImage);

                    holder.tvProductName.setText(product.getName());
                    holder.tvProductPrice.setText(String.format("$ %.2f", product.getPrice()));

                    // Bấm vào sản phẩm để xem chi tiết
                    holder.itemView.setOnClickListener(v -> {
                        Intent intent = new Intent(context, ProductDetailActivity.class);
                        intent.putExtra("productId", product.getId());
                        intent.putExtra("name", product.getName());
                        intent.putExtra("imageUrl", product.getImageUrl());
                        intent.putExtra("price", product.getPrice());
                        context.startActivity(intent);
                    });

                    // Nút xoá khỏi danh sách yêu thích
                    holder.btnRemove.setOnClickListener(v -> {
                        wishlistDAO.removeFromWishlist(userId, product.getId(), new FirestoreCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                wishlistItems.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, wishlistItems.size());
                                Toast.makeText(context, "Đã xóa khỏi danh sách yêu thích!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("Firestore", "❌ Lỗi khi xóa khỏi wishlist", e);
                                Toast.makeText(context, "Lỗi khi xóa sản phẩm!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });

                    // Nút thêm vào giỏ hàng
                    holder.btnAddToCart.setOnClickListener(v -> {
                        cartItemDAO.getCartItemByUserIdAndProductId(userId, product.getId(), new FirestoreCallback<CartItem>() {
                            @Override
                            public void onSuccess(CartItem existingCartItem) {
                                if (existingCartItem != null) {
                                    int newQuantity = existingCartItem.getQuantity() + 1;
                                    existingCartItem.setQuantity(newQuantity);
                                    cartItemDAO.updateCartItem(existingCartItem, new FirestoreCallback<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean result) {
                                            Toast.makeText(context, "Sản phẩm đã cập nhật số lượng trong giỏ hàng!", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            Log.e("Firestore", "❌ Lỗi khi cập nhật giỏ hàng", e);
                                        }
                                    });
                                } else {
                                    // Thêm sản phẩm mới vào giỏ hàng
                                    String cartId = java.util.UUID.randomUUID().toString();
                                    CartItem cartItem = new CartItem(cartId, userId, product.getId(), product.getImageUrl(), 1, product.getPrice());
                                    cartItemDAO.insertCart(cartItem, new FirestoreCallback<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean result) {
                                            Toast.makeText(context, "Sản phẩm đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            Log.e("Firestore", "❌ Lỗi khi thêm vào giỏ hàng", e);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("Firestore", "❌ Lỗi khi kiểm tra giỏ hàng", e);
                            }
                        });
                    });
                } else {
                    Log.e("Firestore", "❌ Không tìm thấy sản phẩm với ID: " + wishlistItem.getProductId());
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "❌ Lỗi khi tải sản phẩm từ Firestore", e);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wishlistItems.size();
    }

    public static class WishlistViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage, btnRemove, btnAddToCart;
        TextView tvProductName, tvProductPrice;

        public WishlistViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            btnRemove = itemView.findViewById(R.id.btnRemoveWishlist);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}
