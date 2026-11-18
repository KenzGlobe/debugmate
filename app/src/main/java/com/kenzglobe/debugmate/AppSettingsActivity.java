package com.kenzglobe.debugmate;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.content.SharedPreferences;

public class AppSettingsActivity extends Activity {

    private Switch switchShowHidden;
    private TextView textStartupValue;
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
        switchShowHidden.setChecked(prefs.getBoolean("show_hidden_files", false));
        switchShowHidden.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("show_hidden_files", isChecked).apply();
            }
        });

        // Setup Startup Behavior Row
        textStartupValue = findViewById(R.id.text_startup_value);
        updateStartupText();
        
        findViewById(R.id.row_startup_behavior).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStartupDialog();
            }
        });

        // Setup GitHub Link
        TextView iconGithub = findViewById(R.id.icon_github_link);
        iconGithub.setTypeface(brandsFont);
        iconGithub.setText("\uf09b");

        findViewById(R.id.layout_github_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/KenzGlobe/debugmate"));
                startActivity(browserIntent);
            }
        });
    }

    private void updateStartupText() {
        int behavior = prefs.getInt("startup_behavior", 0);
        if (behavior == 0) textStartupValue.setText("Welcome Screen");
        else if (behavior == 1) textStartupValue.setText("Last Opened Folder");
        else if (behavior == 2) textStartupValue.setText("Projects Dashboard");
    }

    private void showStartupDialog() {
        // Inflate the custom RadioGroup layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_startup_behaviour, null);
        
        final RadioGroup radioGroup = (RadioGroup) dialogView;
        RadioButton radioWelcome = dialogView.findViewById(R.id.radio_welcome);
        RadioButton radioLastFolder = dialogView.findViewById(R.id.radio_last_folder);
        RadioButton radioProjects = dialogView.findViewById(R.id.radio_projects);
        RadioButton radioWorkspace = dialogView.findViewById(R.id.radio_workspace);

        // Set current selection
        int current = prefs.getInt("startup_behavior", 0);
        if (current == 0) radioGroup.check(R.id.radio_welcome);
        else if (current == 1) radioGroup.check(R.id.radio_last_folder);
        else if (current == 2) radioGroup.check(R.id.radio_projects);
        else if (current == 3) radioGroup.check(R.id.radio_workspace);

        // Show your custom AppDialog
        AppDialog.show(
            AppSettingsActivity.this,
            "Startup Behavior",
            "Choose what DebugMate should do when you open the app.",
            dialogView,
            "SAVE",
            "CANCEL",
            new AppDialog.OnDialogClickListener() {
                @Override
                public void onClick() {
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    int behavior = 0;
                    if (selectedId == R.id.radio_last_folder) behavior = 1;
                    else if (selectedId == R.id.radio_projects) behavior = 2;
                    else if (selectedId == R.id.radio_workspace) behavior = 3;
                    
                    prefs.edit().putInt("startup_behavior", behavior).apply();
                    updateStartupText();
                }
            },
            null
        );
    }
}
