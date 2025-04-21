package com.example.projectv2.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectv2.LLamaAPI;
import com.example.projectv2.R;
import com.example.projectv2.adapter.MessageAdapter;
import com.example.projectv2.db.ChatDbHelper;
import com.example.projectv2.model.Message;

import java.util.List;

public class AiChatFragment extends Fragment implements LLamaAPI.ModelStateListener {
    
    private static final String TAG = "AiChatFragment";
    
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ImageButton clearButton;
    private MessageAdapter messageAdapter;
    private ChatDbHelper dbHelper;
    private LLamaAPI llamaApi;
    private Handler mainHandler;
    private boolean isGenerating = false;
    private long lastUIUpdateTime = 0;
    private static final long UI_UPDATE_INTERVAL = 50; // 毫秒

    public static AiChatFragment newInstance() {
        return new AiChatFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ai_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 初始化Handler
        mainHandler = new Handler(Looper.getMainLooper());
        
        // 获取LLamaAPI实例
        llamaApi = LLamaAPI.getInstance();
        
        // 注册监听器
        llamaApi.addModelStateListener(this);
        
        // 只有在首次创建时重置聊天会话，而不是每次进入页面
        if (savedInstanceState == null) {
            llamaApi.resetChatSession();
        }
        
        // 检查模型状态并记录
        boolean modelLoaded = llamaApi.isModelLoaded();
        Log.d(TAG, "Initial model load state: " + modelLoaded);
        
        // 初始化数据库
        dbHelper = new ChatDbHelper(requireContext());
        
        // 初始化视图
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);
        clearButton = view.findViewById(R.id.clearButton);

        // 设置RecyclerView
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<Message> messages = dbHelper.getAllMessages();
        messageAdapter = new MessageAdapter(messages);
        messagesRecyclerView.setAdapter(messageAdapter);

        // 设置发送按钮点击事件
        sendButton.setOnClickListener(v -> sendMessage());
        
        // 设置清空按钮点击事件
        clearButton.setOnClickListener(v -> clearChatHistory());
        
