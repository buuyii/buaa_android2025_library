package com.example.client;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface SeatDao {
    @Query("SELECT * FROM seats")
    List<Seat> getAllSeats();

    @Insert
    void insertAll(Seat... seats);

    @Query("UPDATE seats SET status = :status WHERE id = :seatId")
    void updateSeatStatus(int seatId, String status);

    @Query("DELETE FROM seats")
    void deleteAll();

    @Query("SELECT * FROM seats WHERE floor = :floor AND seatNumber = :number LIMIT 1")
    Seat findSeatByFloorAndNumber(int floor, int number);
}
