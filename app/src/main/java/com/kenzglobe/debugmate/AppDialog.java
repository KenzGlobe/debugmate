package com.kenzglobe.debugmate;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class AppDialog {

    public interface OnDialogClickListener {
        void onClick();
    }

    // Standard dialog (No custom view)
    public static void show(Context context, String title, String message, 
                            String positiveText, String negativeText,
                            final OnDialogClickListener positiveListener, 
                            final OnDialogClickListener negativeListener) {
        show(context, title, message, null, positiveText, negativeText, positiveListener, negativeListener);
    }

    // Overloaded dialog with custom view (e.g., EditText)
    public static void show(Context context, String title, String message, View customView,
                            String positiveText, String negativeText,
                            final OnDialogClickListener positiveListener, 
                            final OnDialogClickListener negativeListener) {
        
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setCancelable(true);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView titleView = dialog.findViewById(R.id.dialog_title);
        TextView messageView = dialog.findViewById(R.id.dialog_message);
        FrameLayout customContainer = dialog.findViewById(R.id.dialog_custom_view_container);
        Button btnPositive = dialog.findViewById(R.id.dialog_btn_positive);
        Button btnNegative = dialog.findViewById(R.id.dialog_btn_negative);

        titleView.setText(title);
        messageView.setText(message);
        
        // Inject custom view if provided
        if (customView != null && customContainer != null) {
            customContainer.setVisibility(View.VISIBLE);
            customContainer.removeAllViews();
            customContainer.addView(customView);
        }

        btnPositive.setText(positiveText);
        btnNegative.setText(negativeText);

        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (positiveListener != null) positiveListener.onClick();
            }
        });

        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (negativeListener != null) negativeListener.onClick();
            }
        });

        dialog.show();
    }
}