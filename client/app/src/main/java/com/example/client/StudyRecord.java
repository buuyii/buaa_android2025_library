package com.example.client;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import java.util.Date;

@Entity(tableName = "study_records",
        foreignKeys = {
            @ForeignKey(entity = Student.class,
                        parentColumns = "id",
                        childColumns = "studentId"),
            @ForeignKey(entity = Seat.class,
                        parentColumns = "id",
                        childColumns = "seatId")
        })
public class StudyRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int studentId;
    public int seatId;
    public Date startTime;
    public Date endTime;

    public StudyRecord(int studentId, int seatId, Date startTime) {
        this.studentId = studentId;
        this.seatId = seatId;
        this.startTime = startTime;
    }
}
