package com.kenzglobe.debugmate;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.content.SharedPreferences;

public class AppSettingsActivity extends Activity {

    private Switch switchShowHidden, switchShowSizes;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#8B5CF6"));
        }
        if (getActionBar() != null) getActionBar().hide();

        prefs = getSharedPreferences("DebugMatePrefs", MODE_PRIVATE);
        
        Typeface solidFont = Typeface.createFromAsset(getAssets(), "fonts/fa-solid-900-v5.ttf");
        Typeface brandsFont = Typeface.createFromAsset(getAssets(), "fonts/fa-brands-regular-400-v5.ttf");

        // Setup Back Button
        TextView btnBack = findViewById(R.id.btn_app_settings_back);
        btnBack.setTypeface(solidFont);
        btnBack.setText("\uf060");
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Setup Switches
        switchShowHidden = findViewById(R.id.switch_show_hidden);
        switchShowSizes = findViewById(R.id.switch_show_sizes);

        // Load saved preferences (defaults: hide hidden files, show sizes)
        switchShowHidden.setChecked(prefs.getBoolean("show_hidden_files", false));
        switchShowSizes.setChecked(prefs.getBoolean("show_file_sizes", true));

        // Save preferences when toggled
        switchShowHidden.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("show_hidden_files", isChecked).apply();
            }
        });

        switchShowSizes.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("show_file_sizes", isChecked).apply();
            }
        });

        // Setup GitHub Link
        TextView iconGithub = findViewById(R.id.icon_github_link);
        iconGithub.setTypeface(brandsFont);
        iconGithub.setText("\uf09b"); // GitHub icon

        findViewById(R.id.layout_github_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/KenzGlobe/debugmate"));
                startActivity(browserIntent);
            }
        });
    }
}
