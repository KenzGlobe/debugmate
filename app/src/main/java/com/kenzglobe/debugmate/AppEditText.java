package com.kenzglobe.debugmate;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

public class AppEditText extends EditText {
    public AppEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        try {
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/SpaceMono-Regular.ttf");
            setTypeface(typeface);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}