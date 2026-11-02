package com.kenzglobe.debugmate;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class AppDialog {

    public interface OnDialogClickListener {
        void onClick();
    }

    public static void show(Context context, String title, String message, 
                            String positiveText, String negativeText,
                            final OnDialogClickListener positiveListener, 
                            final OnDialogClickListener negativeListener) {
        
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setCancelable(true);
        
        // Make background transparent so our custom rounded drawable shows
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView titleView = dialog.findViewById(R.id.dialog_title);
        TextView messageView = dialog.findViewById(R.id.dialog_message);
        Button btnPositive = dialog.findViewById(R.id.dialog_btn_positive);
        Button btnNegative = dialog.findViewById(R.id.dialog_btn_negative);

        titleView.setText(title);
        messageView.setText(message);
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