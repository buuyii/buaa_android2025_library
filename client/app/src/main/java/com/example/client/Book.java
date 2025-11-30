// Book.java
package com.example.client;

import java.io.Serializable;

public class Book implements Serializable {
    private String title;
    private String author;
    private String publisher;
    private String year;
    private String description; // 添加简介字段

    public Book(String title, String author, String publisher, String year, String description) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.year = year;
        this.description = description;
    }

    // 为保持向后兼容，也可以添加不带description的构造函数
    public Book(String title, String author, String publisher, String year) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.year = year;
        this.description = "暂无简介"; // 默认简介
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