package com.example.videoapp;

import retrofit2.Call;
import retrofit2.http.GET;
import java.util.List;

public interface VideoService {
    @GET("get_videos.php")
    Call<List<String>> getVideoList();
}