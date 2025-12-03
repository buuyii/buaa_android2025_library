package com.example.client;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// 数据库版本已提升至 7，以确保 onCreate 会被调用
@Database(entities = {Student.class, Seat.class, TimeSlot.class, ReservationRecord.class, StudyRecord.class}, version = 7, exportSchema = false)
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
                            .fallbackToDestructiveMigration() // 当版本号增加时，会删除并重建数据库
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

                // 填充时间段数据
                timeSlotDao.insertAll(
                    new TimeSlot("07:00", "12:00"),
                    new TimeSlot("12:00", "17:00"),
                    new TimeSlot("17:00", "23:00")
                );

                // 使用新的方法为每个楼层填充带有精确坐标的座位数据
                for (int floor = 1; floor <= 6; floor++) {
                    List<Seat> seatsForFloor = getInitialSeatsForFloor(floor);
                    seatDao.insertAll(seatsForFloor.toArray(new Seat[0]));
                }
            });
        }
    };

    /**
     * 为指定楼层提供包含精确坐标的初始座位列表。
     * @param floor 楼层号
     * @return 该楼层的座位列表
     */
    private static List<Seat> getInitialSeatsForFloor(int floor) {
        List<Seat> seats = new ArrayList<>();

        // =================================================================================================
        // 请将您获取的64个座位的坐标数据粘贴到下方区域
        //
        // 数据格式:
        // seats.add(new Seat(floor, [座位号], "available", [X坐标]f, [Y坐标]f));
        //
        // 坐标系说明:
        // (0.0f, 0.0f) 代表平面图的左上角
        // (1.0f, 1.0f) 代表平面图的右下角
        //
        // 示例:
        // seats.add(new Seat(floor, 1, "available", 0.123f, 0.456f));
        // seats.add(new Seat(floor, 2, "available", 0.180f, 0.456f));
        // ... 直到座位64
        // =================================================================================================

        // ↓↓↓ 请在这里开始粘贴您的64个座位数据 ↓↓↓
        seats.add(new Seat(floor, 1, "available", 0.232f, 0.235f));
        seats.add(new Seat(floor, 2, "available", 0.234f, 0.282f));
        seats.add(new Seat(floor, 3, "available", 0.234f, 0.357f));
        seats.add(new Seat(floor, 4, "available", 0.233f, 0.411f));
        seats.add(new Seat(floor, 5, "available", 0.234f, 0.522f));
        seats.add(new Seat(floor, 6, "available", 0.233f, 0.574f));
        seats.add(new Seat(floor, 7, "available", 0.234f, 0.644f));
        seats.add(new Seat(floor, 8, "available", 0.233f, 0.691f));
        seats.add(new Seat(floor, 9, "available", 0.300f, 0.233f));
        seats.add(new Seat(floor, 10, "available", 0.298f, 0.284f));
        seats.add(new Seat(floor, 11, "available", 0.300f, 0.358f));
        seats.add(new Seat(floor, 12, "available", 0.301f, 0.405f));
        seats.add(new Seat(floor, 13, "available", 0.301f, 0.524f));
        seats.add(new Seat(floor, 14, "available", 0.300f, 0.574f));
        seats.add(new Seat(floor, 15, "available", 0.298f, 0.646f));
        seats.add(new Seat(floor, 16, "available", 0.300f, 0.693f));
        seats.add(new Seat(floor, 17, "available", 0.376f, 0.233f));
        seats.add(new Seat(floor, 18, "available", 0.379f, 0.284f));
        seats.add(new Seat(floor, 19, "available", 0.380f, 0.355f));
        seats.add(new Seat(floor, 20, "available", 0.377f, 0.408f));
        seats.add(new Seat(floor, 21, "available", 0.377f, 0.527f));
        seats.add(new Seat(floor, 22, "available", 0.380f, 0.575f));
        seats.add(new Seat(floor, 23, "available", 0.379f, 0.646f));
        seats.add(new Seat(floor, 24, "available", 0.379f, 0.693f));
        seats.add(new Seat(floor, 25, "available", 0.447f, 0.236f));
        seats.add(new Seat(floor, 26, "available", 0.446f, 0.286f));
        seats.add(new Seat(floor, 27, "available", 0.444f, 0.356f));
        seats.add(new Seat(floor, 28, "available", 0.445f, 0.406f));
        seats.add(new Seat(floor, 29, "available", 0.446f, 0.522f));
        seats.add(new Seat(floor, 30, "available", 0.445f, 0.572f));
        seats.add(new Seat(floor, 31, "available", 0.445f, 0.643f));
        seats.add(new Seat(floor, 32, "available", 0.446f, 0.690f));
        seats.add(new Seat(floor, 33, "available", 0.529f, 0.237f));
        seats.add(new Seat(floor, 34, "available", 0.531f, 0.286f));
        seats.add(new Seat(floor, 35, "available", 0.531f, 0.356f));
        seats.add(new Seat(floor, 36, "available", 0.529f, 0.408f));
        seats.add(new Seat(floor, 37, "available", 0.529f, 0.525f));
        seats.add(new Seat(floor, 38, "available", 0.533f, 0.574f));
        seats.add(new Seat(floor, 39, "available", 0.530f, 0.643f));
        seats.add(new Seat(floor, 40, "available", 0.529f, 0.697f));
        seats.add(new Seat(floor, 41, "available", 0.596f, 0.237f));
        seats.add(new Seat(floor, 42, "available", 0.595f, 0.288f));
        seats.add(new Seat(floor, 43, "available", 0.596f, 0.362f));
        seats.add(new Seat(floor, 44, "available", 0.596f, 0.406f));
        seats.add(new Seat(floor, 45, "available", 0.594f, 0.522f));
        seats.add(new Seat(floor, 46, "available", 0.596f, 0.575f));
        seats.add(new Seat(floor, 47, "available", 0.597f, 0.645f));
        seats.add(new Seat(floor, 48, "available", 0.594f, 0.693f));
        seats.add(new Seat(floor, 49, "available", 0.668f, 0.237f));
        seats.add(new Seat(floor, 50, "available", 0.670f, 0.286f));
        seats.add(new Seat(floor, 51, "available", 0.670f, 0.358f));
        seats.add(new Seat(floor, 52, "available", 0.668f, 0.408f));
        seats.add(new Seat(floor, 53, "available", 0.668f, 0.525f));
        seats.add(new Seat(floor, 54, "available", 0.670f, 0.577f));
        seats.add(new Seat(floor, 55, "available", 0.671f, 0.640f));
        seats.add(new Seat(floor, 56, "available", 0.670f, 0.691f));
        seats.add(new Seat(floor, 57, "available", 0.735f, 0.237f));
        seats.add(new Seat(floor, 58, "available", 0.734f, 0.282f));
        seats.add(new Seat(floor, 59, "available", 0.734f, 0.357f));
        seats.add(new Seat(floor, 60, "available", 0.735f, 0.406f));
        seats.add(new Seat(floor, 61, "available", 0.734f, 0.524f));
        seats.add(new Seat(floor, 62, "available", 0.733f, 0.574f));
        seats.add(new Seat(floor, 63, "available", 0.735f, 0.646f));
        seats.add(new Seat(floor, 64, "available", 0.735f, 0.691f));



        // ↑↑↑ 请在上方区域粘贴您的64个座位数据 ↑↑↑

        return seats;
    }

    public abstract StudentDao studentDao();

    public abstract SeatDao seatDao();

    public abstract TimeSlotDao timeSlotDao();

    public abstract ReservationRecordDao reservationRecordDao();

    public abstract StudyRecordDao studyRecordDao();
}
