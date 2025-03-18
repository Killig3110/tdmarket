package com.android.tdfruitstore.ui.register;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.tdfruitstore.R;
import com.android.tdfruitstore.data.dao.FirestoreCallback;
import com.android.tdfruitstore.data.dao.UserDAO;
import com.android.tdfruitstore.data.entities.User;
import com.android.tdfruitstore.ui.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private EditText etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private UserDAO userDAO = new UserDAO();
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        // Khởi tạo Room Database
        auth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(RegisterActivity.this, "Mật khẩu không khớp!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Đăng ký tài khoản trên Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            if (firebaseUser != null) {
                                String userName = email.split("@")[0];
                                String userAvatar = "android.resource://" + getPackageName() + "/" + R.drawable.ic_avatar;

                                // ❌ KHÔNG LƯU PASSWORD VÀO FIRESTORE
                                User newUser = new User(firebaseUser.getUid(), email, userName, password, userAvatar);

                                // 🔥 Lưu user vào Firestore và kiểm tra callback
                                userDAO.insertUser(newUser, new FirestoreCallback<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean result) {
                                        if (result) {
                                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                            finish();
                                        } else {
                                            // ❌ Xóa tài khoản Firebase nếu lưu Firestore thất bại
                                            firebaseUser.delete();
                                            Toast.makeText(RegisterActivity.this, "Lỗi khi lưu user! Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        // ❌ Xóa tài khoản Firebase nếu lưu Firestore thất bại
                                        firebaseUser.delete();
                                        Log.e("Firestore", "❌ Lỗi khi lưu user vào Firestore", e);
                                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }
}
