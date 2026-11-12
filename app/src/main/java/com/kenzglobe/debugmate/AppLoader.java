package com.kenzglobe.debugmate;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.TextView;

public class AppLoader {

    private static Dialog loadingDialog;

    public static void show(Context context, String message) {
        // If a dialog is already showing, dismiss it first to avoid overlapping
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }

        loadingDialog = new Dialog(context);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.setCancelable(false); // User cannot dismiss it by clicking outside

        if (loadingDialog.getWindow() != null) {
            loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView textView = loadingDialog.findViewById(R.id.loading_text);
        if (textView != null && message != null) {
            textView.setText(message);
        }

        loadingDialog.show();
    }

    public static void hide() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}