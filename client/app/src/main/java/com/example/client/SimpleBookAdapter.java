package com.example.client;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SimpleBookAdapter extends RecyclerView.Adapter<SimpleBookAdapter.SimpleBookViewHolder> {

    private List<Book> bookList;
    private OnBookClickListener listener;
    private int activePosition = -1;

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    public SimpleBookAdapter(List<Book> bookList) {
        this.bookList = bookList;
    }

    public void setOnBookClickListener(OnBookClickListener listener) {
        this.listener = listener;
    }

    public void setActivePosition(int position) {
        int previousActivePosition = activePosition;
        activePosition = position;
        
        // 更新之前活跃项和当前活跃项的显示
        if (previousActivePosition >= 0 && previousActivePosition < bookList.size()) {
            notifyItemChanged(previousActivePosition);
        }
        if (activePosition >= 0 && activePosition < bookList.size()) {
            notifyItemChanged(activePosition);
        }
    }

    @NonNull
    @Override
    public SimpleBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_simple_book, parent, false);
        return new SimpleBookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SimpleBookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.titleText.setText(book.getTitle());
        holder.indexText.setText((position + 1) + ".");

        // 根据是否为活跃项设置不同的字体大小和样式
        if (position == activePosition) {
            holder.titleText.setTextSize(18); // 放大字体
            holder.titleText.setTypeface(null, Typeface.BOLD); // 加粗
            holder.indexText.setTextSize(18); // 放大序号字体
            holder.indexText.setTypeface(null, Typeface.BOLD); // 加粗序号
            // 显示活动指示器
            holder.activeIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.titleText.setTextSize(16); // 正常字体
            holder.titleText.setTypeface(null, Typeface.NORMAL); // 正常粗细
            holder.indexText.setTextSize(16); // 正常序号字体
            holder.indexText.setTypeface(null, Typeface.NORMAL); // 正常序号粗细
            // 隐藏活动指示器
            holder.activeIndicator.setVisibility(View.INVISIBLE);
        }

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

    static class SimpleBookViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView indexText;
        View activeIndicator;

        SimpleBookViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.book_title);
            indexText = itemView.findViewById(R.id.book_index);
            activeIndicator = itemView.findViewById(R.id.active_indicator);
        }
    }
}