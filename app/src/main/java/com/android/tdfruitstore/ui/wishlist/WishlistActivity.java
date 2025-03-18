package com.android.tdfruitstore.ui.wishlist;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.tdfruitstore.R;
import com.android.tdfruitstore.data.dao.FirestoreCallback;
import com.android.tdfruitstore.data.dao.WishlistDAO;
import com.android.tdfruitstore.data.entities.Wishlist;
import com.android.tdfruitstore.ui.adapter.WishlistAdapter;

import java.util.ArrayList;
import java.util.List;

public class WishlistActivity extends AppCompatActivity {
    private RecyclerView rvWishlist;
    private WishlistAdapter wishlistAdapter;
    private List<Wishlist> wishlistItems;
    private WishlistDAO wishlistDAO;
    private Button btnBack;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        rvWishlist = findViewById(R.id.rvWishlist);
        rvWishlist.setLayoutManager(new LinearLayoutManager(this));

        // Lấy userId từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);

        if (userId == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        wishlistDAO = new WishlistDAO();
        wishlistItems = new ArrayList<>();
        wishlistAdapter = new WishlistAdapter(this, wishlistItems, userId);
        rvWishlist.setAdapter(wishlistAdapter);

        loadWishlist();

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadWishlist() {
        wishlistDAO.getWishlistByUserId(userId, new FirestoreCallback<List<Wishlist>>() {
            @Override
            public void onSuccess(List<Wishlist> items) {
                runOnUiThread(() -> {
                    wishlistItems.clear();
                    wishlistItems.addAll(items);
                    wishlistAdapter.notifyDataSetChanged();

                    if (wishlistItems.isEmpty()) {
                        Toast.makeText(WishlistActivity.this, "Danh sách yêu thích trống!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(WishlistActivity.this, "Lỗi khi tải danh sách yêu thích!", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
