package com.android.tdfruitstore.ui.login;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.android.tdfruitstore.R;
import com.android.tdfruitstore.data.dao.FirestoreCallback;
import com.android.tdfruitstore.data.dao.UserDAO;
import com.android.tdfruitstore.data.entities.User;
import com.android.tdfruitstore.ui.home.HomeActivity;
import com.android.tdfruitstore.ui.register.RegisterActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignUp, tvForgotPassword;
    private UserDAO userDAO = new UserDAO();
    private SharedPreferences sharedPreferences;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        auth = FirebaseAuth.getInstance();

        // 🔥 Kiểm tra nếu đã có userId trong SharedPreferences thì tự động đăng nhập
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);

        if (userId != null) {
            // 🔥 Nếu đã có userId, chuyển thẳng vào HomeActivity
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish(); // Đóng LoginActivity
            return;
        }

        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        // Xử lý đăng nhập
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Vui lòng nhập email và mật khẩu!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Đăng nhập Firebase
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            if (firebaseUser != null) {
                                userDAO.getUserByEmail(email, new FirestoreCallback<User>() {
                                    @Override
                                    public void onSuccess(User user) {
                                        if (user == null) {  // Nếu user chưa có trong Firestore
                                            String userName = email.split("@")[0];
                                            String userAvatar = "android.resource://" + getPackageName() + "/" + R.drawable.ic_avatar;

                                            String userId = java.util.UUID.randomUUID().toString();
                                            User newUser = new User(userId, userName, email, password, userAvatar);
                                            userDAO.insertUser(newUser, new FirestoreCallback<Boolean>() {
                                                @Override
                                                public void onSuccess(Boolean result) {
                                                    saveUserToSharedPreferences(newUser);
                                                    Log.d("Firestore", "✅ User added and saved: " + email);
                                                }

                                                @Override
                                                public void onFailure(Exception e) {
                                                    Log.e("Firestore", "❌ Failed to add user", e);
                                                }
                                            });
                                        } else {
                                            saveUserToSharedPreferences(user);
                                            Log.d("Firestore", "✅ User exists, loading data...");
                                        }

                                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                        finish();
                                    }
                                    @Override
                                    public void onFailure(Exception e) {
                                        Log.e("Firestore", "❌ Lỗi khi lấy user từ Firestore!", e);
                                        Toast.makeText(LoginActivity.this, "Lỗi khi lấy dữ liệu người dùng!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Chuyển sang màn hình Đăng ký
        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }

    private void saveUserToSharedPreferences(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", user.getId());
        editor.putString("userName", user.getName());
        editor.putString("email", user.getEmail());
        editor.putString("userAvatar", user.getAvatarUrl());
        editor.apply();

        Log.d("SharedPreferences", "✅ Saved user to SharedPreferences: " + user.getEmail());
    }

    private void showForgotPasswordDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_change_password_inlogin);

        // 🔥 Set kích thước rộng hơn
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        // 🔥 Ánh xạ các View trong dialog
        EditText etEmail = dialog.findViewById(R.id.etEmailForgot);
        EditText etPassword = dialog.findViewById(R.id.etNewPassword);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        // 🔥 Xử lý sự kiện
        btnConfirm.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String newPassword = etPassword.getText().toString().trim();

            if (email.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email và mật khẩu mới!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔥 Kiểm tra email có tồn tại trên Firestore không
            userDAO.getUserByEmail(email, new FirestoreCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    if (user != null) {
                        // 🔥 Nếu email tồn tại, tiến hành đặt lại mật khẩu
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        auth.sendPasswordResetEmail(email)
                                .addOnSuccessListener(aVoid -> {
                                    // 🔥 Cập nhật mật khẩu trong Firestore
                                    user.setPassword(newPassword);
                                    userDAO.updateUser(user, new FirestoreCallback<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean result) {
                                            runOnUiThread(() -> {
                                                Toast.makeText(btnConfirm.getContext(), "Mật khẩu đã được cập nhật! Vui lòng kiểm tra email!", Toast.LENGTH_SHORT).show();
                                            });
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            runOnUiThread(() -> {
                                                Toast.makeText(btnConfirm.getContext(), "Lỗi cập nhật mật khẩu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                        }
                                    });

                                    runOnUiThread(() -> {
                                        Toast.makeText(btnConfirm.getContext(), "Mật khẩu đã được cập nhật! Vui lòng kiểm tra email!", Toast.LENGTH_SHORT).show();
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    runOnUiThread(() -> {
                                        Toast.makeText(btnConfirm.getContext(), "Lỗi cập nhật mật khẩu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                                });

                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(btnConfirm.getContext(), "Email không tồn tại trong hệ thống!", Toast.LENGTH_SHORT).show();
                        });
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(btnConfirm.getContext(), "Lỗi kết nối Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });


        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
