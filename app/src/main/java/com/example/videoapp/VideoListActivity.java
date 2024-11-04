package com.example.videoapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VideoListActivity extends AppCompatActivity {

    private List<String> videoResources = new ArrayList<>();
    private VideoAdapter videoAdapter;
    private ViewPager2 viewPager;
    private long startTime;
    private int currentPage = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        viewPager = findViewById(R.id.viewPager);

        // Use singleton for SimpleCache
        SimpleCache simpleCache = SimpleCacheSingleton.getInstance(this);

        // Setup Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://120.26.91.147/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        VideoService service = retrofit.create(VideoService.class);

        // Fetch video list from server
        service.getVideoList().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(@NonNull Call<List<String>> call, @NonNull Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    videoResources.addAll(response.body());
                    Log.d("VideoListActivity", "Video URLs: " + videoResources.toString());
                    videoAdapter = new VideoAdapter(VideoListActivity.this, videoResources, simpleCache, viewPager);
                    viewPager.setAdapter(videoAdapter);
                } else {
                    Log.d("VideoListActivity", "Failed to get valid response");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {
                Log.e("VideoListActivity", "Error fetching video list: " + t.getMessage());
            }
        });

        // Attach page change callback to play/stop video based on visibility
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (currentPage >= 0 && currentPage < videoResources.size()) {
                    // Get the video URL for the current page
                    String videoUrl = videoResources.get(currentPage);

                    // Calculate duration watched
                    long currentTime = System.currentTimeMillis();
                    long durationWatched = (currentTime - startTime) / 1000;  // duration in seconds

                    // Send data to server
                    sendWatchTimeToServer(videoUrl, durationWatched);
                }

                startTime = System.currentTimeMillis();
                currentPage = position;
            }
        });
    }

    private String getUserId() {
        SharedPreferences preferences = getSharedPreferences("com.example.videoapp.preferences", Context.MODE_PRIVATE);
        String userId = preferences.getString("user_id", "");
        // 打印SharedPreferences读取的日志
        Log.d("VideoListActivity", "Retrieved user_id: " + userId);
        return userId;
    }

    private void sendWatchTimeToServer(String videoUrl, long duration) {
        new Thread(() -> {
            try {
                URL url = new URL("http://120.26.91.147:80/record_watch_time.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                // 设置连接参数
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);

                // 创建JSON参数
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("id", getUserId());
                jsonParam.put("videourl", videoUrl);
                jsonParam.put("duration", duration);

                // 记录即将发送的数据
                Log.d("VideoListActivity", "Sending data: " + jsonParam.toString());

                // 发送数据
                try (OutputStream os = urlConnection.getOutputStream()) {
                    os.write(jsonParam.toString().getBytes("UTF-8"));
                    os.flush();
                }

                // 检查响应码
                int responseCode = urlConnection.getResponseCode();
                Log.d("VideoListActivity", "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("VideoListActivity", "Watch time recorded successfully");
                } else {
                    Log.e("VideoListActivity", "Failed to record watch time: Response Code = " + responseCode);
                    try (InputStream is = urlConnection.getErrorStream()) {
                        if (is != null) {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                            StringBuilder response = new StringBuilder();
                            String line;

                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }
                            Log.e("VideoListActivity", "Error Response: " + response.toString());
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("VideoListActivity", "Exception: " + e.getMessage(), e);
            }
        }).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (videoAdapter != null) {
            videoAdapter.releaseAllPlayers();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (videoAdapter != null) {
            videoAdapter.initializePlayers();
        }
    }
}