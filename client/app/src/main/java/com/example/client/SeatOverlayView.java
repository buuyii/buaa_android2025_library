package com.example.client;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.List;

public class SeatOverlayView extends View {

    // 点击回调接口
    public interface OnSeatClickListener {
        void onSeatClicked(Seat seat);
    }

    private OnSeatClickListener clickListener;

    public void setOnSeatClickListener(OnSeatClickListener listener) {
        this.clickListener = listener;
    }

    private List<Seat> seats = new ArrayList<>();
    private Paint seatPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private PhotoView photoView;

    // 方块基础大小（px），可以根据实际屏幕微调
    private float seatBlockSize = 40f;
    // 点击容错范围（px）
    private float clickPadding = 10f;

    // 用于追踪按下的座位
    private Seat downSeat = null;

    public SeatOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 绑定要绘制的座位
     */
    public void setSeats(List<Seat> seatList) {
        this.seats = seatList;
        invalidate();
    }

    /**
     * 绑定 PhotoView，以跟随缩放和平移
     */
    public void bindPhotoView(PhotoView pv) {
        this.photoView = pv;
        pv.setOnMatrixChangeListener(rect -> invalidate());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (photoView == null || seats == null) return;

        RectF rect = photoView.getDisplayRect();
        if (rect == null) return;

        for (Seat seat : seats) {
            // 将相对坐标转换成屏幕坐标
            float x = rect.left + seat.relativeX * rect.width();
            float y = rect.top + seat.relativeY * rect.height();

            // 颜色根据状态显示
            seatPaint.setColor(seat.isAvailable() ? Color.GREEN : Color.RED);

            float halfSize = (seatBlockSize / 2f) * photoView.getScale();
            canvas.drawRect(
                    x - halfSize,
                    y - halfSize,
                    x + halfSize,
                    y + halfSize,
                    seatPaint
            );
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (photoView == null || seats == null || seats.isEmpty()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                downSeat = findSeatAt(event.getX(), event.getY());
                // 如果我们点击了一个可用座位，我们就对这个手势感兴趣。
                if (downSeat != null && downSeat.isAvailable()) {
                    return true;
                }
                downSeat = null;
                return false;
            }

            case MotionEvent.ACTION_UP: {
                if (downSeat != null) {
                    Seat upSeat = findSeatAt(event.getX(), event.getY());
                    if (upSeat == downSeat) { // 检查手指是否从同一个座位上抬起
                        if (clickListener != null) {
                            clickListener.onSeatClicked(downSeat);
                        }
                    }
                    downSeat = null;
                    return true;
                }
                return false;
            }

            case MotionEvent.ACTION_CANCEL: {
                downSeat = null;
                return false;
            }
        }
        return false;
    }

    private Seat findSeatAt(float x, float y) {
        if (photoView == null) return null;
        RectF rect = photoView.getDisplayRect();
        if (rect == null) {
            return null;
        }

        float halfSize = (seatBlockSize / 2f) * photoView.getScale();

        for (Seat seat : seats) {
            float seatX = rect.left + seat.relativeX * rect.width();
            float seatY = rect.top + seat.relativeY * rect.height();

            RectF seatRect = new RectF(
                    seatX - halfSize - clickPadding,
                    seatY - halfSize - clickPadding,
                    seatX + halfSize + clickPadding,
                    seatY + halfSize + clickPadding
            );

            if (seatRect.contains(x, y)) {
                return seat;
            }
        }
        return null;
    }
}
