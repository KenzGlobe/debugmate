package com.kenzglobe.debugmate;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

public class TerminalSettingsActivity extends Activity {

    private TextView currentSizeText, textTerminalColor;
    private Switch switchExtraKeys, switchKeepScreenOn, switchHapticEnter;
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
            public void onClick(View v) { finish(); }
        });

        // Initialize views
        currentSizeText = findViewById(R.id.term_current_size);
        switchExtraKeys = findViewById(R.id.switch_extra_keys);
        switchKeepScreenOn = findViewById(R.id.switch_keep_screen_on);
        switchHapticEnter = findViewById(R.id.switch_haptic_enter);
        textTerminalColor = findViewById(R.id.text_terminal_color);
        
        TextView btnMinus = findViewById(R.id.term_btn_size_minus);
        TextView btnPlus = findViewById(R.id.term_btn_size_plus);

        btnMinus.setTypeface(solidFont);
        btnMinus.setText("\uf068");
        btnPlus.setTypeface(solidFont);
        btnPlus.setText("\uf067");

        // Load preferences
        defaultFontSize = prefs.getInt("terminal_font_size", 14);
        currentSizeText.setText(defaultFontSize + "px");
        switchExtraKeys.setChecked(prefs.getBoolean("terminal_extra_keys", true));
        switchKeepScreenOn.setChecked(prefs.getBoolean("terminal_keep_screen_on", false));
        switchHapticEnter.setChecked(prefs.getBoolean("terminal_haptic_enter", true));
        
        updateTextLabels();

        // Font Size Listeners
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

        // Switch Listeners
        switchExtraKeys.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("terminal_extra_keys", isChecked).apply();
            }
        });
        
        switchKeepScreenOn.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("terminal_keep_screen_on", isChecked).apply();
            }
        });
        
        switchHapticEnter.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("terminal_haptic_enter", isChecked).apply();
            }
        });

        // Dialog Rows
        findViewById(R.id.row_terminal_text_color).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectionDialog("Text Color", new String[]{"Classic (Gray)", "Hacker (Green)", "Cyberpunk (Purple)"}, "terminal_theme", "classic");
            }
        });
        
        findViewById(R.id.row_clear_history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppDialog.show(
                    TerminalSettingsActivity.this,
                    "Clear History",
                    "Are you sure you want to wipe your command history? This cannot be undone.",
                    "CLEAR",
                    "CANCEL",
                    new AppDialog.OnDialogClickListener() {
                        @Override
                        public void onClick() {
                            prefs.edit().remove("terminal_history").apply();
                            AppToast.showSuccess(TerminalSettingsActivity.this, "Terminal history cleared.");
                        }
                    },
                    null
                );
            }
        });
    }

    private void updateTextLabels() {
        String theme = prefs.getString("terminal_theme", "classic");
        if (theme.equals("hacker")) textTerminalColor.setText("Hacker (Green)");
        else if (theme.equals("cyberpunk")) textTerminalColor.setText("Cyberpunk (Purple)");
        else textTerminalColor.setText("Classic (Gray)");
    }

    private void showSelectionDialog(String title, final String[] options, final String prefKey, final String defaultValue) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_startup_behaviour, null);
        
        final RadioGroup radioGroup = (RadioGroup) dialogView;
        radioGroup.removeAllViews(); // Clear old radio buttons

        // Map values so we can match the display name to the saved key
        String currentValue = prefs.getString(prefKey, defaultValue);
        String currentDisplay = "";
        
        if (prefKey.equals("terminal_theme")) {
            if (currentValue.equals("hacker")) currentDisplay = "Hacker (Green)";
            else if (currentValue.equals("cyberpunk")) currentDisplay = "Cyberpunk (Purple)";
            else currentDisplay = "Classic (Gray)";
        }

        for (int i = 0; i < options.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(options[i]);
            rb.setTextColor(Color.WHITE);
            rb.setId(i);
            
            // 1. FORCE MATCH_PARENT
            rb.setLayoutParams(new RadioGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            
            // 2. FORCE PURPLE TINT
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                rb.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#8B5CF6")));
            }

            if (options[i].equals(currentDisplay)) {
                rb.setChecked(true);
            }
            radioGroup.addView(rb);
        }

        AppDialog.show(
            TerminalSettingsActivity.this,
            title,
            "Select your preferred option.",
            dialogView,
            "SAVE",
            "CANCEL",
            new AppDialog.OnDialogClickListener() {
                @Override
                public void onClick() {
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    if (selectedId != -1) {
                        String selectedDisplay = options[selectedId];
                        String saveValue = "";
                        
                        // Convert display text back to preference key
                        if (prefKey.equals("terminal_theme")) {
                            if (selectedDisplay.contains("Hacker")) saveValue = "hacker";
                            else if (selectedDisplay.contains("Cyberpunk")) saveValue = "cyberpunk";
                            else saveValue = "classic";
                        } else {
                            if (selectedDisplay.equals("Off")) saveValue = "off";
                            else if (selectedDisplay.equals("Fast")) saveValue = "fast";
                            else saveValue = "slow";
                        }
                        
                        prefs.edit().putString(prefKey, saveValue).apply();
                        updateTextLabels();
                    }
                }
            },
            null
        );
    }
}
