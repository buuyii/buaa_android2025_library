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
        BAR_CHART,
        PIE_CHART
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
            return;
        }
        
        switch (chartType) {
            case BAR_CHART:
                drawBarChart(canvas);
                break;
            case PIE_CHART:
                drawPieChart(canvas);
                break;
        }
    }
    
    private void drawBarChart(Canvas canvas) {
        if (chartDataList.isEmpty()) return;
        
        int width = getWidth();
        int height = getHeight();
        int padding = 50;
        int barWidth = (width - 2 * padding) / chartDataList.size() - 20;
        
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
        
        // Draw bars
        paint.setTextSize(30);
        paint.setColor(Color.BLACK);
        
        for (int i = 0; i < chartDataList.size(); i++) {
            ChartData data = chartDataList.get(i);
            
            // Calculate bar height
            float barHeight = (data.value / maxValue) * (height - 2 * padding);
            
            // Draw bar
            paint.setColor(data.color);
            RectF rect = new RectF(
                padding + i * (barWidth + 20),
                height - padding - barHeight,
                padding + i * (barWidth + 20) + barWidth,
                height - padding
            );
            canvas.drawRect(rect, paint);
            
            // Draw value on top of bar
            paint.setColor(Color.BLACK);
            String valueText = String.format("%.1f", data.value);
            float valueTextWidth = paint.measureText(valueText);
            canvas.drawText(
                valueText,
                rect.left + (barWidth - valueTextWidth) / 2,
                rect.top - 10,
                paint
            );
            
            // Draw label
            float labelWidth = paint.measureText(data.label);
            canvas.drawText(
                data.label,
                rect.left + (barWidth - labelWidth) / 2,
                height - padding + 40,
                paint
            );
        }
    }
    
    private void drawPieChart(Canvas canvas) {
        if (chartDataList.isEmpty()) return;
        
        int width = getWidth();
        int height = getHeight();
        float centerX = width / 2f;
        float centerY = height / 2f;
        float radius = Math.min(width, height) / 3f;
        
        // Calculate total value
        float totalValue = 0;
        for (ChartData data : chartDataList) {
            totalValue += data.value;
        }
        
        // 如果总值为0，设置默认值以便仍能显示图表
        if (totalValue == 0) {
            totalValue = 1.0f;
        }
        
        // Draw pie slices
        float startAngle = 0;
        paint.setTextSize(30);
        
        for (int i = 0; i < chartDataList.size(); i++) {
            ChartData data = chartDataList.get(i);
            
            // Calculate sweep angle
            float sweepAngle = (data.value / totalValue) * 360;
            
            // Draw slice
            paint.setColor(data.color);
            canvas.drawArc(
                centerX - radius, 
                centerY - radius, 
                centerX + radius, 
                centerY + radius, 
                startAngle, 
                sweepAngle, 
                true, 
                paint
            );
            
            // Draw label
            paint.setColor(Color.BLACK);
            float labelAngle = startAngle + sweepAngle / 2;
            float labelX = (float) (centerX + (radius * 1.2) * Math.cos(Math.toRadians(labelAngle)));
            float labelY = (float) (centerY + (radius * 1.2) * Math.sin(Math.toRadians(labelAngle)));
            
            String labelText = String.format("%s\n%.1fH", data.label, data.value);
            String[] lines = labelText.split("\n");
            
            float lineHeight = paint.descent() - paint.ascent();
            float startY = labelY - (lines.length - 1) * lineHeight / 2;
            
            for (int j = 0; j < lines.length; j++) {
                float lineWidth = paint.measureText(lines[j]);
                canvas.drawText(lines[j], labelX - lineWidth / 2, startY + j * lineHeight, paint);
            }
            
            startAngle += sweepAngle;
        }
    }
}