package com.kenzglobe.debugmate;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import java.io.File;

public class ProjectSettingsActivity extends Activity {

    private String projectPath;
    private SharedPreferences prefs;
    private Typeface solidFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_settings);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#8B5CF6"));
        }
        if (getActionBar() != null) getActionBar().hide();

        projectPath = getIntent().getStringExtra("START_DIRECTORY");
        prefs = getSharedPreferences("DebugMatePrefs", MODE_PRIVATE);
        solidFont = Typeface.createFromAsset(getAssets(), "fonts/fa-solid-900-v5.ttf");

        // Setup Back Button
        TextView btnBack = findViewById(R.id.btn_project_settings_back);
        btnBack.setTypeface(solidFont);
        btnBack.setText("\uf060");
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish(); }
        });

        // --- Wire up the rows ---

        findViewById(R.id.row_change_pin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePinDialog();
            }
        });

        findViewById(R.id.row_forget_pin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgetPinDialog();
            }
        });

        findViewById(R.id.row_clear_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClearActivityDialog();
            }
        });

        findViewById(R.id.row_clear_stats).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClearStatsDialog();
            }
        });
    }

    private void showChangePinDialog() {
        final EditText input = new EditText(this);
        input.setHint("Enter new 4-digit PIN");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setPadding(20, 20, 20, 20);
        input.setTextColor(Color.WHITE);

        AppDialog.show(this, "Change Vault PIN", "Set a new 4-digit PIN:", input, "SAVE", "CANCEL",
            new AppDialog.OnDialogClickListener() {
                @Override
                public void onClick() {
                    String newPin = input.getText().toString().trim();
                    if (newPin.length() == 4) {
                        prefs.edit().putString("vault_pin", newPin).apply();
                        AppToast.showSuccess(ProjectSettingsActivity.this, "Vault PIN updated successfully.");
                    } else {
                        AppToast.showError(ProjectSettingsActivity.this, "PIN must be exactly 4 digits.");
                    }
                }
            }, null);
    }

    private void showForgetPinDialog() {
        AppDialog.show(this, "Forget Vault PIN?", 
            "This will remove your PIN protection. Hidden projects will remain hidden, but you will need to set a new PIN to view them again.",
            "FORGET", "CANCEL",
            new AppDialog.OnDialogClickListener() {
                @Override
                public void onClick() {
                    prefs.edit().remove("vault_pin").apply();
                    AppToast.showSuccess(ProjectSettingsActivity.this, "Vault PIN removed.");
                }
            }, null);
    }

    private void showClearActivityDialog() {
        AppDialog.show(this, "Clear Recent Activity?", 
            "This will permanently delete the activity log for this project.",
            "CLEAR", "CANCEL",
            new AppDialog.OnDialogClickListener() {
                @Override
                public void onClick() {
                    File activityFile = new File(projectPath, ".debugmate_activity.json");
                    if (activityFile.exists()) {
                        activityFile.delete();
                    }
                    AppToast.showSuccess(ProjectSettingsActivity.this, "Activity history cleared.");
                }
            }, null);
    }

    private void showClearStatsDialog() {
        AppDialog.show(this, "Clear Stats Cache?", 
            "This will force the dashboard to recalculate all files, folders, and sizes next time it opens.",
            "CLEAR", "CANCEL",
            new AppDialog.OnDialogClickListener() {
                @Override
                public void onClick() {
                    // We need to add this helper method to ProjectStatsManager
                    ProjectStatsManager.clearCache(ProjectSettingsActivity.this, projectPath);
                    AppToast.showSuccess(ProjectSettingsActivity.this, "Stats cache cleared.");
                }
            }, null);
    }
}