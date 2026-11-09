package com.kenzglobe.debugmate;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class AppCodeFontTextView extends TextView {
    public AppCodeFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        try {
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/FiraCode-Regular.ttf");
            setTypeface(typeface);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
