package com.example.client;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tasklist.TaskListPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class AiFragment extends Fragment {

    private EditText promptEditText;
    private Button sendButton;
    private TextView chatResponseTextView;
    private final Qwen qwen = new Qwen();

    // 使用线程池来执行网络请求
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    // 使用Handler将结果传递回主线程
    private final Handler handler = new Handler(Looper.getMainLooper());

    private Markwon markwon;

    public AiFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        markwon = Markwon.builder(requireContext())
                .usePlugin(StrikethroughPlugin.create()) // 启用删除线插件
                .usePlugin(TaskListPlugin.create(requireContext())) // 启用任务列表插件
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ai, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化UI组件
        promptEditText = view.findViewById(R.id.promptEditText);
        sendButton = view.findViewById(R.id.sendButton);
        chatResponseTextView = view.findViewById(R.id.chatResponseTextView);

        markwon.setMarkdown(chatResponseTextView, "你好，有什么可以帮助你的吗？");

        // 设置发送按钮的点击事件
        sendButton.setOnClickListener(v -> {
            String userPrompt = promptEditText.getText().toString().trim();
            if (userPrompt.isEmpty()) {
                Toast.makeText(getContext(), "输入内容不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            // 更新UI，显示用户输入
            String currentChat = chatResponseTextView.getText().toString();
            chatResponseTextView.setText(currentChat + "\n\n你: " + userPrompt);
            promptEditText.setText(""); // 清空输入框

            // 禁用按钮防止重复点击
            sendButton.setEnabled(false);

            // 在后台线程中执行网络请求
            executor.execute(() -> {
                String aiResponse = null;
                try {
                    aiResponse = qwen.callWithMessage(userPrompt).getOutput().getChoices().get(0).getMessage().getContent();
                } catch (Exception e) {
                    e.printStackTrace(); // 在Logcat中打印详细错误，方便调试

                    // 将【错误】信息发送回主线程更新UI
                    handler.post(() -> {
                        String errorMessage;
                        if (e instanceof NoApiKeyException) {
                            errorMessage = "AI: 请求失败，请检查API-KEY是否已配置。";
                        } else if (e instanceof InputRequiredException) {
                            errorMessage = "AI: 请求失败，输入内容不能为空。";
                        } else {
                            errorMessage = "AI: 发生未知错误，请稍后再试。";
                        }
                        sendButton.setEnabled(true); // 出错后也要重新启用按钮
                    });
                }

                // 在主线程中更新UI
                String finalAiResponse = aiResponse;
                handler.post(() -> {
                    // 5. 使用Markwon来渲染AI的回复
                    updateChat("AI: " + finalAiResponse, true);
                    sendButton.setEnabled(true);
                });
            });
        });
    }

    private void updateChat(String message, boolean isMarkdown) {
        // 获取当前TextView已有的内容 (Spanned)
        CharSequence currentContent = chatResponseTextView.getText();

        // 准备新的内容
        CharSequence newMessage;
        if (isMarkdown) {
            // 如果是Markdown，使用Markwon来解析
            newMessage = markwon.toMarkdown(message);
        } else {
            // 如果不是，就是普通文本
            newMessage = message;
        }

        // 检查是否是初始状态，如果是，直接替换
        if (currentContent.toString().equals("你好，有什么可以帮助你的吗？") || currentContent.length() == 0) {
            chatResponseTextView.setText(newMessage);
        } else {
            // 如果不是，追加新内容，并用换行符隔开
            chatResponseTextView.append("\n\n");
            chatResponseTextView.append(newMessage);
        }
    }
}