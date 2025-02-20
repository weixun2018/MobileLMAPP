package com.example.project;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import com.example.project.utils.ApiConfig;

/**
 * 用户注册界面活动
 * 负责处理新用户注册流程
 * 包含输入验证、数据提交和注册结果处理
 */
public class RegisterActivity extends AppCompatActivity {
    // UI 组件
    private EditText usernameInput;          // 用户名输入框
    private EditText emailInput;             // 邮箱输入框
    private EditText passwordInput;          // 密码输入框
    private EditText confirmPasswordInput;   // 确认密码输入框
    private RequestQueue requestQueue;       // Volley 请求队列

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 初始化网络请求队列
        requestQueue = Volley.newRequestQueue(this);
        
        // 绑定 UI 组件
        usernameInput = findViewById(R.id.username_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        Button registerButton = findViewById(R.id.register_button);

        // 设置注册按钮点击事件
        registerButton.setOnClickListener(v -> attemptRegister());
    }

    /**
     * 尝试注册新用户
     * 验证输入数据并提交到服务器
     * 包含输入验证和错误处理
     */
    private void attemptRegister() {
        // 获取并清理输入数据
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        // 验证输入字段是否为空
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, R.string.error_fields_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证两次输入的密码是否一致
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, R.string.error_passwords_not_match, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // 构建注册请求数据
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", username);
            jsonBody.put("email", email);
            jsonBody.put("password", password);

            // 发送注册请求
            JsonObjectRequest registerRequest = new JsonObjectRequest(Request.Method.POST,
                    ApiConfig.AUTH_URL + "register", jsonBody,
                    response -> {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                // 注册成功，显示提示并返回登录界面
                                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                // 注册失败，显示错误信息
                                Toast.makeText(this, R.string.error_register_failed, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Toast.makeText(this, R.string.error_network, Toast.LENGTH_SHORT).show());

            // 将请求添加到请求队列
            requestQueue.add(registerRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 