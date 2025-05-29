package com.example.projectv2.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // 修改为电脑的IP地址，确保手机和电脑在同一网络下
    public static final String BASE_URL = "http://39.106.39.255:8080/"; // 你电脑的实际IP地址
    // 备用地址，如果上面的不工作，可以尝试使用这个
//     public static final String BASE_URL = "http://10.0.2.2:8080/"; // 模拟器使用的地址
    
    // 如果有线网络不可用，可以尝试使用无线网络IP: 192.168.31.161
    
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    public static UserApi getUserApi() {
        return getClient().create(UserApi.class);
    }

    public static NewsApi getNewsApi() {
        return getClient().create(NewsApi.class);
    }
} 