package com.example.client;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

public class LoginStaff extends AppCompatActivity {
    private TextInputEditText staffIdEditText, passwordEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_staff);

        initViews();
        setListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        staffIdEditText = findViewById(R.id.login_staff_id);
        passwordEditText = findViewById(R.id.login_staff_password);
        loginButton = findViewById(R.id.login_staff_button);
    }

    private void setListeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });
    }

    private void performLogin() {
        final String staffId = staffIdEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString();

        if (staffId.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginStaff.this, "请输入工号和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hardcoded staff credentials for demonstration
        if (staffId.equals("admin") && password.equals("admin123")) {
            Toast.makeText(LoginStaff.this, "工作人员登录成功", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to staff main page
        } else {
            Toast.makeText(LoginStaff.this, "工号或密码错误", Toast.LENGTH_SHORT).show();
        }
    }
}
