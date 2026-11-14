package com.kenzglobe.debugmate;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import java.lang.reflect.Field;
import java.util.HashMap;

public class CustomKeyboardView extends KeyboardView {

    private Paint superscriptPaint;
    private HashMap<Character, Character> longPressMap;

    public CustomKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        // Setup the paint for the tiny superscript text
        superscriptPaint = new Paint();
        superscriptPaint.setAntiAlias(true);
        superscriptPaint.setColor(Color.parseColor("#888888")); // Gray color
        superscriptPaint.setTextSize(20f); // Small size
        superscriptPaint.setTextAlign(Paint.Align.RIGHT);
        
        applyCustomFont();
    }

    private void applyCustomFont() {
        try {
            Typeface customFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/SyneMono-Regular.ttf");
            
            Field field = KeyboardView.class.getDeclaredField("mPaint");
            field.setAccessible(true);
            Paint paint = (Paint) field.get(this);
            
            if (paint != null) {
                paint.setTypeface(customFont);
                superscriptPaint.setTypeface(customFont); // Also apply to superscript
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLongPressMap(HashMap<Character, Character> map) {
        this.longPressMap = map;
        invalidate(); // Redraw the keyboard
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (longPressMap == null || getKeyboard() == null) return;

        // Draw the superscript character for each key
        for (Keyboard.Key key : getKeyboard().getKeys()) {
            if (key.label != null && key.label.length() == 1) {
                char primaryChar = key.label.charAt(0);
                
                if (longPressMap.containsKey(primaryChar)) {
                    char secondaryChar = longPressMap.get(primaryChar);
                    
                    // Calculate position: Top-Right corner of the key
                    float x = key.x + key.width - 8f;
                    float y = key.y + 30f; 
                    
                    canvas.drawText(String.valueOf(secondaryChar), x, y, superscriptPaint);
                }
            }
        }
    }
}
