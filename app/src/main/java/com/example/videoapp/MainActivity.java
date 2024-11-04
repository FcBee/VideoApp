package com.example.videoapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String PREFERENCES_FILE = "com.example.videoapp.preferences";
    private static final String USER_ID_KEY = "user_id";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 检查 SharedPreferences 中是否已有用户名
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString(USER_ID_KEY, null);

        if (username == null) {
            showUsernameInputDialog();
        }

        Button startVideoButton = findViewById(R.id.startVideoButton);
        startVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VideoListActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showUsernameInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("输入用户名");

        // 使用LayoutInflater从XML构建输入框视图
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_username_input, null);
        final EditText input = viewInflated.findViewById(R.id.usernameEditText);
        builder.setView(viewInflated);

        // 设置对话框的按钮事件
        builder.setPositiveButton("保存", null);
        builder.setNegativeButton("取消", (dialog, which) -> {
            dialog.cancel();
            finish(); // 如果取消则退出应用
        });

        AlertDialog dialog = builder.create();

        // 手动管理点击事件，以便控制对话框关闭时机
        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                String username = input.getText().toString().trim();

                if (!username.isEmpty()) {
                    saveUsername(username);
                    dialog.dismiss();
                    Toast.makeText(MainActivity.this, "用户名已保存", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void saveUsername(String username) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_ID_KEY, username);
        editor.apply();
        Log.d("MainActivity", "Saved username as user_id: " + username);
    }
}