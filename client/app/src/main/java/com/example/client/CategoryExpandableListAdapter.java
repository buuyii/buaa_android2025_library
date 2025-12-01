// CategoryExpandableListAdapter.java
package com.example.client;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class CategoryExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> categoryList;
    private HashMap<String, List<Book>> categoryBooksMap;

    public CategoryExpandableListAdapter(Context context, List<String> categoryList,
                                       HashMap<String, List<Book>> categoryBooksMap) {
        this.context = context;
        this.categoryList = categoryList;
        this.categoryBooksMap = categoryBooksMap;
    }

    @Override
    public int getGroupCount() {
        return categoryList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return categoryBooksMap.get(categoryList.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return categoryList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return categoryBooksMap.get(categoryList.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String categoryName = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(android.R.layout.simple_expandable_list_item_1, null);
        }

        TextView categoryTextView = convertView.findViewById(android.R.id.text1);
        categoryTextView.setTypeface(null, Typeface.BOLD);
        categoryTextView.setText(categoryName);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Book book = (Book) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(android.R.layout.simple_expandable_list_item_2, null);
        }

        TextView titleTextView = convertView.findViewById(android.R.id.text1);
        TextView authorTextView = convertView.findViewById(android.R.id.text2);

        titleTextView.setText(book.getTitle());
        authorTextView.setText(book.getAuthor());

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}