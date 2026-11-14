package com.kenzglobe.debugmate;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

public class EditorSettingsActivity extends Activity {

    private TextView currentSizeText;
    private Switch switchWordWrap, switchLineNumbers;
    private SharedPreferences prefs;
    private int defaultFontSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_settings);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#8B5CF6"));
        }
        if (getActionBar() != null) getActionBar().hide();

        prefs = getSharedPreferences("DebugMatePrefs", MODE_PRIVATE);
        Typeface solidFont = Typeface.createFromAsset(getAssets(), "fonts/fa-solid-900-v5.ttf");

        // Back Button
        TextView btnBack = findViewById(R.id.btn_editor_settings_back);
        btnBack.setTypeface(solidFont);
        btnBack.setText("\uf060");
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Initialize views
        currentSizeText = findViewById(R.id.editor_current_size);
        switchWordWrap = findViewById(R.id.switch_word_wrap);
        switchLineNumbers = findViewById(R.id.switch_line_numbers);
        TextView btnMinus = findViewById(R.id.editor_btn_size_minus);
        TextView btnPlus = findViewById(R.id.editor_btn_size_plus);

        // Set Icons for +/- buttons
        btnMinus.setTypeface(solidFont);
        btnMinus.setText("\uf068");
        btnPlus.setTypeface(solidFont);
        btnPlus.setText("\uf067");

        // Load current preferences
        defaultFontSize = prefs.getInt("editor_font_size", 14);
        boolean wordWrap = prefs.getBoolean("editor_word_wrap", false);
        boolean lineNumbers = prefs.getBoolean("editor_line_numbers", true);

        currentSizeText.setText(defaultFontSize + "px");
        switchWordWrap.setChecked(wordWrap);
        switchLineNumbers.setChecked(lineNumbers);

        // Font Size Listeners
        btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (defaultFontSize > 8) {
                    defaultFontSize--;
                    currentSizeText.setText(defaultFontSize + "px");
                    prefs.edit().putInt("editor_font_size", defaultFontSize).apply();
                }
            }
        });

        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (defaultFontSize < 32) {
                    defaultFontSize++;
                    currentSizeText.setText(defaultFontSize + "px");
                    prefs.edit().putInt("editor_font_size", defaultFontSize).apply();
                }
            }
        });

        // Switch Listeners
        switchWordWrap.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("editor_word_wrap", isChecked).apply();
            }
        });

        switchLineNumbers.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("editor_line_numbers", isChecked).apply();
            }
        });
    }
}