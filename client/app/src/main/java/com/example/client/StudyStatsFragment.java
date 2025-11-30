package com.example.client;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StudyStatsFragment extends Fragment {

    private TextView dailyTotalTimeText;
    private TextView dailyStartTimeText;
    private TextView dailyEndTimeText;
    private TextView weeklyTotalTimeText;
    private StatisticsChartView weeklyChart;
    
    private AppDataBase db;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dayFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());

    public StudyStatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_study_stats, container, false);
        db = AppDataBase.getInstance(getContext());
        
        // 初始化视图组件
        dailyTotalTimeText = view.findViewById(R.id.daily_total_time);
        dailyStartTimeText = view.findViewById(R.id.daily_start_time);
        dailyEndTimeText = view.findViewById(R.id.daily_end_time);
        weeklyTotalTimeText = view.findViewById(R.id.weekly_total_time);
        weeklyChart = view.findViewById(R.id.weekly_chart);
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStats();
    }

    private void updateStats() {
        executor.execute(() -> {
            // 获取今天的日期范围
            Calendar todayCal = Calendar.getInstance();
            todayCal.set(Calendar.HOUR_OF_DAY, 0);
            todayCal.set(Calendar.MINUTE, 0);
            todayCal.set(Calendar.SECOND, 0);
            todayCal.set(Calendar.MILLISECOND, 0);
            Date todayStart = todayCal.getTime();
            
            todayCal.set(Calendar.HOUR_OF_DAY, 23);
            todayCal.set(Calendar.MINUTE, 59);
            todayCal.set(Calendar.SECOND, 59);
            todayCal.set(Calendar.MILLISECOND, 999);
            Date todayEnd = todayCal.getTime();
            
            // 获取本周的日期范围 (周一到周日)
            Calendar weekCal = Calendar.getInstance();
            weekCal.setFirstDayOfWeek(Calendar.MONDAY);
            weekCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            weekCal.set(Calendar.HOUR_OF_DAY, 0);
            weekCal.set(Calendar.MINUTE, 0);
            weekCal.set(Calendar.SECOND, 0);
            weekCal.set(Calendar.MILLISECOND, 0);
            Date weekStart = weekCal.getTime();
            
            weekCal.add(Calendar.DAY_OF_YEAR, 6);
            weekCal.set(Calendar.HOUR_OF_DAY, 23);
            weekCal.set(Calendar.MINUTE, 59);
            weekCal.set(Calendar.SECOND, 59);
            weekCal.set(Calendar.MILLISECOND, 999);
            Date weekEnd = weekCal.getTime();
            
            // 获取今天的学习记录
            List<StudyRecord> dailyRecords = db.studyRecordDao().getStudyRecordsBetween(todayStart, todayEnd);
            
            // 获取本周的学习记录
            List<StudyRecord> weeklyRecords = db.studyRecordDao().getStudyRecordsBetween(weekStart, weekEnd);
            
            // 获取当前正在进行的学习记录
            StudyRecord currentStudyRecord = db.studyRecordDao().getCurrentStudyRecordForStudent(1); // Assuming student ID is 1
            
            // 计算今日统计数据
            long dailyTotalMillis = 0;
            Date earliestStart = null;
            Date latestEnd = null;
            
            for (StudyRecord record : dailyRecords) {
                if (record.endTime != null && record.startTime != null) {
                    long duration = record.endTime.getTime() - record.startTime.getTime();
                    // Ensure non-negative duration
                    if (duration > 0) {
                        dailyTotalMillis += duration;
                    }
                    
                    if (earliestStart == null || record.startTime.before(earliestStart)) {
                        earliestStart = record.startTime;
                    }
                    
                    if (latestEnd == null || record.endTime.after(latestEnd)) {
                        latestEnd = record.endTime;
                    }
                }
            }
            
            // Add current active session to daily total if it's today
            if (currentStudyRecord != null && currentStudyRecord.startTime != null) {
                long currentTimeMillis = System.currentTimeMillis();
                long duration = currentTimeMillis - currentStudyRecord.startTime.getTime();
                // Ensure non-negative duration
                if (duration > 0) {
                    dailyTotalMillis += duration;
                }
                
                if (earliestStart == null || currentStudyRecord.startTime.before(earliestStart)) {
                    earliestStart = currentStudyRecord.startTime;
                }
                
                // Don't update latestEnd since the session is still ongoing
            }
            
            // Ensure non-negative total time
            double dailyTotalHours = Math.max(0, dailyTotalMillis / (1000.0 * 60 * 60));
            
            // 计算本周统计数据
            long weeklyTotalMillis = 0;
            Date weekEarliestStart = null;
            Date weekLatestEnd = null;
            
            // 按天统计学习时长
            long[] dailyHoursInMillis = new long[7]; // 周一到周日
            
            for (StudyRecord record : weeklyRecords) {
                if (record.endTime != null && record.startTime != null) {
                    long duration = record.endTime.getTime() - record.startTime.getTime();
                    // Ensure non-negative duration
                    if (duration > 0) {
                        weeklyTotalMillis += duration;
                    }
                    
                    if (weekEarliestStart == null || record.startTime.before(weekEarliestStart)) {
                        weekEarliestStart = record.startTime;
                    }
                    
                    if (weekLatestEnd == null || record.endTime.after(weekLatestEnd)) {
                        weekLatestEnd = record.endTime;
                    }
                    
                    // 确定该记录属于星期几
                    Calendar recordCal = Calendar.getInstance();
                    recordCal.setTime(record.startTime);
                    int dayOfWeek = recordCal.get(Calendar.DAY_OF_WEEK);
                    // 调整为周一=0, 周日=6
                    int dayIndex = (dayOfWeek + 5) % 7;
                    
                    // Ensure non-negative duration for daily breakdown
                    if (duration > 0) {
                        dailyHoursInMillis[dayIndex] += duration;
                    }
                }
            }
            
            // Add current active session to weekly total
            if (currentStudyRecord != null && currentStudyRecord.startTime != null) {
                long currentTimeMillis = System.currentTimeMillis();
                long duration = currentTimeMillis - currentStudyRecord.startTime.getTime();
                // Ensure non-negative duration
                if (duration > 0) {
                    weeklyTotalMillis += duration;
                }
                
                if (weekEarliestStart == null || currentStudyRecord.startTime.before(weekEarliestStart)) {
                    weekEarliestStart = currentStudyRecord.startTime;
                }
                
                // Add to today's daily breakdown
                Calendar recordCal = Calendar.getInstance();
                recordCal.setTime(currentStudyRecord.startTime);
                int dayOfWeek = recordCal.get(Calendar.DAY_OF_WEEK);
                // 调整为周一=0, 周日=6
                int dayIndex = (dayOfWeek + 5) % 7;
                // Ensure non-negative duration for daily breakdown
                if (duration > 0) {
                    dailyHoursInMillis[dayIndex] += duration;
                }
            }
            
            // Ensure non-negative total time
            double weeklyTotalHours = Math.max(0, weeklyTotalMillis / (1000.0 * 60 * 60));
            
            // 准备图表数据
            List<StatisticsChartView.ChartData> weeklyChartData = new ArrayList<>();
            String[] weekdays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
            int[] colors = {
                Color.parseColor("#2196F3"), // 普蓝色
                Color.parseColor("#2196F3"), // 普蓝色
                Color.parseColor("#2196F3"), // 普蓝色
                Color.parseColor("#2196F3"), // 普蓝色
                Color.parseColor("#2196F3"), // 普蓝色
                Color.parseColor("#2196F3"), // 普蓝色
                Color.parseColor("#2196F3")  // 普蓝色
            };
            
            for (int i = 0; i < 7; i++) {
                double hours = Math.max(0, dailyHoursInMillis[i] / (1000.0 * 60 * 60));
                weeklyChartData.add(new StatisticsChartView.ChartData(
                        weekdays[i], 
                        (float) hours, 
                        colors[i]));
            }
            
            // 更新UI
            updateUIData(dailyTotalHours, earliestStart, latestEnd, weeklyTotalHours, weeklyChartData);
        });
    }
    
    private void updateUIData(double dailyTotalHours, Date earliestStart, Date latestEnd, 
                              double weeklyTotalHours, List<StatisticsChartView.ChartData> weeklyChartData) {
        handler.post(() -> {
            // 更新日报信息
            dailyTotalTimeText.setText(String.format("今日总学习时长: %.1f 小时", dailyTotalHours));
            
            if (earliestStart != null) {
                dailyStartTimeText.setText(String.format("开始时间: %s", timeFormat.format(earliestStart)));
            } else {
                dailyStartTimeText.setText("开始时间: --:--");
            }
            
            if (latestEnd != null) {
                dailyEndTimeText.setText(String.format("结束时间: %s", timeFormat.format(latestEnd)));
            } else {
                dailyEndTimeText.setText("结束时间: --:--");
            }
            
            // 更新周报信息
            weeklyTotalTimeText.setText(String.format("本周总学习时长: %.1f 小时", weeklyTotalHours));
            
            // 设置图表数据（仅周报图表），固定使用柱状图
            weeklyChart.setChartData(weeklyChartData);
            weeklyChart.setChartType(StatisticsChartView.ChartType.BAR_CHART);
        });
    }
}