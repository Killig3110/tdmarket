package com.android.tdfruitstore.ui.home;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.tdfruitstore.R;
import com.android.tdfruitstore.data.dao.CartItemDAO;
import com.android.tdfruitstore.data.dao.CommentDAO;
import com.android.tdfruitstore.data.dao.FirestoreCallback;
import com.android.tdfruitstore.data.dao.OrderDAO;
import com.android.tdfruitstore.data.dao.OrderDetailDAO;
import com.android.tdfruitstore.data.dao.ProductDAO;
import com.android.tdfruitstore.data.dao.UserDAO;
import com.android.tdfruitstore.data.dao.WishlistDAO;
import com.android.tdfruitstore.data.entities.CartItem;
import com.android.tdfruitstore.data.entities.Comment;
import com.android.tdfruitstore.data.entities.Order;
import com.android.tdfruitstore.data.entities.OrderDetail;
import com.android.tdfruitstore.data.entities.Product;
import com.android.tdfruitstore.data.entities.User;
import com.android.tdfruitstore.data.entities.Wishlist;
import com.android.tdfruitstore.ui.adapter.CommentAdapter;
import com.android.tdfruitstore.ui.adapter.ProductAdapter;
import com.android.tdfruitstore.ui.cart.CartActivity;
import com.android.tdfruitstore.ui.order.OrderActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;

