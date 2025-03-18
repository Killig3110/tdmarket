package com.android.tdfruitstore.data.dao;

import android.util.Log;

import androidx.annotation.NonNull;

import com.android.tdfruitstore.data.entities.Category;
import com.android.tdfruitstore.data.dao.FirestoreCallback;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    private final FirebaseFirestore db;
    private final CollectionReference categoryRef;

    public CategoryDAO() {
        db = FirebaseFirestore.getInstance();
        categoryRef = db.collection("categories");
    }

    /**
     * Thêm danh mục mới vào Firestore
     */
    public void insertCategory(Category category, FirestoreCallback<Void> callback) {
        categoryRef.add(category)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "✅ Thêm danh mục thành công: " + documentReference.getId());
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Lỗi khi thêm danh mục", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Cập nhật danh mục trong Firestore
     */
    public void updateCategory(String categoryId, Category category, FirestoreCallback<Void> callback) {
        categoryRef.document(categoryId)
                .set(category)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ Cập nhật danh mục thành công: " + categoryId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Lỗi khi cập nhật danh mục", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Xóa danh mục khỏi Firestore
     */
    public void deleteCategory(String categoryId, FirestoreCallback<Void> callback) {
        categoryRef.document(categoryId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ Xóa danh mục thành công: " + categoryId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Lỗi khi xóa danh mục", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Lấy tất cả danh mục từ Firestore
     */
    public void getAllCategories(FirestoreCallback<List<Category>> callback) {
        categoryRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Category> categoryList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Category category = document.toObject(Category.class);
                        categoryList.add(category);
                    }
                    Log.d("Firestore", "✅ Lấy danh sách danh mục thành công");
                    callback.onSuccess(categoryList);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Lỗi khi lấy danh sách danh mục", e);
                    callback.onFailure(e);
                });
    }

    public void insertCategories(List<Category> categories, FirestoreCallback<Boolean> callback) {
        WriteBatch batch = db.batch();

        for (Category category : categories) {
            DocumentReference docRef = db.collection("categories").document();
            category.setId(docRef.getId()); // Gán ID cho category
            batch.set(docRef, category);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ Đã thêm danh mục mặc định!");
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Lỗi khi thêm danh mục!", e);
                    callback.onFailure(e);
                });
    }
}