package com.example.project;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import com.example.project.utils.ApiConfig;
import com.example.project.models.ChatMessage;
import com.example.project.adapters.ChatAdapter;
import com.android.volley.DefaultRetryPolicy;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 聊天界面 Fragment
 * 实现用户与 AI 助手的对话交互功能
 */
public class AIChatFragment extends Fragment {
    // UI 组件
    private RecyclerView chatRecyclerView;    // 聊天消息列表
    private EditText messageInput;             // 消息输入框
    private Button sendButton;                 // 发送按钮
    private Button loadMoreButton;            // 加载更多按钮
    
    // 数据和状态
    private ChatAdapter chatAdapter;           // 聊天列表适配器
    private List<ChatMessage> chatMessages;    // 聊天消息数据
    private int currentPage = 1;              // 当前页码
    private boolean isLoading = false;        // 是否正在加载数据
    private boolean hasMoreData = true;       // 是否还有更多数据

    /**
     * 创建 Fragment 视图
     * 初始化 UI 组件和加载历史消息
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_chat, container, false);
        
        initViews(view);
        setupRecyclerView();
        loadInitialHistory();
        
        return view;
    }

    /**
     * 初始化视图组件
     * 设置按钮点击监听器
     */
    private void initViews(View view) {
        chatRecyclerView = view.findViewById(R.id.chat_recycler_view);
        messageInput = view.findViewById(R.id.message_input);
        sendButton = view.findViewById(R.id.send_button);
        loadMoreButton = view.findViewById(R.id.load_more_button);

        sendButton.setOnClickListener(v -> sendMessage());
        loadMoreButton.setOnClickListener(v -> loadMoreHistory());
    }

    /**
     * 设置 RecyclerView
     * 配置布局管理器和适配器
     */
    private void setupRecyclerView() {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);  // 消息从底部开始显示
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);
    }

    /**
     * 加载初始聊天历史
     * 重置页码并加载第一页数据
     */
    private void loadInitialHistory() {
        currentPage = 1;
        loadChatHistory();
    }

    /**
     * 加载更多历史消息
     * 检查是否正在加载和是否还有更多数据
     */
    private void loadMoreHistory() {
        if (!isLoading && hasMoreData) {
            currentPage++;
            loadChatHistory();
        }
    }

    /**
     * 加载聊天历史记录
     * 发送网络请求获取历史消息数据
     */
    private void loadChatHistory() {
        if (isLoading || !hasMoreData) return;
        isLoading = true;
        loadMoreButton.setText(R.string.loading);

        String url = ApiConfig.CHAT_URL + "history?page=" + currentPage;
        Log.d("ChatHistory", "Loading page: " + currentPage + " from URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Log.d("ChatHistory", "Response: " + response.toString());
                        if (response.getBoolean("success")) {
                            JSONArray messagesArray = response.getJSONArray("messages");
                            List<ChatMessage> newMessages = new ArrayList<>();
                            
                            // 解析消息数据
                            for (int i = 0; i < messagesArray.length(); i++) {
                                JSONObject msg = messagesArray.getJSONObject(i);
                                boolean isUser;
                                try {
                                    isUser = msg.getBoolean("isUser");
                                } catch (JSONException e) {
                                    // 兼容处理整数类型的 isUser 字段
                                    isUser = msg.getInt("isUser") == 1;
                                }
                                newMessages.add(new ChatMessage(
                                    msg.getLong("id"),
                                    msg.getString("content"),
                                    isUser,
                                    msg.getString("timestamp")
                                ));
                            }

                            // 更新 UI 和数据状态
                            if (newMessages.isEmpty()) {
                                hasMoreData = false;
                                loadMoreButton.setText(R.string.no_more_history);
                            } else {
                                chatMessages.addAll(0, newMessages);
                                chatAdapter.notifyItemRangeInserted(0, newMessages.size());
                                loadMoreButton.setText(R.string.load_more);
                                currentPage++;
                            }
                        }
                    } catch (Exception e) {
                        Log.e("ChatHistory", "Error parsing response", e);
                        e.printStackTrace();
                    } finally {
                        isLoading = false;
                    }
                },
                error -> {
                    Log.e("ChatHistory", "Network error: " + error.getMessage());
                    error.printStackTrace();
                    isLoading = false;
                    loadMoreButton.setText(R.string.load_more);
                    Toast.makeText(getContext(), 
                        "Error loading chat history: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + UserSession.getInstance().getToken());
                return headers;
            }
        };

        // 设置请求超时和重试策略
        request.setRetryPolicy(new DefaultRetryPolicy(
            10000,  // 10 秒超时
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        Volley.newRequestQueue(requireContext()).add(request);
    }

    /**
     * 发送消息
     * 处理用户输入并发送到服务器
     */
    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (message.isEmpty()) return;

        messageInput.setText("");

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("content", message);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                    ApiConfig.CHAT_URL + "send", jsonBody,
                    response -> {
                        try {
                            // 处理服务器响应的消息
                            JSONArray messages = response.getJSONArray("messages");
                            for (int i = 0; i < messages.length(); i++) {
                                JSONObject msg = messages.getJSONObject(i);
                                boolean isUser;
                                try {
                                    isUser = msg.getBoolean("isUser");
                                } catch (JSONException e) {
                                    isUser = msg.getInt("isUser") == 1;
                                }
                                chatMessages.add(new ChatMessage(
                                    msg.getLong("id"),
                                    msg.getString("content"),
                                    isUser,
                                    msg.getString("timestamp")
                                ));
                            }
                            // 更新 UI 并滚动到最新消息
                            chatAdapter.notifyItemRangeInserted(
                                chatMessages.size() - messages.length(),
                                messages.length()
                            );
                            chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Toast.makeText(getContext(), R.string.error_network, Toast.LENGTH_SHORT).show()) {
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
} 