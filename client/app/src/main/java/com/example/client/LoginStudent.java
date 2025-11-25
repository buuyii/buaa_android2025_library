package com.example.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class LoginStudent extends AppCompatActivity {
    private TextInputEditText accountEditText, passwordEditText;
    private Button loginButton;
    private TextView gotoRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_student);

        initViews();
        setListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        accountEditText = findViewById(R.id.login_student_account);
        passwordEditText = findViewById(R.id.login_student_password);
        loginButton = findViewById(R.id.login_student_button);
        gotoRegister = findViewById(R.id.goto_register);
    }

    private void setListeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        gotoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginStudent.this, RegisterStudent.class);
                startActivity(intent);
            }
        });
    }

    private void performLogin() {
        final String account = accountEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString();

        if (account.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginStudent.this, "请输入账号和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        AppDataBase.databaseWriteExecutor.execute(() -> {
            List<Student> students = AppDataBase.getInstance(getApplicationContext())
                    .studentDao()
                    .findByAccount(new String[]{account});

            runOnUiThread(() -> {
                if (students.isEmpty()) {
                    Toast.makeText(LoginStudent.this, "账号不存在", Toast.LENGTH_SHORT).show();
                } else {
                    Student student = students.get(0);
                    if (student.password.equals(password)) {
                        Toast.makeText(LoginStudent.this, "登录成功", Toast.LENGTH_SHORT).show();
                        // 跳转到图书馆管理系统主界面
                        Intent intent = new Intent(LoginStudent.this, LibraryMainActivity.class);
                        startActivity(intent);
                        finish(); // 关闭当前Activity
                    } else {
                        Toast.makeText(LoginStudent.this, "密码错误", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }
}
