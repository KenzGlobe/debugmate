package com.kenzglobe.debugmate;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class AppToast {
    
    public static void showSuccess(Context context, String message) {
        show(context, message, "\uf058", "#3FB950"); // Checkmark icon, Green
    }

    public static void showError(Context context, String message) {
        show(context, message, "\uf057", "#F85149"); // X circle icon, Red
    }

    public static void showInfo(Context context, String message) {
        show(context, message, "\uf05a", "#58A6FF"); // Info circle icon, Blue
    }

    private static void show(Context context, String message, String iconUnicode, String colorHex) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView icon = layout.findViewById(R.id.toast_icon);
        TextView text = layout.findViewById(R.id.toast_text);

        Typeface solidFont = Typeface.createFromAsset(context.getAssets(), "fonts/fa-solid-900-v5.ttf");
        icon.setTypeface(solidFont);
        icon.setText(iconUnicode);
        icon.setTextColor(android.graphics.Color.parseColor(colorHex));

        text.setText(message);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}