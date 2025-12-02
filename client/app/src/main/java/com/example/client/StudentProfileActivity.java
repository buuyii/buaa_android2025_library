package com.example.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class StudentProfileActivity extends AppCompatActivity {
    private TextInputEditText accountEditText, nameEditText, studentIdEditText;
    private Button changePasswordButton;
    private Button deleteAccountButton;
    private Student currentStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        // Enable the back button in the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("个人信息");
        }

        initViews();
        loadStudentInfo();
        setListeners();
    }

    private void initViews() {
        accountEditText = findViewById(R.id.profile_account);
        nameEditText = findViewById(R.id.profile_name);
        studentIdEditText = findViewById(R.id.profile_student_id);
        changePasswordButton = findViewById(R.id.profile_change_password_button);
        deleteAccountButton = findViewById(R.id.profile_delete_account_button);
    }

    private void loadStudentInfo() {
        // 获取当前登录用户的ID
        SharedPreferences sharedPreferences = getSharedPreferences("library_app", MODE_PRIVATE);
        int studentId = sharedPreferences.getInt("student_id", -1);
        
        if (studentId == -1) {
            Toast.makeText(this, "无法获取用户信息", Toast.LENGTH_SHORT).show();
            return;
        }

        // 根据ID获取学生信息
        AppDataBase.databaseWriteExecutor.execute(() -> {
            currentStudent = AppDataBase.getInstance(getApplicationContext())
                    .studentDao()
                    .getStudentById(studentId);
            
            if (currentStudent != null) {
                runOnUiThread(() -> {
                    accountEditText.setText(currentStudent.account);
                    nameEditText.setText(currentStudent.name);
                    studentIdEditText.setText(currentStudent.studentId);
                });
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(StudentProfileActivity.this, "用户信息不存在", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setListeners() {
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePasswordDialog();
            }
        });
        
        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAccountDialog();
            }
        });
    }

    private void showChangePasswordDialog() {
        if (currentStudent == null) {
            Toast.makeText(StudentProfileActivity.this, "用户信息加载中，请稍后再试", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);
        
        TextInputEditText oldPasswordEditText = dialogView.findViewById(R.id.dialog_old_password);
        TextInputEditText newPasswordEditText = dialogView.findViewById(R.id.dialog_new_password);
        TextInputEditText confirmNewPasswordEditText = dialogView.findViewById(R.id.dialog_confirm_new_password);
        Button cancelButton = dialogView.findViewById(R.id.dialog_cancel_button);
        Button confirmButton = dialogView.findViewById(R.id.dialog_confirm_button);
        
        AlertDialog dialog = builder.create();
        
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = oldPasswordEditText.getText().toString().trim();
                String newPassword = newPasswordEditText.getText().toString().trim();
                String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();
                
                if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                    Toast.makeText(StudentProfileActivity.this, "请填写所有密码字段", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!currentStudent.password.equals(oldPassword)) {
                    Toast.makeText(StudentProfileActivity.this, "原密码不正确", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPassword.equals(confirmNewPassword)) {
                    Toast.makeText(StudentProfileActivity.this, "两次输入的新密码不一致", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (oldPassword.equals(newPassword)) {
                    Toast.makeText(StudentProfileActivity.this, "新密码不能与原密码相同", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update password in database
                AppDataBase.databaseWriteExecutor.execute(() -> {
                    currentStudent.password = newPassword;
                    AppDataBase.getInstance(getApplicationContext()).studentDao().updateStudent(currentStudent);
                    
                    runOnUiThread(() -> {
                        Toast.makeText(StudentProfileActivity.this, "密码修改成功", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                });
            }
        });
        
        dialog.show();
    }
    
    private void showDeleteAccountDialog() {
        if (currentStudent == null) {
            Toast.makeText(StudentProfileActivity.this, "用户信息加载中，请稍后再试", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_delete_account, null);
        builder.setView(dialogView);
        
        Button cancelButton = dialogView.findViewById(R.id.dialog_cancel_button);
        Button confirmButton = dialogView.findViewById(R.id.dialog_confirm_button);
        
        AlertDialog dialog = builder.create();
        
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Delete account from database
                AppDataBase.databaseWriteExecutor.execute(() -> {
                    try {
                        AppDataBase.getInstance(getApplicationContext()).studentDao().delete(currentStudent);
                        
                        // Clear user session
                        SharedPreferences sharedPreferences = getSharedPreferences("library_app", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();
                        
                        runOnUiThread(() -> {
                            Toast.makeText(StudentProfileActivity.this, "账号已注销", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            
                            // Redirect to login screen
                            Intent intent = new Intent(StudentProfileActivity.this, LoginStudent.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(StudentProfileActivity.this, "注销账号失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        });
                    }
                });
            }
        });
        
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}