package com.android.tdfruitstore.ui.home;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.tdfruitstore.R;
import com.android.tdfruitstore.data.ApiClient;
import com.android.tdfruitstore.data.ApiService;
import com.android.tdfruitstore.data.FirestoreUtils;
import com.android.tdfruitstore.data.dao.CategoryDAO;
import com.android.tdfruitstore.data.dao.FirestoreCallback;
import com.android.tdfruitstore.data.dao.ProductDAO;
import com.android.tdfruitstore.data.dao.UserDAO;
import com.android.tdfruitstore.data.entities.Category;
import com.android.tdfruitstore.data.entities.Product;
import com.android.tdfruitstore.data.entities.User;
import com.android.tdfruitstore.ui.adapter.CategoryAdapter;
import com.android.tdfruitstore.ui.adapter.ProductAdapter;
import com.android.tdfruitstore.ui.cart.CartActivity;
import com.android.tdfruitstore.ui.order.OrderHistoryActivity;
import com.android.tdfruitstore.ui.profile.ProfileActivity;
import com.android.tdfruitstore.ui.wishlist.WishlistActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener {
    private RecyclerView rvCategory, rvProducts;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private List<Category> categoryList;
    private List<Product> productList;
    private TextView tvUserName;
    private CircleImageView ivUserAvatar;
    private Button btnViewAll;
    private UserDAO userDAO;
    private CategoryDAO categoryDAO;
    private ProductDAO productDAO;
    private String userName;
    private ImageView btnFilter;
    private EditText etSearchProduct;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        userDAO = new UserDAO();
        categoryDAO = new CategoryDAO();
        productDAO = new ProductDAO();

        FirestoreUtils.fetchUsersFromFirestore(this);

        // Ánh xạ RecyclerView
        rvCategory = findViewById(R.id.rvCategory);
        rvProducts = findViewById(R.id.rvProducts);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        etSearchProduct = findViewById(R.id.etSearchProduct);
        btnFilter = findViewById(R.id.btnFilter);

        // Cấu hình danh mục RecyclerView
        rvCategory.setLayoutManager(new GridLayoutManager(this, 4));
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(this, categoryList, this);
        rvCategory.setAdapter(categoryAdapter);

        // Cấu hình sản phẩm RecyclerView
        rvProducts.setLayoutManager(new GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false));
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, productList, false);
        rvProducts.setAdapter(productAdapter);

        btnViewAll = findViewById(R.id.btnViewALL);
        btnViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AllProductsActivity.class);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
            startActivity(intent, options.toBundle());
        });

        // Lấy userName từ SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        userName = sharedPreferences.getString("userName", "User");
        tvUserName = findViewById(R.id.tvUserName);
        tvUserName.setText("Hello, " + userName + "!");

        ivUserAvatar = findViewById(R.id.ivUserAvatar);
        loadUserAvatar();

        loadCategoriesFromFirestore();
        loadProductsFromFirestore();

        etSearchProduct.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                startSearch();
                return true;
            }
            return false;
        });

        btnFilter.setOnClickListener(v -> startSearch());

        // Xử lý Bottom Navigation
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_explore) {
                startActivity(new Intent(HomeActivity.this, HomeActivity.class));
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(HomeActivity.this, CartActivity.class));
            } else if (id == R.id.nav_order) {
                startActivity(new Intent(HomeActivity.this, OrderHistoryActivity.class));
            } else if (id == R.id.nav_wishlist) {
                startActivity(new Intent(HomeActivity.this, WishlistActivity.class));
            } else {
                Toast.makeText(HomeActivity.this, "Chức năng đang phát triển!", Toast.LENGTH_SHORT).show();
            }
            return false;
        });
    }

    private void startSearch() {
        String keyword = etSearchProduct.getText().toString().trim();

        if (!keyword.isEmpty()) {
            Intent intent = new Intent(this, AllProductsActivity.class);
            intent.putExtra("search_keyword", keyword);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Vui lòng nhập từ khóa tìm kiếm!", Toast.LENGTH_SHORT).show();
        }
    }

    // 🔥 Xử lý sự kiện khi người dùng chọn danh mục
    @Override
    public void onCategoryClick(String categoryTag) {
        Intent intent = new Intent(this, AllProductsActivity.class);
        intent.putExtra("category_tag", categoryTag);
        startActivity(intent);
    }

    private void loadUserAvatar() {
        String email = sharedPreferences.getString("email", null);
        if (email == null) {
            Log.e("DEBUG", "Không tìm thấy email trong SharedPreferences!");
            return;
        }

        userDAO.getUserByEmail(email, new FirestoreCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null && user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                    Glide.with(HomeActivity.this)
                            .load(user.getAvatarUrl())
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .placeholder(R.drawable.ic_avatar)
                            .into(ivUserAvatar);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "❌ Không tìm thấy User!", e);
            }
        });
    }

    private void loadCategoriesFromFirestore() {
        categoryDAO.getAllCategories(new FirestoreCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> categories) {
                categoryList.clear();
                if (categories.isEmpty()) {
                    // 🔥 Nếu danh mục từ Firestore trống, thêm danh mục mặc định
                    addDefaultCategoriesToFirestore();
                } else {
                    categoryList.addAll(categories);
                    categoryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "❌ Lỗi khi tải danh mục!", e);
            }
        });
    }

    private void addDefaultCategoriesToFirestore() {
        List<Category> defaultCategories = new ArrayList<>();
        defaultCategories.add(new Category(UUID.randomUUID().toString(),"Fruits", "fruits", R.drawable.ic_fruits));
        defaultCategories.add(new Category(UUID.randomUUID().toString(),"Vegetables", "vegetables", R.drawable.ic_vegetables));
        defaultCategories.add(new Category(UUID.randomUUID().toString(),"Dairies", "dairies", R.drawable.ic_dairy));
        defaultCategories.add(new Category(UUID.randomUUID().toString(),"Beverages", "beverages", R.drawable.ic_drinks));
        defaultCategories.add(new Category(UUID.randomUUID().toString(),"Produits", "produits", R.drawable.ic_produits));
        defaultCategories.add(new Category(UUID.randomUUID().toString(),"Grains", "grains", R.drawable.ic_grains));
        defaultCategories.add(new Category(UUID.randomUUID().toString(),"Snacks", "snacks", R.drawable.ic_snack));
        defaultCategories.add(new Category(UUID.randomUUID().toString(),"Others", "others", R.drawable.ic_unknown));

        categoryDAO.insertCategories(defaultCategories, new FirestoreCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    Log.d("Firestore", "🔥 Đã thêm danh mục mặc định vào Firestore!");
                    categoryList.addAll(defaultCategories);
                    categoryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "❌ Lỗi khi thêm danh mục mặc định vào Firestore!", e);
            }
        });
    }

    private void loadProductsFromFirestore() {
        productDAO.getAllProducts(new FirestoreCallback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> products) {
                if (products != null && !products.isEmpty()) {
                    // 🔥 Nếu có sản phẩm từ Firestore, cập nhật danh sách
                    productList.clear();
                    productList.addAll(products);
                    productAdapter.notifyDataSetChanged();
                } else {
                    // 🔥 Nếu Firestore không có dữ liệu, tải từ JSON
                    Log.w("Firestore", "⚠ Firestore trống, tải dữ liệu từ JSON...");
//                    loadProductsFromJSON();
                    loadProductsFromAPI();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "❌ Lỗi khi tải sản phẩm!", e);
                // 🔥 Nếu lỗi Firestore, cũng tải từ JSON để đảm bảo hiển thị
//                loadProductsFromJSON();
                loadProductsFromAPI();
            }
        });
    }

