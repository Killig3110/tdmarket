package com.android.tdfruitstore.data.dao;

import android.util.Log;
import com.android.tdfruitstore.data.entities.Product;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "products";

    public ProductDAO() {
        this.db = FirebaseFirestore.getInstance();
    }

    // 🔥 Thêm một sản phẩm
    public void insertProduct(Product product, FirestoreCallback<Boolean> callback) {
        db.collection(COLLECTION_NAME).document(product.getId()) // ID là String
                .set(product)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ Sản phẩm đã thêm: " + product.getName());
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Lỗi khi thêm sản phẩm", e);
                    callback.onFailure(e);
                });
    }

    // 🔥 Cập nhật một sản phẩm
    public void updateProduct(Product product, FirestoreCallback<Boolean> callback) {
        db.collection(COLLECTION_NAME).document(product.getId()) // ID là String
                .set(product)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ Sản phẩm đã cập nhật: " + product.getName());
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Lỗi khi cập nhật sản phẩm", e);
                    callback.onFailure(e);
                });
    }

    // 🔥 Xóa một sản phẩm
    public void deleteProduct(String productId, FirestoreCallback<Boolean> callback) {
        db.collection(COLLECTION_NAME).document(productId) // ID là String
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ Sản phẩm đã bị xóa: " + productId);
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Lỗi khi xóa sản phẩm", e);
                    callback.onFailure(e);
                });
    }

    // 🔥 Lấy sản phẩm theo ID
    public void getProductById(String productId, FirestoreCallback<Product> callback) {
        db.collection(COLLECTION_NAME).document(productId) // ID là String
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Product product = documentSnapshot.toObject(Product.class);
                        callback.onSuccess(product);
                    } else {
                        Log.e("Firestore", "❌ Không tìm thấy sản phẩm với ID: " + productId);
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Lỗi khi lấy sản phẩm theo ID", e);
                    callback.onFailure(e);
                });
    }

    // 🔥 Lấy tất cả sản phẩm
    public void getAllProducts(FirestoreCallback<List<Product>> callback) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> productList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = doc.toObject(Product.class);
                        productList.add(product);
                    }
                    callback.onSuccess(productList);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Lỗi khi lấy danh sách sản phẩm", e);
                    callback.onFailure(e);
                });
    }

    // 🔥 Lấy sản phẩm theo danh mục
    public void getProductsByCategory(String category, FirestoreCallback<List<Product>> callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("category", category)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> productList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = doc.toObject(Product.class);
                        productList.add(product);
                    }
                    callback.onSuccess(productList);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Lỗi khi lấy sản phẩm theo danh mục", e);
                    callback.onFailure(e);
                });
    }

    // 🔥 Cập nhật số lượng sản phẩm
    public void updateProductStock(String productId, int newStock, FirestoreCallback<Boolean> callback) {
        db.collection(COLLECTION_NAME).document(productId) // ID là String
                .update("stock", newStock)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ Số lượng sản phẩm đã được cập nhật!");
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Lỗi khi cập nhật số lượng sản phẩm", e);
                    callback.onFailure(e);
                });
    }

    // 🔥 Cập nhật đánh giá sản phẩm
    public void updateProductRating(String productId, float newRating, FirestoreCallback<Boolean> callback) {
        db.collection(COLLECTION_NAME).document(productId) // ID là String
                .update("rating", newRating)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ Đánh giá sản phẩm đã được cập nhật!");
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Lỗi khi cập nhật đánh giá sản phẩm", e);
                    callback.onFailure(e);
                });
    }

    // 🔥 Tìm sản phẩm theo từ khóa (tìm theo tên hoặc danh mục)
    public void getProductsByKeyword(String keyword, FirestoreCallback<List<Product>> callback) {
        db.collection(COLLECTION_NAME)
                .whereGreaterThanOrEqualTo("name", keyword)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> productList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = doc.toObject(Product.class);
                        productList.add(product);
                    }
                    callback.onSuccess(productList);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Lỗi khi tìm kiếm sản phẩm", e);
                    callback.onFailure(e);
                });
    }

    // 🔥 Thêm danh sách sản phẩm
    public void insertProducts(List<Product> productList, FirestoreCallback<Boolean> callback) {
        for (Product product : productList) {
            insertProduct(product, new FirestoreCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    Log.d("Firestore", "✅ Đã thêm sản phẩm: " + product.getName());
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("Firestore", "❌ Lỗi khi thêm sản phẩm: " + product.getName(), e);
                }
            });
        }
        callback.onSuccess(true);
    }

    // 🔥 Lấy sản phẩm theo tên
    public void getProductByName(String name, FirestoreCallback<Product> callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("name", name)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Product product = queryDocumentSnapshots.getDocuments().get(0).toObject(Product.class);
                        callback.onSuccess(product);
                    } else {
                        Log.e("Firestore", "❌ Không tìm thấy sản phẩm với tên: " + name);
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Lỗi khi lấy sản phẩm theo tên", e);
                    callback.onFailure(e);
                });
    }
}
