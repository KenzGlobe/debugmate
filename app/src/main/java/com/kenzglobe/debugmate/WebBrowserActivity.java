package com.kenzglobe.debugmate;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class WebBrowserActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_browser); // Ensure you updated the XML filename

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#262626"));
            getWindow().getDecorView().setSystemUiVisibility(0);
        }

        if (getActionBar() != null) {
            getActionBar().hide();
        }

        WebView webView = findViewById(R.id.web_browser_view);
        TextView btnClose = findViewById(R.id.btn_browser_close);
        TextView browserTitle = findViewById(R.id.browser_title);

        Typeface solidFont = Typeface.createFromAsset(getAssets(), "fonts/fa-solid-900-v5.ttf");
        btnClose.setTypeface(solidFont);
        btnClose.setText("\uf00d");

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 1. Configure WebView Settings
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); // ENABLE JAVASCRIPT
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);

        // 2. ADD WEBCHROME CLIENT (This enables alert() and other JS dialogs)
        webView.setWebChromeClient(new WebChromeClient());

        // 3. ADD WEBVIEW CLIENT (This prevents the app from crashing on internal links)
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // If the link is a local file, load it inside our WebView
                if (url.startsWith("file://")) {
                    view.loadUrl(url);
                    return true; 
                }
                // For external links (like google.com), let the system handle it
                return false; 
            }
        });

        String code = getIntent().getStringExtra("code");
        String fileName = getIntent().getStringExtra("file_name");
        String ext = getIntent().getStringExtra("extension");
        String baseUrl = getIntent().getStringExtra("base_url");

        if (fileName != null) {
            browserTitle.setText(fileName);
        }

        if (code != null) {
            if ("js".equals(ext)) {
                code = "<html><body><script>" + code + "</script></body></html>";
            } else if ("css".equals(ext)) {
                code = "<html><head><style>" + code + "</style></head><body>" +
                       "<h1>CSS Preview</h1><p>This paragraph is styled by your CSS.</p>" +
                       "</body></html>";
            }

            // 4. Load with baseUrl so relative links (like ./css/style.css) work
            webView.loadDataWithBaseURL(baseUrl, code, "text/html", "UTF-8", null);
        }
    }
}