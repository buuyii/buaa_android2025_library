package com.example.client;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.List;

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
    private PhotoView photoView;

    // -- Configuration --
    private final float seatBlockSize = 20f;
    private final float clickPadding = 10f;

    // -- Edit & Drag State --
    private boolean isEditMode = false;
    private Seat seatBeingDragged = null;
    private float dragOffsetX, dragOffsetY;

    public SeatOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSeats(List<Seat> seatList) {
        this.seats = seatList;
        invalidate();
    }

    public void bindPhotoView(PhotoView pv) {
        this.photoView = pv;
        // Redraw overlay when the background image's matrix changes (zoom/pan)
        pv.setOnMatrixChangeListener(rect -> invalidate());
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
        // Disable the parent PhotoView's touch interception when in edit mode
        photoView.setZoomable(!isEditMode);
        invalidate(); // Redraw to show edit mode visuals if any
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (photoView == null || seats == null) return;

        final RectF rect = photoView.getDisplayRect();
        if (rect == null) return;

        for (Seat seat : seats) {
            float x = rect.left + seat.relativeX * rect.width();
            float y = rect.top + seat.relativeY * rect.height();

            // Set color based on availability
            seatPaint.setColor(seat.isAvailable() ? Color.GREEN : Color.RED);
            // Add a visual indicator for edit mode (e.g., slight transparency)
            seatPaint.setAlpha(isEditMode ? 200 : 255);

            float halfSize = (seatBlockSize / 2f) * photoView.getScale();
            canvas.drawRect(x - halfSize, y - halfSize, x + halfSize, y + halfSize, seatPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (seats == null || seats.isEmpty()) {
            return false; // No seats, do nothing
        }

        if (isEditMode) {
            return handleEditModeTouch(event);
        } else {
            return handleNormalModeTouch(event);
        }
    }

    private boolean handleNormalModeTouch(MotionEvent event) {
        // Standard click handling
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Seat clickedSeat = findSeatAt(event.getX(), event.getY());
            if (clickedSeat != null && clickedSeat.isAvailable() && clickListener != null) {
                clickListener.onSeatClicked(clickedSeat);
                return true; // Consume the event
            }
        }
        // We return true on ACTION_DOWN if a seat is under the press, which allows ACTION_UP to be received.
        return event.getAction() == MotionEvent.ACTION_DOWN && findSeatAt(event.getX(), event.getY()) != null;
    }

    private boolean handleEditModeTouch(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                seatBeingDragged = findSeatAt(event.getX(), event.getY());
                if (seatBeingDragged != null) {
                    // When dragging starts, we are interested in handling the move.
                    return true;
                }
                return false; // No seat found, let other views handle it.

            case MotionEvent.ACTION_MOVE:
                if (seatBeingDragged != null) {
                    RectF rect = photoView.getDisplayRect();
                    if (rect == null || rect.width() == 0 || rect.height() == 0) break;

                    // Convert screen touch coordinates to relative coordinates within the drawable
                    float relativeX = (event.getX() - rect.left) / rect.width();
                    float relativeY = (event.getY() - rect.top) / rect.height();

                    // Update the seat's position, clamping to the [0, 1] bounds
                    seatBeingDragged.relativeX = Math.max(0f, Math.min(1f, relativeX));
                    seatBeingDragged.relativeY = Math.max(0f, Math.min(1f, relativeY));

                    invalidate(); // Redraw the overlay to show the seat in its new position
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (seatBeingDragged != null) {
                    // Fire the callback to notify that the seat has been moved to a new final position
                    if (moveListener != null) {
                        moveListener.onSeatMoved(seatBeingDragged);
                    }
                    seatBeingDragged = null; // Reset dragging state
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

        // Iterate backwards to prioritize seats drawn on top
        for (int i = seats.size() - 1; i >= 0; i--) {
            Seat seat = seats.get(i);
            float seatCenterX = rect.left + seat.relativeX * rect.width();
            float seatCenterY = rect.top + seat.relativeY * rect.height();

            // Create a touchable area rect for the seat
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
        return null; // No seat found at this position
    }
}
