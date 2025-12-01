package com.example.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class RegisterStudent extends AppCompatActivity {
    private EditText editAccount, editPassword1, editPassword2, editName;
    private Button buttonCommit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_student);
        setEdit();
        buttonCommit = findViewById(R.id.reg_st_commit_id);
        buttonCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 准备资源
                final String account = editAccount.getText().toString().trim();
                final String password = editPassword1.getText().toString();
                final String passwordConfirm = editPassword2.getText().toString();
                final String name = editName.getText().toString().trim();
                // 校验
                if (account.isEmpty() || password.isEmpty() || name.isEmpty()) {
                    Toast.makeText(RegisterStudent.this, "注册信息不能为空", Toast.LENGTH_SHORT).show();
                    return; // 结束执行
                }
                if (!password.equals(passwordConfirm)) {
                    Toast.makeText(RegisterStudent.this, "两次输入的密码不相同", Toast.LENGTH_SHORT).show();
                    return; // 结束执行
                }
                AppDataBase.databaseWriteExecutor.execute(() -> {
                    // 检查账号是否存在
                    List<Student> existingStudents = AppDataBase.getInstance(getApplicationContext()).studentDao().getAll();
                    boolean accountExists = false;
                    for (Student s : existingStudents) {
                        if (s.account.equals(account)) {
                            accountExists = true;
                            break;
                        }
                    }
                    if (accountExists) {
                        // 如果账号存在，需要切换回主线程来显示Toast
                        runOnUiThread(() -> {
                            Toast.makeText(RegisterStudent.this, "该账号已被注册", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        // 账号不存在，可以进行注册
                        Student newStudent = new Student(account, password, name, "");
                        AppDataBase.getInstance(getApplicationContext()).studentDao().insertAll(newStudent);

                        // 注册成功，切换回主线程来提示用户并跳转页面
                        runOnUiThread(() -> {
                            Toast.makeText(RegisterStudent.this, "注册成功", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterStudent.this, LoginStudent.class);
                            // 清除之前的Activity栈，使用户不能通过返回键回到注册页
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        });
                    }
                });
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    protected void setEdit() {
        editAccount = findViewById(R.id.reg_st_acc_id);
        editPassword1 = findViewById(R.id.reg_st_pass1_id);
        editPassword2 = findViewById(R.id.reg_st_pass2_id);
        editName = findViewById(R.id.reg_st_name_id);
    }

}