package com.example.client;

public class Book {
    private String title;
    private String author;
    private String publisher;
    private String year;

    public Book(String title, String author, String publisher, String year) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.year = year;
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
}
