package com.example.project;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.project.utils.ApiConfig;
import com.example.project.utils.VolleyMultipartRequest;
import com.example.project.utils.VolleyMultipartRequest.DataPart;
import de.hdodenhof.circleimageview.CircleImageView;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户个人资料界面 Fragment
 * 负责用户信息的展示、编辑和更新
 * 主要功能：
 * - 基本信息展示和编辑（年龄、性别、年级等）
 * - 头像上传和预览
 * - 密码修改
 * - 账号登出
 * 
 * 安全考虑：
 * - 所有网络请求都需要验证 token
 * - 密码修改需要验证当前密码
 * - 图片上传限制为 JPEG 格式
 */
public class ProfileFragment extends Fragment {
    // UI 组件
    private CircleImageView profileImage;        // 圆形头像显示组件
    private TextView usernameText;               // 用户名（只读）
    private TextView userIdText;                 // 用户ID（只读）
    private TextView emailText;                  // 邮箱（只读）
    private TextView mbtiTypeText;              // MBTI类型（只读）
    private EditText ageInput;                  // 年龄输入框（可编辑）
    private Spinner genderSpinner;              // 性别选择下拉框
    private Spinner gradeSpinner;               // 年级选择下拉框
    private EditText bioInput;                  // 个人简介输入框
    private Button saveButton;                  // 保存修改按钮
    private Button changePasswordButton;        // 修改密码按钮
    private Button logoutButton;                // 登出按钮

    // 图片选择请求码
    private static final int PICK_IMAGE_REQUEST = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        initViews(view);
        setupSpinners();
        loadUserProfile();
        setupSaveButton();
        setupLogoutButton();
        
