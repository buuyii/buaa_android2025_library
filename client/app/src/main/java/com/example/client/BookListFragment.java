// BookListFragment.java
package com.example.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookListFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_BOOK_LIST = "book_list";
    
    private String title;
    private List<Book> bookList;
    private BookAdapter bookAdapter;

    public BookListFragment() {
        // Required empty public constructor
    }

    public static BookListFragment newInstance(String title, List<Book> bookList) {
        BookListFragment fragment = new BookListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putSerializable(ARG_BOOK_LIST, (java.io.Serializable) bookList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            bookList = (List<Book>) getArguments().getSerializable(ARG_BOOK_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_list, container, false);
        
        TextView titleText = view.findViewById(R.id.list_title);
        RecyclerView bookListView = view.findViewById(R.id.book_list_recycler_view);
        
        titleText.setText(title);
        
        bookAdapter = new BookAdapter(bookList);
        bookListView.setLayoutManager(new LinearLayoutManager(getContext()));
        bookListView.setAdapter(bookAdapter);
        
        // 添加书籍点击监听器
        bookAdapter.setOnBookClickListener(book -> {
            BookDetailFragment detailFragment = BookDetailFragment.newInstance(book);
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, detailFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 如果是收藏图书页面，则每次返回时刷新列表
        if ("收藏图书".equals(title)) {
            refreshFavoriteBooks();
        }
    }
    
    private void refreshFavoriteBooks() {
        // 更新收藏图书列表
        List<Book> favoriteBooks = FavoritesManager.getInstance().getFavoriteBooks();
        bookAdapter.updateList(favoriteBooks);
    }
}