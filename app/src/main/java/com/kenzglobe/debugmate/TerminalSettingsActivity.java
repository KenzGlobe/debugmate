package com.kenzglobe.debugmate;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public class TerminalSettingsActivity extends Activity {

    private TextView currentSizeText;
    private Switch switchExtraKeys;
    private Spinner themeSpinner;
    private SharedPreferences prefs;
    private int defaultFontSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal_settings);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#8B5CF6"));
        }
        if (getActionBar() != null) getActionBar().hide();

        prefs = getSharedPreferences("DebugMatePrefs", MODE_PRIVATE);
        Typeface solidFont = Typeface.createFromAsset(getAssets(), "fonts/fa-solid-900-v5.ttf");

        // Back Button
        TextView btnBack = findViewById(R.id.btn_term_settings_back);
        btnBack.setTypeface(solidFont);
        btnBack.setText("\uf060");
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Initialize views
        currentSizeText = findViewById(R.id.term_current_size);
        switchExtraKeys = findViewById(R.id.switch_extra_keys);
        themeSpinner = findViewById(R.id.spinner_terminal_theme);
        TextView btnMinus = findViewById(R.id.term_btn_size_minus);
        TextView btnPlus = findViewById(R.id.term_btn_size_plus);

        btnMinus.setTypeface(solidFont);
        btnMinus.setText("\uf068");
        btnPlus.setTypeface(solidFont);
        btnPlus.setText("\uf067");

        // Load preferences
        defaultFontSize = prefs.getInt("terminal_font_size", 14);
        boolean showExtraKeys = prefs.getBoolean("terminal_extra_keys", true);
        String theme = prefs.getString("terminal_theme", "classic");

        currentSizeText.setText(defaultFontSize + "px");
        switchExtraKeys.setChecked(showExtraKeys);

        // Setup Spinner
        String[] themes = {"Classic (Gray)", "Hacker (Green)", "Cyberpunk (Purple)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, themes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        themeSpinner.setAdapter(adapter);

        if (theme.equals("hacker")) themeSpinner.setSelection(1);
        else if (theme.equals("cyberpunk")) themeSpinner.setSelection(2);
        else themeSpinner.setSelection(0);

        // Listeners
        btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (defaultFontSize > 8) {
                    defaultFontSize--;
                    currentSizeText.setText(defaultFontSize + "px");
                    prefs.edit().putInt("terminal_font_size", defaultFontSize).apply();
                }
            }
        });

        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (defaultFontSize < 32) {
                    defaultFontSize++;
                    currentSizeText.setText(defaultFontSize + "px");
                    prefs.edit().putInt("terminal_font_size", defaultFontSize).apply();
                }
            }
        });

        switchExtraKeys.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("terminal_extra_keys", isChecked).apply();
            }
        });

        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTheme = "classic";
                if (position == 1) selectedTheme = "hacker";
                else if (position == 2) selectedTheme = "cyberpunk";
                prefs.edit().putString("terminal_theme", selectedTheme).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}