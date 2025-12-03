package com.example.client;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GraphicalSeatSelectionActivity extends AppCompatActivity {

    private TextView floorTitleText;
    private PhotoView photoView;
    private SeatOverlayView seatOverlay;
    private AppDataBase db;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private int selectedFloor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphical_seat_selection);

        db = AppDataBase.getInstance(this);
        floorTitleText = findViewById(R.id.floor_title_text);
        photoView = findViewById(R.id.floor_background_image);
        seatOverlay = findViewById(R.id.seat_overlay);

        selectedFloor = getIntent().getIntExtra("FLOOR_NUMBER", 1);
        floorTitleText.setText(String.format("楼层 %d - 座位图", selectedFloor));

        updateSeatViewsFromDb();
    }

    private void updateSeatViewsFromDb() {
        executor.execute(() -> {
            List<Seat> seats = db.seatDao().getSeatsByFloor(selectedFloor);
            handler.post(() -> {
                if (isFinishing() || isDestroyed()) return;

                seatOverlay.bindPhotoView(photoView);
                seatOverlay.setSeats(seats);

                seatOverlay.setOnSeatClickListener(seat -> {
                    if (!seat.isAvailable()) {
                        Toast.makeText(this, "该座位不可用", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    seatOverlay.setSelectedSeat(seat);

                    // Use a handler to delay finishing the activity so the user can see the selection
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("SELECTED_SEAT_NUMBER", seat.seatNumber);
                        resultIntent.putExtra("SELECTED_FLOOR_NUMBER", selectedFloor);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }, 300); // 300ms delay

                    Toast.makeText(this, "已选择 " + seat.seatNumber + " 号座位", Toast.LENGTH_SHORT).show();
                });
            });
        });
    }
}
