package com.example.client;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Student.class, Seat.class, TimeSlot.class, ReservationRecord.class, StudyRecord.class}, version = 6, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDataBase extends RoomDatabase {
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private static volatile AppDataBase db;

    static AppDataBase getInstance(Context context) {
        if (db == null) {
            synchronized (AppDataBase.class) {
                if (db == null) {
                    db = Room.databaseBuilder(context.getApplicationContext(),
                            AppDataBase.class, "dbRoom")
                            .fallbackToDestructiveMigration() // This will clear the db on version change
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return db;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase _db) {
            super.onCreate(_db);
            databaseWriteExecutor.execute(() -> {
                SeatDao seatDao = db.seatDao();
                TimeSlotDao timeSlotDao = db.timeSlotDao();

                seatDao.deleteAll();
                timeSlotDao.deleteAll();

                // Populate time slots
                timeSlotDao.insertAll(
                    new TimeSlot("07:00", "12:00"),
                    new TimeSlot("12:00", "17:00"),
                    new TimeSlot("17:00", "23:00")
                );

                // Populate 64 seats per floor in an 8x8 grid
                for (int floor = 1; floor <= 6; floor++) {
                    for (int i = 0; i < 64; i++) {
                        int row = i / 8;
                        int col = i % 8;
                        int seatNumber = i + 1;

                        // Normalize coordinates to be between 0.1 and 0.9 to avoid being too close to the edge
                        float relativeX = 0.1f + (col / 7.0f) * 0.8f;
                        float relativeY = 0.1f + (row / 7.0f) * 0.8f;

                        seatDao.insertAll(new Seat(floor, seatNumber, "available", relativeX, relativeY));
                    }
                }
            });
        }
    };

    public abstract StudentDao studentDao();

    public abstract SeatDao seatDao();

    public abstract TimeSlotDao timeSlotDao();

    public abstract ReservationRecordDao reservationRecordDao();

    public abstract StudyRecordDao studyRecordDao();
}