//    private void loadProductsFromJSON() {
//        String jsonData = JsonReader.readJsonFromRaw(this, R.raw.products);
//        if (jsonData == null) {
//            Log.e("ERROR", "Không thể đọc file JSON!");
//            return;
//        }
//
//        try {
//            JSONObject jsonObject = new JSONObject(jsonData);
//            JSONArray productsArray = jsonObject.getJSONArray("products");
//
//            for (int i = 0; i < productsArray.length(); i++) {
//                JSONObject productObj = productsArray.getJSONObject(i);
//
//                String name = productObj.optString("product_name", "No Name");
//                String imageUrl = productObj.optString("image_url", "");
//                String category = productObj.optString("categories", "Unknown");
//                String code = productObj.optString("code", "Unknown");
//                double randomPrice = ThreadLocalRandom.current().nextDouble(10, 50);
//                int stock = ThreadLocalRandom.current().nextInt(10, 200);
//                double rating = ThreadLocalRandom.current().nextDouble(3, 5);
//                //Cắt chuỗi ở category lấy ra từ đầu đến dấu "," or " "
//                if (category.contains(" ")) {
//                    category = category.substring(0, category.indexOf(" "));
//                } else if (category.contains(",")) {
//                    category = category.substring(0, category.indexOf(","));
//                }
//
//                Product product = new Product(UUID.randomUUID().toString(), name, imageUrl, category, code, randomPrice, stock, rating);
//                productDAO.insertProduct(product, new FirestoreCallback<Boolean>() {
//                    @Override
//                    public void onSuccess(Boolean result) {
//                        if (result) {
//                            Log.d("Firestore", "🔥 Đã thêm sản phẩm vào Firestore!");
//                            productList.add(product);
//                            productAdapter.notifyDataSetChanged();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Exception e) {
//                        Log.e("Firestore", "❌ Lỗi khi thêm sản phẩm vào Firestore!", e);
//                    }
//                });
//                productList.add(product);
//            }
//
//            runOnUiThread(() -> productAdapter.notifyDataSetChanged());
//        } catch (JSONException e) {
//            e.printStackTrace();
//            Log.e("ERROR", "Lỗi khi parse JSON!");
//        }
//    }

    private void loadProductsFromAPI() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getRawJson().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // 🔹 Lấy JSON dưới dạng String
                        String jsonData = response.body().string();
                        Log.d("API", "🔥 JSON API: " + jsonData); // Debug JSON

                        // 🔹 Gọi hàm xử lý JSON như cũ
                        processJsonData(jsonData);

                    } catch (IOException e) {
                        Log.e("API", "❌ Lỗi khi đọc JSON từ API", e);
                    }
                } else {
                    Log.e("API", "❌ Lỗi phản hồi API! HTTP Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("API", "❌ Lỗi kết nối API: " + t.getMessage());
            }
        });
    }

    private void processJsonData(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray productsArray = jsonObject.getJSONArray("products");

            for (int i = 0; i < productsArray.length(); i++) {
                JSONObject productObj = productsArray.getJSONObject(i);

                String name = productObj.optString("product_name", "No Name");
                String imageUrl = productObj.optString("image_url", "");
                String category = productObj.optString("categories", "Unknown");
                String code = productObj.optString("code", "Unknown");

                // 🔹 Tạo giá trị random cho price, stock, rating
                double randomPrice = ThreadLocalRandom.current().nextDouble(10, 50);
                int stock = ThreadLocalRandom.current().nextInt(10, 200);
                double rating = ThreadLocalRandom.current().nextDouble(3, 5);

                // 🔹 Cắt chuỗi category lấy phần đầu tiên
                if (category.contains(" ")) {
                    category = category.substring(0, category.indexOf(" "));
                } else if (category.contains(",")) {
                    category = category.substring(0, category.indexOf(","));
                }

                // 🔹 Tạo đối tượng Product
                Product product = new Product(UUID.randomUUID().toString(), name, imageUrl, category, code, randomPrice, stock, rating);

                // 🔹 Thêm sản phẩm vào danh sách
                productList.add(product);

                // 🔹 Thêm sản phẩm vào Firestore
                productDAO.insertProduct(product, new FirestoreCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        if (result) {
                            Log.d("Firestore", "🔥 Đã thêm sản phẩm vào Firestore!");
                            runOnUiThread(() -> productAdapter.notifyDataSetChanged());
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("Firestore", "❌ Lỗi khi thêm sản phẩm vào Firestore!", e);
                    }
                });
            }

            // 🔹 Cập nhật UI
            runOnUiThread(() -> productAdapter.notifyDataSetChanged());
            Log.d("API", "🔥 Đã xử lý sản phẩm: " + productList.size());

        } catch (JSONException e) {
            Log.e("API", "❌ Lỗi khi parse JSON!", e);
        }
    }


}
