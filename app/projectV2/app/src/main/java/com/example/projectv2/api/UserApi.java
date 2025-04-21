package com.example.projectv2.api;

import com.example.projectv2.model.MbtiQuestion;
import com.example.projectv2.model.MbtiType;
import com.example.projectv2.model.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import okhttp3.RequestBody;
import okhttp3.MultipartBody;

public interface UserApi {
    @POST("api/user/login")
    Call<User> login(@Body User user);

    @POST("api/user/register")
    Call<User> register(@Body User user);

    @GET("api/user/{userId}")
    Call<User> getUserInfo(@Path("userId") Long userId);

    @GET("api/mbti/questions")
    Call<List<MbtiQuestion>> getMbtiQuestions();

    @GET("api/mbti/types/{typeCode}")
    Call<MbtiType> getMbtiType(@Path("typeCode") String typeCode);

    @PUT("api/mbti/user/{userId}")
    Call<Void> updateUserMbtiType(@Path("userId") Long userId, @Body Map<String, String> mbtiType);

    @PUT("api/user/{userId}/field")
    Call<User> updateUserField(@Path("userId") Long userId, @Body Map<String, String> fieldData);

    @PUT("api/user/{userId}/password")
    Call<Void> updatePassword(@Path("userId") Long userId, @Body Map<String, String> passwordData);

    @Multipart
    @POST("api/upload/avatar")
    Call<Map<String, String>> uploadAvatar(
            @Part MultipartBody.Part file,
            @Part("userId") RequestBody userId
    );
} 