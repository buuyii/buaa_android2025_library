package com.example.client;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import java.util.List;

public class StatisticsChartView extends View {
    private Paint paint;
    private List<ChartData> chartDataList;
    private ChartType chartType = ChartType.BAR_CHART;
    
    public enum ChartType {
        BAR_CHART
    }
    
    public static class ChartData {
        public String label;
        public float value;
        public int color;
        
        public ChartData(String label, float value, int color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }
    
    public StatisticsChartView(Context context) {
        super(context);
        init();
    }
    
    public StatisticsChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public StatisticsChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
    }
    
    public void setChartData(List<ChartData> data) {
        this.chartDataList = data;
        invalidate();
    }
    
    public void setChartType(ChartType type) {
        this.chartType = type;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (chartDataList == null || chartDataList.isEmpty()) {
            // Draw empty state
            paint.setTextSize(40);
            paint.setColor(Color.GRAY);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("暂无数据", getWidth() / 2f, getHeight() / 2f, paint);
            paint.setTextAlign(Paint.Align.LEFT);
            return;
        }
        
        switch (chartType) {
            case BAR_CHART:
                drawBarChart(canvas);
                break;
        }
    }
    
    private void drawBarChart(Canvas canvas) {
        if (chartDataList.isEmpty()) return;
        
        int width = getWidth();
        int height = getHeight();
        int padding = 80;
        int barWidth = (width - 2 * padding) / chartDataList.size() - 30;
        
        // Find max value for scaling
        float maxValue = 0;
        for (ChartData data : chartDataList) {
            if (data.value > maxValue) {
                maxValue = data.value;
            }
        }
        
        // 如果最大值为0，设置默认值以便仍能显示图表
        if (maxValue == 0) {
            maxValue = 1.0f;
        }
        
        // Draw axes
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(2);
        // Y axis
        canvas.drawLine(padding, padding, padding, height - padding, paint);
        // X axis
        canvas.drawLine(padding, height - padding, width - padding, height - padding, paint);
        
        // Draw bars
        paint.setTextSize(30);
        paint.setTextAlign(Paint.Align.CENTER);
        
        for (int i = 0; i < chartDataList.size(); i++) {
            ChartData data = chartDataList.get(i);
            
            // Calculate bar height
            float barHeight = (data.value / maxValue) * (height - 2 * padding);
            
            // Draw bar
            paint.setColor(data.color);
            RectF rect = new RectF(
                padding + i * (barWidth + 30) + 15,
                height - padding - barHeight,
                padding + i * (barWidth + 30) + barWidth + 15,
                height - padding
            );
            canvas.drawRect(rect, paint);
            
            // Draw value on top of bar
            paint.setColor(Color.BLACK);
            String valueText = String.format("%.1fH", data.value);
            canvas.drawText(
                valueText,
                rect.centerX(),
                rect.top - 20,
                paint
            );
            
            // Draw label
            canvas.save();
            canvas.rotate(-45, rect.centerX(), height - padding + 30);
            paint.setColor(Color.BLACK);
            canvas.drawText(
                data.label,
                rect.centerX(),
                height - padding + 30,
                paint
            );
            canvas.restore();
        }
        
        paint.setTextAlign(Paint.Align.LEFT);
    }
}