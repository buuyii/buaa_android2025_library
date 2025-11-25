package com.example.client;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.Date;
import java.util.List;

@Dao
public interface ReservationRecordDao {
    @Insert
    void insert(ReservationRecord reservationRecord);

    @Query("SELECT * FROM reservation_records WHERE studentId = :studentId")
    List<ReservationRecord> getReservationsForStudent(int studentId);

    @Query("SELECT * FROM reservation_records WHERE reservationDate = :date")
    List<ReservationRecord> getReservationsByDate(Date date);

    @Query("DELETE FROM reservation_records WHERE studentId = :studentId AND seatId = :seatId AND timeSlotId = :timeSlotId AND reservationDate = :reservationDate")
    void deleteReservation(int studentId, int seatId, int timeSlotId, Date reservationDate);

    @Query("SELECT * FROM reservation_records WHERE studentId = :studentId AND reservationDate = :reservationDate LIMIT 1")
    ReservationRecord findReservationByUserAndDate(int studentId, Date reservationDate);

    @Query("SELECT * FROM reservation_records WHERE seatId = :seatId AND timeSlotId = :timeSlotId AND reservationDate = :reservationDate")
    List<ReservationRecord> findReservationsForSeat(int seatId, int timeSlotId, Date reservationDate);
}
