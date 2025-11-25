package com.example.client;

import androidx.room.Entity;
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

    public Seat(int floor, int seatNumber, String status) {
        this.floor = floor;
        this.seatNumber = seatNumber;
        this.status = status;
    }
}