import java.sql.Date;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProductDetailActivity extends AppCompatActivity {
    private ImageView ivProductImage, btnWishlist, btnBack;
    private TextView tvProductName, tvProductPrice, tvQuantity, tvDescription;
    private EditText etComment;
    private RatingBar ratingBarComment, ratingBar;
    private Button btnAddToCart, btnOrderNow, btnSendComment;
    private CircleImageView btnDecreaseQuantity, btnIncreaseQuantity;
    private FloatingActionButton fabCart;
    private int quantity = 1;

    public String name, imageUrl, category, code, price, email, productId, userId, replyUserName, replyingToCommentId = null;
    private RecyclerView rvRelatedProducts, rvComments;
    private LinearLayout layoutAddComment;
    private ProductAdapter relatedProductAdapter;
    private CommentAdapter commentAdapter;
    private List<Product> relatedProductList;
    private List<Comment> commentList;
    private boolean hasPurchased;
    private List<Product> allProducts;
    private ProductDAO productDAO;
    private CartItemDAO cartItemDAO;
    private CommentDAO commentDAO;
    private OrderDAO orderDAO;
    private OrderDetailDAO orderDetailDAO;
    private UserDAO userDAO;
    private User currentUser;
    private WishlistDAO wishlistDAO;
    private float ratingCurrent = 0;
    private boolean hasCommented = false;
    private ImageView ivFavorite;
    private boolean isFavorite = false;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Ánh xạ View từ XML
        ivProductImage = findViewById(R.id.ivProductImage);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvDescription = findViewById(R.id.tvDescription);
        btnDecreaseQuantity = findViewById(R.id.btnDecreaseQuantity);
        btnIncreaseQuantity = findViewById(R.id.btnIncreaseQuantity);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnOrderNow = findViewById(R.id.btnOrderNow);
        rvRelatedProducts = findViewById(R.id.rvRelatedProducts);
        btnBack = findViewById(R.id.ivBack);
        fabCart = findViewById(R.id.fabCart);

        relatedProductList = new ArrayList<>();
        relatedProductAdapter = new ProductAdapter(this, relatedProductList, false);
        rvRelatedProducts.setLayoutManager(new GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false));
        rvRelatedProducts.setAdapter(relatedProductAdapter);

        rvComments = findViewById(R.id.rvComments);
        etComment = findViewById(R.id.etComment);
        btnSendComment = findViewById(R.id.btnSendComment);
        ratingBarComment = findViewById(R.id.ratingBarComment);
        ratingBar = findViewById(R.id.ratingBar);
        layoutAddComment = findViewById(R.id.layoutAddComment);
        ivFavorite = findViewById(R.id.ivFavorite);

        commentDAO = new CommentDAO();
        orderDAO = new OrderDAO();
        productDAO = new ProductDAO();
        wishlistDAO = new WishlistDAO();
        userDAO = new UserDAO();
        orderDetailDAO = new OrderDetailDAO();
        cartItemDAO = new CartItemDAO();
        currentUser = new User();

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);
        email = sharedPreferences.getString("email", "");
        productId = getIntent().getStringExtra("productId");

        // 🔥 Lấy user từ Firestore thay vì Room
        userDAO.getUserByEmail(email, new FirestoreCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    currentUser = user;

                    // 🔥 Kiểm tra xem user có thể bình luận không
                    orderDAO.canUserCommentOnProduct(userId, productId, new FirestoreCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean canComment) {
                            Log.d("DEBUG", "Can Comment: " + canComment);

                            runOnUiThread(() -> {
                                if (canComment) {
                                    layoutAddComment.setVisibility(View.VISIBLE);
                                } else {
                                    layoutAddComment.setVisibility(View.GONE);
                                }
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e("Firestore", "❌ Lỗi khi kiểm tra điều kiện bình luận", e);
                        }
                    });

                    // 🔥 Lấy rating từ Firestore
                    productDAO.getProductById(productId, new FirestoreCallback<Product>() {
                        @Override
                        public void onSuccess(Product product) {
                            if (product != null) {
                                ratingCurrent = (float) product.getRating();
                                runOnUiThread(() -> ratingBar.setRating(ratingCurrent));
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e("Firestore", "❌ Lỗi khi lấy rating sản phẩm", e);
                        }
                    });

                } else {
                    Log.e("Firestore", "❌ Không tìm thấy User!");
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "❌ Lỗi khi lấy User từ Firestore", e);
            }
        });

        // Kiểm tra nếu sản phẩm đã có trong wishlist
        checkFavoriteStatus();

        // Sự kiện khi nhấn vào nút yêu thích
        ivFavorite.setOnClickListener(v -> toggleWishlist());

        // Cấu hình RecyclerView
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList, comment -> {
            Log.d("DEBUG", "onReplyClick được gọi cho comment ID: " + comment.getId());

            // Hiển thị ô nhập bình luận
            layoutAddComment.setVisibility(View.VISIBLE);

            // Lay userName cua comment dang tra loi de hien thi tren hint
            userDAO.getUserById(comment.getUserId(), new FirestoreCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    if (user != null) {
                        replyUserName = user.getName();
                        etComment.setHint("Trả lời @" + user.getName() + ": ");
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("Firestore", "❌ Lỗi khi lấy user từ Firestore!", e);
                }
            });

            // Lưu ID comment đang trả lời (sẽ cần khi gửi bình luận)
            replyingToCommentId = comment.getId();

            // Ẩn RatingBar khi đang trả lời và đặt rating = -1
            ratingBarComment.setVisibility(View.GONE);
            ratingBarComment.setRating(-1);

            // Focus vào EditText và mở bàn phím
            etComment.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(etComment, InputMethodManager.SHOW_IMPLICIT);
        });

        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);

        loadComments(); // Gọi loadComments() ngay sau khi cấu hình Adapter

        btnSendComment.setOnClickListener(v -> addComment());

        // Thiết lập số lượng ban đầu
        tvQuantity.setText(String.valueOf(quantity));

        // 🔥 Nhận dữ liệu từ Intent thay vì SQLite
        Intent intent = getIntent();
        if (intent != null) {
            String productName = intent.getStringExtra("name");
            String imageUrl = intent.getStringExtra("imageUrl");
            String category = intent.getStringExtra("category");
            String code = intent.getStringExtra("code");
            double price = intent.getDoubleExtra("price", 0.00);

            // Cập nhật UI
            tvProductName.setText(productName);
            tvProductPrice.setText(String.format("$ %.2f", price));
            tvDescription.setText("This is a description for " + name + ". And " + category + " with code " + code);
            Glide.with(this).load(imageUrl).into(ivProductImage);

            // 🔥 Sau khi lấy sản phẩm, load danh sách sản phẩm liên quan từ SQLite
            loadRelatedProducts(category);
        } else {
            Toast.makeText(this, "Lỗi: Không có dữ liệu sản phẩm!", Toast.LENGTH_SHORT).show();
            finish();
        }


        // Xử lý sự kiện nút tăng số lượng
        btnIncreaseQuantity.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
        });

        // Xử lý sự kiện nút giảm số lượng
        btnDecreaseQuantity.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            } else {
                Toast.makeText(this, "Số lượng không thể nhỏ hơn 1", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý nút "Thêm vào giỏ hàng"
        btnAddToCart.setOnClickListener(v -> addToCart());

        // Xử lý nút "Mua ngay"
        btnOrderNow.setOnClickListener(v -> orderNow());

        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Nút giỏ hàng - Chỉ xử lý sự kiện Click nếu không có di chuyển
        fabCart.setOnTouchListener(new View.OnTouchListener() {
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

                        // 🔥 Chỉ mở CartActivity nếu không có di chuyển
                        if (lastAction == MotionEvent.ACTION_DOWN) {
                            Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
                            startActivity(intent);
                        }

                        lastAction = MotionEvent.ACTION_UP;
                        return true;
                }
                return false;
            }
        });

    }

    // Kiểm tra trạng thái yêu thích
    // Kiểm tra trạng thái yêu thích
    private void checkFavoriteStatus() {
        wishlistDAO.isProductInWishlist(userId, productId, new FirestoreCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                isFavorite = result;
                runOnUiThread(() -> {
                    if (isFavorite) {
                        ivFavorite.setImageResource(R.drawable.ic_heart_filled);
                    } else {
                        ivFavorite.setImageResource(R.drawable.ic_heart_filled_full);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "❌ Lỗi khi kiểm tra sản phẩm trong danh sách yêu thích", e);
            }
        });
    }

    // Xử lý thêm/xóa khỏi wishlist
    private void toggleWishlist() {
        if (isFavorite) {
            wishlistDAO.removeFromWishlist(userId, productId, new FirestoreCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    isFavorite = false;
                    runOnUiThread(() -> {
                        ivFavorite.setImageResource(R.drawable.ic_heart_filled_full);
                        Toast.makeText(getApplicationContext(), "Đã xóa khỏi yêu thích!", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("Firestore", "❌ Lỗi khi xóa khỏi danh sách yêu thích", e);
                }
            });
        } else {
            Wishlist wishlist = new Wishlist(userId, productId, Timestamp.now(), false);
            wishlistDAO.insertWishlist(wishlist, new FirestoreCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    isFavorite = true;
                    runOnUiThread(() -> {
                        ivFavorite.setImageResource(R.drawable.ic_heart_filled);
                        Toast.makeText(getApplicationContext(), "Đã thêm vào yêu thích!", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("Firestore", "❌ Lỗi khi thêm vào danh sách yêu thích", e);
                }
            });
        }
    }

    private void addComment() {

        String content;
        if (replyingToCommentId != null) {
            if (replyUserName == null) {
                content = etComment.getText().toString();
            } else {
                content = "@" + replyUserName + ": " + etComment.getText().toString();
            }
        } else {
            content = etComment.getText().toString();
        }

        float rating = ratingBarComment.getRating();

        if (content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung bình luận!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userId == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (hasCommented && replyingToCommentId == null) {
            Toast.makeText(this, "Bạn đã bình luận sản phẩm này rồi!", Toast.LENGTH_SHORT).show();
            return;
        }

        Comment comment = new Comment(userId, productId, replyingToCommentId, Timestamp.now(), content, rating);

        commentDAO.insertComment(comment, new FirestoreCallback<String>() {
            @Override
            public void onSuccess(String commentId) {  // commentId là String
                // 🔥 Cập nhật rating nếu có đánh giá
                if (rating > 0) {
                    updateProductRating(rating);
                }

                runOnUiThread(() -> {
                    etComment.setText("");
                    replyingToCommentId = null; // Nếu là int, đổi thành replyingToCommentId = -1;
                    ratingBarComment.setRating(5); // Reset rating về mặc định
                    layoutAddComment.setVisibility(View.GONE);
                    loadComments(); // Load lại danh sách bình luận
                    Toast.makeText(getApplicationContext(), "Bình luận đã được gửi!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "❌ Lỗi khi thêm bình luận", e);
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Lỗi khi gửi bình luận!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateProductRating(float newRating) {
        productDAO.getProductById(productId, new FirestoreCallback<Product>() {
            @Override
            public void onSuccess(Product product) {
                if (product != null) {
                    commentDAO.countCommentsByProduct(productId, new FirestoreCallback<Integer>() {
                        @Override
                        public void onSuccess(Integer count) {
                            int totalRatings = (count == null) ? 0 : count; // Đảm bảo không bị null
                            float totalRating = (totalRatings > 0) ? ((float) product.getRating() * totalRatings) : 0;
                            int newTotalRatings = totalRatings + 1;
                            float updatedRating = (totalRating + newRating) / newTotalRatings;

                            // Cập nhật Firestore với rating mới
                            productDAO.updateProductRating(productId, updatedRating, new FirestoreCallback<Boolean>() {
                                @Override
                                public void onSuccess(Boolean result) {
                                    if (result) {
                                        Log.d("Firestore", "🔥 Đã cập nhật rating sản phẩm: " + updatedRating);
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Log.e("Firestore", "❌ Lỗi khi cập nhật rating sản phẩm", e);
                                }
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e("Firestore", "❌ Lỗi khi đếm số lượng bình luận", e);
                        }
                    });
                } else {
                    Log.e("Firestore", "❌ Không tìm thấy sản phẩm để cập nhật rating");
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "❌ Lỗi khi lấy sản phẩm từ Firestore", e);
            }
        });
    }

    private void loadComments() {
        productId = getIntent().getStringExtra("productId");
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy sản phẩm!", Toast.LENGTH_SHORT).show();
            return;
        }

        commentDAO.getCommentsByProduct(productId, new FirestoreCallback<List<Comment>>() {
            @Override
            public void onSuccess(List<Comment> comments) {

                if (comments == null || comments.isEmpty()) {
                    runOnUiThread(() -> findViewById(R.id.tvNoComments).setVisibility(View.VISIBLE));
                    return;
                }

                // 🔥 Nhóm bình luận con vào một Map
                Map<String, List<Comment>> repliesMap = new HashMap<>();
                List<Comment> rootComments = new ArrayList<>();

                for (Comment comment : comments) {
                    if (comment.getParentCommentId() == null) {
                        rootComments.add(comment); // Bình luận gốc
                    } else {
                        repliesMap.computeIfAbsent(comment.getParentCommentId(), k -> new ArrayList<>()).add(comment);
                    }
                }

                // 🔥 Hiển thị tất cả bình luận nhưng chỉ thụt lề 1 cấp cho mọi bình luận con
                List<Comment> finalCommentList = new ArrayList<>();
                for (Comment parent : rootComments) {
                    addCommentWithFlatIndent(parent, finalCommentList, repliesMap);
                }

                runOnUiThread(() -> {
                    commentList.clear();
                    commentList.addAll(finalCommentList);
                    commentAdapter.setComments(commentList);
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "❌ Lỗi khi tải bình luận", e);
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Lỗi khi tải bình luận!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void addCommentWithFlatIndent(Comment parent, List<Comment> resultList, Map<String, List<Comment>> repliesMap) {
        resultList.add(parent); // Thêm bình luận cha

        if (repliesMap.containsKey(parent.getId())) {
            List<Comment> replies = repliesMap.get(parent.getId());

            // 🔥 Sắp xếp theo thời gian
            replies.sort(Comparator.comparing(Comment::getCreatedAt));

            for (Comment reply : replies) {
                reply.setIndentLevel(1); // 🔥 Tất cả bình luận con đều có indent cố định
                resultList.add(reply);

                // Tiếp tục thêm các bình luận con khác (nếu có) mà không thay đổi mức thụt lề
                if (repliesMap.containsKey(reply.getId())) {
                    addCommentWithFlatIndent(reply, resultList, repliesMap);
                }
            }
        }
    }

    private void loadRelatedProducts(String category) {
        if (category == null || category.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy danh mục sản phẩm!", Toast.LENGTH_SHORT).show();
            return;
        }

        productDAO.getProductsByCategory(category, new FirestoreCallback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> products) {
                runOnUiThread(() -> {
                    relatedProductList.clear();
                    relatedProductList.addAll(products);

                    // Nếu chưa đủ sản phẩm liên quan, thêm sản phẩm mặc định
                    if (relatedProductList.size() < 6) {
                        defaultProduct(relatedProductList);
                    }

                    relatedProductAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "❌ Lỗi khi tải sản phẩm liên quan", e);
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Lỗi khi tải sản phẩm liên quan!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void addToCart() {
        // Lấy userId từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);
        productId = getIntent().getStringExtra("productId");

        if (userId == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy thông tin sản phẩm từ Firestore
        productDAO.getProductById(productId, new FirestoreCallback<Product>() {
            @Override
            public void onSuccess(Product product) {
                if (product == null) {
                    Toast.makeText(getApplicationContext(), "Không tìm thấy sản phẩm!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String productName = product.getName();
                String imageUrl = product.getImageUrl();
                double price = product.getPrice();
                int quantity = Integer.parseInt(tvQuantity.getText().toString());

                // Kiểm tra sản phẩm đã có trong giỏ hàng chưa
                cartItemDAO.getCartItemByUserIdAndProductId(userId, productId, new FirestoreCallback<CartItem>() {
                    @Override
                    public void onSuccess(CartItem existingItem) {
                        if (existingItem != null) {
                            // Nếu sản phẩm đã có trong giỏ hàng, cập nhật số lượng
                            existingItem.setQuantity(existingItem.getQuantity() + quantity);
                            cartItemDAO.updateCartItem(existingItem, new FirestoreCallback<Boolean>() {
                                @Override
                                public void onSuccess(Boolean result) {
                                    Toast.makeText(getApplicationContext(), "Đã cập nhật số lượng sản phẩm!", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Log.e("Firestore", "❌ Lỗi khi cập nhật giỏ hàng", e);
                                }
                            });
                        } else {
                            // Nếu sản phẩm chưa có trong giỏ hàng, thêm mới
                            CartItem cartItem = new CartItem(UUID.randomUUID().toString(), userId, productId, imageUrl, quantity, price);
                            cartItemDAO.insertCart(cartItem, new FirestoreCallback<Boolean>() {
                                @Override
                                public void onSuccess(Boolean result) {
                                    Toast.makeText(getApplicationContext(), "Đã thêm " + productName + " vào giỏ hàng!", Toast.LENGTH_SHORT).show();
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
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "❌ Lỗi khi lấy sản phẩm từ Firestore", e);
            }
        });
    }

    private void orderNow() {
        // Lấy userId từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);
        productId = getIntent().getStringExtra("productId");

        if (userId == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy thông tin sản phẩm từ Firestore
        productDAO.getProductById(productId, new FirestoreCallback<Product>() {
            @Override
            public void onSuccess(Product product) {
                if (product == null) {
                    Toast.makeText(getApplicationContext(), "Không tìm thấy sản phẩm!", Toast.LENGTH_SHORT).show();
                    return;
                }
                double price = product.getPrice();
                int quantity = Integer.parseInt(tvQuantity.getText().toString());

                // 🔥 Tạo đơn hàng mới
                Order order = new Order(userId, Timestamp.now(), price * quantity, "PROCESSING");
                List<OrderDetail> orderDetails = new ArrayList<>();
                orderDetails.add(new OrderDetail(productId, quantity, price, quantity * price));

                orderDAO.insertOrder(order, new FirestoreCallback<String>() {
                    @Override
                    public void onSuccess(String orderId) { // Chờ Firestore tạo xong orderId
                        Log.d("Firestore", "✅ Order created with ID: " + orderId);

                        // 🔥 Sau khi có OrderId mới lưu OrderDetail
                        for (OrderDetail detail : orderDetails) {
                            detail.setOrderId(orderId); // Đặt OrderId cho từng chi tiết
                        }

                        // 🔥 Chỉ thêm OrderDetail khi đã có OrderId
                        orderDetailDAO.insertOrderDetails(orderId, orderDetails, new FirestoreCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean detailResult) {
                                if (detailResult) {
                                    // Mở màn hình đặt hàng thành công
                                    Intent intent = new Intent(ProductDetailActivity.this, OrderActivity.class);
                                    intent.putExtra("order_id", orderId);
                                    intent.putExtra("total_price", String.format("$ %.2f", price * quantity));
                                    intent.putExtra("voucher_code", "Voucher: NO CODE");
                                    startActivity(intent);
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("Firestore", "❌ Lỗi khi lưu chi tiết đơn hàng", e);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("Firestore", "❌ Lỗi khi tạo đơn hàng", e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "❌ Lỗi khi lấy sản phẩm từ Firestore", e);
            }
        });
    }

    public void defaultProduct(List<Product> relatedProducts) {
        relatedProducts.add(new Product(UUID.randomUUID().toString() ,"Apple", "android.resource://com.android.tdfruitstore/drawable/apple", "fruits-based-foods", "0001", ThreadLocalRandom.current().nextDouble(10, 50), ThreadLocalRandom.current().nextInt(10, 200), ThreadLocalRandom.current().nextDouble(3, 5)));
        relatedProducts.add(new Product(UUID.randomUUID().toString() ,"Banana", "android.resource://com.android.tdfruitstore/drawable/banana", "vegetables-based-foods", "0002", ThreadLocalRandom.current().nextDouble(10, 50), ThreadLocalRandom.current().nextInt(10, 200), ThreadLocalRandom.current().nextDouble(3, 5)));
        relatedProducts.add(new Product(UUID.randomUUID().toString() ,"Carrot", "android.resource://com.android.tdfruitstore/drawable/carrot", "vegetables-based-foods", "0003", ThreadLocalRandom.current().nextDouble(10, 50), ThreadLocalRandom.current().nextInt(10, 200), ThreadLocalRandom.current().nextDouble(3, 5)));
        relatedProducts.add(new Product(UUID.randomUUID().toString() ,"Milk", "android.resource://com.android.tdfruitstore/drawable/milk", "daires", "0004", ThreadLocalRandom.current().nextDouble(10, 50), ThreadLocalRandom.current().nextInt(10, 200), ThreadLocalRandom.current().nextDouble(3, 5)));
        relatedProducts.add(new Product(UUID.randomUUID().toString() ,"Orange Juice", "android.resource://com.android.tdfruitstore/drawable/orange_juice", "beverages", "0005", ThreadLocalRandom.current().nextDouble(10, 50), ThreadLocalRandom.current().nextInt(10, 200), ThreadLocalRandom.current().nextDouble(3, 5)));
        relatedProducts.add(new Product(UUID.randomUUID().toString() ,"Rice", "android.resource://com.android.tdfruitstore/drawable/rice", "grains", "0006", ThreadLocalRandom.current().nextDouble(10, 50), ThreadLocalRandom.current().nextInt(10, 200), ThreadLocalRandom.current().nextDouble(3, 5)));
        relatedProducts.add(new Product(UUID.randomUUID().toString() ,"Bread", "android.resource://com.android.tdfruitstore/drawable/bread", "grains", "0007", ThreadLocalRandom.current().nextDouble(10, 50), ThreadLocalRandom.current().nextInt(10, 200), ThreadLocalRandom.current().nextDouble(3, 5)));
        relatedProducts.add(new Product(UUID.randomUUID().toString() ,"Cheese", "android.resource://com.android.tdfruitstore/drawable/cheese", "dairies", "0008", ThreadLocalRandom.current().nextDouble(10, 50), ThreadLocalRandom.current().nextInt(10, 200), ThreadLocalRandom.current().nextDouble(3, 5)));
        relatedProducts.add(new Product(UUID.randomUUID().toString() ,"Tomato", "android.resource://com.android.tdfruitstore/drawable/tomato", "vegetables-based-foods", "0009", ThreadLocalRandom.current().nextDouble(10, 50), ThreadLocalRandom.current().nextInt(10, 200), ThreadLocalRandom.current().nextDouble(3, 5)));
        relatedProducts.add(new Product(UUID.randomUUID().toString() ,"Eggs","android.resource://com.android.tdfruitstore/drawable/eggs", "grains", "0010", ThreadLocalRandom.current().nextDouble(10, 50), ThreadLocalRandom.current().nextInt(10, 200), ThreadLocalRandom.current().nextDouble(3, 5)));

        for (Product product : relatedProducts) {
            productDAO.getProductByName(product.getName(), new FirestoreCallback<Product>() {
                @Override
                public void onSuccess(Product existingProduct) {
                    if (existingProduct == null) {
                        // Sản phẩm chưa tồn tại, thêm vào Firestore
                        productDAO.insertProduct(product, new FirestoreCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                Log.d("Firestore", "🔥 Đã thêm sản phẩm: " + product.getName());
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("Firestore", "❌ Lỗi khi thêm sản phẩm: " + product.getName(), e);
                            }
                        });
                    } else {
                        Log.d("Firestore", "✅ Sản phẩm đã tồn tại: " + product.getName());
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("Firestore", "❌ Lỗi khi kiểm tra sản phẩm: " + product.getName(), e);
                }
            });
        }
    }
}