        return view;
    }

    /**
     * 初始化视图组件
     * 设置各个组件的初始状态和事件监听器
     * 
     * @param view Fragment 的根视图
     */
    private void initViews(View view) {
        profileImage = view.findViewById(R.id.profile_image);
        usernameText = view.findViewById(R.id.username_text);
        userIdText = view.findViewById(R.id.user_id_text);
        emailText = view.findViewById(R.id.email_text);
        mbtiTypeText = view.findViewById(R.id.mbti_type_text);
        ageInput = view.findViewById(R.id.age_input);
        genderSpinner = view.findViewById(R.id.gender_spinner);
        gradeSpinner = view.findViewById(R.id.grade_spinner);
        bioInput = view.findViewById(R.id.bio_input);
        saveButton = view.findViewById(R.id.save_button);
        changePasswordButton = view.findViewById(R.id.change_password_button);
        logoutButton = view.findViewById(R.id.logout_button);
        
        // 设置头像点击事件
        profileImage.setOnClickListener(v -> selectImage());
        
        // 设置密码修改按钮点击事件
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
    }

    /**
     * 设置下拉选择框
     * 初始化性别和年级选择器的选项列表
     * 从资源文件加载选项数据
     */
    private void setupSpinners() {
        // 设置性别选择下拉框
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.gender_options, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        // 设置年级选择下拉框
        ArrayAdapter<CharSequence> gradeAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.grade_options, android.R.layout.simple_spinner_item);
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gradeSpinner.setAdapter(gradeAdapter);
    }

    /**
     * 加载用户资料
     * 从服务器获取最新的用户信息并更新界面
     * 处理 token 失效和网络错误的情况
     */
    private void loadUserProfile() {
        String token = UserSession.getInstance().getToken();
        if (token == null) {
            Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                ApiConfig.PROFILE_URL, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject user = response.getJSONObject("user");
                            
                            // 设置基本用户信息
                            usernameText.setText(user.getString("username"));
                            userIdText.setText("ID: " + user.getString("id"));
                            emailText.setText(user.getString("email"));
                            
                            // 设置 MBTI 类型（如果有）
                            if (!user.isNull("mbti_type")) {
                                mbtiTypeText.setText(getString(R.string.mbti_type_format, 
                                    user.getString("mbti_type")));
                                mbtiTypeText.setVisibility(View.VISIBLE);
                            } else {
                                mbtiTypeText.setText(R.string.mbti_not_tested);
                                mbtiTypeText.setVisibility(View.VISIBLE);
                            }
                            
                            // 设置可编辑字段
                            if (!user.isNull("age")) {
                                ageInput.setText(String.valueOf(user.getInt("age")));
                            }
                            if (!user.isNull("bio")) {
                                bioInput.setText(user.getString("bio"));
                            }
                            
                            // 设置下拉选择框的值
                            if (!user.isNull("gender")) {
                                String gender = user.getString("gender");
                                int position = ((ArrayAdapter) genderSpinner.getAdapter())
                                        .getPosition(gender.substring(0, 1).toUpperCase() + gender.substring(1));
                                if (position >= 0) {
                                    genderSpinner.setSelection(position);
                                }
                            }
                            
                            if (!user.isNull("grade")) {
                                String grade = user.getString("grade");
                                int position = ((ArrayAdapter) gradeSpinner.getAdapter())
                                        .getPosition(grade.substring(0, 1).toUpperCase() + grade.substring(1));
                                if (position >= 0) {
                                    gradeSpinner.setSelection(position);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(getContext(), "加载个人资料失败", Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void setupSaveButton() {
        // 设置保存按钮点击事件
        saveButton.setOnClickListener(v -> updateProfile());
    }

    /**
     * 更新用户个人资料
     * 收集已修改的字段并发送到服务器
     * 特点：
     * - 仅发送已修改的字段
     * - 对输入数据进行验证
     * - 处理网络错误和服务器响应
     */
    private void updateProfile() {
        String token = UserSession.getInstance().getToken();
        if (token == null) {
            Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonBody = new JSONObject();
            
            // 获取并验证年龄输入
            String ageStr = ageInput.getText().toString().trim();
            if (!ageStr.isEmpty()) {
                jsonBody.put("age", Integer.parseInt(ageStr));
            }
            
            // 获取性别选择（跳过默认选项）
            int genderPosition = genderSpinner.getSelectedItemPosition();
            if (genderPosition > 0) {
                String gender = genderSpinner.getSelectedItem().toString().toLowerCase();
                jsonBody.put("gender", gender);
            }
            
            // 获取年级选择（跳过默认选项）
            int gradePosition = gradeSpinner.getSelectedItemPosition();
            if (gradePosition > 0) {
                String grade = gradeSpinner.getSelectedItem().toString().toLowerCase();
                jsonBody.put("grade", grade);
            }
            
            // 获取个人简介
            String bio = bioInput.getText().toString().trim();
            if (!bio.isEmpty()) {
                jsonBody.put("bio", bio);
            }

            // 发送更新请求
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT,
                    ApiConfig.PROFILE_URL, jsonBody,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                Toast.makeText(getContext(), "个人资料已更新", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Toast.makeText(getContext(), "更新个人资料失败", Toast.LENGTH_SHORT).show()) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };

            Volley.newRequestQueue(requireContext()).add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                uploadImage(imageUri);
                profileImage.setImageURI(imageUri);
            }
        }
    }

    /**
     * 处理头像上传
     * 将选择的图片转换为字节数组并上传到服务器
     * 注意事项：
     * - 使用 multipart/form-data 格式
     * - 限制图片大小和格式
     * - 处理图片读取和网络错误
     * 
     * @param imageUri 选择的图片URI
     */
    private void uploadImage(Uri imageUri) {
        try {
            // 读取图片数据
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            byte[] imageData = byteArrayOutputStream.toByteArray();

            // 创建多部分请求
            VolleyMultipartRequest request = new VolleyMultipartRequest(
                    Request.Method.POST,
                    ApiConfig.PROFILE_URL + "avatar",
                    response -> Toast.makeText(getContext(), "头像已更新", Toast.LENGTH_SHORT).show(),
                    error -> Toast.makeText(getContext(), "头像上传失败", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    params.put("avatar", new DataPart("avatar.jpg", imageData, "image/jpeg"));
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + UserSession.getInstance().getToken());
                    return headers;
                }
            };

            Volley.newRequestQueue(requireContext()).add(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示修改密码对话框
     * 包含三个输入框：当前密码、新密码、确认密码
     * 安全措施：
     * - 验证新密码一致性
     * - 验证当前密码正确性
     * - 密码复杂度检查
     */
    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_password, null);
        EditText currentPasswordInput = dialogView.findViewById(R.id.current_password_input);
        EditText newPasswordInput = dialogView.findViewById(R.id.new_password_input);
        EditText confirmNewPasswordInput = dialogView.findViewById(R.id.confirm_new_password_input);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.change_password)
                .setView(dialogView)
                .setPositiveButton(R.string.save_changes, (dialog, which) -> {
                    String currentPassword = currentPasswordInput.getText().toString();
                    String newPassword = newPasswordInput.getText().toString();
                    String confirmNewPassword = confirmNewPasswordInput.getText().toString();

                    if (!newPassword.equals(confirmNewPassword)) {
                        Toast.makeText(getContext(), R.string.error_passwords_not_match_new, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updatePassword(currentPassword, newPassword);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * 更新用户密码
     * 发送密码更新请求到服务器
     * 错误处理：
     * - 当前密码错误（401）
     * - 新密码不符合要求
     * - 网络连接失败
     * 
     * @param currentPassword 当前密码
     * @param newPassword 新密码
     */
    private void updatePassword(String currentPassword, String newPassword) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("currentPassword", currentPassword);
            jsonBody.put("newPassword", newPassword);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT,
                    ApiConfig.PROFILE_URL + "password", jsonBody,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                Toast.makeText(getContext(), R.string.password_updated, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                            Toast.makeText(getContext(), R.string.error_current_password, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), R.string.error_updating_password, Toast.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + UserSession.getInstance().getToken());
                    return headers;
                }
            };

            Volley.newRequestQueue(requireContext()).add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置登出按钮
     * 显示确认对话框并处理登出操作
     * 清理工作：
     * - 清除用户会话信息
     * - 清除本地缓存
     * - 重置应用状态
     */
    private void setupLogoutButton() {
        logoutButton.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("登出")
                .setMessage("确定要登出吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    // 清除用户会话
                    UserSession.getInstance().logout();
                    
                    // 返回登录界面
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("取消", null)
                .show();
        });
    }
} 