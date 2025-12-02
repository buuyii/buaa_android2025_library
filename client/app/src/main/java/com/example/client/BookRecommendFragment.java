package com.example.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BookRecommendFragment extends Fragment {
    private static final String ARG_BOOK_LIST = "book_list";
    
    private List<Book> bookList;
    
    public BookRecommendFragment() {
        // Required empty public constructor
    }
    
    public static BookRecommendFragment newInstance(List<Book> books) {
        BookRecommendFragment fragment = new BookRecommendFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BOOK_LIST, (java.io.Serializable) books);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookList = (List<Book>) getArguments().getSerializable(ARG_BOOK_LIST);
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_recommend, container, false);
        
        RecyclerView recyclerView = view.findViewById(R.id.recommend_books_recycler);
        SimpleBookAdapter adapter = new SimpleBookAdapter(bookList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        
        // 设置书籍点击监听器
        adapter.setOnBookClickListener(book -> {
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
}