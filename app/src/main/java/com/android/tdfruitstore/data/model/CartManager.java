package com.android.tdfruitstore.data.model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance; // Singleton instance
    private List<CartItem> cartItems;
    private DatabaseReference databaseRef; // Firebase Realtime Database Reference

    private CartManager() {
        cartItems = new ArrayList<>();
        databaseRef = FirebaseDatabase.getInstance().getReference("cart"); // Lưu giỏ hàng vào "cart"
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    /**
     * 🔹 Thêm sản phẩm vào giỏ hàng
     */
    public void addToCart(CartItem item) {
        boolean exists = false;
        for (CartItem cartItem : cartItems) {
            if (cartItem.getName().equals(item.getName())) {
                cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
                exists = true;
                break;
            }
        }
        if (!exists) {
            cartItems.add(item);
        }

        saveCartToFirebase(); // Lưu giỏ hàng lên Firebase mỗi khi có thay đổi
    }

    /**
     * 🔹 Xóa sản phẩm khỏi giỏ hàng
     */
    public void removeFromCart(CartItem item) {
        cartItems.remove(item);
        saveCartToFirebase(); // Cập nhật Firebase
    }

    /**
     * 🔹 Xóa toàn bộ giỏ hàng
     */
    public void clearCart() {
        cartItems.clear();
        databaseRef.removeValue(); // Xóa giỏ hàng trên Firebase
    }

    /**
     * 🔹 Tính tổng giá của giỏ hàng
     */
    public double getTotalPrice(String voucherCode) {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getQuantity() * item.getPrice();
        }
        if (voucherCode != null && !voucherCode.equals("Choose voucher code")) {
            if (voucherCode.contains("10%")) total *= 0.9;
            else if (voucherCode.contains("20%")) total *= 0.8;
            else if (voucherCode.contains("30%")) total *= 0.7;
            else if (voucherCode.contains("40%")) total *= 0.6;
        }
        return Math.round(total * 100.0) / 100.0;
    }

    /**
     * 🔹 Lưu giỏ hàng lên Firebase từng sản phẩm (mỗi sản phẩm có key riêng)
     */
    private void saveCartToFirebase() {
        databaseRef.setValue(null); // Xóa giỏ hàng cũ trước khi lưu
        for (CartItem item : cartItems) {
            databaseRef.child(item.getName()).setValue(item); // Lưu từng sản phẩm với tên là key
        }
    }

    /**
     * 🔹 Tải giỏ hàng từ Firebase
     */
    public void loadCartFromFirebase(OnCartLoadedListener listener) {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<CartItem> loadedCart = new ArrayList<>();

                if (!dataSnapshot.exists()) {
                    System.out.println("🔥 Firebase không có dữ liệu giỏ hàng!");
                    listener.onCartLoaded(loadedCart);
                    return;
                }

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CartItem item = snapshot.getValue(CartItem.class);
                    if (item != null) {
                        loadedCart.add(item);
                    }
                }

                System.out.println("🔥 Tải giỏ hàng thành công: " + loadedCart.size() + " sản phẩm.");
                listener.onCartLoaded(loadedCart);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Firebase Error: " + databaseError.getMessage());
                listener.onCartLoaded(new ArrayList<>()); // Trả về danh sách rỗng thay vì null
            }
        });
    }

    /**
     * Interface lắng nghe khi dữ liệu giỏ hàng tải xong
     */
    public interface OnCartLoadedListener {
        void onCartLoaded(List<CartItem> cartItems);
    }
}
