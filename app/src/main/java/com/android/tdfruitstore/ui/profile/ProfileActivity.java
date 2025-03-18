package com.android.tdfruitstore.ui.profile;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.tdfruitstore.R;
import com.android.tdfruitstore.data.dao.FirestoreCallback;
import com.android.tdfruitstore.data.dao.UserDAO;
import com.android.tdfruitstore.data.entities.User;
import com.android.tdfruitstore.ui.home.HomeActivity;
import com.android.tdfruitstore.ui.login.LoginActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

import com.google.firebase.auth.AuthCredential;

public class ProfileActivity extends AppCompatActivity {

    private ImageView btnBackHome, btnPayment, ivSettings, btnNotifications;
    private Button btnEditProfile;
    private TextView tvUserName, tvUserEmail, tvEmail, tvAddress, tvPoints, tvChangePass;
    private SharedPreferences sharedPreferences;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri; // Để lưu ảnh tạm thời
    private CircleImageView isAvatar;
    private CircleImageView ivPreviewAvatar;
    private UserDAO userDAO = new UserDAO();
    private String email, userName;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Kiểm tra quyền đọc ảnh từ thư viện
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }

        // Ánh xạ View từ XML
        btnBackHome = findViewById(R.id.backHomeBtn);
        btnEditProfile = findViewById(R.id.button3);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvEmail2);
        tvEmail = findViewById(R.id.tvEmail);
        tvAddress = findViewById(R.id.textView27);
        tvPoints = findViewById(R.id.textView31);
        tvChangePass = findViewById(R.id.tvChangePass);
        ivSettings = findViewById(R.id.ivSettings);
        isAvatar = findViewById(R.id.isAvatar); // Avatar ImageView

        // Xu ly su kien nut change password
        tvChangePass.setOnClickListener(view -> showCustomChangePasswordDialog());

        // Khi nhấn Settings, hiển thị Dialog đổi Avatar
        ivSettings.setOnClickListener(v -> showChangeAvatarDialog());

        // Xử lý sự kiện nút Back quay lại HomeActivity
        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        email = sharedPreferences.getString("email", null);

        if (email == null) {
            Log.e("ProfileActivity", "❌ Không tìm thấy email trong SharedPreferences!");
            Toast.makeText(this, "Lỗi: Không tìm thấy email người dùng!", Toast.LENGTH_SHORT).show();
            finish(); // Thoát khỏi ProfileActivity nếu không có email
            return;
        }

        userName = email.split("@")[0];

        // Load thông tin người dùng
        loadUserData();
        loadAvatarFromFirestore();

        // Xử lý sự kiện Edit Profile
        btnEditProfile.setOnClickListener(v -> {
            sharedPreferences.edit().clear().apply();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });

        // Xử lý sự kiện nhấn vào các mục Payment, Settings, Notification
        findViewById(R.id.imageView14).setOnClickListener(v -> showMessage("Payment Clicked!"));
        findViewById(R.id.imageView16).setOnClickListener(v -> showMessage("Notifications Clicked!"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImageChooser();
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền để chọn ảnh đại diện!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Hiển thị Custom Dialog đổi Avatar
     */
    private void showChangeAvatarDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_change_avatar);

        // 🔥 Set kích thước rộng hơn
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        ivPreviewAvatar = dialog.findViewById(R.id.ivPreviewAvatar);
        Button btnChooseAvatar = dialog.findViewById(R.id.btnChooseAvatar);
        Button btnCancel = dialog.findViewById(R.id.btnCancelAvatar);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirmAvatar);

        btnChooseAvatar.setOnClickListener(v -> checkAndRequestPermissions());

        // Load ảnh hiện tại nếu có
        File avatarFile = new File(getFilesDir(), userName + ".jpg");
        if (avatarFile.exists()) {
            ivPreviewAvatar.setImageURI(Uri.fromFile(avatarFile));
        }

        // Hủy
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Xác nhận thay đổi
        btnConfirm.setOnClickListener(v -> {
            File tempAvatarFile = new File(getFilesDir(), "temp_avatar_" + userName + ".jpg");

            if (!tempAvatarFile.exists()) {
                Toast.makeText(this, "Lỗi: File ảnh không tồn tại!", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Xóa ảnh cũ nếu tồn tại
            if (avatarFile.exists()) {
                avatarFile.delete();
                Log.d("DEBUG", "🗑 Xóa ảnh cũ: " + avatarFile.getAbsolutePath());
            }

            // ✅ Đổi ảnh tạm thành ảnh chính thức
            if (tempAvatarFile.renameTo(avatarFile)) {
                Log.d("DEBUG", "✅ Ảnh đã được đổi thành chính thức: " + avatarFile.getAbsolutePath());
            } else {
                Log.e("DEBUG", "❌ Lỗi khi đổi ảnh tạm thành chính thức!");
                return;
            }

            // ✅ Cập nhật UI ngay lập tức trên `isAvatar`
            Glide.with(ProfileActivity.this)
                    .load(Uri.fromFile(avatarFile))
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.ic_avatar)
                    .into(isAvatar);

            // ✅ Lưu đường dẫn vào Firestore
            saveAvatarPathToFirestore(avatarFile.getAbsolutePath());

            // ✅ Hiển thị thông báo
            Toast.makeText(ProfileActivity.this, "Cập nhật avatar thành công!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_CODE_STORAGE_PERMISSION);
            } else {
                openImageChooser();
            }
        } else { // Android 10 - 12
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION);
            } else {
                openImageChooser();
            }
        }
    }

    /**
     * Mở thư viện ảnh để chọn Avatar mới
     */
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Log.d("DEBUG", "Ảnh đã chọn URI: " + selectedImageUri.toString());

            File tempAvatarFile = new File(getFilesDir(), "temp_avatar_" + userName + ".jpg");

            if (saveImageToFile(selectedImageUri, tempAvatarFile)) {
                // ✅ Hiển thị ảnh tạm ngay trên `ivPreviewAvatar`
                Glide.with(this)
                        .load(Uri.fromFile(tempAvatarFile))
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(ivPreviewAvatar);

                Log.d("DEBUG", "✅ Ảnh tạm đã lưu tại: " + tempAvatarFile.getAbsolutePath());
            } else {
                Toast.makeText(this, "Lỗi khi lưu ảnh!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveAvatarPathToFirestore(String avatarPath) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(email);

        userRef.update("avatarUrl", avatarPath)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ Avatar path updated: " + avatarPath);
                    // 🟢 Cập nhật ảnh ngay sau khi lưu Firestore
                    loadAvatarFromFirestore();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "❌ Lỗi khi cập nhật avatar path", e));
    }

    private void loadAvatarFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(email);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String avatarPath = documentSnapshot.getString("avatarUrl");
                if (avatarPath != null) {
                    File avatarFile = new File(avatarPath);
                    if (avatarFile.exists()) {
                        Log.d("DEBUG", "✅ Avatar loaded từ Firestore: " + avatarPath);

                        Glide.with(this)
                                .load(Uri.fromFile(avatarFile))
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .placeholder(R.drawable.ic_avatar)
                                .into(isAvatar);
                    } else {
                        Log.e("DEBUG", "❌ Lỗi: File avatar không tồn tại trong bộ nhớ!");
                        isAvatar.setImageResource(R.drawable.ic_avatar);
                    }
                }
            }
        }).addOnFailureListener(e -> Log.e("Firestore", "❌ Lỗi khi tải avatar từ Firestore", e));
    }

    private boolean saveImageToFile(Uri imageUri, File file) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return false;

            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Hiển thị Custom Dialog đổi mật khẩu
     */
    private void showCustomChangePasswordDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_change_password);

        // 🔥 Set kích thước rộng hơn
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        // Lấy ID của các view trong dialog
        TextInputEditText etOldPassword = dialog.findViewById(R.id.etOldPassword);
        TextInputEditText etNewPassword = dialog.findViewById(R.id.etNewPassword);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);

        // Xử lý nút Hủy
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Xử lý nút Xác nhận
        btnConfirm.setOnClickListener(v -> {
            String oldPassword = etOldPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();

            if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(ProfileActivity.this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(ProfileActivity.this, "Mật khẩu mới phải có ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                Toast.makeText(ProfileActivity.this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), oldPassword);

            // 🔥 Xác thực lại người dùng trước khi đổi mật khẩu
            firebaseUser.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("FirebaseAuth", "✅ Xác thực thành công, tiến hành đổi mật khẩu...");

                        // 🔥 Nếu xác thực thành công, tiến hành đổi mật khẩu
                        firebaseUser.updatePassword(newPassword)
                                .addOnSuccessListener(aVoid1 -> {
                                    Log.d("FirebaseAuth", "✅ Mật khẩu đã được cập nhật!");

                                    // 🔥 Cập nhật mật khẩu trong Firestore
                                    userDAO.getUserByEmail(firebaseUser.getEmail(), new FirestoreCallback<User>() {
                                        @Override
                                        public void onFailure(Exception e) {
                                            Log.e("Firestore", "❌ Lỗi khi lấy User từ Firestore", e);
                                            Toast.makeText(ProfileActivity.this, "Lỗi khi lấy User từ Firestore!", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onSuccess(User result) {
                                            if (result != null) {
                                                result.setPassword(newPassword);
                                                userDAO.updateUser(result, new FirestoreCallback<Boolean>() {
                                                    @Override
                                                    public void onFailure(Exception e) {
                                                        Log.e("Firestore", "❌ Lỗi khi cập nhật mật khẩu trong Firestore", e);
                                                    }

                                                    @Override
                                                    public void onSuccess(Boolean result) {
                                                        Log.d("Firestore", "✅ Mật khẩu đã được cập nhật trong Firestore!");
                                                        Toast.makeText(ProfileActivity.this, "Mật khẩu đã thay đổi thành công!", Toast.LENGTH_SHORT).show();
                                                        dialog.dismiss();
                                                    }
                                                });
                                            } else {
                                                Log.e("Firestore", "❌ User không tồn tại!");
                                                Toast.makeText(ProfileActivity.this, "Lỗi: Không tìm thấy người dùng!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FirebaseAuth", "❌ Lỗi khi cập nhật mật khẩu", e);
                                    Toast.makeText(ProfileActivity.this, "Lỗi cập nhật mật khẩu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseAuth", "❌ Xác thực thất bại", e);
                        Toast.makeText(ProfileActivity.this, "Lỗi xác thực, vui lòng nhập đúng mật khẩu cũ!", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    /**
     * Load thông tin người dùng từ dữ liệu giả lập
     */
    private void loadUserData() {
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        email = sharedPreferences.getString("email", null);

        if (email == null) {
            Toast.makeText(ProfileActivity.this, "Không tìm thấy email!", Toast.LENGTH_SHORT).show();
            Log.e("Firestore", "❌ Lỗi: Email null trong SharedPreferences!");
            return;
        }

        // 🔥 Lấy user từ Firestore thay vì Room
        userDAO.getUserByEmail(email, new FirestoreCallback<User>() {
            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "❌ Lỗi khi lấy thông tin user từ Firestore", e);
                Toast.makeText(ProfileActivity.this, "Lỗi tải dữ liệu người dùng!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    tvUserName.setText(user.getName());
                    tvEmail.setText(user.getEmail());
                    tvUserEmail.setText(user.getEmail());

                    if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                        Glide.with(ProfileActivity.this)
                                .load(user.getAvatarUrl())  // 🔥 Tải ảnh từ Firestore
                                .placeholder(R.drawable.ic_avatar)
                                .into(isAvatar);

                        Log.d("Firestore", "✅ Avatar loaded từ Firestore: " + user.getAvatarUrl());
                    } else {
                        Log.e("Firestore", "❌ User không có avatar!");
                        isAvatar.setImageResource(R.drawable.ic_avatar);
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Không tìm thấy User trong Firestore!", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "❌ User không tồn tại trong Firestore!");
                }
            }
        });
    }

    /**
     * Hiển thị thông báo khi nhấn vào mục trong Profile
     */
    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