        // 添加长按发送按钮清除历史记录的功能
        sendButton.setOnLongClickListener(v -> {
            clearChatHistory();
            return true;
        });
    }

    private void sendMessage() {
        String content = messageInput.getText().toString().trim();
        if (!content.isEmpty()) {
            // 检查模型是否已加载
            boolean modelLoaded = llamaApi.isModelLoaded();
            Log.d(TAG, "Checking model before chat: isModelLoaded = " + modelLoaded);
            
            if (!modelLoaded) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "请先在个人中心加载模型", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            
            // 避免重复生成
            if (isGenerating) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "AI正在思考中，请稍候...", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            
            // 保存并显示用户消息
            Message userMessage = new Message(content, false);
            dbHelper.insertMessage(userMessage);
            messageAdapter.addMessage(userMessage);

            // 清空输入框
            messageInput.setText("");

            // 滚动到底部
            messagesRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);

            // 显示AI正在输入的状态
            Message aiMessage = new Message("AI思考中...", true);
            messageAdapter.addMessage(aiMessage);
            messagesRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
            
            // 禁用发送按钮
            isGenerating = true;
            sendButton.setEnabled(false);
            
            // 使用LLamaAPI生成回复
            StringBuilder responseBuilder = new StringBuilder();


            // 匿名类实现回调接口 CompletionCallback
            /*
            *   onToken 实现流式返回token
            *   OnComplete 实现任务完成后的通知
            *   onError 实现异常通知
            *   todo：匿名类可能性能损耗较大，因为每次对话都会新建对象，使用static创建静态对象也许性能更高
            * */
            llamaApi.chat(content, new LLamaAPI.CompletionCallback() {
                @Override
                public void onToken(String token) {
                    responseBuilder.append(token);
                    
                    // 使用时间间隔控制UI更新频率，减少UI线程负担
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastUIUpdateTime > UI_UPDATE_INTERVAL) {
                        mainHandler.post(() -> {
                            if (isAdded()) {
                                aiMessage.setContent(responseBuilder.toString());
                                messageAdapter.notifyItemChanged(messageAdapter.getItemCount() - 1);
                                messagesRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                            }
                        });
                        lastUIUpdateTime = currentTime;
                    }
                }

                @Override
                public void onComplete() {
                    // 立即清空所有待处理的UI更新
                    mainHandler.removeCallbacksAndMessages(null);
                    
                    // 确保最终更新显示完整的响应
                    mainHandler.post(() -> {
                        if (isAdded()) {
                            // 更新最终结果并保存到数据库
                            String finalContent = responseBuilder.toString();
                            aiMessage.setContent(finalContent);
                            dbHelper.updateMessage(aiMessage);
                            messageAdapter.notifyItemChanged(messageAdapter.getItemCount() - 1);
                            
                            // 更新对话状态指示器
                            showChatHistoryStatus();
                            
                            // 重新启用发送按钮
                            isGenerating = false;
                            sendButton.setEnabled(true);
                            
                            Log.d(TAG, "生成完成，内容长度: " + finalContent.length());
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    mainHandler.post(() -> {
                        if (isAdded() && getContext() != null) {
                            Log.e(TAG, "Chat error", e);
                            Toast.makeText(getContext(), "生成失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            aiMessage.setContent("生成失败: " + e.getMessage());
                            messageAdapter.notifyItemChanged(messageAdapter.getItemCount() - 1);
                            
                            // 重新启用发送按钮
                            isGenerating = false;
                            sendButton.setEnabled(true);
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 清理资源
        if (dbHelper != null) {
            dbHelper.close();
        }
        // 移除监听器
        if (llamaApi != null) {
            llamaApi.removeModelStateListener(this);
        }
    }
    
    // 实现ModelStateListener接口
    @Override
    public void onModelLoaded() {
        // 模型已加载，可以更新UI状态
        Log.d(TAG, "onModelLoaded callback received");
        mainHandler.post(() -> {
            if (isAdded()) {
                // 可以添加视觉提示表明模型已加载
                sendButton.setEnabled(true);
                
                if (getContext() != null) {
                    String modelName = llamaApi.getCurrentModelName();
                    String modelMessage;
                    
                    if (modelName != null) {
                        if (modelName.contains("QwQ")) {
                            modelMessage = "小模型 (QwQ-0.5B) 已加载完成，可以开始对话";
                        } else if (modelName.contains("Minicpm")) {
                            modelMessage = "大模型 (Minicpm-4B) 已加载完成，可以开始对话";
                        } else {
                            modelMessage = "模型 " + modelName + " 已加载完成";
                        }
                    } else {
                        modelMessage = "AI模型已加载完成，可以开始对话";
                    }
                    
                    Toast.makeText(getContext(), modelMessage, Toast.LENGTH_SHORT).show();
                    
                    // 添加系统消息告知用户
                    Message systemMessage = new Message("系统：" + modelMessage, true);
                    messageAdapter.addMessage(systemMessage);
                    dbHelper.insertMessage(systemMessage);
                    messagesRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                }
            }
        });
    }
    
    @Override
    public void onModelUnloaded() {
        // 模型已卸载，可以更新UI状态
        Log.d(TAG, "onModelUnloaded callback received");
        mainHandler.post(() -> {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "模型已卸载，需要重新加载才能使用AI对话", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 添加一个重置聊天历史的方法
    private void clearChatHistory() {
        new AlertDialog.Builder(requireContext())
            .setTitle("清除聊天历史")
            .setMessage("是否要清除所有聊天历史？AI将不再记得之前的对话内容。")
            .setPositiveButton("确定", (dialog, which) -> {
                // 清除历史记录
                llamaApi.resetChatSession(true);
                // 更新UI
                if (messageAdapter != null) {
                    messageAdapter.clearMessages();
                    messageAdapter.notifyDataSetChanged();
                }
                
                Toast.makeText(requireContext(), "聊天历史已清除", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("取消", null)
            .show();
    }

    // 显示当前对话状态
    private void showChatHistoryStatus() {
        if (isAdded() && llamaApi != null) {
            int historySize = llamaApi.getChatHistorySize();
            
            if (historySize > 2) {
                // 计算轮数（一轮是用户+AI的对话）
                int rounds = historySize / 2;
                String status = "AI已记忆" + rounds + "轮对话";
                
                // 只记录到日志，不打扰用户
                Log.d(TAG, status + ", 历史记录长度: " + historySize);
            }
        }
    }
} 