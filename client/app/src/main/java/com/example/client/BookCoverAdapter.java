package com.example.client;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookCoverAdapter extends RecyclerView.Adapter<BookCoverAdapter.BookCoverViewHolder> {

    private List<Book> bookList;
    private OnBookClickListener listener;

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    public BookCoverAdapter(List<Book> bookList) {
        this.bookList = bookList;
    }

    public void setOnBookClickListener(OnBookClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookCoverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book_cover, parent, false);
        return new BookCoverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookCoverViewHolder holder, int position) {
        Book book = bookList.get(position);
        
        // 设置封面图片
        if (book.getCoverResId() != -1) {
            holder.coverImage.setImageResource(book.getCoverResId());
        }
        
        // 设置书名
        holder.titleText.setText(book.getTitle());
        
        // 设置点击事件
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

    static class BookCoverViewHolder extends RecyclerView.ViewHolder {
        ImageView coverImage;
        TextView titleText;

        BookCoverViewHolder(@NonNull View itemView) {
            super(itemView);
            coverImage = itemView.findViewById(R.id.book_cover_image);
            titleText = itemView.findViewById(R.id.book_title);
        }
    }
}