package com.example.client;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StudyStatsFragment extends Fragment {

    private TextView totalTimeText;
    private AppDataBase db;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public StudyStatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_study_stats, container, false);
        db = AppDataBase.getInstance(getContext());
        totalTimeText = view.findViewById(R.id.total_time_text);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStats();
    }

    private void updateStats() {
        executor.execute(() -> {
            // Calculate the start and end of the current week (Monday to Sunday)
            Calendar cal = Calendar.getInstance();
            // Set the calendar to the first day of the week (Monday)
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date weekStart = cal.getTime();

            // Move to the end of the week (Sunday)
            cal.add(Calendar.DAY_OF_YEAR, 6);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            Date weekEnd = cal.getTime();

            // Fetch records for the current week
            List<StudyRecord> weeklyRecords = db.studyRecordDao().getStudyRecordsBetween(weekStart, weekEnd);

            long totalMillis = 0;
            for (StudyRecord record : weeklyRecords) {
                if (record.endTime != null && record.startTime != null) {
                    totalMillis += (record.endTime.getTime() - record.startTime.getTime());
                }
            }

            // Convert milliseconds to hours
            double totalHours = totalMillis / (1000.0 * 60 * 60);

            handler.post(() -> {
                totalTimeText.setText(String.format("本周总学习时长: %.1f 小时", totalHours));
            });
        });
    }
}
