package com.example.client;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "time_slots")
public class TimeSlot {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String startTime;
    public String endTime;

    public TimeSlot(String startTime, String endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
