package com.example.client;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;


import java.util.ArrayList;

public class StudyStatsFragment extends Fragment {

    private TextView totalTimeText;

    public StudyStatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_study_stats, container, false);

        totalTimeText = view.findViewById(R.id.total_time_text);

        updateStats();

        return view;
    }

    private void updateStats() {
        // 计算总学习时间
        float totalHours = 2.5f + 3.2f + 4.0f + 2.8f + 5.1f + 6.3f + 4.2f;
        totalTimeText.setText(String.format("本周总学习时长: %.1f 小时", totalHours));
    }
}
