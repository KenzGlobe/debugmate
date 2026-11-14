
package com.kenzglobe.debugmate;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.webkit.WebView;

public class NoSelectionWebView extends WebView {

    public NoSelectionWebView(Context context) {
        super(context);
    }

    public NoSelectionWebView(
            Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoSelectionWebView(
            Context context,
            AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public ActionMode startActionMode(
            ActionMode.Callback callback) {
        return null;
    }

    @Override
    public ActionMode startActionMode(
            ActionMode.Callback callback, int type) {
        return null;
    }
}
