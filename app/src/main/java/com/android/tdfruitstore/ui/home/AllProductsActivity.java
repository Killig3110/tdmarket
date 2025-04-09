package com.android.tdfruitstore.ui.home;

//import static com.android.tdfruitstore.ui.widget.Widget.productList;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Slide;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import com.android.tdfruitstore.R;
import com.android.tdfruitstore.data.dao.FirestoreCallback;
import com.android.tdfruitstore.data.dao.ProductDAO;
import com.android.tdfruitstore.data.entities.Product;
import com.android.tdfruitstore.ui.adapter.ProductAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AllProductsActivity extends AppCompatActivity {
    private RecyclerView rvAllProducts;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private FloatingActionButton fabBack;
    private EditText etSearchProduct;
    private ProductDAO productDAO;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_products);

        getWindow().setEnterTransition(new Slide(Gravity.START));
        getWindow().setExitTransition(new Slide(Gravity.END));

        rvAllProducts = findViewById(R.id.rvAllProducts);
        rvAllProducts.setLayoutManager(new GridLayoutManager(this, 2));
        etSearchProduct = findViewById(R.id.etSearchProduct);

        fabBack = findViewById(R.id.fabBack);

        // Nhận từ khóa từ Intent
        String keyword = getIntent().getStringExtra("search_keyword");
        String category = getIntent().getStringExtra("category_name");

        // Xử lý sự kiện kéo nút Back
        fabBack.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;
            private int lastAction;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        lastAction = MotionEvent.ACTION_DOWN;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        v.setX(event.getRawX() + dX);
                        v.setY(event.getRawY() + dY);
                        lastAction = MotionEvent.ACTION_MOVE;
                        return true;

                    case MotionEvent.ACTION_UP:
                        float screenWidth = getResources().getDisplayMetrics().widthPixels;
                        float fabX = v.getX();
                        float newX;

                        // Nếu thả bên trái, kéo về sát mép trái
                        if (fabX < screenWidth / 2) {
                            newX = 0;
                        } else {
                            newX = screenWidth - v.getWidth();
                        }

                        // Hiệu ứng trượt về mép màn hình
                        ObjectAnimator animator = ObjectAnimator.ofFloat(v, "x", newX);
                        animator.setDuration(300);
                        animator.start();

                        if (lastAction == MotionEvent.ACTION_DOWN) {
                            // Back lại home
                            Intent intent = new Intent(AllProductsActivity.this, HomeActivity.class);
                            startActivity(intent);
                        }

                        lastAction = MotionEvent.ACTION_UP;
                        return true;
                }
                return false;
            }
        });

        productDAO = new ProductDAO();
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, productList, true);
        rvAllProducts.setAdapter(productAdapter);

        if (keyword != null) {
            etSearchProduct.setText(keyword);
            searchProducts(keyword);
        } else if (category != null) {
            loadProductsByCategory(category);
        } else {
            loadAllProducts();
        }

        // Xử lý sự kiện tìm kiếm
        etSearchProduct.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                searchProducts(etSearchProduct.getText().toString());
                return true;
            }
            return false;
        });

    }

    // 🔥 **Tìm kiếm sản phẩm từ Firestore theo từ khóa**
    private void searchProducts(String keyword) {
        productDAO.getProductsByKeyword(keyword, new FirestoreCallback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> filteredList) {
                // Cập nhật danh sách sản phẩm trong adapter
                // Xóa danh sách cũ và thêm danh sách mới
                productList.clear();
                productList.addAll(filteredList);
                // Cập nhật danh sách sản phẩm trong adapter
                productAdapter.updateList(productList);
                productAdapter.notifyDataSetChanged();
                if (filteredList.isEmpty()) {
                    Toast.makeText(AllProductsActivity.this, "Không tìm thấy sản phẩm phù hợp!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AllProductsActivity.this, "Lỗi khi tìm kiếm sản phẩm!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔥 **Lấy sản phẩm theo danh mục từ Firestore**
    private void loadProductsByCategory(String category) {
        productDAO.getProductsByCategory(category, new FirestoreCallback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> filteredList) {
                productAdapter.updateList(filteredList);
                if (filteredList.isEmpty()) {
                    Toast.makeText(AllProductsActivity.this, "Không tìm thấy sản phẩm phù hợp!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AllProductsActivity.this, "Lỗi khi tải sản phẩm!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔥 **Lấy toàn bộ sản phẩm từ Firestore**
    private void loadAllProducts() {
        productDAO.getAllProducts(new FirestoreCallback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> products) {
                productAdapter.updateList(products);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AllProductsActivity.this, "Lỗi khi tải sản phẩm!", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
