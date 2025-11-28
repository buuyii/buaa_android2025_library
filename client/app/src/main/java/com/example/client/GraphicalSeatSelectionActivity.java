package com.example.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GraphicalSeatSelectionActivity extends AppCompatActivity {

    private GridLayout seatGridLayout;
    private TextView floorTitleText;
    private AppDataBase db;
    private final Executor executor = Executors.newSingleThreadExecutor();

    private int selectedFloor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphical_seat_selection);

        db = AppDataBase.getInstance(this);
        seatGridLayout = findViewById(R.id.seat_grid_layout);
        floorTitleText = findViewById(R.id.floor_title_text);

        // 1. 从启动它的Intent中获取楼层号
        selectedFloor = getIntent().getIntExtra("FLOOR_NUMBER", 1); // 默认第一层

        floorTitleText.setText(String.format("楼层 %d - 座位图", selectedFloor));
        loadAndDisplaySeats();
    }

    private void loadAndDisplaySeats() {
        executor.execute(() -> {
            List<Seat> seats = db.seatDao().getSeatsByFloor(selectedFloor);
            runOnUiThread(() -> {
                seatGridLayout.removeAllViews();
                for (Seat seat : seats) {
                    seatGridLayout.addView(createSeatButton(seat));
                }
            });
        });
    }

    private View createSeatButton(Seat seat) {
        AppCompatButton seatButton = new AppCompatButton(this);
        seatButton.setText(String.valueOf(seat.seatNumber));

        // （请确保你的 colors.xml 中有这些颜色定义）
        switch (seat.status) {
            case "available":
                seatButton.setBackgroundColor(ContextCompat.getColor(this, R.color.seat_available));
                seatButton.setOnClickListener(v -> selectSeatAndFinish(seat));
                break;
            case "occupied":
                seatButton.setBackgroundColor(ContextCompat.getColor(this, R.color.seat_occupied));
                seatButton.setEnabled(false);
                break;
            case "reserved":
                seatButton.setBackgroundColor(ContextCompat.getColor(this, R.color.seat_reserved));
                seatButton.setEnabled(false);
                break;
        }

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.setMargins(8, 8, 8, 8);
        seatButton.setLayoutParams(params);
        return seatButton;
    }

    private void selectSeatAndFinish(Seat seat) {
        // 2. 将选择的座位号放入一个新的Intent中
        Intent resultIntent = new Intent();
        resultIntent.putExtra("SELECTED_SEAT_NUMBER", seat.seatNumber);

        // 3. 设置结果，并关闭当前Activity
        setResult(RESULT_OK, resultIntent);
        Toast.makeText(this, "已选择 " + seat.seatNumber + " 号座位", Toast.LENGTH_SHORT).show();
        finish(); // 关闭当前Activity，返回上一个界面
    }
}
