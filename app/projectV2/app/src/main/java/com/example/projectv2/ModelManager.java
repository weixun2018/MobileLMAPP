package com.example.projectv2;

import android.util.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ModelManager {
    private static final String TAG = "ModelManager";
    private static final ModelManager INSTANCE = new ModelManager();
    private static final AtomicBoolean isModelLoaded = new AtomicBoolean(false);
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // 静态代码块加载库文件
    static {
        try {
            System.loadLibrary("llama-android");
            Log.d(TAG, "Native library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load native library", e);
        }
    }

    private ModelManager() {}

    public static ModelManager getInstance() {
        return INSTANCE;
    }

    public void loadModel(String modelPath, ModelLoadCallback callback) {
        executorService.execute(() -> {
            try {
                if (isModelLoaded.get()) {
                    callback.onError(new IllegalStateException("Model is already loaded"));
                    return;
                }

                // 调用native方法加载模型
                long result = load_model(modelPath);
                if (result == 0) {
                    callback.onError(new IllegalStateException("Failed to load model"));
                    return;
                }

                isModelLoaded.set(true);
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void unloadModel(ModelUnloadCallback callback) {
        executorService.execute(() -> {
            try {
                if (!isModelLoaded.get()) {
                    callback.onError(new IllegalStateException("No model is loaded"));
                    return;
                }

                // 调用native方法卸载模型
                free_model();
                isModelLoaded.set(false);
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void generateResponse(String prompt, ResponseCallback callback) {
        executorService.execute(() -> {
            try {
                if (!isModelLoaded.get()) {
                    callback.onError(new IllegalStateException("No model is loaded"));
                    return;
                }

                String response = generate_response(prompt);
                callback.onSuccess(response);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public boolean isModelLoaded() {
        return isModelLoaded.get();
    }

    // Native methods
    private native long load_model(String modelPath);
    private native void free_model();
    private native String generate_response(String prompt);

    // Callback interfaces
    public interface ModelLoadCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface ModelUnloadCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface ResponseCallback {
        void onSuccess(String response);
        void onError(Exception e);
    }
}