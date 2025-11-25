package com.example.client;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface TimeSlotDao {
    @Query("SELECT * FROM time_slots")
    List<TimeSlot> getAllTimeSlots();

    @Insert
    void insertAll(TimeSlot... timeSlots);

    @Query("DELETE FROM time_slots")
    void deleteAll();
}
