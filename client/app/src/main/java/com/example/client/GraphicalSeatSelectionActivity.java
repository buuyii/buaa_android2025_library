package com.example.client;

import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.graphics.Matrix;
import android.graphics.drawable.Drawable;

public class GraphicalSeatSelectionActivity extends AppCompatActivity {
    private TextView floorTitleText;
    private ConstraintLayout seatMapContainer;
    private PhotoView photoView;
    private AppDataBase db;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int selectedFloor;
    // --- V4: Final Robust Seat Positioning Logic ---
    private final List<View> seatViews = new ArrayList<>();
    private final Map<View, float[]> seatInitialLayoutPositions = new HashMap<>();
    private final Map<View, float[]> seatRelativePositions = new HashMap<>();
    private float initialImageScale = 1.0f;
    private boolean areInitialStatesStored = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphical_seat_selection);
        db = AppDataBase.getInstance(this);
        floorTitleText = findViewById(R.id.floor_title_text);
        seatMapContainer = findViewById(R.id.seat_map_container);
        photoView = findViewById(R.id.floor_background_image);
        selectedFloor = getIntent().getIntExtra("FLOOR_NUMBER", 1);
        floorTitleText.setText(String.format("楼层 %d - 座位图", selectedFloor));
        updateSeatViewsFromDb();
    }

    private void updateSeatViewsFromDb() {
        executor.execute(() -> {
            List<Seat> seats = db.seatDao().getSeatsByFloor(selectedFloor);
            handler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                SeatOverlayView overlay = findViewById(R.id.seat_overlay);
                overlay.bindPhotoView(photoView);
                overlay.setSeats(seats);
                overlay.setOnSeatClickListener(seat -> {
                    // 用户点击座位后的操作
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("SELECTED_SEAT_NUMBER", seat.seatNumber);
                    setResult(RESULT_OK, resultIntent);
                    Toast.makeText(this, "已选择 " + seat.seatNumber + " 号座位", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        });
    }

    private void setupSeatPositioning() {
        seatMapContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (areInitialStatesStored) return;
                Drawable drawable = photoView.getDrawable();
                RectF imageRect = photoView.getDisplayRect();
                if (drawable == null || imageRect == null || imageRect.width() == 0)
                    return;
                // 确保所有 seatView 都已经测量
                for (View seatView : seatViews) {
                    if (seatView.getWidth() == 0 || seatView.getHeight() == 0) return;
                }
                seatMapContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                // 存储初始布局位置和尺寸（相对于父容器）
                for (View seatView : seatViews) {
                    seatInitialLayoutPositions.put(seatView, new float[]{seatView.getX(), seatView.getY(), seatView.getWidth(), seatView.getHeight()});
                }
                // 将每个 seat 的中心从“父容器坐标”转换为“drawable 像素坐标”，然后标准化为 0..1
                final int drawableW = drawable.getIntrinsicWidth();
                final int drawableH = drawable.getIntrinsicHeight();
                // 需要用 imageMatrix 的逆矩阵：view坐标 -> drawable坐标
                Matrix imageMatrix = new Matrix(photoView.getImageMatrix());
                Matrix inverse = new Matrix();
                if (!imageMatrix.invert(inverse)) {
                    // 不能反转（极少见），退回使用 displayRect 的归一化（不推荐）
                    return;
                }
                for (View seatView : seatViews) {
                    float seatCenterX_inParent = seatView.getX() + seatView.getWidth() / 2f;
                    float seatCenterY_inParent = seatView.getY() + seatView.getHeight() / 2f;
                    // 转换为相对于 photoView 的本地坐标（view内部坐标）
                    float localX = seatCenterX_inParent - photoView.getLeft();
                    float localY = seatCenterY_inParent - photoView.getTop();
                    float[] pts = new float[]{localX, localY};
                    inverse.mapPoints(pts);
                    // 现在 pts 是 drawable 内的像素坐标
                    float normX = pts[0] / (float) drawableW;
                    float normY = pts[1] / (float) drawableH;
                    seatRelativePositions.put(seatView, new float[]{normX, normY});
                }
                initialImageScale = photoView.getScale();
                photoView.setOnMatrixChangeListener(currentRect -> updateSeatPositions());
                areInitialStatesStored = true;
                updateSeatPositions();
            }
        });
    }

    private void updateSeatPositions() {
        if (!areInitialStatesStored) return;
        Drawable drawable = photoView.getDrawable();
        if (drawable == null) return;
        int drawableW = drawable.getIntrinsicWidth();
        int drawableH = drawable.getIntrinsicHeight();
        Matrix imageMatrix = photoView.getImageMatrix();
        float currentScale = photoView.getScale();
        // PhotoView 提供的 scale
        for (View seatView : seatViews) {
            float[] initialLayout = seatInitialLayoutPositions.get(seatView);
            float[] relative = seatRelativePositions.get(seatView);
            if (initialLayout == null || relative == null) continue;
            // 把标准化的 drawable 点映射到 view 坐标
            float[] pts = new float[]{relative[0] * drawableW, relative[1] * drawableH};
            imageMatrix.mapPoints(pts);
            // 现在 pts 是相对于 photoView 的 view 坐标（局部）
            // 转换到父容器坐标（container）
            float newCenterX_inParent = photoView.getLeft() + pts[0];
            float newCenterY_inParent = photoView.getTop() + pts[1];
            // 计算缩放比例（相对于初始）
            float relativeScale = currentScale / initialImageScale;
            float initialWidth = initialLayout[2];
            float initialHeight = initialLayout[3];
            float scaledWidth = initialWidth * relativeScale;
            float scaledHeight = initialHeight * relativeScale;
            float newX = newCenterX_inParent - scaledWidth / 2f;
            float newY = newCenterY_inParent - scaledHeight / 2f;
            float translationX = newX - initialLayout[0];
            float translationY = newY - initialLayout[1];
            seatView.setTranslationX(translationX);
            seatView.setTranslationY(translationY);
            seatView.setScaleX(relativeScale);
            seatView.setScaleY(relativeScale);
        }
    }

    private void selectSeatAndFinish(Seat seat) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("SELECTED_SEAT_NUMBER", seat.seatNumber);
        setResult(RESULT_OK, resultIntent);
        Toast.makeText(this, "已选择 " + seat.seatNumber + " 号座位", Toast.LENGTH_SHORT).show();
        finish();
    }
}