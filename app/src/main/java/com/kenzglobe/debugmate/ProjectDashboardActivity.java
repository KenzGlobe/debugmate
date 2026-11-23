package com.kenzglobe.debugmate;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.TextView;
import android.widget.LinearLayout;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class ProjectDashboardActivity extends Activity {
    
    private Typeface solidFont, brandsFont;
    private String currentProjectPath;
    
    // Header Views
    private LinearLayout headerInfoLayout;
    private TextView backBtn, optionBtn, projectTypeIcon, pinnedIcon, hiddenIcon;
    private TextView projectName, projectType, projectPath;
    private TextView tvFiles, tvFolders, tvSize, tvModified;
    private TextView mainFileTv, languageTv, dependenciesTv;
    
    // Quick Action Icons & Layouts
    private TextView iconEditor, iconTerminal, iconRun, iconGit, iconFiles, iconSettings;
    private LinearLayout btnEditor, btnTerminal, btnRun, btnGit, btnFiles, btnSettings;
    
    // Recent Activity Views
    private TextView recentActivityDropBtn, tvNoActivity;
    private LinearLayout activityContainerVisible, activityContainerHidden;
    private boolean isActivityExpanded = false;
    
    private TextView tvLinesOfCode, tvTodos, readmeTitle, readmePreview, readmeActionBtn;
    private LinearLayout readmeCard;
    
    private TextView tvHealthyStatus;
    private LinearLayout healthWarningsContainer;
    
    private TextView tvProjectStructure, btnViewInFiles;
    
    private ProjectAnalyzer.ProjectInfo currentProjectInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_dashboard);
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#8B5CF6"));
            getWindow().getDecorView().setSystemUiVisibility(0);
        }
        if (getActionBar() != null) getActionBar().hide();
        
        // INITIALIZE FONTS
        solidFont = Typeface.createFromAsset(getAssets(), "fonts/fa-solid-900-v5.ttf");
        brandsFont = Typeface.createFromAsset(getAssets(), "fonts/fa-brands-regular-400-v5.ttf");
        
        // GET INTENT DATA
        currentProjectPath = getIntent().getStringExtra("START_DIRECTORY");
        if (currentProjectPath == null) currentProjectPath = "/storage/emulated/0/KenzGlobe/MyCodes/PythonConsole/MyProject"; // Fallback
        
        // DECLARE VIEWS
        backBtn = findViewById(R.id.back_btn);
        optionBtn = findViewById(R.id.option_btn);
        projectTypeIcon = findViewById(R.id.project_type_icon);
        headerInfoLayout = findViewById(R.id.header_info_layout);
        projectName = findViewById(R.id.project_name);
        projectType = findViewById(R.id.project_type);
        projectPath = findViewById(R.id.project_path);
        pinnedIcon = findViewById(R.id.pinned_icon);
        hiddenIcon = findViewById(R.id.hidden_icon);
        
        tvFiles = findViewById(R.id.tvFiles);
        tvFolders = findViewById(R.id.tvFolders);
        tvSize = findViewById(R.id.tvSize);
        tvModified = findViewById(R.id.tvModified);
        
        mainFileTv = findViewById(R.id.main_file);
        languageTv = findViewById(R.id.project_language);
        dependenciesTv = findViewById(R.id.no_of_dependencies);
        
        iconEditor = findViewById(R.id.quick_action_editor_icon);
        iconTerminal = findViewById(R.id.quick_action_terminal_icon);
        iconRun = findViewById(R.id.quick_action_run_icon);
        iconGit = findViewById(R.id.quick_action_git_icon);
        iconFiles = findViewById(R.id.quick_action_files_icon);
        iconSettings = findViewById(R.id.quick_action_settings_icon);
        
        btnEditor = findViewById(R.id.quick_action_editor);
        btnTerminal = findViewById(R.id.quick_action_terminal);
        btnRun = findViewById(R.id.quick_action_run);
        btnGit = findViewById(R.id.quick_action_git);
        btnFiles = findViewById(R.id.quick_action_files);
        btnSettings = findViewById(R.id.quick_action_settings);
        
        recentActivityDropBtn = findViewById(R.id.recent_activity_drop_btn);
        tvNoActivity = findViewById(R.id.tv_no_activity);
        activityContainerVisible = findViewById(R.id.activity_container_visible);
        activityContainerHidden = findViewById(R.id.activity_container_hidden);
        
        tvLinesOfCode = findViewById(R.id.tvLinesOfCode);
        tvTodos = findViewById(R.id.tvTodos);
        readmeCard = findViewById(R.id.readme_card);
        readmeTitle = findViewById(R.id.readme_title);
        readmePreview = findViewById(R.id.readme_preview);
        readmeActionBtn = findViewById(R.id.readme_action_btn);
        
        tvHealthyStatus = findViewById(R.id.tvHealthyStatus);
        healthWarningsContainer = findViewById(R.id.health_warnings_container);
        
        tvProjectStructure = findViewById(R.id.tvProjectStructure);
        btnViewInFiles = findViewById(R.id.btnViewInFiles);
        
        // SET HEADER ICONS & DATA
        setIcon(backBtn, "\uf060", solidFont);
        setIcon(optionBtn, "\uf142", solidFont);
        setIcon(projectTypeIcon, "\uf121", solidFont);
        setIcon(pinnedIcon, "\uf08d", solidFont);
        setIcon(hiddenIcon, "\uf023", solidFont);
        
        // Extract folder name for UI display
        File f = new File(currentProjectPath);
        if (f.exists()) {
            projectName.setText(f.getName());
        } else {
            projectName.setText("Unknown Project");
        }
        projectPath.setText(currentProjectPath);
        
        // SET QUICK ACTION ICONS
        setIcon(iconEditor, "\uf121", solidFont);   // Code icon
        setIcon(iconTerminal, "\uf120", solidFont); // Terminal icon
        setIcon(iconRun, "\uf144", solidFont);      // Play circle icon
        setIcon(iconGit, "\uf1d3", brandsFont);     // Git icon
        setIcon(iconFiles, "\uf07b", solidFont);    // Folder icon
        setIcon(iconSettings, "\uf013", solidFont); // Cog icon
        
        // SET ACTIVITY ICONS
        setIcon(recentActivityDropBtn, "\uf0dd", solidFont);
        
        updatePinAndHiddenStatus();
        
        // WIRE UP CLICKS (AIDE Compatible)
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish(); }
        });
        
        optionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Create the dialog
                final android.app.Dialog dialog = new android.app.Dialog(ProjectDashboardActivity.this);
                dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_dashboard_options);
                
                // 2. Make the dialog background transparent so our rounded corners show
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
                    
                    // 3. Position it near the top-right corner (under the 3-dots)
                    android.view.WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                    params.gravity = android.view.Gravity.TOP | android.view.Gravity.END;
                    params.x = 20; // margin from right
                    params.y = 90; // margin from top (below the action bar)
                    dialog.getWindow().setAttributes(params);
                }
        
                // 4. Find views and set icons
                TextView iconRefresh = dialog.findViewById(R.id.icon_refresh);
                TextView iconExport = dialog.findViewById(R.id.icon_export);
                TextView iconSettings = dialog.findViewById(R.id.icon_settings);
                
                setIcon(iconRefresh, "\uf021", solidFont); // Refresh icon
                setIcon(iconExport, "\uf56e", solidFont);  // File Export icon
                setIcon(iconSettings, "\uf013", solidFont); // Cog icon
        
                // 5. Wire up the clicks
                dialog.findViewById(R.id.opt_refresh).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        AppToast.showInfo(ProjectDashboardActivity.this, "Refreshing dashboard...");
                        loadDashboardData(true); // Force cache bypass
                    }
                });
        
                dialog.findViewById(R.id.opt_export).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        exportProjectReport();
                    }
                });
        
                dialog.findViewById(R.id.opt_settings).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        Intent intent = new Intent(ProjectDashboardActivity.this, ProjectSettingsActivity.class);
                        intent.putExtra("START_DIRECTORY", currentProjectPath);
                        startActivity(intent);
                    }
                });
        
                dialog.show();
            }
        });
        
        headerInfoLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 1. Get the Clipboard Service
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                
                // 2. Create the ClipData with the project path
                android.content.ClipData clip = android.content.ClipData.newPlainText("Project Path", currentProjectPath);
                
                // 3. Copy to clipboard
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    
                    // 4. Provide feedback
                    AppToast.showSuccess(ProjectDashboardActivity.this, "Project path copied to clipboard!");
                    
                    // Optional: Give a little haptic vibration feedback
                    v.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
                }
                
                return true; // Consume the event
            }
        });
        
        btnEditor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProjectDashboardActivity.this, EditorActivity.class);
                intent.putExtra("START_DIRECTORY", currentProjectPath);
                startActivity(intent);
            }
        });
        
        btnTerminal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProjectDashboardActivity.this, TerminalActivity.class);
                intent.putExtra("START_DIRECTORY", currentProjectPath);
                startActivity(intent);
            }
        });
        
        btnFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Assuming MainActivity is your file manager
                Intent intent = new Intent(ProjectDashboardActivity.this, MainActivity.class);
                intent.putExtra("START_DIRECTORY", currentProjectPath);
                startActivity(intent);
            }
        });
        
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProjectDashboardActivity.this, ProjectSettingsActivity.class);
                intent.putExtra("START_DIRECTORY", currentProjectPath);
                startActivity(intent);
            }
        });
        
        btnGit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Replace with your Git Activity when built
                AppToast.showInfo(ProjectDashboardActivity.this, "Git Manager coming soon!");
            }
        });
        
        btnRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Phase 3 will handle specific run commands.
                // For now, let's just open the terminal.
                
                // Log the activity
                RecentActivityManager.log(ProjectDashboardActivity.this, currentProjectPath, "run", "Project executed (main file)", "\uf144", false);
                loadRecentActivities();
                AppToast.showInfo(ProjectDashboardActivity.this, "Run functionality requires ProjectAnalyzer (Phase 3).");
            }
        });
        
        btnViewInFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProjectDashboardActivity.this, MainActivity.class);
                intent.putExtra("START_DIRECTORY", currentProjectPath);
                startActivity(intent);
            }
        });
        
        recentActivityDropBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isActivityExpanded = !isActivityExpanded;
                
                if (isActivityExpanded) {
                    activityContainerHidden.setVisibility(View.VISIBLE);
                    setIcon(recentActivityDropBtn, "\uf0de", solidFont); // Up arrow
                } else {
                    activityContainerHidden.setVisibility(View.GONE);
                    setIcon(recentActivityDropBtn, "\uf0dd", solidFont); // Down arrow
                }
            }
        });
        
        loadDashboardData(false);
        
        // Log that the dashboard was opened
        RecentActivityManager.log(this, currentProjectPath, "open", "Project dashboard opened", "\uf07b", false);
        
        // Load the list
        loadRecentActivities();
        
        recentActivityDropBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isActivityExpanded = !isActivityExpanded;
                if (isActivityExpanded) {
                    activityContainerHidden.setVisibility(View.VISIBLE);
                    setIcon(recentActivityDropBtn, "\uf0de", solidFont); // Up arrow
                } else {
                    activityContainerHidden.setVisibility(View.GONE);
                    setIcon(recentActivityDropBtn, "\uf0dd", solidFont); // Down arrow
                }
            }
        });
        
        readmeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File readmeFile = new File(currentProjectPath, "README.md");
                if (!readmeFile.exists()) {
                    readmeFile = new File(currentProjectPath, "readme.md");
                }
                
                if (!readmeFile.exists()) {
                    // Create a new README template
                    try {
                        readmeFile = new File(currentProjectPath, "README.md");
                        FileWriter writer = new FileWriter(readmeFile);
                        writer.write("# " + new File(currentProjectPath).getName() + "\n\nDescription here...\n");
                        writer.close();
                        AppToast.showSuccess(ProjectDashboardActivity.this, "README.md created!");
                        RecentActivityManager.log(ProjectDashboardActivity.this, currentProjectPath, "open", "README.md file created", "\uf15b", false);
                        loadRecentActivities();
                    } catch (Exception e) {
                        AppToast.showError(ProjectDashboardActivity.this, "Failed to create README.");
                        return;
                    }
                }
                
                // Open the file in the Editor
                Intent intent = new Intent(ProjectDashboardActivity.this, EditorActivity.class);
                intent.putExtra("file_path", readmeFile.getAbsolutePath());
                startActivity(intent);
            }
        });
    }
    
    private void setIcon(TextView v, String unicode, Typeface font) {
        if (v != null) {
            v.setTypeface(font);
            v.setText(unicode);
        }
    }
    
    private void updatePinAndHiddenStatus() {
        ArrayList<ProjectItem> projects = ProjectManager.loadProjects(this);
        
        for (int i = 0; i < projects.size(); i++) {
            ProjectItem p = projects.get(i);
            
            // Find the current project in the list
            if (p.path.equals(currentProjectPath)) {
                
                // Set Pin Icon Visibility
                if (p.isPinned) {
                    pinnedIcon.setVisibility(View.VISIBLE);
                } else {
                    pinnedIcon.setVisibility(View.GONE);
                }
                
                // Set Hidden Icon Visibility
                if (p.isHidden) {
                    hiddenIcon.setVisibility(View.VISIBLE);
                } else {
                    hiddenIcon.setVisibility(View.GONE);
                }
                
                return; // Found the project, stop looping
            }
        }
        
        // Fallback: If not found in the list, hide both
        pinnedIcon.setVisibility(View.GONE);
        hiddenIcon.setVisibility(View.GONE);
    }
    
    private void loadRecentActivities() {
        ArrayList<ActivityItem> activities = RecentActivityManager.getActivities(currentProjectPath);
        activityContainerVisible.removeAllViews();
        activityContainerHidden.removeAllViews();
        
        if (activities.isEmpty()) {
            tvNoActivity.setVisibility(View.VISIBLE);
            recentActivityDropBtn.setVisibility(View.GONE);
            return;
        }
        
        tvNoActivity.setVisibility(View.GONE);
        
        // If we have 3 or less, hide the dropdown arrow
        if (activities.size() <= 3) {
            recentActivityDropBtn.setVisibility(View.GONE);
            activityContainerHidden.setVisibility(View.GONE);
        } else {
            recentActivityDropBtn.setVisibility(View.VISIBLE);
            recentActivityDropBtn.setVisibility(View.VISIBLE);
        }
        
        for (int i = 0; i < activities.size(); i++) {
            ActivityItem item = activities.get(i);
            
            // Inflate the custom layout
            View view = getLayoutInflater().inflate(R.layout.item_activity, null);
            
            TextView icon = view.findViewById(R.id.item_activity_icon);
            TextView text = view.findViewById(R.id.item_activity_text);
            TextView time = view.findViewById(R.id.item_activity_time);
            
            // Set Data
            setIcon(icon, item.iconUnicode, item.isBrandIcon ? brandsFont : solidFont);
            text.setText(item.message);
            time.setText(getFormattedTime(item.timestamp));
            
            // Route to visible (first 3) or hidden container
            if (i < 3) {
                activityContainerVisible.addView(view);
            } else {
                activityContainerHidden.addView(view);
            }
        }
    }
    
    private String getFormattedTime(long timestamp) {
        long now = System.currentTimeMillis();
        android.text.format.DateUtils.getRelativeTimeSpanString(timestamp, now, android.text.format.DateUtils.MINUTE_IN_MILLIS);
        
        // Simple custom logic for "Today", "Yesterday", or dates
        java.util.Calendar msgCal = java.util.Calendar.getInstance();
        msgCal.setTimeInMillis(timestamp);
        java.util.Calendar nowCal = java.util.Calendar.getInstance();
        nowCal.setTimeInMillis(now);
        
        String timeStr = android.text.format.DateFormat.format("h:mm a", msgCal).toString();
        
        if (msgCal.get(java.util.Calendar.YEAR) == nowCal.get(java.util.Calendar.YEAR) &&
            msgCal.get(java.util.Calendar.DAY_OF_YEAR) == nowCal.get(java.util.Calendar.DAY_OF_YEAR)) {
            return "Today, " + timeStr;
        }
        
        nowCal.add(java.util.Calendar.DAY_OF_YEAR, -1);
        if (msgCal.get(java.util.Calendar.YEAR) == nowCal.get(java.util.Calendar.YEAR) &&
            msgCal.get(java.util.Calendar.DAY_OF_YEAR) == nowCal.get(java.util.Calendar.DAY_OF_YEAR)) {
            return "Yesterday, " + timeStr;
        }
        
        return android.text.format.DateFormat.format("MMM d, h:mm a", msgCal).toString();
    }
    
    private void exportProjectReport() {
        if (currentProjectInfo == null) {
            AppToast.showError(this, "Please wait for analysis to complete.");
            return;
        }
    
        AppLoader.show(this, "Generating report...");
    
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File projectDir = new File(currentProjectPath);
                    final File reportFile = new File(projectDir, "Project_Report.md");
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append("# Project Report: ").append(projectDir.getName()).append("\n\n");
                    sb.append("**Generated on:** ").append(new java.util.Date().toString()).append("\n\n");
                    
                    sb.append("## General Information\n");
                    sb.append("- **Type:** ").append(currentProjectInfo.type).append("\n");
                    sb.append("- **Language:** ").append(currentProjectInfo.language).append("\n");
                    sb.append("- **Main File:** ").append(currentProjectInfo.mainFile).append("\n");
                    sb.append("- **Dependencies:** ").append(currentProjectInfo.dependencies).append("\n\n");
                    
                    sb.append("## Code Health\n");
                    sb.append("- **Lines of Code:** ").append(currentProjectInfo.totalLines).append("\n");
                    sb.append("- **TODOs / FIXMEs:** ").append(currentProjectInfo.todoCount).append("\n\n");
                    
                    sb.append("## Diagnostics\n");
                    if (currentProjectInfo.healthWarnings.isEmpty()) {
                        sb.append("- ✅ Project looks healthy!\n\n");
                    } else {
                        for (String warning : currentProjectInfo.healthWarnings) {
                            sb.append("- ⚠️ ").append(warning).append("\n");
                        }
                        sb.append("\n");
                    }
                    
                    sb.append("## Root Structure\n```text\n");
                    sb.append(projectDir.getName()).append("/\n");
                    if (currentProjectInfo.structurePreview.isEmpty()) {
                        sb.append("    └── (Empty Folder)\n");
                    } else {
                        for (String line : currentProjectInfo.structurePreview) {
                            sb.append(line).append("\n");
                        }
                    }
                    sb.append("```\n");
    
                    FileWriter writer = new FileWriter(reportFile);
                    writer.write(sb.toString());
                    writer.close();
    
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AppLoader.hide();
                            AppToast.showSuccess(ProjectDashboardActivity.this, "Report saved as " + reportFile.getName());
                            RecentActivityManager.log(ProjectDashboardActivity.this, currentProjectPath, "export", "Exported project report", "\uf56e", false);
                            loadRecentActivities();
                        }
                    });
    
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AppLoader.hide();
                            AppToast.showError(ProjectDashboardActivity.this, "Export failed: " + e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }
    
    private void loadDashboardData(boolean forceRefresh) {
        // 1. Load Stats
        ProjectStatsManager.getStats(this, currentProjectPath, forceRefresh, new ProjectStatsManager.StatsCallback() {
            @Override
            public void onStatsCalculated(final ProjectStats stats) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvFiles.setText(String.valueOf(stats.fileCount));
                        tvFolders.setText(String.valueOf(stats.folderCount));
                        String formattedSize = android.text.format.Formatter.formatShortFileSize(ProjectDashboardActivity.this, stats.totalSize);
                        tvSize.setText(formattedSize);
                        
                        if (stats.lastModified > 0) {
                            CharSequence relativeTime = android.text.format.DateUtils.getRelativeTimeSpanString(
                                    stats.lastModified, System.currentTimeMillis(), android.text.format.DateUtils.MINUTE_IN_MILLIS
                            );
                            String timeStr = relativeTime.toString();
                            if (timeStr.contains("minutes ago") || timeStr.contains("hours ago")) {
                                tvModified.setText("Today");
                            } else {
                                tvModified.setText(timeStr);
                            }
                        } else {
                            tvModified.setText("Unknown");
                        }
                    }
                });
            }
        });
    
        // 2. Analyze Project Type (This always runs fresh)
        ProjectAnalyzer.analyze(this, currentProjectPath, new ProjectAnalyzer.AnalyzeCallback() {
            @Override
            public void onAnalysisComplete(final ProjectAnalyzer.ProjectInfo info) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        currentProjectInfo = info; // Save for Export
                        
                        projectType.setText(info.type.toUpperCase() + " PROJECT");
                        if (info.isBrandIcon) {
                            setIcon(projectTypeIcon, info.iconUnicode, brandsFont);
                        } else {
                            setIcon(projectTypeIcon, info.iconUnicode, solidFont);
                        }
                        projectTypeIcon.setTextColor(info.iconColor);
                        
                        mainFileTv.setText(info.mainFile);
                        languageTv.setText(info.language);
                        dependenciesTv.setText(info.dependencies);
                        
                        tvLinesOfCode.setText(String.format(java.util.Locale.getDefault(), "%,d", info.totalLines));
                        tvTodos.setText(String.valueOf(info.todoCount));
                        
                        if (info.hasReadme) {
                            readmeTitle.setText("README.md");
                            readmePreview.setText(info.readmePreview);
                            readmeActionBtn.setText("Open README");
                        } else {
                            readmeTitle.setText("No README Found");
                            readmePreview.setText("It is highly recommended to document your project.");
                            readmeActionBtn.setText("+ Create README.md");
                        }
                        
                        if (info.type.equals("Python") || info.type.equals("Web") || info.type.equals("Node.js")) {
                            btnRun.setAlpha(1.0f);
                            btnRun.setClickable(true);
                        } else {
                            btnRun.setAlpha(0.4f);
                            btnRun.setClickable(false);
                        }
                        
                        // Health Warnings
                        healthWarningsContainer.removeAllViews();
                        if (info.healthWarnings.isEmpty()) {
                            tvHealthyStatus.setVisibility(View.VISIBLE);
                            healthWarningsContainer.setVisibility(View.GONE);
                        } else {
                            tvHealthyStatus.setVisibility(View.GONE);
                            healthWarningsContainer.setVisibility(View.VISIBLE);
                            
                            for (int i = 0; i < info.healthWarnings.size(); i++) {
                                String warning = info.healthWarnings.get(i);
                                
                                LinearLayout warningRow = new LinearLayout(ProjectDashboardActivity.this);
                                warningRow.setOrientation(LinearLayout.HORIZONTAL);
                                warningRow.setPadding(16, 16, 16, 16);
                                warningRow.setBackgroundResource(R.drawable.settings_section_bg);
                                
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                params.setMargins(0, 0, 0, 8);
                                warningRow.setLayoutParams(params);
                                
                                TextView icon = new TextView(ProjectDashboardActivity.this);
                                setIcon(icon, "\uf071", solidFont);
                                icon.setTextColor(Color.parseColor("#F85149"));
                                icon.setTextSize(16);
                                icon.setPadding(0, 0, 12, 0);
                                
                                TextView text = new TextView(ProjectDashboardActivity.this);
                                text.setText(warning);
                                text.setTextColor(Color.parseColor("#AAAAAA"));
                                text.setTextSize(13);
                                text.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/SpaceMono-Regular.ttf"));
                                
                                warningRow.addView(icon);
                                warningRow.addView(text);
                                healthWarningsContainer.addView(warningRow);
                            }
                        }
                        
                        // Project Structure
                        StringBuilder treeStr = new StringBuilder();
                        treeStr.append(new File(currentProjectPath).getName()).append("/\n");
                        if (info.structurePreview.isEmpty()) {
                            treeStr.append("    └── (Empty Folder)");
                        } else {
                            for (int i = 0; i < info.structurePreview.size(); i++) {
                                treeStr.append(info.structurePreview.get(i)).append("\n");
                            }
                        }
                        tvProjectStructure.setText(treeStr.toString().trim());
                    }
                });
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updatePinAndHiddenStatus();
        loadRecentActivities();
        loadDashboardData(false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
