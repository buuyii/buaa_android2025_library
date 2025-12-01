package com.example.client;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tasklist.TaskListPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class AiFragment extends Fragment {

    private EditText promptEditText;
    private ImageButton sendButton;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
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
        
        // 初始化聊天消息列表
        chatMessages = new ArrayList<>();
        // 添加初始欢迎消息
        chatMessages.add(new ChatMessage("你好，有什么可以帮助你的吗？", false));
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
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);

        // 设置RecyclerView
        chatAdapter = new ChatAdapter(chatMessages, markwon);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(chatAdapter);

        // 设置发送按钮的点击事件
        sendButton.setOnClickListener(v -> sendMessage());
        
        // 设置EditText的回车发送功能
        promptEditText.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void sendMessage() {
        String userPrompt = promptEditText.getText().toString().trim();
        if (userPrompt.isEmpty()) {
            Toast.makeText(getContext(), "输入内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 添加用户消息到列表
        chatMessages.add(new ChatMessage(userPrompt, true));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        
        // 清空输入框
        promptEditText.setText("");
        
        // 滚动到底部
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
        
        // 禁用按钮防止重复点击
        sendButton.setEnabled(false);

        // 在后台线程中执行网络请求
        executor.execute(() -> {
            try {
                String aiResponse = qwen.callWithMessage(userPrompt).getOutput().getChoices().get(0).getMessage().getContent();
                // 在主线程中更新UI
                handler.post(() -> handleAiResponse(aiResponse));
            } catch (Exception e) {
                e.printStackTrace(); // 在Logcat中打印详细错误，方便调试
                // 将【错误】信息发送回主线程更新UI
                handler.post(() -> handleError(e));
            }
        });
    }

    private void handleAiResponse(String aiResponse) {
        // 添加AI回复到聊天列表
        chatMessages.add(new ChatMessage(aiResponse, false));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
        sendButton.setEnabled(true);
    }

    private void handleError(Exception e) {
        String errorMessage;
        if (e instanceof NoApiKeyException) {
            errorMessage = "请求失败，请检查API-KEY是否已配置。";
        } else if (e instanceof InputRequiredException) {
            errorMessage = "请求失败，输入内容不能为空。";
        } else {
            errorMessage = "发生未知错误，请稍后再试。";
        }
        
        // 添加错误消息到聊天列表
        chatMessages.add(new ChatMessage(errorMessage, false));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
        sendButton.setEnabled(true); // 出错后也要重新启用按钮
    }
}