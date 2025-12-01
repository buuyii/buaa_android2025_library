package com.example.client;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import java.util.Date;

@Entity(tableName = "reservation_records",
        foreignKeys = {
            @ForeignKey(entity = Student.class,
                        parentColumns = "id",
                        childColumns = "studentId"),
            @ForeignKey(entity = Seat.class,
                        parentColumns = "id",
                        childColumns = "seatId"),
            @ForeignKey(entity = TimeSlot.class,
                        parentColumns = "id",
                        childColumns = "timeSlotId")
        })
public class ReservationRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int studentId;
    public int seatId;
    public int timeSlotId;
    public Date reservationDate;
    public long reservationTimestamp; // To store the exact moment of reservation

    public ReservationRecord(int studentId, int seatId, int timeSlotId, Date reservationDate, long reservationTimestamp) {
        this.studentId = studentId;
        this.seatId = seatId;
        this.timeSlotId = timeSlotId;
        this.reservationDate = reservationDate;
        this.reservationTimestamp = reservationTimestamp;
    }
}
