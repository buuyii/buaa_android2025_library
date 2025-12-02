package com.example.client;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookPagerAdapter extends RecyclerView.Adapter<BookPagerAdapter.BookPagerViewHolder> {

    private List<Book> bookList;
    private OnBookClickListener listener;

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    public BookPagerAdapter(List<Book> bookList) {
        this.bookList = bookList;
    }

    public void setOnBookClickListener(OnBookClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookPagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book_pager, parent, false);
        return new BookPagerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookPagerViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.titleText.setText(book.getTitle());
        holder.authorText.setText("作者: " + book.getAuthor());
        holder.publisherText.setText("出版社: " + book.getPublisher());
        holder.yearText.setText("出版年份: " + book.getYear());
        
        // 设置图书简介
        if (book.getDescription() != null) {
            holder.descriptionText.setText(book.getDescription());
        } else {
            holder.descriptionText.setText("暂无简介");
        }

        // 设置默认封面图片
        holder.coverImage.setImageResource(R.drawable.lib_logo);

        // 设置整个项目的点击监听器
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    static class BookPagerViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, authorText, publisherText, yearText, descriptionText;
        ImageView coverImage;

        BookPagerViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.book_title);
            authorText = itemView.findViewById(R.id.book_author);
            publisherText = itemView.findViewById(R.id.book_publisher);
            yearText = itemView.findViewById(R.id.book_year);
            descriptionText = itemView.findViewById(R.id.book_description);
            coverImage = itemView.findViewById(R.id.book_cover);
        }
    }
}