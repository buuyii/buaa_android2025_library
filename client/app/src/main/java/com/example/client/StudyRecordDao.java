package com.example.client;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.Date;
import java.util.List;

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

    @Query("SELECT * FROM study_records WHERE endTime IS NULL")
    List<StudyRecord> getAllActiveStudyRecords();

    @Query("SELECT * FROM study_records WHERE startTime BETWEEN :startDate AND :endDate AND endTime IS NOT NULL")
    List<StudyRecord> getStudyRecordsBetween(Date startDate, Date endDate);
    
    @Query("SELECT * FROM study_records WHERE studentId = :studentId ORDER BY startTime DESC")
    List<StudyRecord> getAllStudyRecordsForStudent(int studentId);
}