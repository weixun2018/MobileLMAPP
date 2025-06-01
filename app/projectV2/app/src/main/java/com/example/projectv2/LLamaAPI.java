package com.example.projectv2;

import android.util.Log;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;
import java.io.File;

public class LLamaAPI {
    private static final String TAG = LLamaAPI.class.getSimpleName();
    private static final LLamaAPI INSTANCE = new LLamaAPI();

    // 线程本地状态
    private final ThreadLocal<State> threadLocalState = ThreadLocal.withInitial(() -> State.IDLE);

    // 添加全局状态变量，解决线程间状态同步问题
    private volatile boolean isModelLoaded = false;

    // 记录当前加载的模型名称
    private volatile String currentModelName = null;

    // 执行器服务
    private final ExecutorService executorService;

    // 预测长度
    private final int nlen = 256;

    // 上下文大小，对于大模型可能需要调整
    private final int ctxSize = 2048;

    // 添加消息历史
    private final List<ChatMessage> messageHistory = new ArrayList<>();

    // 添加监听器接口和相关方法
    private final List<ModelStateListener> modelStateListeners = new ArrayList<>();

    // 添加控制变量
    private boolean useIncrementalGeneration = true;
    private boolean clearKVCacheNeeded = false;

    // 添加这些变量
    private float temperature = 0.8f;

    public interface ModelStateListener {
        void onModelLoaded();

        void onModelUnloaded();
    }

    public void addModelStateListener(ModelStateListener listener) {
        synchronized (modelStateListeners) {
            if (!modelStateListeners.contains(listener)) {
                modelStateListeners.add(listener);

                // 立即通知当前状态
                if (isModelLoaded) {
                    try {
                        listener.onModelLoaded();
                    } catch (Exception e) {
                        Log.e(TAG, "Error notifying new listener of loaded state", e);
                    }
                }
            }
        }
    }

    public void removeModelStateListener(ModelStateListener listener) {
        synchronized (modelStateListeners) {
            modelStateListeners.remove(listener);
        }
    }

    private void notifyModelLoaded() {
        Log.d(TAG, "Notifying listeners: Model loaded");
        synchronized (modelStateListeners) {
            for (ModelStateListener listener : modelStateListeners) {
                try {
                    listener.onModelLoaded();
                } catch (Exception e) {
                    Log.e(TAG, "Error notifying listener of model load", e);
                }
            }
        }
    }

    private void notifyModelUnloaded() {
        Log.d(TAG, "Notifying listeners: Model unloaded");
        synchronized (modelStateListeners) {
            for (ModelStateListener listener : modelStateListeners) {
                try {
                    listener.onModelUnloaded();
                } catch (Exception e) {
                    Log.e(TAG, "Error notifying listener of model unload", e);
                }
            }
        }
    }

    //    LLamaAPI使用单线程执行器处理所有AI任务
    private LLamaAPI() {
        executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "Llm-RunLoop");
                thread.setUncaughtExceptionHandler((t, e) ->
                        Log.e(TAG, "Unhandled exception", e));

                // 初始化本地库
                System.loadLibrary("llama-android");
                log_to_android();
                backend_init(false);

                Log.d(TAG, system_info());

