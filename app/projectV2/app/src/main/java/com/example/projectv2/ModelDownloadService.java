package com.example.projectv2;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ModelDownloadService extends IntentService {
    private static final String TAG = "ModelDownloadService";
    
    // 添加SharedPreferences常量
    private static final String PREF_DOWNLOAD_STATE = "model_download_state";
    private static final String KEY_DOWNLOAD_STATUS = "download_status";
    private static final String KEY_MODEL_PATH = "model_path";
    private static final String KEY_EXPECTED_SIZE = "expected_size";
    private static final String KEY_DOWNLOAD_TIMESTAMP = "download_timestamp";
    
    // 下载状态常量
    public static final String STATUS_DOWNLOADING = "downloading";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_NONE = "none";
    
    public static final String ACTION_DOWNLOAD = "com.example.projectv2.action.DOWNLOAD";
    public static final String EXTRA_MODEL_URL = "com.example.projectv2.extra.MODEL_URL";
    public static final String EXTRA_MODEL_PATH = "com.example.projectv2.extra.MODEL_PATH";
    
    // 广播Action
    public static final String ACTION_DOWNLOAD_PROGRESS = "com.example.projectv2.action.DOWNLOAD_PROGRESS";
    public static final String ACTION_DOWNLOAD_COMPLETE = "com.example.projectv2.action.DOWNLOAD_COMPLETE";
    public static final String ACTION_DOWNLOAD_ERROR = "com.example.projectv2.action.DOWNLOAD_ERROR";
    
    // 广播Extra
    public static final String EXTRA_PROGRESS = "com.example.projectv2.extra.PROGRESS";
    public static final String EXTRA_DOWNLOADED_BYTES = "com.example.projectv2.extra.DOWNLOADED_BYTES";
    public static final String EXTRA_TOTAL_BYTES = "com.example.projectv2.extra.TOTAL_BYTES";
    public static final String EXTRA_ERROR_MESSAGE = "com.example.projectv2.extra.ERROR_MESSAGE";
    public static final String EXTRA_MODEL_FILE_PATH = "com.example.projectv2.extra.MODEL_FILE_PATH";
    
    private static boolean isServiceRunning = false;
    private OkHttpClient client;
    private boolean isDownloading = false;
    private Handler mainHandler;
    
    public ModelDownloadService() {
        super("ModelDownloadService");
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        isServiceRunning = true;
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        mainHandler = new Handler(Looper.getMainLooper());
        
        // 服务启动时设置下载状态
        saveDownloadStatus(STATUS_DOWNLOADING, "", 0);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceRunning = false;
    }
    
    /**
     * 检查下载服务是否正在运行
     */
    public static boolean isDownloadServiceRunning() {
        return isServiceRunning;
    }
    
    /**
     * 启动下载服务的便捷方法
     */
    public static void startDownload(Context context, String modelUrl, String modelPath) {
        Intent intent = new Intent(context, ModelDownloadService.class);
        intent.setAction(ACTION_DOWNLOAD);
        intent.putExtra(EXTRA_MODEL_URL, modelUrl);
        intent.putExtra(EXTRA_MODEL_PATH, modelPath);
        context.startService(intent);
    }
    
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DOWNLOAD.equals(action)) {
                final String modelUrl = intent.getStringExtra(EXTRA_MODEL_URL);
                final String modelPath = intent.getStringExtra(EXTRA_MODEL_PATH);
                handleDownload(modelUrl, modelPath);
            }
        }
    }
    
    private void handleDownload(String modelUrl, String modelPath) {
        if (isDownloading) {
            Log.d(TAG, "下载已在进行中，忽略新的下载请求");
            return;
        }
        
        isDownloading = true;
        // 更新下载状态为开始下载
        saveDownloadStatus(STATUS_DOWNLOADING, modelPath, 0);
        
        // 确保目录存在
        File modelFile = new File(modelPath);
        modelFile.getParentFile().mkdirs();
        
        // 创建请求
        Request request = new Request.Builder()
                .url(modelUrl)
                .build();
        
        try {
            // 执行同步请求（因为我们已经在后台线程中）
            Response response = client.newCall(request).execute();
            
            if (!response.isSuccessful()) {
                // 处理HTTP错误
                saveDownloadStatus(STATUS_FAILED, modelPath, 0);
                sendErrorBroadcast("HTTP错误: " + response.code());
                return;
            }
            
            // 获取响应体
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                saveDownloadStatus(STATUS_FAILED, modelPath, 0);
                sendErrorBroadcast("空响应");
                return;
            }
            
            // 获取文件总大小
            long totalBytes = responseBody.contentLength();
            
            // 更新期望的文件大小
            if (totalBytes > 0) {
                saveDownloadStatus(STATUS_DOWNLOADING, modelPath, totalBytes);
            }
            
            try (InputStream inputStream = responseBody.byteStream();
                 FileOutputStream outputStream = new FileOutputStream(modelFile)) {
                
                byte[] buffer = new byte[8192];
                long downloadedBytes = 0;
                int read;
                int lastProgressPercentage = 0;
                
                while ((read = inputStream.read(buffer)) != -1) {
                    // 写入文件
                    outputStream.write(buffer, 0, read);
                    
                    // 更新下载进度
                    downloadedBytes += read;
                    final int progressPercentage = totalBytes > 0 
                        ? (int) (downloadedBytes * 100 / totalBytes) 
                        : -1;
                    
                    // 只有在进度百分比变化时才发送广播，减少UI更新频率
                    if (progressPercentage != lastProgressPercentage) {
                        sendProgressBroadcast(progressPercentage, downloadedBytes, totalBytes);
                        lastProgressPercentage = progressPercentage;
                    }
                }
                
                // 下载完成，标记状态为完成
                saveDownloadStatus(STATUS_COMPLETED, modelPath, totalBytes > 0 ? totalBytes : modelFile.length());
                
                // 下载完成，发送完成广播
                mainHandler.post(() -> {
                    sendCompleteBroadcast(modelFile.getAbsolutePath());
                    isDownloading = false;
                });
                
            } catch (IOException e) {
                // 删除可能损坏的文件
                modelFile.delete();
                saveDownloadStatus(STATUS_FAILED, modelPath, 0);
                sendErrorBroadcast("下载失败: " + e.getMessage());
            }
            
        } catch (IOException e) {
            Log.e(TAG, "下载失败", e);
            saveDownloadStatus(STATUS_FAILED, modelPath, 0);
            sendErrorBroadcast("下载失败: " + e.getMessage());
        } finally {
            isDownloading = false;
        }
    }
    
    private void sendProgressBroadcast(int progress, long downloadedBytes, long totalBytes) {
        Intent intent = new Intent(ACTION_DOWNLOAD_PROGRESS);
        intent.putExtra(EXTRA_PROGRESS, progress);
        intent.putExtra(EXTRA_DOWNLOADED_BYTES, downloadedBytes);
        intent.putExtra(EXTRA_TOTAL_BYTES, totalBytes);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    
    private void sendCompleteBroadcast(String filePath) {
        Intent intent = new Intent(ACTION_DOWNLOAD_COMPLETE);
        intent.putExtra(EXTRA_MODEL_FILE_PATH, filePath);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    
    private void sendErrorBroadcast(String errorMessage) {
        Intent intent = new Intent(ACTION_DOWNLOAD_ERROR);
        intent.putExtra(EXTRA_ERROR_MESSAGE, errorMessage);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        isDownloading = false;
    }
    
    /**
     * 保存下载状态到SharedPreferences
     */
    private void saveDownloadStatus(String status, String modelPath, long expectedSize) {
        SharedPreferences prefs = getSharedPreferences(PREF_DOWNLOAD_STATE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_DOWNLOAD_STATUS, status);
        editor.putString(KEY_MODEL_PATH, modelPath);
        editor.putLong(KEY_EXPECTED_SIZE, expectedSize);
        editor.putLong(KEY_DOWNLOAD_TIMESTAMP, System.currentTimeMillis());
        editor.apply();
        
        Log.d(TAG, "保存下载状态: " + status + ", 路径: " + modelPath + ", 期望大小: " + expectedSize);
    }
    
    /**
     * 获取当前的下载状态
     */
    public static String getDownloadStatus(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_DOWNLOAD_STATE, Context.MODE_PRIVATE);
        return prefs.getString(KEY_DOWNLOAD_STATUS, STATUS_NONE);
    }
    
    /**
     * 获取期望的模型大小
     */
    public static long getExpectedModelSize(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_DOWNLOAD_STATE, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_EXPECTED_SIZE, 0);
    }
    
    /**
     * 获取模型路径
     */
    public static String getSavedModelPath(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_DOWNLOAD_STATE, Context.MODE_PRIVATE);
        return prefs.getString(KEY_MODEL_PATH, "");
    }
    
    /**
     * 检查模型文件是否有效
     */
    public static boolean isModelFileValid(Context context, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        
        // 获取期望的文件大小
        long expectedSize = getExpectedModelSize(context);
        
        // 如果我们知道期望的大小，进行比较
        if (expectedSize > 0) {
            // 允许有1MB的误差
            long allowedDifference = 1024 * 1024;
            boolean sizeMatch = Math.abs(file.length() - expectedSize) <= allowedDifference;
            if (!sizeMatch) {
                Log.w(TAG, "模型文件大小不匹配: 实际=" + file.length() + ", 期望=" + expectedSize);
            }
            return sizeMatch;
        }
        
        // 如果不知道期望的大小，至少要确保文件足够大
        // 一般的模型文件至少应该有100MB
        long minValidSize = 100 * 1024 * 1024;
        boolean isBigEnough = file.length() >= minValidSize;
        if (!isBigEnough) {
            Log.w(TAG, "模型文件过小: " + file.length() + " bytes");
        }
        return isBigEnough;
    }
    
    /**
     * 清除下载状态
     */
    public static void clearDownloadStatus(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_DOWNLOAD_STATE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_DOWNLOAD_STATUS, STATUS_NONE);
        editor.apply();
        
        Log.d(TAG, "清除下载状态");
    }
} 