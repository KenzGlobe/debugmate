package com.kenzglobe.debugmate;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class AppBoldTextView extends TextView {
    public AppBoldTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        try {
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/SpaceMono-Bold.ttf");
            setTypeface(typeface);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Hello
    }
}