                return thread;
            }
        });
    }

    // 获取单例实例
    public static LLamaAPI getInstance() {
        return INSTANCE;
    }

    // Native 方法声明
    private native void log_to_android();

    private native long load_model(String filename);

    private native void free_model(long model);

    private native long new_context(long model);

    private native void free_context(long context);

    private native void backend_init(boolean numa);

    private native void backend_free();

    private native long new_batch(int nTokens, int embd, int nSeqMax);

    private native void free_batch(long batch);

    private native long new_sampler(float temp);

    private native void free_sampler(long sampler);

    private native String bench_model(long context, long model, long batch,
                                      int pp, int tg, int pl, int nr);

    private native String system_info();

    private native int completion_init(long context, long batch, String text,
                                       boolean formatChat, int nLen);

    private native String completion_loop(long context, long batch, long sampler,
                                          int nLen, IntVar ncur);

    private native void kv_cache_clear(long context);

    // 新的JNI方法声明
    private native int chat_completion_init(long context, long batch, long model,
                                            List<ChatMessage> messages, int nLen);

    // 添加新的原生方法声明
    private native int incremental_chat_completion(long context, long batch, long model, 
                                                  ChatMessage newMessage, int nLen);
    private native int get_kv_cache_used(long context);
    private native int get_context_size(long context);

    // 添加新的native方法声明
    private native boolean can_fit_in_kv_cache(long context, int n_tokens_to_add);

    // 辅助方法：估算token数量
    private int estimateTokenCount(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        // 模仿simple-chat.cpp中的token计算方式
        // 简化版本：英文约4字符/token，中文约1字符/token
        int englishChars = 0;
        int chineseChars = 0;
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 0x4E00 && c <= 0x9FA5) {
                // 中文字符
                chineseChars++;
            } else if (c >= 32 && c <= 126) {
                // 英文字符
                englishChars++;
            }
        }
        
        // 基础token数估算
        int baseTokens = (englishChars / 4) + chineseChars;
        
        // 添加角色和格式开销 (类似simple-chat.cpp中模板overhead)
        int overhead = 20;
        
        return baseTokens + overhead;
    }

    // 辅助方法：生成响应
    private void generateResponse(State.Loaded state, IntVar ncur, int maxTokens, 
                                CompletionCallback callback) {
        // 用于收集完整响应
        StringBuilder response = new StringBuilder();
        
        // 循环生成tokens，直到达到限制或生成结束标记
        while (ncur.getValue() < maxTokens) {
            // 获取下一个token
            String token = completion_loop(state.context, state.batch, 
                                          state.sampler, maxTokens, ncur);
            
            // 检查是否结束生成
            if (token == null) {
                Log.d(TAG, "检测到EOG标记，结束生成");
                break;
            }
            
            // 过滤掉特殊标记（如有需要）
            if (!token.contains("<|") && !token.contains("</s>")) {
                response.append(token);
                callback.onToken(token);
            }
        }
        
        // 清理最终响应并添加到历史
        String finalResponse = cleanSpecialTokens(response.toString());
        if (!finalResponse.isEmpty()) {
            messageHistory.add(new ChatMessage("assistant", finalResponse));
        }
        
        // 完成回调
        callback.onComplete();
        
        // 检查并记录KV缓存状态
        int newKvUsed = get_kv_cache_used(state.context);
        int ctxSize = get_context_size(state.context);
        Log.i(TAG, "生成完成后KV缓存: " + newKvUsed + "/" + ctxSize + 
              " (" + (newKvUsed * 100 / ctxSize) + "%)");
    }

    // 清除聊天历史
    public void clearChatHistory() {
        messageHistory.clear();
        Log.i(TAG, "聊天历史已彻底清除");
    }

    // 清理特殊标记
    private String cleanSpecialTokens(String text) {
        if (text == null) {
            return "";
        }

        // 彻底清理所有特殊标记和角色标签
        text = text.replaceAll("<\\|im_\\w+\\|>", "") // 明确匹配所有im_xxx标记
                  .replaceAll("<\\|.*?\\|>", "")      // 匹配所有<|...|>格式标记
                  .replaceAll("</s>|<s>|<pad>", "")
                  .replaceAll("im_\\w+", "")          // 匹配所有im_xxx格式
                  .replaceAll("\\b(assistant|user|system)\\b", "")
                  .replaceAll("(?i)(user|assistant|system)\\s*:", "")
                  .replaceAll("\\s+", " ")
                  .trim();
        
        return text;
    }

    // 加载模型
    public void loadModel(String pathToModel) throws IllegalStateException {
        // 记录模型文件名
        final String modelFileName = new File(pathToModel).getName();

        executorService.execute(() -> {
            try {
                if (isModelLoaded || threadLocalState.get() != State.IDLE) {
                    // 如果模型已经加载，通知监听器但不抛出异常
                    Log.i(TAG, "Model already loaded: " + currentModelName);
                    isModelLoaded = true;
                    notifyModelLoaded();
                    return;
                }

                // 开始加载前先清理内存
                System.gc();

                Log.i(TAG, "Starting to load model: " + modelFileName);
                long model = load_model(pathToModel);
                if (model == 0L) {
                    throw new IllegalStateException("load_model() failed");
                }

                // 根据模型大小动态调整线程数
                int threadCount = Runtime.getRuntime().availableProcessors() - 1;
                threadCount = Math.max(1, Math.min(threadCount, 4)); // 保证在1-4之间
                Log.i(TAG, "Using " + threadCount + " threads for model inference");

                long context = new_context(model);
                if (context == 0L) {
                    // 清理模型资源
                    free_model(model);
                    throw new IllegalStateException("new_context() failed");
                }

                long batch = new_batch(512, 0, 1);
                if (batch == 0L) {
                    // 清理已分配的资源
                    free_context(context);
                    free_model(model);
                    throw new IllegalStateException("new_batch() failed");
                }

                long sampler = new_sampler(temperature);
                if (sampler == 0L) {
                    // 清理已分配的资源
                    free_batch(batch);
                    free_context(context);
                    free_model(model);
                    throw new IllegalStateException("new_sampler() failed");
                }

                // 更新当前模型名称
                currentModelName = modelFileName;
                Log.i(TAG, "Successfully loaded model: " + currentModelName);

                threadLocalState.set(new State.Loaded(model, context, batch, sampler));

                // 设置全局状态
                isModelLoaded = true;

                // 通知监听器模型已加载
                notifyModelLoaded();

                // 再次清理内存
                System.gc();
            } catch (Exception e) {
                Log.e(TAG, "Error loading model: " + e.getMessage(), e);

                // 确保状态一致
                isModelLoaded = false;
                currentModelName = null;
                threadLocalState.set(State.IDLE);

                // 清理内存
                System.gc();
            }
        });
    }

    // 添加聊天方法
    public void chat(String userMessage, CompletionCallback callback) {
        if (!isModelLoaded) {
            callback.onError(new IllegalStateException("未加载模型"));
            return;
        }
        
        executorService.execute(() -> {
            try {
                State.Loaded loadedState = (State.Loaded) threadLocalState.get();
                ChatMessage userMsg = new ChatMessage("user", userMessage);
                
                // 估算用户消息token数量
                int estimated_tokens = estimateTokenCount(userMessage);
                Log.i(TAG, "用户消息估计token数: " + estimated_tokens);
                
                // 检查KV缓存状态
                int n_ctx = get_context_size(loadedState.context);
                int n_ctx_used = get_kv_cache_used(loadedState.context);
                Log.i(TAG, String.format("KV缓存状态: %d/%d (%.1f%%)", 
                      n_ctx_used, n_ctx, (float)n_ctx_used/n_ctx*100));
                
                // 检查是否可以容纳新消息和回复
                if (n_ctx_used > 0 && canFitInKVCache(estimated_tokens + nlen)) {
                    // 增量处理
                    int result = incremental_chat_completion(
                        loadedState.context,
                        loadedState.batch,
                        loadedState.model,
                        userMsg,
                        nlen);
                    
                    if (result >= 0) {
                        // 增量处理成功
                        messageHistory.add(userMsg);
                        Log.i(TAG, "增量处理成功，位置: " + result);
                        processCompletion(loadedState, result, callback);
                        return;
                    } else if (result == -2) {
                        Log.i(TAG, "增量处理失败: KV缓存空间不足");
                    } else {
                        Log.i(TAG, "增量处理失败，错误码: " + result);
                    }
                } else if (n_ctx_used > 0) {
                    Log.i(TAG, "KV缓存空间不足，需要完整处理");
                }
                
                // 完整处理流程
                kv_cache_clear(loadedState.context);
                Log.i(TAG, "已清理KV缓存，开始完整处理");
                
                // 添加到历史
                messageHistory.add(userMsg);
                
                // 如果历史太长，裁剪一些早期消息
                if (messageHistory.size() > 8) {
                    int toRemove = messageHistory.size() / 2;
                    messageHistory.subList(0, toRemove).clear();
                    Log.i(TAG, String.format("历史记录已裁剪，保留最新%d条消息", messageHistory.size()));
                }
                
                // 初始化完整处理
                int pos = chat_completion_init(
                    loadedState.context,
                    loadedState.batch,
                    loadedState.model,
                    messageHistory,
                    nlen);
                
                if (pos < 0) {
                    throw new RuntimeException("聊天初始化失败，错误码: " + pos);
                }
                
                // 处理生成
                processCompletion(loadedState, pos, callback);
                
            } catch (Exception e) {
                Log.e(TAG, "聊天过程中出错: " + e.getMessage(), e);
                callback.onError(e);
            }
        });
    }

    // 处理生成过程的辅助方法
    private void processCompletion(State.Loaded state, int startPos, 
                                 CompletionCallback callback) {
        StringBuilder response = new StringBuilder();
        IntVar ncur = new IntVar(startPos);
        int emptyResponseCount = 0;
        int maxEmptyResponses = 5;
        int tokenCount = 0;
        long startTime = System.currentTimeMillis();
        
        try {
            while (emptyResponseCount < maxEmptyResponses) {
                String token = completion_loop(
                    state.context,
                    state.batch,
                    state.sampler,
                    nlen,
                    ncur);
                
                if (token == null) {
                    // 正常结束
                    Log.i(TAG, "检测到结束标记，生成完成");
                    break;
                }
                
                // 处理空token(可能是被跳过的特殊标记)
                if (token.isEmpty()) {
                    emptyResponseCount++;
                    continue;
                }
                
                // 重置空响应计数
                emptyResponseCount = 0;
                tokenCount++;
                
                // 发送token
                response.append(token);
                callback.onToken(token);
            }
            
            // 处理完成
            String finalResponse = response.toString().trim();
            if (!finalResponse.isEmpty()) {
                messageHistory.add(new ChatMessage("assistant", finalResponse));
            }
            
            // 记录性能数据
            long endTime = System.currentTimeMillis();
            float seconds = (endTime - startTime) / 1000f;
            float tokensPerSecond = tokenCount / Math.max(seconds, 0.1f);
            
            // 记录KV缓存使用情况
            int finalKvUsed = get_kv_cache_used(state.context);
            Log.i(TAG, String.format("生成完成, 生成了%d个token, 用时%.1f秒, 速率%.1f tokens/秒, KV使用: %d/%d (%.1f%%)", 
                  tokenCount, seconds, tokensPerSecond,
                  finalKvUsed, get_context_size(state.context),
                  (float)finalKvUsed/get_context_size(state.context)*100));
            
            callback.onComplete();
        }
        catch (Exception e) {
            Log.e(TAG, "生成过程出错: " + e.getMessage(), e);
            callback.onError(e);
        }
    }


     // 定义的回调接口
    /*
    *   回调模式设计 处理需要长时间运行的文本生成任务
    *
    *   接口定义的方法，在 AiChatFragment.java 中使用匿名类实现
    * */
    public interface CompletionCallback {
        void onToken(String token);     // 逐个返回生成的文本片段，流式输出

        void onComplete();              // 任务完成时发出通知

        void onError(Exception e);      // 出错时传递异常
    }

    /*
    * llamacpp原仓库官方demo的代码实现，本项目废弃不用，暂作保存
    * */
    public void generateCompletion(String message, boolean formatChat, CompletionCallback callback) {
        if (!isModelLoaded) {
            callback.onError(new IllegalStateException("No model loaded"));
            return;
        }

        if (formatChat) {
            // 如果需要聊天格式，直接调用chat方法
            chat(message, callback);
        } else {
            executorService.execute(() -> {
                try {
                    State currentState = threadLocalState.get();
                    if (!(currentState instanceof State.Loaded)) {
                        throw new IllegalStateException("No model loaded");
                    }

                    State.Loaded loadedState = (State.Loaded) currentState;

                    // 在开始新的文本生成前清除KV缓存
                    kv_cache_clear(loadedState.context);

                    IntVar ncur = new IntVar(completion_init(loadedState.context,
                            loadedState.batch, message, false, nlen));

                    while (ncur.getValue() <= nlen) {
                        String str = completion_loop(loadedState.context, loadedState.batch,
                                loadedState.sampler, nlen, ncur);
                        if (str == null) {
                            break;
                        }
                        callback.onToken(str);
                    }

                    kv_cache_clear(loadedState.context);
                    callback.onComplete();
                } catch (Exception e) {
                    callback.onError(e);
                }
            });
        }
    }

    // 添加重置聊天会话的方法
    public void resetChatSession() {
        resetChatSession(true);
    }

    public void resetChatSession(boolean clearHistory) {
        executorService.execute(() -> {
            try {
                if (clearHistory) {
                    // 清空历史记录
                    messageHistory.clear();
                    Log.i(TAG, "已清空聊天历史记录");
                } else {
                    Log.i(TAG, "保留聊天历史，仅重置KV缓存");
                }
                
                State currentState = threadLocalState.get();
                if (currentState instanceof State.Loaded) {
                    State.Loaded loadedState = (State.Loaded) currentState;
                    
                    // 彻底清除KV缓存
                    kv_cache_clear(loadedState.context);
                    
                    Log.i(TAG, "聊天会话已重置，KV缓存已清除");
                }
            } catch (Exception e) {
                Log.e(TAG, "重置聊天会话时出错", e);
            }
        });
    }

    // 卸载模型
    public void unloadModel() {
        executorService.execute(() -> {
            State currentState = threadLocalState.get();
            if (currentState instanceof State.Loaded) {
                State.Loaded loadedState = (State.Loaded) currentState;
                try {
                    Log.i(TAG, "Unloading model: " + currentModelName);

                    // 设置状态
                    threadLocalState.set(State.IDLE);
                    isModelLoaded = false;
                    currentModelName = null;

                    free_context(loadedState.context);
                    free_model(loadedState.model);
                    free_batch(loadedState.batch);
                    free_sampler(loadedState.sampler);

                    // 通知监听器模型已卸载
                    notifyModelUnloaded();

                    // 清理内存
                    System.gc();

                    Log.i(TAG, "Model unloaded successfully");
                } catch (Exception e) {
                    Log.e(TAG, "Error unloading model", e);
                }
            } else {
                // 确保状态一致
                isModelLoaded = false;
                currentModelName = null;
                threadLocalState.set(State.IDLE);

                // 通知监听器
                notifyModelUnloaded();
            }
        });
    }

    // 模型性能测试
    public void benchModel(int pp, int tg, int pl, int nr, BenchCallback callback) {
        executorService.execute(() -> {
            try {
                State currentState = threadLocalState.get();
                if (!(currentState instanceof State.Loaded)) {
                    throw new IllegalStateException("No model loaded");
                }

                State.Loaded loadedState = (State.Loaded) currentState;
                String result = bench_model(loadedState.context, loadedState.model,
                        loadedState.batch, pp, tg, pl, nr);
                callback.onComplete(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public interface BenchCallback {
        void onComplete(String result);

        void onError(Exception e);
    }

    // 内部类定义
    /*
     *  线程安全计数器封装
     *  AtomicInteger   保证多线程环境下对整数的操作（如递增、读取）是原子性的
     *
     *  Method:
     *       对外提供简单的 getValue() 和 inc() 方法
     *
     *  作用： 在cpp和java中实现当前预测token长度的信息通信
     *  todo：该设计可能不是必须的
     * */
    private static class IntVar {
        private final AtomicInteger value;

        IntVar(int initialValue) {
            this.value = new AtomicInteger(initialValue);
        }

        public int getValue() {
            return value.get();
        }

        public void inc() {
            value.incrementAndGet();
        }
    }

    // 状态类定义
    private static abstract class State {
        static final State IDLE = new Idle();

        private static class Idle extends State {
        }

        static class Loaded extends State {
            final long model;
            final long context;
            final long batch;
            final long sampler;

            Loaded(long model, long context, long batch, long sampler) {
                this.model = model;
                this.context = context;
                this.batch = batch;
                this.sampler = sampler;
            }
        }
    }

    // 判断模型是否已加载
    public boolean isModelLoaded() {
        return isModelLoaded;
    }

    // 保存聊天记录
    public static class ChatMessage {
        public final String role;
        public final String content;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    // 获取当前加载的模型名称
    public String getCurrentModelName() {
        return currentModelName;
    }

    // 添加获取当前对话历史长度的方法
    public int getChatHistorySize() {
        return messageHistory.size();
    }

    // 添加一个方法在界面显示当前对话状态
    public String getChatHistorySummary() {
        if (messageHistory.isEmpty()) {
            return "当前没有对话历史";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("当前对话历史：").append(messageHistory.size()).append("条消息\n");
        
        // 可以选择性地显示最近几轮对话的简短摘要
        int recentCount = Math.min(4, messageHistory.size());
        if (recentCount > 0) {
            summary.append("最近的消息：\n");
            for (int i = messageHistory.size() - recentCount; i < messageHistory.size(); i++) {
                ChatMessage msg = messageHistory.get(i);
                String role = "user".equals(msg.role) ? "用户" : "AI";
                String content = msg.content;
                if (content.length() > 20) {
                    content = content.substring(0, 20) + "...";
                }
                summary.append(role).append(": ").append(content).append("\n");
            }
        }
        
        return summary.toString();
    }

    // 估算历史消息的token数量
    private int estimateHistoryTokens() {
        // 更准确地估算：每个英文字符约0.25个token，每个中文字符约1个token
        int estimate = 0;
        for (ChatMessage msg : messageHistory) {
            String content = msg.content;
            int englishChars = 0;
            int chineseChars = 0;
            
            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c >= 0x4E00 && c <= 0x9FA5) {
                    // 中文字符
                    chineseChars++;
                } else if (c >= 32 && c <= 126) {
                    // ASCII可打印字符
                    englishChars++;
                }
            }
            
            // 每个角色标记和格式标记大约需要10个token
            int roleTokens = 10;
            // 英文字符通常每4个字符约1个token
            int textTokens = (englishChars / 4) + chineseChars;
            
            estimate += roleTokens + textTokens;
            
            Log.d(TAG, "消息: [" + msg.role + "] 长度=" + content.length() + 
                  ", 估算token=" + (roleTokens + textTokens));
        }
        
        // 添加格式化开销
        estimate += messageHistory.size() * 5;
        
        return estimate;
    }

    /**
     * 检测并处理生成结束标记，确保终止生成过程
     * @param token 当前token
     * @param buffer 缓冲区
     * @return 是否检测到结束标记
     */
    private boolean isEndOfGenerationToken(String token, StringBuilder buffer) {
        // 检测null (直接EOG标记)
        if (token == null) {
            Log.d(TAG, "检测到明确的EOG标记，立即终止生成");
            buffer.setLength(0); // 清空缓冲区
            return true;
        }
        
        // 检测长度限制标记
        if (token.contains("<|length_limit|>")) {
            Log.d(TAG, "达到最大长度限制，终止生成");
            return true;
        }
        
        // 检测可能的结束标记
        if (token.contains("<|endoftext|>") || 
            token.contains("<|eot|>") || 
            token.contains("</s>")) {
            Log.d(TAG, "检测到间接EOG标记，终止生成");
            return true;
        }
        
        return false;
    }

    // 设置生成温度的方法
    public void setTemperature(float temperature) {
        this.temperature = temperature;
        Log.i(TAG, String.format("设置生成温度: %.2f", temperature));
    }

    // 添加辅助方法检查KV缓存
    public boolean canFitInKVCache(int n_tokens_to_add) {
        if (!isModelLoaded) {
            return false;
        }
        
        State.Loaded loadedState = (State.Loaded) threadLocalState.get();
        return can_fit_in_kv_cache(loadedState.context, n_tokens_to_add);
    }
}