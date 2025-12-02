// BookDetailFragment.java
package com.example.client;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookDetailFragment extends Fragment {

    private static final String ARG_BOOK = "book";
    private Book book;
    private Button favoriteButton;
    private Button generateReviewButton;

    public BookDetailFragment() {
        // Required empty public constructor
    }

    public static BookDetailFragment newInstance(Book book) {
        BookDetailFragment fragment = new BookDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BOOK, book);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            book = (Book) getArguments().getSerializable(ARG_BOOK);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_detail, container, false);

        TextView titleText = view.findViewById(R.id.detail_title);
        TextView authorText = view.findViewById(R.id.detail_author);
        TextView publisherText = view.findViewById(R.id.detail_publisher);
        TextView yearText = view.findViewById(R.id.detail_year);
        TextView descriptionText = view.findViewById(R.id.detail_description); // 新增
        favoriteButton = view.findViewById(R.id.favorite_button);
        Button backButton = view.findViewById(R.id.back_button);

        generateReviewButton = view.findViewById(R.id.generate_review_button);
        generateReviewButton.setOnClickListener(v -> generateAndShowReview());

        if (book != null) {
            titleText.setText(book.getTitle());
            authorText.setText("作者: " + book.getAuthor());
            publisherText.setText("出版社: " + book.getPublisher());
            yearText.setText("出版年份: " + book.getYear());
            descriptionText.setText(book.getDescription()); // 设置简介内容
            
            // 将图书添加到搜索历史
            SearchHistoryManager.getInstance().addBookToHistory(book);
            
            // 检查图书是否已被收藏，设置按钮初始状态
            updateFavoriteButtonState();
            
            // 设置收藏按钮的点击事件
            favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleFavorite();
                }
            });
        }

        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return view;
    }
    
    private void updateFavoriteButtonState() {
        if (FavoritesManager.getInstance().isFavorite(book)) {
            favoriteButton.setText("取消收藏");
        } else {
            favoriteButton.setText("收藏图书");
        }
    }
    
    private void toggleFavorite() {
        if (FavoritesManager.getInstance().isFavorite(book)) {
            // 当前已收藏，执行取消收藏操作
            FavoritesManager.getInstance().removeFavorite(book);
            favoriteButton.setText("收藏图书");
            Toast.makeText(getContext(), "已取消收藏: " + book.getTitle(), Toast.LENGTH_SHORT).show();
        } else {
            // 当前未收藏，执行收藏操作
            FavoritesManager.getInstance().addFavorite(book);
            favoriteButton.setText("取消收藏");
            Toast.makeText(getContext(), "已收藏: " + book.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private void generateAndShowReview() {
        if (book == null) {
            Toast.makeText(getContext(), "图书信息不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. 显示加载对话框
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("AI正在生成书评，请稍候...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // 2. 构建提问的Prompt
        String prompt = String.format("请为书籍《%s》（作者：%s）写一篇200字左右的简短书评，风格要客观且吸引人。用户只会和你进行一次对话，不要向用户提出问题",
                book.getTitle(), book.getAuthor());

        // 3. 调用您现有的Qwen类方法
        // 假设您的Qwen类有一个静态方法 `generate`，它接受 (prompt, callback)
        // 您需要根据您Qwen类的实际方法进行调整
        executor.execute(() -> {
            try {
                String review
                        = Qwen.callWithMessage(prompt).getOutput().getChoices().get(0).getMessage().getContent();
                // 在主线程中更新UI
                handler.post(() -> {
                    progressDialog.dismiss(); // 关闭加载对话框

                    if (review != null && !review.isEmpty()) {
                        // 4. 使用弹窗显示结果
                        new AlertDialog.Builder(requireContext())
                                .setTitle("AI 生成的书评")
                                .setMessage(review)
                                .setPositiveButton("确定", null)
                                .show();
                    } else {
                        // 处理生成失败或返回空内容的情况
                        Toast.makeText(getContext(), "生成书评失败，请稍后重试", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace(); // 在Logcat中打印详细错误，方便调试
                // 将【错误】信息发送回主线程更新UI
                handler.post(() -> handleError(e));
            }
        });
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
        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
    }
}