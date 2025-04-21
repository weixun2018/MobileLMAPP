package com.example.projectv2.api;

import com.example.projectv2.model.News;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface NewsApi {
    @GET("api/news")
    Call<List<News>> getLatestNews();

    @POST("api/news/refresh")
    Call<Void> refreshNews();
} 