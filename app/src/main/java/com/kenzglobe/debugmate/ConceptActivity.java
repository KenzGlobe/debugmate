package com.kenzglobe.debugmate;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

public class ConceptActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // --- Use the new concept layout ---
        setContentView(R.layout.activity_concept); 

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#8B5CF6"));
        }
        if (getActionBar() != null) getActionBar().hide();

        Typeface solidFont = Typeface.createFromAsset(getAssets(), "fonts/fa-solid-900-v5.ttf");
        Typeface appFont = Typeface.createFromAsset(getAssets(), "fonts/SpaceMono-Bold.ttf");

        // --- Use the new IDs ---
        TextView btnBack = findViewById(R.id.btn_concept_back);
        btnBack.setTypeface(solidFont);
        btnBack.setText("\uf060");
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish(); }
        });

        TextView titleView = findViewById(R.id.concept_title);
        titleView.setTypeface(appFont);

        String topic = getIntent().getStringExtra("topic");
        if (topic == null) topic = "unknown";

        // Format the title nicely (e.g., "ui_elements" -> "UI ELEMENTS")
        String formattedTitle = topic.replace("_", " ").toUpperCase();
        titleView.setText(formattedTitle);

        // --- Use the new WebView ID ---
        WebView webView = findViewById(R.id.concept_webview);
        webView.setWebViewClient(new android.webkit.WebViewClient());
        webView.getSettings().setJavaScriptEnabled(false);
        webView.setBackgroundColor(Color.parseColor("#1E1E1E"));
        
        // Load the specific concept HTML file from assets/learn/
        webView.loadUrl("file:///android_asset/learn/" + topic + ".html");
    }
}