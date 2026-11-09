package com.kenzglobe.debugmate;

import android.content.Context;
import android.content.Intent;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.widget.TextView;

public class CustomLinkMovementMethod extends LinkMovementMethod {

    private Context context;

    public CustomLinkMovementMethod(Context context) {
        this.context = context;
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();
            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);
            if (link.length != 0) {
                String url = link[0].getURL();
                
                if (url.startsWith("learn://")) {
                    // Extract the topic, e.g., "ui_elements"
                    String topic = url.replace("learn://", "");
                    Intent intent = new Intent(context, LearnHubActivity.class);
                    intent.putExtra("TARGET_TOPIC", topic);
                    context.startActivity(intent);
                } else if (url.startsWith("cheat://")) {
                    // Extract language, e.g., "python"
                    String lang = url.replace("cheat://", "");
                    Intent intent = new Intent(context, CheatSheetActivity.class);
                    intent.putExtra("title", lang);
                    intent.putExtra("language", lang);
                    context.startActivity(intent);
                } else {
                    // Fallback for normal web links
                    super.onTouchEvent(widget, buffer, event);
                }
                return true;
            }
        }
        return super.onTouchEvent(widget, buffer, event);
    }
}