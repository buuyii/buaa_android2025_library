package com.example.client;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class StudyRecordAdapter extends RecyclerView.Adapter<StudyRecordAdapter.ViewHolder> {
    private List<StudyRecord> studyRecords;
    private List<Seat> seats;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public StudyRecordAdapter(List<StudyRecord> studyRecords, List<Seat> seats) {
        this.studyRecords = studyRecords;
        this.seats = seats;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_study_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudyRecord record = studyRecords.get(position);
        
        // 查找对应的座位
        Seat seat = findSeatById(record.seatId);
        
        // 显示座位信息
        if (seat != null) {
            holder.seatInfo.setText("座位: " + seat.floor + "楼" + seat.seatNumber + "号");
        } else {
            holder.seatInfo.setText("座位: 未知");
        }
        
        // 显示签到时间
        if (record.startTime != null) {
            holder.checkInTime.setText("签到时间: " + dateFormat.format(record.startTime));
        } else {
            holder.checkInTime.setText("签到时间: 未知");
        }
        
        // 显示签退时间
        if (record.endTime != null) {
            holder.checkOutTime.setText("签退时间: " + dateFormat.format(record.endTime));
            
            // 计算学习时长
            long durationMillis = record.endTime.getTime() - record.startTime.getTime();
            long minutes = durationMillis / (1000 * 60);
            long hours = minutes / 60;
            minutes = minutes % 60;
            holder.studyDuration.setText(String.format("学习时长: %d小时%d分钟", hours, minutes));
        } else {
            holder.checkOutTime.setText("签退时间: 进行中");
            holder.studyDuration.setText("学习时长: 进行中");
        }
    }

    @Override
    public int getItemCount() {
        return studyRecords.size();
    }

    private Seat findSeatById(int seatId) {
        for (Seat seat : seats) {
            if (seat.id == seatId) {
                return seat;
            }
        }
        return null;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView seatInfo;
        TextView checkInTime;
        TextView checkOutTime;
        TextView studyDuration;

        ViewHolder(View itemView) {
            super(itemView);
            seatInfo = itemView.findViewById(R.id.seat_info);
            checkInTime = itemView.findViewById(R.id.check_in_time);
            checkOutTime = itemView.findViewById(R.id.check_out_time);
            studyDuration = itemView.findViewById(R.id.study_duration);
        }
    }
}