package com.kenzglobe.debugmate;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorPickerView extends View {

    private Paint colorWheelPaint;
    private Paint saturationPaint;
    private Paint selectorPaint;
    private int[] hueColors;
    private float centerX, centerY, radius;
    private float selectorX, selectorY;
    private OnColorChangedListener listener;
    private int currentColor = Color.parseColor("#8B5CF6"); // Default purple

    public interface OnColorChangedListener {
        void onColorChanged(int color);
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        hueColors = new int[]{
                Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED
        };

        colorWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        saturationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectorPaint.setStyle(Paint.Style.STROKE);
        selectorPaint.setStrokeWidth(6f);
        selectorPaint.setColor(Color.WHITE);
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;
        radius = Math.min(w, h) / 2f - 10;

        // Create the rainbow effect
        SweepGradient sweepGradient = new SweepGradient(centerX, centerY, hueColors, null);
        colorWheelPaint.setShader(sweepGradient);

        // Create the white center (saturation/brightness)
        RadialGradient radialGradient = new RadialGradient(centerX, centerY, radius,
                Color.WHITE, 0x00FFFFFF, Shader.TileMode.CLAMP);
        saturationPaint.setShader(radialGradient);

        // Set initial selector position (top right for purple)
        updateSelector(centerX, centerY - radius / 2f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(centerX, centerY, radius, colorWheelPaint);
        canvas.drawCircle(centerX, centerY, radius, saturationPaint);
        canvas.drawCircle(selectorX, selectorY, 15f, selectorPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getX();
            float y = event.getY();

            // Calculate distance from center
            float dx = x - centerX;
            float dy = y - centerY;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            // Constrain to the circle
            if (dist > radius) {
                float ratio = radius / dist;
                x = centerX + dx * ratio;
                y = centerY + dy * ratio;
            }

            updateSelector(x, y);
            invalidate(); // Redraw
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void updateSelector(float x, float y) {
        selectorX = x;
        selectorY = y;

        float dx = x - centerX;
        float dy = y - centerY;
        
        // Calculate Angle (Hue)
        double angle = Math.atan2(dy, dx);
        float degrees = (float) Math.toDegrees(angle);
        if (degrees < 0) degrees += 360;
        
        // Convert degrees to a 0-1 scale for HSV
        float hue = degrees;

        // Calculate Distance (Saturation)
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        float saturation = dist / radius; // 0 is center, 1 is edge
        if (saturation > 1) saturation = 1;

        // Brightness is 1.0 for now (we can add a slider for it later if needed)
        currentColor = Color.HSVToColor(new float[]{hue, saturation, 1f});

        if (listener != null) {
            listener.onColorChanged(currentColor);
        }
    }

    public int getCurrentColor() {
        return currentColor;
    }
    
    public void setColorFromHex(int color) {
        if (currentColor == color) return; // Prevent infinite loops
        
        currentColor = color;
        
        // Convert color to HSV to find the X/Y coordinates
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        
        float hueRadians = (float) Math.toRadians(hsv[0]);
        float dist = hsv[1] * radius; // Saturation determines distance from center
        
        selectorX = centerX + (float) Math.cos(hueRadians) * dist;
        selectorY = centerY + (float) Math.sin(hueRadians) * dist;
        
        invalidate(); // Redraw the view
    }
}
