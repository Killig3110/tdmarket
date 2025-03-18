package com.android.tdfruitstore.ui.cart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.tdfruitstore.R;
import com.android.tdfruitstore.data.dao.CartItemDAO;
import com.android.tdfruitstore.data.dao.FirestoreCallback;
import com.android.tdfruitstore.data.entities.CartItem;
import com.android.tdfruitstore.ui.adapter.CartAdapter;
import com.android.tdfruitstore.ui.order.OrderActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CartActivity extends AppCompatActivity {
    private RecyclerView rvCart;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList = new ArrayList<>();
    private TextView tvTotalPrice;
    private Button btnCheckout;
    private ImageView btnBack;
    private Spinner spnVoucher;
    private CartItemDAO cartItemDAO;
    private SharedPreferences sharedPreferences;
    private String userId, voucherCode;
    private boolean isUpdatingVoucher = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        rvCart = findViewById(R.id.rvCart);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnCheckout = findViewById(R.id.btnCheckout);
        spnVoucher = findViewById(R.id.spVoucherCode);
        btnBack = findViewById(R.id.btnBack);

        cartItemDAO = new CartItemDAO();

        // Lấy userId từ SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);

        if (userId == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Spinner spVoucherCode = findViewById(R.id.spVoucherCode);

        String[] voucherList = {"No Voucher", "DISCOUNT10 - 10% Off", "FREESHIP - Free Shipping", "SALE20 - 20% Off"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_spinner_item, voucherList);
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        spVoucherCode.setAdapter(adapter);


        // Load cart items from room database
        loadCartItems();

        btnCheckout.setOnClickListener(v -> {
            if (cartItemList.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng trống!", Toast.LENGTH_SHORT).show();
            } else {
                // Xóa sản phẩm có số lượng = 0 trước khi Checkout
                cleanUpCartBeforeCheckout();

                Intent intent = new Intent(this, OrderActivity.class);
                intent.putParcelableArrayListExtra("order_items", new ArrayList<>(cartItemList));

                // Truyền tổng giá & mã giảm giá
                String selectedVoucher = spnVoucher.getSelectedItem().toString();
                intent.putExtra("total_price", tvTotalPrice.getText().toString());
                intent.putExtra("voucher_code", selectedVoucher);

                startActivity(intent);
            }
        });


        btnBack.setOnClickListener(v -> {
            finish();
        });

        spnVoucher.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isUpdatingVoucher) {
                    return; // 🔥 Bỏ qua sự kiện nếu đang cập nhật
                }

                String newVoucher = parent.getItemAtPosition(position).toString();
                if (!newVoucher.equals(voucherCode)) { // 🔥 Chỉ cập nhật nếu mã giảm giá thay đổi
                    voucherCode = newVoucher;
                    updateTotalPrice();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // Hàm xóa sản phẩm có số lượng = 0
    private void cleanUpCartBeforeCheckout() {
        List<CartItem> validItems = new ArrayList<>();
        for (CartItem item : cartItemList) {
            if (item.getQuantity() > 0) {
                validItems.add(item);
            } else {
                cartItemDAO.deleteCartItem(item.getId(), new FirestoreCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean success) {
                        if (success) {
                            Log.d("Firestore", "✅ Xóa sản phẩm khỏi giỏ hàng (số lượng 0): " + item.getProductId());
                        } else {
                            Log.e("Firestore", "❌ Lỗi khi xóa sản phẩm khỏi giỏ hàng!");
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("Firestore", "❌ Lỗi khi xóa sản phẩm khỏi giỏ hàng!", e);
                    }
                });
            }
        }
        cartItemList.clear();
        cartItemList.addAll(validItems);
        cartAdapter.notifyDataSetChanged();
        updateTotalPrice();
    }

    // 🔥 Áp dụng giảm giá
    private double applyDiscount(String voucher, double totalPrice) {
        if (cartItemList == null || cartItemList.isEmpty()) {
            return 0.0;
        }

        double discount = 0.0;

        // 🔥 Kiểm tra xem voucher có % không
        Pattern pattern = Pattern.compile("(\\d+)%");
        Matcher matcher = pattern.matcher(voucher);

        if (matcher.find()) {
            try {
                int percent = Integer.parseInt(matcher.group(1));
                discount = percent / 100.0;
            } catch (NumberFormatException e) {
                Log.e("Discount", "❌ Không thể parse phần trăm giảm giá!", e);
            }
        }

        // 🔥 Tính tổng giá sau khi giảm
        double discountedPrice = totalPrice * (1 - discount);

        return discountedPrice;
    }

    // 🔥 Tính tổng giá
    private double calculateTotalPrice() {
        if (cartItemList == null || cartItemList.isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        for (CartItem item : cartItemList) {
            total += item.getPrice() * item.getQuantity();
        }

        // Kiểm tra xem có giảm giá không
        voucherCode = spnVoucher.getSelectedItem().toString();
        if (voucherCode.contains("No Voucher")) {
            return total;
        } else {
            return applyDiscount(voucherCode, total);
        }
    }

    // Load cart items from Room database
    // 🔥 Load giỏ hàng từ Firestore
    private void loadCartItems() {
        Log.d("UserId", "🆔 User ID: " + userId);
        cartItemDAO.getCartItemsByUserId(userId, new FirestoreCallback<List<CartItem>>() {
            @Override
            public void onSuccess(List<CartItem> cartItems) {
                cartItemList.clear();
                cartItemList.addAll(cartItems);
                cartAdapter = new CartAdapter(CartActivity.this, cartItems);
                rvCart.setLayoutManager(new LinearLayoutManager(CartActivity.this));
                rvCart.setAdapter(cartAdapter);
                updateTotalPrice();
                cartAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "❌ Lỗi khi lấy dữ liệu giỏ hàng!", e);
            }
        });
    }

    //Cập nhật tổng tiền
    public void updateTotalPrice() {
        tvTotalPrice.setText(String.format("Total: $%.2f", calculateTotalPrice()));
    }

}
