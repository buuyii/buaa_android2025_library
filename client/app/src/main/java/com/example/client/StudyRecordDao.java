package com.example.client;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface StudyRecordDao {
    @Insert
    void insert(StudyRecord studyRecord);

    @Update
    void update(StudyRecord studyRecord);

    @Query("SELECT * FROM study_records WHERE studentId = :studentId AND endTime IS NULL")
    StudyRecord getCurrentStudyRecordForStudent(int studentId);

    @Query("SELECT * FROM study_records WHERE studentId = :studentId AND seatId = :seatId AND endTime IS NULL")
    StudyRecord findActiveStudyRecordByStudentAndSeat(int studentId, int seatId);
}
