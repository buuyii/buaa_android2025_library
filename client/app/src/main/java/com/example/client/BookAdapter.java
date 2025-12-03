// BookAdapter.java
package com.example.client;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import java.io.File;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import android.util.Log; // 解决 Log 类找不到的问题

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> bookList;
    private OnBookClickListener listener;

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    public BookAdapter(List<Book> bookList) {
        this.bookList = bookList != null ? bookList : new ArrayList<>();
    }

    public void setOnBookClickListener(OnBookClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.titleText.setText(book.getTitle());
        holder.authorText.setText("作者: " + book.getAuthor());
        holder.publisherText.setText("出版社: " + book.getPublisher());
        holder.yearText.setText("出版年份: " + book.getYear());

        // 设置默认封面图片
        //holder.coverImage.setImageResource(R.drawable.lib_logo);
        // 封面图片加载（核心修改）
        loadBookCover(holder.coverImage, book.getCoverResId());

        // 设置整个项目的点击监听器
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(book);
            }
        });
    }

    private void loadBookCover(ImageView imageView, int coverResId) { // 参数改为资源 ID
        // 直接通过资源 ID 加载 drawable 图片
        //imageView.setImageResource(coverResId);
        Log.d("COVER_DEBUG", "加载封面资源ID: " + coverResId); // 先打印实际值
        if (coverResId != -1) {
            imageView.setImageResource(coverResId);
            Log.d("COVER_DEBUG", "尝试加载封面:0 "); // 添加这行
        } else {
            // 无封面时的降级逻辑：比如显示占位图 / 隐藏 ImageView
            imageView.setImageResource(R.drawable.lib_logo);
            Log.d("COVER_DEBUG", "尝试加载默认封面:1 ");
            // 或 imageView.setVisibility(View.GONE);
        }
        // 若需用 Glide 加载（如添加过渡动画），也可：
        // Glide.with(imageView.getContext()).load(coverResId).into(imageView);
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public void updateList(List<Book> newList) {
        this.bookList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, authorText, publisherText, yearText;
        ImageView coverImage;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.book_title);
            authorText = itemView.findViewById(R.id.book_author);
            publisherText = itemView.findViewById(R.id.book_publisher);
            yearText = itemView.findViewById(R.id.book_year);
            coverImage = itemView.findViewById(R.id.book_cover);
        }
    }
}