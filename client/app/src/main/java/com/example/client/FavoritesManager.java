package com.example.client;

import java.util.ArrayList;
import java.util.List;

public class FavoritesManager {
    private static FavoritesManager instance;
    private List<Book> favoriteBooks;

    private FavoritesManager() {
        favoriteBooks = new ArrayList<>();
    }

    public static synchronized FavoritesManager getInstance() {
        if (instance == null) {
            instance = new FavoritesManager();
        }
        return instance;
    }

    public void addFavorite(Book book) {
        // Check if book is already in favorites
        if (!isFavorite(book)) {
            favoriteBooks.add(book);
        }
    }

    public void removeFavorite(Book book) {
        favoriteBooks.removeIf(b -> b.getTitle().equals(book.getTitle()) && 
                                   b.getAuthor().equals(book.getAuthor()));
    }

    public boolean isFavorite(Book book) {
        for (Book b : favoriteBooks) {
            if (b.getTitle().equals(book.getTitle()) && 
                b.getAuthor().equals(book.getAuthor())) {
                return true;
            }
        }
        return false;
    }

    public List<Book> getFavoriteBooks() {
        return new ArrayList<>(favoriteBooks);
    }
}