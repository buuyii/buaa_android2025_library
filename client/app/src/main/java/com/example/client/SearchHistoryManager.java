package com.example.client;

import java.util.ArrayList;
import java.util.List;

public class SearchHistoryManager {
    private static SearchHistoryManager instance;
    private List<Book> searchHistory;

    private SearchHistoryManager() {
        searchHistory = new ArrayList<>();
    }

    public static synchronized SearchHistoryManager getInstance() {
        if (instance == null) {
            instance = new SearchHistoryManager();
        }
        return instance;
    }

    public void addBookToHistory(Book book) {
        // 检查图书是否已在历史记录中
        for (int i = 0; i < searchHistory.size(); i++) {
            Book historyBook = searchHistory.get(i);
            if (historyBook.getTitle().equals(book.getTitle()) && 
                historyBook.getAuthor().equals(book.getAuthor())) {
                // 如果已存在，将其移到列表开头（最新）
                searchHistory.remove(i);
                break;
            }
        }
        // 将图书添加到列表开头（最新）
        searchHistory.add(0, book);
    }

    public List<Book> getSearchHistory() {
        return new ArrayList<>(searchHistory);
    }
    
    public void clearHistory() {
        searchHistory.clear();
    }
}