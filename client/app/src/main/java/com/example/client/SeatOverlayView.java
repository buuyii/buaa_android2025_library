package com.example.client;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SeatOverlayView extends View {

    // --- Callbacks ---
    public interface OnSeatClickListener {
        void onSeatClicked(Seat seat);
    }

    public interface OnSeatMoveListener {
        void onSeatMoved(Seat seat);
    }

    private OnSeatClickListener clickListener;
    private OnSeatMoveListener moveListener;

    public void setOnSeatClickListener(OnSeatClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnSeatMoveListener(OnSeatMoveListener listener) {
        this.moveListener = listener;
    }

    // -- View State --
    private List<Seat> seats = new ArrayList<>();
    private final Paint seatPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private PhotoView photoView;
    private Seat selectedSeat = null;

    // -- Configuration --
    private final float seatBlockSize = 20f;
    private final float clickPadding = 10f;
    private final float seatNumberTextSize = 12f;

    // -- Edit & Drag State --
    private boolean isEditMode = false;
    private Seat seatBeingDragged = null;

    public SeatOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setSeats(List<Seat> seatList) {
        this.seats = seatList;
        this.selectedSeat = null;
        invalidate();
    }

    public void setSelectedSeat(Seat seat) {
        this.selectedSeat = seat;
        invalidate();
    }

    public void bindPhotoView(PhotoView pv) {
        this.photoView = pv;
        pv.setOnMatrixChangeListener(rect -> invalidate());
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
        if (photoView != null) {
            photoView.setZoomable(!isEditMode);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (photoView == null || seats == null || photoView.getDisplayRect() == null) return;

        final RectF viewRect = photoView.getDisplayRect();
        final float scale = photoView.getScale();

        for (Seat seat : seats) {
            float x = viewRect.left + seat.relativeX * viewRect.width();
            float y = viewRect.top + seat.relativeY * viewRect.height();
            float halfSize = (seatBlockSize / 2f) * scale;

            // Set color based on availability and selection
            if (selectedSeat != null && Objects.equals(seat.id, selectedSeat.id)) {
                seatPaint.setColor(Color.GREEN); // Currently selected
            } else if (seat.isAvailable()) {
                seatPaint.setColor(Color.BLUE);  // Available
            } else {
                seatPaint.setColor(Color.RED);   // Occupied
            }

            seatPaint.setAlpha(isEditMode ? 200 : 255);
            canvas.drawRect(x - halfSize, y - halfSize, x + halfSize, y + halfSize, seatPaint);

            // Draw seat number
            textPaint.setTextSize(seatNumberTextSize * scale);
            float textY = y - ((textPaint.descent() + textPaint.ascent()) / 2f); // Center vertically
            canvas.drawText(String.valueOf(seat.seatNumber), x, textY, textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (seats == null || seats.isEmpty() || photoView == null) {
            return false;
        }

        if (isEditMode) {
            return handleEditModeTouch(event);
        } else {
            return handleNormalModeTouch(event);
        }
    }

    private boolean handleNormalModeTouch(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Seat clickedSeat = findSeatAt(event.getX(), event.getY());
            if (clickedSeat != null && clickListener != null) {
                clickListener.onSeatClicked(clickedSeat);
                return true;
            }
        }
        return event.getAction() == MotionEvent.ACTION_DOWN && findSeatAt(event.getX(), event.getY()) != null;
    }

    private boolean handleEditModeTouch(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                seatBeingDragged = findSeatAt(event.getX(), event.getY());
                return seatBeingDragged != null;

            case MotionEvent.ACTION_MOVE:
                if (seatBeingDragged != null) {
                    RectF rect = photoView.getDisplayRect();
                    if (rect == null || rect.width() == 0 || rect.height() == 0) break;

                    float relativeX = (event.getX() - rect.left) / rect.width();
                    float relativeY = (event.getY() - rect.top) / rect.height();

                    seatBeingDragged.relativeX = Math.max(0f, Math.min(1f, relativeX));
                    seatBeingDragged.relativeY = Math.max(0f, Math.min(1f, relativeY));

                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (seatBeingDragged != null) {
                    if (moveListener != null) {
                        moveListener.onSeatMoved(seatBeingDragged);
                    }
                    seatBeingDragged = null;
                    return true;
                }
                break;
        }
        return false;
    }

    private Seat findSeatAt(float x, float y) {
        if (photoView == null) return null;
        RectF rect = photoView.getDisplayRect();
        if (rect == null) return null;

        float scaledHalfSize = (seatBlockSize / 2f) * photoView.getScale() + clickPadding;

        for (int i = seats.size() - 1; i >= 0; i--) {
            Seat seat = seats.get(i);
            float seatCenterX = rect.left + seat.relativeX * rect.width();
            float seatCenterY = rect.top + seat.relativeY * rect.height();

            RectF seatRect = new RectF(
                seatCenterX - scaledHalfSize,
                seatCenterY - scaledHalfSize,
                seatCenterX + scaledHalfSize,
                seatCenterY + scaledHalfSize
            );

            if (seatRect.contains(x, y)) {
                return seat;
            }
        }
        return null;
    }
}
