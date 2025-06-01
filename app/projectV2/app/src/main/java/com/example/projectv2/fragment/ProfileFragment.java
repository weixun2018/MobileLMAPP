package com.example.projectv2.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.projectv2.LLamaAPI;
import com.example.projectv2.LoginActivity;
import com.example.projectv2.ModelDownloadService;
import com.example.projectv2.R;
import com.example.projectv2.api.ApiClient;
import com.example.projectv2.model.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

public class ProfileFragment extends Fragment implements LLamaAPI.ModelStateListener {
    private static final String TAG = "ProfileFragment";
    
    private CardView avatarContainer;
    private ImageView avatarImage;
    private TextView usernameText;
    private TextView mbtiTypeText;
    private TextView bioText;
    private TextView gradeText;
    private TextView genderText;
    private TextView ageText;
    private View gradeContainer;
    private View genderContainer;
    private View ageContainer;
    private TextView changePasswordButton;
    private TextView logoutButton;

    // 添加模型管理相关UI元素
    private CardView modelManagementCard;
    private TextView modelStatusText;
    private Button loadModelButton;
    private Button unloadModelButton;
    private ProgressBar modelLoadingProgress;
    
    private Long userId;
    private User currentUser;
    private LLamaAPI llamaApi;
    private boolean isModelLoading = false;
    
    // 下载模型相关变量
    private OkHttpClient client;
    private boolean isDownloading = false;
//    private static final String MODEL_URL = "https://huggingface.co/qwqcoder/MiniCPM3-4B_Q4_K_M/resolve/main/MiniCPM3-4B-F16_Q4_k_m.gguf?download=true"; // 将这里替换为实际的模型URL
    private static final String MODEL_URL = "https://www.modelscope.cn/models/qwqcoder/blue-cat/resolve/master/model.gguf"; // 将这里替换为实际的模型URL
    private static final String MODEL_FILENAME = "blue-cat.gguf";

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST = 2;
    
    // 添加广播接收器
    private BroadcastReceiver downloadReceiver;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupListeners();
        loadUserInfo();
        
        // 初始化LLamaAPI
        llamaApi = LLamaAPI.getInstance();
        
        // 初始化OkHttpClient
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        
        // 初始化广播接收器
        initDownloadReceiver();
        
        // 检查之前的下载状态
        checkPreviousDownloadState();
        
