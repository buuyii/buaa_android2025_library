package com.example.client;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SeatDao {
    @Query("SELECT * FROM seats")
    List<Seat> getAllSeats();

    @Insert
    void insertAll(Seat... seats);

    @Update
    void update(Seat seat);

    @Query("UPDATE seats SET status = :status WHERE id = :seatId")
    void updateSeatStatus(int seatId, String status);

    @Query("DELETE FROM seats")
    void deleteAll();

    @Query("SELECT * FROM seats WHERE floor = :floor AND seatNumber = :number LIMIT 1")
    Seat findSeatByFloorAndNumber(int floor, int number);

    @Query("SELECT * FROM seats WHERE floor = :floor ORDER BY seatNumber ASC")
    List<Seat> getSeatsByFloor(int floor);

    @Query("SELECT * FROM seats WHERE status = :status")
    List<Seat> getSeatsByStatus(String status);
}
