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

                seatViews.clear();
                for (Seat seat : seats) {
                    int resId = getResources().getIdentifier("seat_" + seat.seatNumber, "id", getPackageName());
                    View seatView = seatMapContainer.findViewById(resId);
                    if (seatView != null) {
                        seatViews.add(seatView);
                        if (seatView instanceof Button) {
                            Button seatButton = (Button) seatView;
                            seatButton.setEnabled("available".equals(seat.status));
                            if ("available".equals(seat.status)) {
                                seatButton.setOnClickListener(v -> selectSeatAndFinish(seat));
                            }
                        }
                    }
                }

                if (!seatViews.isEmpty() && !areInitialStatesStored) {
                    setupSeatPositioning();
                }
            });
        });
    }

    private void setupSeatPositioning() {
        seatMapContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (areInitialStatesStored) return;

                RectF imageRect = photoView.getDisplayRect();
                if (imageRect == null || imageRect.width() == 0) return;

                for (View seatView : seatViews) {
                    if (seatView.getWidth() == 0 || seatView.getHeight() == 0) return;
                }

                seatMapContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                initialImageScale = photoView.getScale();

                for (View seatView : seatViews) {
                    seatInitialLayoutPositions.put(seatView, new float[]{seatView.getX(), seatView.getY()});
                    float seatCenterX = seatView.getX() + seatView.getWidth() / 2f;
                    float seatCenterY = seatView.getY() + seatView.getHeight() / 2f;
                    float relativeX = (seatCenterX - imageRect.left) / imageRect.width();
                    float relativeY = (seatCenterY - imageRect.top) / imageRect.height();
                    seatRelativePositions.put(seatView, new float[]{relativeX, relativeY});
                }

                photoView.setOnMatrixChangeListener(currentRect -> updateSeatPositions());
                areInitialStatesStored = true;
                updateSeatPositions();
            }
        });
    }

    private void updateSeatPositions() {
        if (!areInitialStatesStored) return;

        RectF currentImageRect = photoView.getDisplayRect();
        if (currentImageRect == null) return;

        for (View seatView : seatViews) {
            float[] initialLayoutPos = seatInitialLayoutPositions.get(seatView);
            float[] relativePos = seatRelativePositions.get(seatView);
            if (initialLayoutPos == null || relativePos == null) continue;

            float relativeScale = photoView.getScale() / initialImageScale;

            float newCenterX = currentImageRect.left + relativePos[0] * currentImageRect.width();
            float newCenterY = currentImageRect.top + relativePos[1] * currentImageRect.height();

            float scaledWidth = seatView.getWidth() * relativeScale;
            float newX = newCenterX - (scaledWidth / 2f);

            float scaledHeight = seatView.getHeight() * relativeScale;
            float newY = newCenterY - (scaledHeight / 2f);

            float translationX = newX - initialLayoutPos[0];
            float translationY = newY - initialLayoutPos[1];

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
