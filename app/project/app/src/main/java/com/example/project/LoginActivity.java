package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import com.example.project.utils.ApiConfig;
import com.example.project.utils.PreferenceManager;

/**
 * 登录界面活动
 * 处理用户登录认证和凭证管理
 */
public class LoginActivity extends AppCompatActivity {
    // UI 组件
    private EditText usernameInput;            // 用户名输入框
    private EditText passwordInput;            // 密码输入框
    private CheckBox rememberPasswordCheckbox; // 记住密码复选框
    private Button loginButton;                // 登录按钮
    
    // 网络和数据管理
    private RequestQueue requestQueue;         // Volley 请求队列
    private PreferenceManager preferenceManager; // 用户偏好管理器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        requestQueue = Volley.newRequestQueue(this);
        
        /**
         * 测试服务器连接
         * 在应用启动时验证服务器是否可用
         */
        JsonObjectRequest testRequest = new JsonObjectRequest(Request.Method.GET,
                ApiConfig.AUTH_URL + "test", null,
                response -> {
                    Toast.makeText(this, "Server connection successful", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Toast.makeText(this, "Server connection failed: " + error.toString(), 
                        Toast.LENGTH_LONG).show();
                });
        requestQueue.add(testRequest);

        // 初始化 UI 组件
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        rememberPasswordCheckbox = findViewById(R.id.remember_password_checkbox);
        loginButton = findViewById(R.id.login_button);
        Button registerButton = findViewById(R.id.register_button);

        preferenceManager = new PreferenceManager(this);

        /**
         * 加载保存的登录凭证
         * 如果用户之前选择了记住密码，自动填充登录表单
         */
        if (preferenceManager.isRememberPassword()) {
            usernameInput.setText(preferenceManager.getUsername());
            passwordInput.setText(preferenceManager.getPassword());
            rememberPasswordCheckbox.setChecked(true);
        }

        // 设置登录按钮点击事件
        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            // 验证输入不为空
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.error_fields_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            login(username, password);
        });
        
        // 设置注册按钮点击事件
        registerButton.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    /**
     * 执行登录操作
     * 向服务器发送登录请求并处理响应
     * 
     * @param username 用户名
     * @param password 密码
     */
    private void login(String username, String password) {
        try {
            // 构建登录请求体
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", username);
            jsonBody.put("password", password);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                    ApiConfig.AUTH_URL + "login", jsonBody,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                // 登录成功，保存令牌
                                String token = response.getString("token");
                                UserSession.getInstance().login(token);
                               
                                // 根据用户选择保存登录凭证
                                preferenceManager.saveLoginCredentials(username, password, 
                                    rememberPasswordCheckbox.isChecked());

                                // 跳转到主界面
                                Intent intent = new Intent(this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Toast.makeText(this, R.string.error_login_failed, Toast.LENGTH_SHORT).show());

            Volley.newRequestQueue(this).add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 