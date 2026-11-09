package com.kenzglobe.debugmate;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import java.lang.reflect.Field;

public class CustomKeyboardView extends KeyboardView {

    public CustomKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}