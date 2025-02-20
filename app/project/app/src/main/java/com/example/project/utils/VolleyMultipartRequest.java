package com.example.project.utils;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Volley 多部分请求类
 * 用于处理包含文件上传的 HTTP 请求
 * 支持 multipart/form-data 格式的数据传输
 */
public class VolleyMultipartRequest extends Request<NetworkResponse> {
    // 多部分表单数据的分隔符
    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";
    private final String boundary = "apiclient-" + System.currentTimeMillis();

    // 响应监听器
    private Response.Listener<NetworkResponse> mListener;
    private Response.ErrorListener mErrorListener;
    private Map<String, String> mHeaders;

    /**
     * 构造函数
     * @param method HTTP 请求方法（GET, POST 等）
     * @param url 请求 URL
     * @param listener 成功响应监听器
     * @param errorListener 错误响应监听器
     */
    public VolleyMultipartRequest(int method, String url,
                                Response.Listener<NetworkResponse> listener,
                                Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
    }

    /**
     * 获取请求头
     * @return 自定义的请求头或默认请求头
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return (mHeaders != null) ? mHeaders : super.getHeaders();
    }

    /**
     * 获取内容类型
     * @return multipart/form-data 格式的内容类型，包含boundary参数
     */
    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + boundary;
    }

    /**
     * 构建请求体
     * 将文件数据和其他参数组装成 multipart/form-data 格式
     */
    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try {
            // 获取文件数据
            Map<String, DataPart> params = getByteData();
            if (params != null && params.size() > 0) {
                // 遍历所有文件，构建对应的表单项
                for (Map.Entry<String, DataPart> entry : params.entrySet()) {
                    buildDataPart(dos, entry.getValue(), entry.getKey());
                }
            }

            // 写入结束标记
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取要上传的文件数据
     * 子类需要重写此方法提供具体的文件数据
     */
    protected Map<String, DataPart> getByteData() throws AuthFailureError {
        return null;
    }

    /**
     * 解析网络响应
     * @param response 原始网络响应
     * @return 解析后的响应对象
     */
    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    /**
     * 处理响应结果
     * @param response 网络响应
     */
    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    /**
     * 处理请求错误
     * @param error 错误信息
     */
    @Override
    public void deliverError(VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }

    /**
     * 构建单个文件的表单项
     * @param dataOutputStream 数据输出流
     * @param dataPart 文件数据
     * @param inputName 表单项名称
     */
    private void buildDataPart(DataOutputStream dataOutputStream, DataPart dataPart, String inputName) throws IOException {
        // 写入分隔符和表单项头部
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" +
                inputName + "\"; filename=\"" + dataPart.getFileName() + "\"" + lineEnd);
        
        // 写入内容类型（如果有）
        if (dataPart.getType() != null && !dataPart.getType().trim().isEmpty()) {
            dataOutputStream.writeBytes("Content-Type: " + dataPart.getType() + lineEnd);
        }
        dataOutputStream.writeBytes(lineEnd);

        // 写入文件数据
        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(dataPart.getContent());
        int bytesAvailable = fileInputStream.available();

        // 使用缓冲区写入数据
        int maxBufferSize = 1024 * 1024;
        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
        byte[] buffer = new byte[bufferSize];

        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

        while (bytesRead > 0) {
            dataOutputStream.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }

        dataOutputStream.writeBytes(lineEnd);
    }

    /**
     * 文件数据封装类
     * 用于存储上传文件的相关信息
     */
    public static class DataPart {
        private String fileName;    // 文件名
        private byte[] content;     // 文件内容
        private String type;        // 文件类型

        /**
         * 构造函数
         * @param name 文件名
         * @param data 文件数据
         */
        public DataPart(String name, byte[] data) {
            fileName = name;
            content = data;
        }

        /**
         * 构造函数
         * @param name 文件名
         * @param data 文件数据
         * @param mimeType 文件MIME类型
         */
        public DataPart(String name, byte[] data, String mimeType) {
            fileName = name;
            content = data;
            type = mimeType;
        }

        /**
         * 获取文件名
         */
        public String getFileName() {
            return fileName;
        }

        /**
         * 获取文件内容
         */
        public byte[] getContent() {
            return content;
        }

        /**
         * 获取文件类型
         */
        public String getType() {
            return type;
        }
    }
} 