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

public class EditorSettingsActivity extends Activity {

    private TextView currentSizeText, textFontStyle, textTabSize, textTheme, textCursorStyle;
    private Switch switchWordWrap, switchLineNumbers, switchSoftTabs, switchLiveAutocomplete;
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
            public void onClick(View v) { finish(); }
        });

        // Initialize views
        currentSizeText = findViewById(R.id.editor_current_size);
        switchWordWrap = findViewById(R.id.switch_word_wrap);
        switchLineNumbers = findViewById(R.id.switch_line_numbers);
        switchSoftTabs = findViewById(R.id.switch_soft_tabs);
        switchLiveAutocomplete = findViewById(R.id.switch_live_autocomplete);
        
        textFontStyle = findViewById(R.id.text_font_style);
        textTabSize = findViewById(R.id.text_tab_size);
        textTheme = findViewById(R.id.text_theme);
        textCursorStyle = findViewById(R.id.text_cursor_style);

        TextView btnMinus = findViewById(R.id.editor_btn_size_minus);
        TextView btnPlus = findViewById(R.id.editor_btn_size_plus);

        btnMinus.setTypeface(solidFont);
        btnMinus.setText("\uf068");
        btnPlus.setTypeface(solidFont);
        btnPlus.setText("\uf067");

        // --- Load Current Preferences ---
        defaultFontSize = prefs.getInt("editor_font_size", 14);
        currentSizeText.setText(defaultFontSize + "px");
        
        switchWordWrap.setChecked(prefs.getBoolean("editor_word_wrap", false));
        switchLineNumbers.setChecked(prefs.getBoolean("editor_line_numbers", true));
        switchSoftTabs.setChecked(prefs.getBoolean("editor_soft_tabs", true));
        switchLiveAutocomplete.setChecked(prefs.getBoolean("editor_live_autocomplete", true));

        updateTextLabels();

        // --- Font Size Listeners ---
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

        // --- Switch Listeners ---
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
        
        switchSoftTabs.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("editor_soft_tabs", isChecked).apply();
            }
        });
        
        switchLiveAutocomplete.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("editor_live_autocomplete", isChecked).apply();
            }
        });

        // --- Clickable Rows ---
        findViewById(R.id.row_font_style).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectionDialog("Font Style", new String[]{"Space Mono", "Fira Code", "JetBrains Mono", "Roboto Mono", "Source Code Pro"}, "editor_font_style", "Space Mono");
            }
        });

        findViewById(R.id.row_tab_size).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectionDialog("Tab Size", new String[]{"2", "4", "8"}, "editor_tab_size", "4");
            }
        });

        findViewById(R.id.row_theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectionDialog("Editor Theme", new String[]{"Dracula", "Monokai", "Twilight", "Tomorrow Night", "Gruvbox", "Solarized Dark", "One Dark"}, "editor_theme", "Dracula");
            }
        });

        findViewById(R.id.row_cursor_style).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectionDialog("Cursor Style", new String[]{"Line", "Block", "Underline"}, "editor_cursor_style", "Line");
            }
        });
    }

    private void updateTextLabels() {
        textFontStyle.setText(prefs.getString("editor_font_style", "Space Mono"));
        textTabSize.setText(prefs.getString("editor_tab_size", "4") + " spaces");
        textTheme.setText(prefs.getString("editor_theme", "Dracula"));
        textCursorStyle.setText(prefs.getString("editor_cursor_style", "Line"));
    }

    private void showSelectionDialog(String title, final String[] options, final String prefKey, final String defaultValue) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_startup_behaviour, null); // Reuse the layout!
        
        final RadioGroup radioGroup = (RadioGroup) dialogView;
        radioGroup.removeAllViews(); // Clear old radio buttons

        String currentValue = prefs.getString(prefKey, defaultValue);

        for (int i = 0; i < options.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(options[i]);
            rb.setTextColor(Color.WHITE);
            rb.setId(i);
            
            rb.setLayoutParams(new RadioGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                rb.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#8B5CF6")));
            }
            
            if (options[i].equals(currentValue)) {
                rb.setChecked(true);
            }
            radioGroup.addView(rb);
        }

        AppDialog.show(
            EditorSettingsActivity.this,
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
                        prefs.edit().putString(prefKey, options[selectedId]).apply();
                        updateTextLabels();
                    }
                }
            },
            null
        );
    }
}
