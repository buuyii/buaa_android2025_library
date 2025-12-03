// Book.java
package com.example.client;

import java.io.Serializable;
import android.util.Log; // 解决 Log 类找不到的问题

public class Book implements Serializable {
    private String title;
    private String author;
    private String publisher;
    private String year;
    private String description; // 添加简介字段
    private int coverResId; // 新增：封面资源 ID（如 R.drawable.xxx）

//    public Book(String title, String author, String publisher, String year, String description) {
//        this.title = title;
//        this.author = author;
//        this.publisher = publisher;
//        this.year = year;
//        this.description = description;
//    }
//
//    // 为保持向后兼容，也可以添加不带description的构造函数
//    public Book(String title, String author, String publisher, String year) {
//        this.title = title;
//        this.author = author;
//        this.publisher = publisher;
//        this.year = year;
//        this.description = "暂无简介"; // 默认简介
//    }

    // 修改后的全参数构造函数（包含封面路径）
    public Book(String title, String author, String publisher,
                String year, String description, int coverResIdParam) { // ★ 添加coverPath参数
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.year = year;
        this.description = description;
        this.coverResId = coverResIdParam; // 初始化资源 ID
        Log.d("BOOK_CONSTRUCTOR", "调用六参数构造函数，coverResId=" + coverResId); // 打印日志
    }

    // 保持原构造函数（自动设置默认封面路径为null）
    public Book(String title, String author, String publisher, String year, String description) {
        this(title, author, publisher, year, description, -1); // ★ 调用新构造函数
        Log.d("BOOK_CONSTRUCTOR", "调用五参数构造函数，coverResId=-1"); // 打印日志
    }

    // 保持原简化构造函数（自动设置默认描述和封面路径）
    public Book(String title, String author, String publisher, String year) {
        this(title, author, publisher, year, "暂无简介", -1); // ★ 调用新构造函数
    }

    // Getter
    public int getCoverResId() {
        return coverResId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getYear() {
        return year;
    }

    public String getDescription() {
        return description;
    }
}