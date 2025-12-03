package com.example.client;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "seats")
public class Seat {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int floor;
    public int seatNumber;

    /**
     * "available", "reserved", "occupied"
     */
    public String status;

    //归一化坐标，相对于drawable,0...1
    public float relativeX;
    public float relativeY;

    // Empty constructor for Room
    public Seat() {}

    @Ignore
    public Seat(int floor, int seatNumber, String status) {
        this.floor = floor;
        this.seatNumber = seatNumber;
        this.status = status;
    }

    // 新的构造器（用于初始化含坐标的 seat）
    @Ignore
    public Seat(int floor, int seatNumber, String status, float relativeX, float relativeY) {
        this.floor = floor;
        this.seatNumber = seatNumber;
        this.status = status;
        this.relativeX = relativeX;
        this.relativeY = relativeY;
    }

    // helper
    public boolean isAvailable() {
        return "available".equals(status);
    }
}
