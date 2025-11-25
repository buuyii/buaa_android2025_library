package com.example.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BookSearchFragment extends Fragment {

    private EditText searchInput;
    private Button searchButton;
    private RecyclerView booksRecyclerView;
    private BookAdapter bookAdapter;
    private List<Book> bookList;

    public BookSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_search, container, false);

        searchInput = view.findViewById(R.id.search_input);
        searchButton = view.findViewById(R.id.search_button);
        booksRecyclerView = view.findViewById(R.id.books_recycler_view);

        bookList = new ArrayList<>();
        bookAdapter = new BookAdapter(bookList);
        booksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        booksRecyclerView.setAdapter(bookAdapter);

        searchButton.setOnClickListener(v -> performSearch());

        // 初始化一些示例数据
        initializeSampleData();

        return view;
    }

    private void performSearch() {
        String query = searchInput.getText().toString().trim();
        if (!query.isEmpty()) {
            // 这里应该调用实际的搜索API
            filterBooks(query);
        }
    }

    private void filterBooks(String query) {
        List<Book> filteredList = new ArrayList<>();
        for (Book book : bookList) {
            if (book.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    book.getAuthor().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(book);
            }
        }
        bookAdapter.updateList(filteredList);
    }

    private void initializeSampleData() {
        bookList.add(new Book("Java核心技术", "Cay S. Horstmann", "机械工业出版社", "2020"));
        bookList.add(new Book("Android开发艺术探索", "任玉刚", "电子工业出版社", "2019"));
        bookList.add(new Book("算法导论", "Thomas H. Cormen", "机械工业出版社", "2013"));
        bookList.add(new Book("设计模式", "Gang of Four", "机械工业出版社", "2010"));
        bookList.add(new Book("重构", "Martin Fowler", "人民邮电出版社", "2019"));
        bookAdapter.updateList(bookList);
    }
}
