// BookDetailFragment.java
package com.example.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class BookDetailFragment extends Fragment {

    private static final String ARG_BOOK = "book";
    private Book book;

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
        Button backButton = view.findViewById(R.id.back_button);

        if (book != null) {
            titleText.setText(book.getTitle());
            authorText.setText("作者: " + book.getAuthor());
            publisherText.setText("出版社: " + book.getPublisher());
            yearText.setText("出版年份: " + book.getYear());
            descriptionText.setText(book.getDescription()); // 设置简介内容
        }

        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return view;
    }
}