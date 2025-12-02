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

public class ReservationRecordAdapter extends RecyclerView.Adapter<ReservationRecordAdapter.ViewHolder> {
    private List<ReservationRecord> reservationRecords;
    private List<Seat> seats;
    private List<TimeSlot> timeSlots;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public ReservationRecordAdapter(List<ReservationRecord> reservationRecords, List<Seat> seats, List<TimeSlot> timeSlots) {
        this.reservationRecords = reservationRecords;
        this.seats = seats;
        this.timeSlots = timeSlots;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReservationRecord record = reservationRecords.get(position);
        
        // 查找对应的座位和时间段
        Seat seat = findSeatById(record.seatId);
        TimeSlot timeSlot = findTimeSlotById(record.timeSlotId);
        
        // 显示座位信息
        if (seat != null) {
            holder.seatInfo.setText("座位: " + seat.floor + "楼" + seat.seatNumber + "号");
        } else {
            holder.seatInfo.setText("座位: 未知");
        }
        
        // 显示签到时间（预约时间）
        if (record.reservationDate != null) {
            holder.checkInTime.setText("预约时间: " + dateFormat.format(record.reservationDate));
        } else {
            holder.checkInTime.setText("预约时间: 未知");
        }
        
        // 显示签退时间（暂时显示为时间段结束）
        if (timeSlot != null) {
            holder.checkOutTime.setText("时间段: " + timeSlot.toString());
        } else {
            holder.checkOutTime.setText("时间段: 未知");
        }
    }

    @Override
    public int getItemCount() {
        return reservationRecords.size();
    }

    private Seat findSeatById(int seatId) {
        for (Seat seat : seats) {
            if (seat.id == seatId) {
                return seat;
            }
        }
        return null;
    }
    
    private TimeSlot findTimeSlotById(int timeSlotId) {
        for (TimeSlot timeSlot : timeSlots) {
            if (timeSlot.id == timeSlotId) {
                return timeSlot;
            }
        }
        return null;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView seatInfo;
        TextView checkInTime;
        TextView checkOutTime;

        ViewHolder(View itemView) {
            super(itemView);
            seatInfo = itemView.findViewById(R.id.seat_info);
            checkInTime = itemView.findViewById(R.id.check_in_time);
            checkOutTime = itemView.findViewById(R.id.check_out_time);
        }
    }
}