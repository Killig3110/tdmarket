package com.android.tdfruitstore.data;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.android.tdfruitstore.data.entities.Category;
import com.android.tdfruitstore.data.entities.Product;
import com.android.tdfruitstore.data.entities.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FirestoreUtils {
    private static final String TAG = "FirestoreUtils";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void fetchDataFromFirestore(Context context) {
        db.collection("products") // 🔹 Thay "products" bằng collection của bạn
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Product> productList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            productList.add(product);
                        }
                        saveProductJsonToFile(context, productList);
                    } else {
                        Log.e(TAG, "❌ Lỗi khi lấy dữ liệu Firestore", task.getException());
                    }
                });
    }

    public static void fetchCategoriesFromFirestore(Context context) {
        db.collection("categories") // 🔹 Đảm bảo collection đúng
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Category> categoryList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Category category = new Category(
                                    document.getString("id"),
                                    document.getString("categoryName"),
                                    document.getString("tag"),
                                    document.contains("imageResId") ? document.getLong("imageResId").intValue() : 0
                            );
                            categoryList.add(category);
                        }
                        saveCateJsonToFile(context, categoryList);
                    } else {
                        Log.e(TAG, "❌ Lỗi khi lấy dữ liệu Firestore", task.getException());
                    }
                });
    }

    public static void fetchUsersFromFirestore(Context context) {
        db.collection("users") // 🔹 Đảm bảo collection đúng
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> userList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = new User(
                                    document.getString("id"),
                                    document.getString("email"),
                                    document.getString("name"),
                                    document.getString("password"),
                                    document.getString("avatarUrl")
                            );
                            userList.add(user);
                        }
                        saveUsersJsonToFile(context, userList);
                    } else {
                        Log.e(TAG, "❌ Lỗi khi lấy dữ liệu Firestore", task.getException());
                    }
                });
    }

    private static void saveUsersJsonToFile(Context context, List<User> userList) {
        String jsonData = gson.toJson(userList);
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "users.json");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(jsonData);
            Log.d(TAG, "✅ Đã lưu JSON vào: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "❌ Lỗi khi ghi file JSON", e);
        }
    }

    private static void saveCateJsonToFile(Context context, List<Category> categoryList) {
        String jsonData = gson.toJson(categoryList);
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "categories.json");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(jsonData);
            Log.d(TAG, "✅ Đã lưu JSON vào: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "❌ Lỗi khi ghi file JSON", e);
        }
    }

    private static void saveProductJsonToFile(Context context, List<Product> productList) {
        String jsonData = gson.toJson(productList);
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "products.json");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(jsonData);
            Log.d(TAG, "✅ Đã lưu JSON vào: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "❌ Lỗi khi ghi file JSON", e);
        }
    }
}