        // 检查模型状态
        updateModelStatus();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            uploadImage(data.getData());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else if (isAdded() && getContext() != null) {
                Toast.makeText(requireContext(), "需要存储权限才能选择图片", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 注销广播接收器
        if (downloadReceiver != null && getContext() != null) {
            try {
                LocalBroadcastManager.getInstance(getContext())
                        .unregisterReceiver(downloadReceiver);
            } catch (Exception e) {
                Log.e(TAG, "注销广播接收器失败", e);
            }
        }
    }

    private void initViews(View view) {
        avatarContainer = view.findViewById(R.id.avatarContainer);
        avatarImage = view.findViewById(R.id.avatarImage);
        usernameText = view.findViewById(R.id.usernameText);
        mbtiTypeText = view.findViewById(R.id.mbtiTypeText);
        bioText = view.findViewById(R.id.bioText);
        gradeText = view.findViewById(R.id.gradeText);
        genderText = view.findViewById(R.id.genderText);
        ageText = view.findViewById(R.id.ageText);
        gradeContainer = view.findViewById(R.id.gradeContainer);
        genderContainer = view.findViewById(R.id.genderContainer);
        ageContainer = view.findViewById(R.id.ageContainer);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        logoutButton = view.findViewById(R.id.logoutButton);

        // 初始化模型管理相关UI元素
        modelManagementCard = view.findViewById(R.id.modelManagementCard);
        modelStatusText = view.findViewById(R.id.modelStatusText);
        loadModelButton = view.findViewById(R.id.loadModelButton);
        unloadModelButton = view.findViewById(R.id.unloadModelButton);
        modelLoadingProgress = view.findViewById(R.id.modelLoadingProgress);

        // 从SharedPreferences获取用户ID
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        userId = prefs.getLong("user_id", -1);
    }

    private void setupListeners() {
        avatarContainer.setOnClickListener(v -> showChangeAvatarDialog());
        usernameText.setOnClickListener(v -> showEditUsernameDialog());
        bioText.setOnClickListener(v -> showEditBioDialog());
        gradeContainer.setOnClickListener(v -> showEditGradeDialog());
        genderContainer.setOnClickListener(v -> showEditGenderDialog());
        ageContainer.setOnClickListener(v -> showEditAgeDialog());
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        logoutButton.setOnClickListener(v -> showLogoutConfirmDialog());
        
        // 设置模型管理相关监听器
        loadModelButton.setOnClickListener(v -> loadModel());
        unloadModelButton.setOnClickListener(v -> unloadModel());
    }

    private void loadUserInfo() {
        if (userId == -1) {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        ApiClient.getUserApi().getUserInfo(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null) {
                        currentUser = response.body();
                        updateUI();
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "获取用户信息失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI() {
        if (currentUser == null) return;

        usernameText.setText(currentUser.getUsername());
        mbtiTypeText.setText(currentUser.getMbtiType() != null ? currentUser.getMbtiType() : "未完成MBTI测试");
        bioText.setText(currentUser.getBio() != null ? currentUser.getBio() : "点击添加个性签名");
        gradeText.setText(currentUser.getGrade() != null ? currentUser.getGrade() : "未设置");
        genderText.setText(currentUser.getGender() != null ? currentUser.getGender() : "未设置");
        ageText.setText(currentUser.getAge() != null ? String.valueOf(currentUser.getAge()) : "未设置");

        // 加载头像
        if (currentUser.getAvatarUrl() != null) {
            String avatarUrl = ApiClient.BASE_URL.substring(0, ApiClient.BASE_URL.length() - 1) + currentUser.getAvatarUrl();
            Log.d(TAG, "Loading avatar from URL: " + avatarUrl);
            
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                  Target<Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "Avatar load failed for URL: " + avatarUrl + ", error: " + 
                                  (e != null ? e.getMessage() : "unknown"));
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                     Target<Drawable> target, DataSource dataSource,
                                                     boolean isFirstResource) {
                            Log.d(TAG, "Avatar loaded successfully from: " + avatarUrl);
                            return false;
                        }
                    })
                    .into(avatarImage);
        } else {
            avatarImage.setImageResource(R.drawable.default_avatar);
        }
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10及以上版本不需要存储权限就可以访问媒体文件
            openImagePicker();
        } else {
            // Android 9及以下版本需要请求存储权限
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST);
            } else {
                openImagePicker();
            }
        }
    }

    private void showChangeAvatarDialog() {
        String[] options = {"从相册选择", "取消"};
        new AlertDialog.Builder(requireContext())
                .setTitle("更换头像")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        checkStoragePermission();
                    }
                })
                .show();
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "选择图片"), PICK_IMAGE_REQUEST);
    }

    private void uploadImage(Uri imageUri) {
        try {
            // 获取原始图片
            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
            
            // 压缩图片
            Bitmap compressedBitmap = compressImage(originalBitmap);
            
            // 确保缓存目录存在
            File cacheDir = requireContext().getCacheDir();
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            
            // 将压缩后的图片转换为文件
            File compressedFile = new File(cacheDir, "compressed_avatar.jpg");
            FileOutputStream fos = new FileOutputStream(compressedFile);
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();
            
            Log.d(TAG, "压缩后的图片已保存到: " + compressedFile.getAbsolutePath() + ", 文件大小: " + compressedFile.length() + " 字节");

            // 直接使用文件路径创建RequestBody
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), compressedFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile);
            RequestBody userId = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(this.userId));

            // 发送请求
            ApiClient.getUserApi().uploadAvatar(body, userId).enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (isAdded() && getContext() != null) {
                        if (response.isSuccessful() && response.body() != null) {
                            String avatarUrl = response.body().get("url");
                            Log.d(TAG, "头像上传成功，URL: " + avatarUrl);
                            
                            if (currentUser != null) {
                                currentUser.setAvatarUrl(avatarUrl);
                            }
                            
                            // 更新UI显示新头像
                            String fullUrl = ApiClient.BASE_URL.substring(0, ApiClient.BASE_URL.length() - 1) + avatarUrl;
                            Log.d(TAG, "加载头像: " + fullUrl);
                            
                            Glide.with(ProfileFragment.this)
                                    .load(fullUrl)
                                    .placeholder(R.drawable.default_avatar)
                                    .error(R.drawable.default_avatar)
                                    .into(avatarImage);
                            Toast.makeText(getContext(), "头像上传成功", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                                Log.e(TAG, "上传失败: HTTP " + response.code() + " - " + errorBody);
                                Toast.makeText(getContext(), "头像上传失败: " + errorBody, Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Log.e(TAG, "读取错误响应失败", e);
                                Toast.makeText(getContext(), "头像上传失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    if (isAdded() && getContext() != null) {
                        Log.e(TAG, "上传请求失败", t);
                        Toast.makeText(getContext(), "头像上传失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // 清理资源
            originalBitmap.recycle();
            compressedBitmap.recycle();
            // 不要立即删除文件，等上传完成后再删除
            // compressedFile.delete();

        } catch (Exception e) {
            Log.e(TAG, "处理图片失败", e);
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "文件处理失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap compressImage(Bitmap image) {
        if (image == null) return null;

        // 计算压缩比例
        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();
        
        // 目标大小为800像素（可以根据需求调整）
        float maxDimension = 800.0f;
        
        float scale = 1.0f;
        if (originalWidth > originalHeight) {
            if (originalWidth > maxDimension) {
                scale = maxDimension / originalWidth;
            }
        } else {
            if (originalHeight > maxDimension) {
                scale = maxDimension / originalHeight;
            }
        }
        
        // 计算新的尺寸
        int newWidth = Math.round(originalWidth * scale);
        int newHeight = Math.round(originalHeight * scale);
        
        // 创建新的缩放后的位图
        return Bitmap.createScaledBitmap(image, newWidth, newHeight, true);
    }

    private void showEditUsernameDialog() {
        if (!isAdded() || getContext() == null) return;
        
        EditText input = new EditText(getContext());
        input.setText(currentUser.getUsername());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setSingleLine(true);

        new AlertDialog.Builder(getContext())
                .setTitle("修改用户名")
                .setView(input)
                .setPositiveButton("确定", (dialog, which) -> {
                    String newUsername = input.getText().toString().trim();
                    if (newUsername.isEmpty()) {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "用户名不能为空", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }
                    if (newUsername.equals(currentUser.getUsername())) {
                        return;
                    }
                    updateUserField("username", newUsername);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showEditBioDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("编辑个性签名");

        final EditText input = new EditText(requireContext());
        input.setText(currentUser.getBio());
        builder.setView(input);

        builder.setPositiveButton("确定", (dialog, which) -> {
            String newBio = input.getText().toString().trim();
            updateUserField("bio", newBio);
        });
        builder.setNegativeButton("取消", null);

        builder.show();
    }

    private void showEditGradeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("选择年级");

        final String[] grades = {"大一", "大二", "大三", "大四", "研究生"};
        builder.setItems(grades, (dialog, which) -> {
            String selectedGrade = grades[which];
            updateUserField("grade", selectedGrade);
        });

        builder.show();
    }

    private void showEditGenderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("选择性别");

        final String[] genders = {"男", "女", "其他"};
        builder.setItems(genders, (dialog, which) -> {
            String selectedGender = genders[which];
            updateUserField("gender", selectedGender);
        });

        builder.show();
    }

    private void showEditAgeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("编辑年龄");

        final EditText input = new EditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (currentUser.getAge() != null) {
            input.setText(String.valueOf(currentUser.getAge()));
        }
        builder.setView(input);

        builder.setPositiveButton("确定", (dialog, which) -> {
            try {
                int newAge = Integer.parseInt(input.getText().toString().trim());
                if (newAge > 0 && newAge < 150) {
                    updateUserField("age", String.valueOf(newAge));
                } else {
                    Toast.makeText(getContext(), "请输入有效年龄", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "请输入有效数字", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("取消", null);

        builder.show();
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null);
        EditText oldPasswordInput = dialogView.findViewById(R.id.oldPasswordInput);
        EditText newPasswordInput = dialogView.findViewById(R.id.newPasswordInput);
        EditText confirmPasswordInput = dialogView.findViewById(R.id.confirmPasswordInput);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("修改密码")
                .setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    String oldPassword = oldPasswordInput.getText().toString().trim();
                    String newPassword = newPasswordInput.getText().toString().trim();
                    String confirmPassword = confirmPasswordInput.getText().toString().trim();

                    if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                        Toast.makeText(getContext(), "请填写所有字段", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newPassword.equals(confirmPassword)) {
                        Toast.makeText(getContext(), "新密码两次输入不一致", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updatePassword(oldPassword, newPassword);
                })
                .setNegativeButton("取消", null);

        builder.show();
    }

    private void showLogoutConfirmDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("退出登录")
                .setMessage("确定要退出登录吗？")
                .setPositiveButton("确定", (dialog, which) -> logout())
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateUserField(String field, String value) {
        Map<String, String> updateData = new HashMap<>();
        updateData.put(field, value);

        ApiClient.getUserApi().updateUserField(userId, updateData).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (isAdded() && getContext() != null) {
                    if (response.isSuccessful() && response.body() != null) {
                        currentUser = response.body();
                        updateUI();
                        Toast.makeText(getContext(), "更新成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "更新失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "更新失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updatePassword(String oldPassword, String newPassword) {
        Map<String, String> passwordData = new HashMap<>();
        passwordData.put("oldPassword", oldPassword);
        passwordData.put("newPassword", newPassword);

        ApiClient.getUserApi().updatePassword(userId, passwordData).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (isAdded() && getContext() != null) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "密码修改成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "密码修改失败，请检查原密码是否正确", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "密码修改失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void logout() {
        // 清除SharedPreferences中的用户信息
        SharedPreferences.Editor editor = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        // 跳转到登录页面
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    // 修改loadModel方法，使用ModelDownloadService的检查方法
    private void loadModel() {
        if (isModelLoading) {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "模型正在加载中，请稍候...", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        
        // 获取模型文件路径
        String modelFilePath = getModelFilePath();
        
        // 使用服务提供的方法检查模型文件是否有效
        boolean modelFileValid = ModelDownloadService.isModelFileValid(requireContext(), modelFilePath);
        
        if (modelFileValid) {
            // 模型文件有效，直接加载
            loadModelFromFile(modelFilePath);
        } else {
            // 检查设备存储空间是否足够
            long requiredSpace = 3072L * 1024 * 1024; // 预估模型文件大小：3GB
            long availableSpace = 0;
            
            File externalDir = getContext().getExternalFilesDir(null);
            if (externalDir != null) {
                availableSpace = externalDir.getFreeSpace();
            }
            
            if (availableSpace < requiredSpace) {
                // 存储空间不足，提示用户
                new AlertDialog.Builder(requireContext())
                    .setTitle("存储空间不足")
                    .setMessage("下载模型需要约3GB存储空间，您的设备可用空间不足。请清理存储空间后再试。")
                    .setPositiveButton("确定", null)
                    .show();
                return;
            }
            
            // 如果存在旧的模型文件，但不完整，删除它
            File oldModelFile = new File(modelFilePath);
            if (oldModelFile.exists()) {
                oldModelFile.delete();
                Log.d(TAG, "删除不完整的模型文件: " + modelFilePath);
            }
            
            // 模型不存在或无效，需要下载
            startModelDownload();
        }
    }
    
    private void startModelDownload() {
        // 显示下载进度条
        isDownloading = true;
        isModelLoading = true;
        modelLoadingProgress.setVisibility(View.VISIBLE);
        modelStatusText.setText("正在下载模型...");
        loadModelButton.setEnabled(false);
        unloadModelButton.setEnabled(false);
        
        // 获取模型文件路径
        final String modelFilePath = getModelFilePath();
        
        // 保存当前要下载的模型路径，以便应用重启后能找到
        ModelDownloadService.clearDownloadStatus(requireContext());
        
        // 使用下载服务
        ModelDownloadService.startDownload(requireContext(), MODEL_URL, modelFilePath);
    }
    
    private String getModelFilePath() {
        if (!isAdded() || getContext() == null) return "";
        File externalDir = getContext().getExternalFilesDir(null);
        return new File(externalDir, MODEL_FILENAME).getAbsolutePath();
    }
    
    private void loadModelFromFile(String modelPath) {
        if (!isAdded() || getActivity() == null) return;
        
        // 检查文件是否存在
        File modelFile = new File(modelPath);
        if (!modelFile.exists() || modelFile.length() == 0) {
            Log.e(TAG, "模型文件不存在或为空: " + modelPath);
            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    isModelLoading = false;
                    modelLoadingProgress.setVisibility(View.GONE);
                    modelStatusText.setText("模型文件不存在或已损坏");
                    loadModelButton.setEnabled(true);
                    unloadModelButton.setEnabled(false);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "模型文件不存在或已损坏", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return;
        }
        
        isModelLoading = true;
        
        getActivity().runOnUiThread(() -> {
            modelLoadingProgress.setVisibility(View.VISIBLE);
            modelStatusText.setText("正在加载模型...");
            loadModelButton.setEnabled(false);
            unloadModelButton.setEnabled(false);
        });
        
        // 在后台线程中加载模型
        new Thread(() -> {
            try {
                Log.d(TAG, "开始加载模型: " + modelPath);
                // 加载模型 (监听器将处理成功加载后的UI更新)
                llamaApi.loadModel(modelPath);
            } catch (Exception e) {
                Log.e(TAG, "模型加载失败", e);
                // 主线程更新UI
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded() && getActivity() != null) {
                        isModelLoading = false;
                        modelLoadingProgress.setVisibility(View.GONE);
                        modelStatusText.setText("模型加载失败: " + e.getMessage());
                        loadModelButton.setEnabled(true);
                        unloadModelButton.setEnabled(false);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "模型加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }
    
    private void unloadModel() {
        modelStatusText.setText("正在卸载模型...");
        loadModelButton.setEnabled(false);
        unloadModelButton.setEnabled(false);
        
        // 卸载模型 (监听器将处理卸载后的UI更新)
        llamaApi.unloadModel();
    }
    
    private void updateModelStatus() {
        try {
            // 检查下载服务是否正在运行
            boolean isServiceRunning = ModelDownloadService.isDownloadServiceRunning();
            // 获取持久化的下载状态
            String downloadStatus = ModelDownloadService.getDownloadStatus(requireContext());
            
            // 如果服务正在运行，我们始终认为是下载状态
            if (isServiceRunning) {
                isDownloading = true;
                isModelLoading = true;
            } else if (ModelDownloadService.STATUS_DOWNLOADING.equals(downloadStatus)) {
                // 如果服务不在运行，但状态是下载中，说明下载被中断了
                // 在这种情况下，我们不会自动更新UI状态，因为checkPreviousDownloadState已经处理了
            }
            
            // 检查模型是否已加载
            boolean isModelLoaded = llamaApi != null && llamaApi.isModelLoaded();
            String currentModelName = llamaApi != null ? llamaApi.getCurrentModelName() : null;
            
            Log.d(TAG, "updateModelStatus: isModelLoaded = " + isModelLoaded + 
                  ", isModelLoading = " + isModelLoading + 
                  ", isDownloading = " + isDownloading +
                  ", isServiceRunning = " + isServiceRunning +
                  ", downloadStatus = " + downloadStatus);
            
            // 检查模型文件是否有效
            String modelFilePath = getModelFilePath();
            boolean modelFileValid = ModelDownloadService.isModelFileValid(requireContext(), modelFilePath);
            
            modelLoadingProgress.setVisibility(isDownloading || isModelLoading && !isModelLoaded ? View.VISIBLE : View.GONE);
            loadModelButton.setEnabled(!isModelLoaded && !isModelLoading && !isDownloading);
            unloadModelButton.setEnabled(isModelLoaded && !isModelLoading);
            
            if (isModelLoaded) {
                if (currentModelName != null) {
                    modelStatusText.setText("模型已加载: " + currentModelName);
                } else {
                    modelStatusText.setText("模型已加载，可以开始聊天");
                }
            } else if (isModelLoading) {
                if (isDownloading || isServiceRunning) {
                    modelStatusText.setText("模型下载中...");
                    modelLoadingProgress.setVisibility(View.VISIBLE);
                } else {
                    modelStatusText.setText("模型加载中...");
                    modelLoadingProgress.setVisibility(View.VISIBLE);
                }
            } else {
                // 既不是加载中也不是已加载
                if (modelFileValid) {
                    modelStatusText.setText("模型已下载但未加载，点击加载按钮开始加载");
                } else {
                    modelStatusText.setText("模型未下载，点击加载按钮开始下载");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "更新模型状态失败", e);
            modelStatusText.setText("无法获取模型状态");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        
        // 注册监听器
        if (llamaApi != null) {
            llamaApi.addModelStateListener(this);
        }
        
        // 检查下载服务状态
        boolean isServiceRunning = ModelDownloadService.isDownloadServiceRunning();
        if (isServiceRunning) {
            // 如果服务正在运行，我们应该将状态设置为下载中
            isDownloading = true;
            isModelLoading = true;
        }
        
        // 更新状态
        updateModelStatus();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        // 移除监听器
        if (llamaApi != null) {
            llamaApi.removeModelStateListener(this);
        }
    }
    
    // 实现ModelStateListener接口
    @Override
    public void onModelLoaded() {
        // 在主线程更新UI
        if (isAdded() && getActivity() != null) {
            Log.d(TAG, "onModelLoaded callback received");
            getActivity().runOnUiThread(() -> {
                isModelLoading = false;
                updateModelStatus();
                if (getContext() != null) {
                    Toast.makeText(getContext(), "模型已加载", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    @Override
    public void onModelUnloaded() {
        // 在主线程更新UI
        if (isAdded() && getActivity() != null) {
            Log.d(TAG, "onModelUnloaded callback received");
            getActivity().runOnUiThread(() -> {
                updateModelStatus();
                if (getContext() != null) {
                    Toast.makeText(getContext(), "模型已卸载", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void initDownloadReceiver() {
        downloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action == null) return;
                
                switch (action) {
                    case ModelDownloadService.ACTION_DOWNLOAD_PROGRESS:
                        // 接收下载进度更新
                        int progress = intent.getIntExtra(ModelDownloadService.EXTRA_PROGRESS, 0);
                        long downloadedBytes = intent.getLongExtra(ModelDownloadService.EXTRA_DOWNLOADED_BYTES, 0);
                        long totalBytes = intent.getLongExtra(ModelDownloadService.EXTRA_TOTAL_BYTES, 0);
                        
                        if (isAdded() && getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                isDownloading = true;
                                isModelLoading = true;
                                modelLoadingProgress.setVisibility(View.VISIBLE);
                                modelLoadingProgress.setProgress(progress);
                                
                                String progressText;
                                if (totalBytes > 0) {
                                    // 显示下载百分比和MB数
                                    double downloadedMB = downloadedBytes / (1024.0 * 1024.0);
                                    double totalMB = totalBytes / (1024.0 * 1024.0);
                                    progressText = String.format("正在下载模型... %.1f%%（%.1fMB/%.1fMB）", 
                                                               progress * 1.0, downloadedMB, totalMB);
                                } else {
                                    // 如果总大小未知，只显示已下载部分
                                    double downloadedMB = downloadedBytes / (1024.0 * 1024.0);
                                    progressText = String.format("正在下载模型...（已下载%.1fMB）", downloadedMB);
                                }
                                
                                modelStatusText.setText(progressText);
                                loadModelButton.setEnabled(false);
                                unloadModelButton.setEnabled(false);
                            });
                        }
                        break;
                        
                    case ModelDownloadService.ACTION_DOWNLOAD_COMPLETE:
                        // 下载完成
                        String filePath = intent.getStringExtra(ModelDownloadService.EXTRA_MODEL_FILE_PATH);
                        if (isAdded() && getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                isDownloading = false;
                                // 下载完成不要立即修改isModelLoading，因为还要进入加载模型阶段
                                modelStatusText.setText("下载完成，正在加载模型...");
                                loadModelFromFile(filePath);
                            });
                        }
                        break;
                        
                    case ModelDownloadService.ACTION_DOWNLOAD_ERROR:
                        // 下载错误
                        String errorMessage = intent.getStringExtra(ModelDownloadService.EXTRA_ERROR_MESSAGE);
                        if (isAdded() && getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                isModelLoading = false;
                                isDownloading = false;
                                modelLoadingProgress.setVisibility(View.GONE);
                                modelStatusText.setText("模型下载失败: " + errorMessage);
                                loadModelButton.setEnabled(true);
                                unloadModelButton.setEnabled(false);
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "模型下载失败: " + errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        break;
                }
            }
        };
        
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(ModelDownloadService.ACTION_DOWNLOAD_PROGRESS);
        filter.addAction(ModelDownloadService.ACTION_DOWNLOAD_COMPLETE);
        filter.addAction(ModelDownloadService.ACTION_DOWNLOAD_ERROR);
        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(downloadReceiver, filter);
    }

    /**
     * 检查之前的下载状态，如果有未完成的下载则处理
     */
    private void checkPreviousDownloadState() {
        String downloadStatus = ModelDownloadService.getDownloadStatus(requireContext());
        String modelPath = ModelDownloadService.getSavedModelPath(requireContext());
        
        Log.d(TAG, "检查之前的下载状态: " + downloadStatus + ", 路径: " + modelPath);
        
        switch (downloadStatus) {
            case ModelDownloadService.STATUS_COMPLETED:
                // 如果标记为已完成，验证文件是否真的完整
                if (!TextUtils.isEmpty(modelPath) && ModelDownloadService.isModelFileValid(requireContext(), modelPath)) {
                    // 文件有效，什么都不做，updateModelStatus会处理
                    Log.d(TAG, "发现有效的已完成下载: " + modelPath);
                } else {
                    // 文件无效或不完整，清除状态
                    Log.w(TAG, "发现无效的已完成下载: " + modelPath);
                    ModelDownloadService.clearDownloadStatus(requireContext());
                    // 如果有模型文件但不完整，删除它
                    if (!TextUtils.isEmpty(modelPath)) {
                        File file = new File(modelPath);
                        if (file.exists()) {
                            boolean deleted = file.delete();
                            Log.d(TAG, "删除不完整的模型文件: " + deleted);
                        }
                    }
                }
                break;
                
            case ModelDownloadService.STATUS_DOWNLOADING:
                // 如果标记为下载中，但服务不在运行，说明下载被中断
                if (!ModelDownloadService.isDownloadServiceRunning()) {
                    Log.w(TAG, "发现被中断的下载");
                    
                    // 弹出提示并询问是否继续下载
                    new AlertDialog.Builder(requireContext())
                        .setTitle("下载被中断")
                        .setMessage("您的模型下载似乎被中断，是否继续下载？")
                        .setPositiveButton("继续下载", (dialog, which) -> {
                            // 重新开始下载
                            startModelDownload();
                        })
                        .setNegativeButton("取消", (dialog, which) -> {
                            // 清除状态，删除不完整的文件
                            ModelDownloadService.clearDownloadStatus(requireContext());
                            if (!TextUtils.isEmpty(modelPath)) {
                                File file = new File(modelPath);
                                if (file.exists()) {
                                    boolean deleted = file.delete();
                                    Log.d(TAG, "删除不完整的模型文件: " + deleted);
                                }
                            }
                        })
                        .setCancelable(false)
                        .show();
                }
                break;
                
            case ModelDownloadService.STATUS_FAILED:
                // 如果标记为失败，清除状态
                Log.w(TAG, "发现失败的下载");
                ModelDownloadService.clearDownloadStatus(requireContext());
                // 删除可能损坏的文件
                if (!TextUtils.isEmpty(modelPath)) {
                    File file = new File(modelPath);
                    if (file.exists()) {
                        boolean deleted = file.delete();
                        Log.d(TAG, "删除损坏的模型文件: " + deleted);
                    }
                }
                break;
                
            case ModelDownloadService.STATUS_NONE:
            default:
                // 没有之前的下载状态，什么都不做
                break;
        }
    }
} 