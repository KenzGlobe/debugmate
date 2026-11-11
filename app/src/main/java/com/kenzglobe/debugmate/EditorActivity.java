package com.kenzglobe.debugmate;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.Bundle;
import android.view.View;
import android.view.Gravity;
import android.text.TextWatcher;
import android.text.Editable;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.AdapterView;
import android.widget.*;
import org.json.JSONArray;
import java.io.*;
import java.util.ArrayList;

public class EditorActivity extends Activity {

    private File currentFile;
    private WebView codeEditorWebView;
    private android.support.v4.widget.DrawerLayout editorDrawer;
    private ListView treeList;
    private FrameLayout colorPickerView;
    private com.kenzglobe.debugmate.ColorPickerView colorPickerWheel;
    private View colorPreview;
    
    private EditText colorHexText,
        gotoLineInput;
        
    private Button btnInsertColor,
        reduceFontSize,
        increaseFontSize,
        btnGoToLine;
    
    private TextView treeRootName,
        treeUpBtn,
        settingsBtn,
        createFolder,
        createFile,
        palette,
        btnClosePalette,
        currentFontSize,
        dropActivityBarBtn,
        navExplorerIc,
        navSearchIc,
        navGitIc,
        navRunIc,
        navExtensionsIc,
        navAccountsIc;

    private LinearLayout sideBarHeader, activityBarLayout, panelExplorer, panelSourceControl, navExplorer, navSearch, navGit, navRun, navExtensions, navAccounts, bottomPanel;

    private File treeCurrentDir;
    private EditorTreeAdapter treeAdapter;
    private ArrayList<File> treeFiles = new ArrayList<>();
    private boolean editorReady = false;
    private String pendingCode = null;
    private String pendingExtension = "text";
    private int currentEditorFontSize;
    private int pendingJumpToLine = -1;

    private Typeface appFont, solidFont, brandsRegularFont;

    private TextView fileNameView, fileNamePath, sideBarBtn,
            btnBack, btnSave, btnFind, btnUndo, btnRedo, btnRun;

    private LinearLayout tabContainer;
    
    private ListView gitFileList;
    private Button btnGitStatus, btnGitAdd, btnGitCommit, btnGitPush;
    private com.kenzglobe.debugmate.AppEditText gitCommitMessage;
    private ArrayList<GitFileItem> changedFilesList = new ArrayList<>();
    private GitAdapter gitFileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#8B5CF6"));
            getWindow().getDecorView().setSystemUiVisibility(0);
        }

        if (getActionBar() != null) {
            getActionBar().hide();
        }
        
        pendingJumpToLine = getIntent().getIntExtra("jump_to_line", -1);
        
        SharedPreferences prefsFontSize = getSharedPreferences("DebugMatePrefs", MODE_PRIVATE);
        currentEditorFontSize = prefsFontSize.getInt("editor_font_size", 14);

        appFont = Typeface.createFromAsset(getAssets(), "fonts/SpaceMono-Bold.ttf");
        solidFont = Typeface.createFromAsset(getAssets(), "fonts/fa-solid-900-v5.ttf");
        brandsRegularFont = Typeface.createFromAsset(getAssets(), "fonts/fa-brands-regular-400-v5.ttf");

        fileNameView = findViewById(R.id.editor_file_name);
        fileNamePath = findViewById(R.id.editor_file_path);
        sideBarBtn = findViewById(R.id.side_bar);
        codeEditorWebView = findViewById(R.id.code_editor_webview);
        btnBack = findViewById(R.id.btn_back);
        btnSave = findViewById(R.id.btn_save);
        btnFind = findViewById(R.id.btn_find);
        btnUndo = findViewById(R.id.btn_undo);
        btnRedo = findViewById(R.id.btn_redo);
        btnRun = findViewById(R.id.btn_run);
        tabContainer = findViewById(R.id.tab_container);
        colorPickerView = findViewById(R.id.color_picker_view);

        setupBottomIcon(sideBarBtn, "\uf07b", solidFont);
        setupBottomIcon(btnBack, "\uf060", solidFont);
        setupBottomIcon(btnSave, "\uf0c7", solidFont);
        setupBottomIcon(btnFind, "\uf002", solidFont);
        setupBottomIcon(btnUndo, "\uf0e2", solidFont);
        setupBottomIcon(btnRedo, "\uf01e", solidFont);
        setupBottomIcon(btnRun, "\uf04b", solidFont);

        fileNameView.setTypeface(appFont);
        fileNamePath.setTypeface(appFont);
        
        editorDrawer = findViewById(R.id.editor_drawer);
        treeList = findViewById(R.id.tree_list);
        treeRootName = findViewById(R.id.tree_root_name);
        treeUpBtn = findViewById(R.id.tree_up_btn);
        settingsBtn = findViewById(R.id.settings_btn);
        createFolder = findViewById(R.id.create_folder);
        createFile = findViewById(R.id.create_file);
        palette = findViewById(R.id.palette);
        colorPickerWheel = findViewById(R.id.color_picker_wheel);
        colorPreview = findViewById(R.id.color_preview);
        colorHexText = findViewById(R.id.color_hex_text);
        btnInsertColor = findViewById(R.id.btn_insert_color);
        btnClosePalette = findViewById(R.id.btn_close_palette);
        reduceFontSize = findViewById(R.id.reduce_font_size);
        increaseFontSize = findViewById(R.id.increase_font_size);
        currentFontSize = findViewById(R.id.current_font_size);
        gotoLineInput = findViewById(R.id.goto_line_input);
        btnGoToLine = findViewById(R.id.btn_goto_line);
        
        // --- NEW: Initialize Activity Bar Views ---
        sideBarHeader = findViewById(R.id.sidebar_header);
        dropActivityBarBtn = findViewById(R.id.drop_activity_bar_btn);
        activityBarLayout = findViewById(R.id.activity_bar_layout);
        panelExplorer = findViewById(R.id.panel_explorer);
        panelSourceControl = findViewById(R.id.panel_source_control);
        navExplorer = findViewById(R.id.nav_explorer);
        navExplorerIc = findViewById(R.id.nav_explorer_ic);
        navSearch = findViewById(R.id.nav_search);
        navSearchIc = findViewById(R.id.nav_search_ic);
        navGit = findViewById(R.id.nav_git);
        navGitIc = findViewById(R.id.nav_git_ic);
        navRun = findViewById(R.id.nav_run);
        navRunIc = findViewById(R.id.nav_run_ic);
        navExtensions = findViewById(R.id.nav_extensions);
        navExtensionsIc = findViewById(R.id.nav_extensions_ic);
        navAccounts = findViewById(R.id.nav_accounts);
        navAccountsIc = findViewById(R.id.nav_accounts_ic);
        bottomPanel = findViewById(R.id.bottom_panel);
        
        gitFileList = findViewById(R.id.git_file_list);
        btnGitStatus = findViewById(R.id.btn_git_status);
        btnGitAdd = findViewById(R.id.btn_git_add);
        btnGitCommit = findViewById(R.id.btn_git_commit);
        btnGitPush = findViewById(R.id.btn_git_push);
        gitCommitMessage = findViewById(R.id.git_commit_message);
        
        gitFileAdapter = new GitAdapter(this, changedFilesList);
        gitFileList.setAdapter(gitFileAdapter);

        dropActivityBarBtn.setTypeface(solidFont);
        dropActivityBarBtn.setText("\uf060"); // Back arrow icon
        // -------------------------------------------

        treeRootName.setTypeface(appFont);
        treeUpBtn.setTypeface(solidFont);
        treeUpBtn.setText("\uf062");
        navExplorerIc.setTypeface(solidFont);
        navExplorerIc.setText("\uf07b");
        navSearchIc.setTypeface(solidFont);
        navSearchIc.setText("\uf002");
        navGitIc.setTypeface(solidFont);
        navGitIc.setText("\uf126");
        navRunIc.setTypeface(solidFont);
        navRunIc.setText("\uf04b");
        navExtensionsIc.setTypeface(solidFont);
        navExtensionsIc.setText("\uf12e");
        navAccountsIc.setTypeface(solidFont);
        navAccountsIc.setText("\uf007");
        settingsBtn.setTypeface(solidFont);
        settingsBtn.setText("\uf013");
        createFolder.setTypeface(solidFont);
        createFolder.setText("\uf65e");
        createFile.setTypeface(solidFont);
        createFile.setText("\uf067");
        reduceFontSize.setTypeface(solidFont);
        reduceFontSize.setText("\uf068");
        increaseFontSize.setTypeface(solidFont);
        increaseFontSize.setText("\uf067");
        currentFontSize.setText(currentEditorFontSize + "px");
        gotoLineInput.setTypeface(appFont);
        gotoLineInput.setHint("1 - 0");
        
        // --- NEW: Activity Bar Click Listeners ---
        navExplorer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPanel("explorer");
            }
        });

        navGit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPanel("git");
            }
        });

        dropActivityBarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPanel("menu");
            }
        });
        // ------------------------------------------

        reduceFontSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentEditorFontSize > 8) {
                    currentEditorFontSize--;
                    currentFontSize.setText(currentEditorFontSize + "px");
                    updateEditorFontSize();
                }
            }
        });

        increaseFontSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentEditorFontSize < 32) {
                    currentEditorFontSize++;
                    currentFontSize.setText(currentEditorFontSize + "px");
                    updateEditorFontSize();
                }
            }
        });
            
        treeUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (treeUpBtn.getText().toString().equals("\uf062")) {
                    if (treeCurrentDir != null && treeCurrentDir.getParentFile() != null) {
                        loadTree(treeCurrentDir.getParentFile());
                    }
                } else {
                    AppToast.showInfo(EditorActivity.this, "Not arrow icon!!");
                }
            }
        });
        
        btnGoToLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = gotoLineInput.getText().toString().trim();
                if (!input.isEmpty()) {
                    try {
                        int line = Integer.parseInt(input);
                        if (line > 0) {
                            goToLine(line);
                            gotoLineInput.setText(""); 
                            editorDrawer.closeDrawer(Gravity.LEFT); 
                        } else {
                            AppToast.showInfo(EditorActivity.this, "Line must be greater than 0");
                        }
                    } catch (NumberFormatException e) {
                        AppToast.showError(EditorActivity.this, "Invalid number");
                    }
                }
            }
        });
        
        // Open drawer when sidebar button is clicked
        sideBarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPanel("menu"); // Default to menu when opening drawer
                editorDrawer.openDrawer(Gravity.LEFT);
            }
        });
        
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.content.Intent intent = new android.content.Intent(EditorActivity.this, EditorSettingsActivity.class);
                startActivity(intent);
            }
        });
        
        createFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateFolderDialog();
            }
        });

        createFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateFileDialog();
            }
        });
        
        palette.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editorDrawer.closeDrawer(Gravity.LEFT);
                colorPickerView.setVisibility(View.VISIBLE);
            }
        });
        
        colorPickerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorPickerView.setVisibility(View.GONE);
            }
        });
        
        colorPickerWheel.setOnColorChangedListener(new ColorPickerView.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                colorPreview.setBackgroundColor(color);
                String hex = String.format("#%06X", (0xFFFFFF & color));
                if (!colorHexText.getText().toString().equalsIgnoreCase(hex)) {
                    colorHexText.setText(hex);
                    colorHexText.setSelection(hex.length()); 
                }
            }
        });
        
        colorHexText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String hex = s.toString().trim();
                if (hex.length() == 6 && !hex.startsWith("#")) {
                    hex = "#" + hex;
                    colorHexText.setText(hex);
                    colorHexText.setSelection(7);
                    return;
                }
                if (hex.length() == 7 && hex.startsWith("#")) {
                    try {
                        int color = Color.parseColor(hex);
                        colorPreview.setBackgroundColor(color);
                        colorPickerWheel.setColorFromHex(color);
                    } catch (IllegalArgumentException e) {}
                }
            }
        });

        btnInsertColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hex = colorHexText.getText().toString();
                codeEditorWebView.evaluateJavascript("insertText('" + hex + "');", null);
                colorPickerView.setVisibility(View.GONE);
            }
        });
        
        btnClosePalette.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorPickerView.setVisibility(View.GONE);
            }
        });

        treeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File clickedFile = treeFiles.get(position);
                if (clickedFile.isDirectory()) {
                    loadTree(clickedFile);
                } else {
                    String filePath = clickedFile.getAbsolutePath();
                    String ext = "";
                    int dotIndex = filePath.lastIndexOf('.');
                    if (dotIndex != -1) {
                        ext = filePath.substring(dotIndex + 1).toLowerCase();
                    }
                    
                    boolean textFiles = ext.equals("py") ||
                        ext.equals("java") ||
                        ext.equals("xml") ||
                        ext.equals("txt") ||
                        ext.equals("md") ||
                        ext.equals("js") ||
                        ext.equals("json") ||
                        ext.equals("html") ||
                        ext.equals("css") ||
                        ext.equals("sql") ||
                        ext.equals("gitignore") ||
                        ext.equals("gradle") ||
                        ext.equals("pro") ||
                        ext.equals("svg") ||
                        ext.equals("sh") ||
                        ext.equals("");
                    
                    if (textFiles) {
                        android.content.SharedPreferences prefs = getSharedPreferences("DebugMatePrefs", MODE_PRIVATE);
                        String recentStr = prefs.getString("recent_files", "");
                        ArrayList<String> recentList = new ArrayList<>();
                        
                        if (!recentStr.isEmpty()) {
                            String[] arr = recentStr.split("\n");
                            for (String s : arr) recentList.add(s);
                        }
                        
                        while (recentList.contains(filePath)) recentList.remove(filePath);
                        recentList.add(0, filePath);
                        while (recentList.size() > 20) recentList.remove(recentList.size() - 1);
                        
                        StringBuilder sb = new StringBuilder();
                        for (String s : recentList) sb.append(s).append("\n");
                        prefs.edit().putString("recent_files", sb.toString().trim()).apply();
                    
                        loadFile(clickedFile);
                        buildTabs();
                        editorDrawer.closeDrawer(Gravity.LEFT);
                    } else {
                        AppToast.showInfo(EditorActivity.this, "Feature not available yet.");
                    }
                }
            }
        });
        
        // --- Git Panel Logic ---
        btnGitStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (treeCurrentDir == null) return;
                AppToast.showInfo(EditorActivity.this, "Checking status...");
                
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final ArrayList<GitFileItem> status = GitManager.getStatus(treeCurrentDir);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                changedFilesList.clear();
                                changedFilesList.addAll(status);
                                gitFileAdapter.notifyDataSetChanged();
                                if (changedFilesList.isEmpty()) {
                                    AppToast.showSuccess(EditorActivity.this, "Working tree clean!");
                                }
                            }
                        });
                    }
                }).start();
            }
        });
        
        btnGitAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (treeCurrentDir == null) return;
                AppToast.showInfo(EditorActivity.this, "Staging changes...");
                
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final String result = GitManager.stageAll(treeCurrentDir);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result.startsWith("Success")) {
                                    AppToast.showSuccess(EditorActivity.this, "Staged successfully!");
                                    btnGitStatus.performClick(); // Refresh the list
                                } else {
                                    AppToast.showError(EditorActivity.this, result);
                                }
                            }
                        });
                    }
                }).start();
            }
        });
        
        btnGitCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (treeCurrentDir == null) return;
                final String msg = gitCommitMessage.getText().toString().trim();
                if (msg.isEmpty()) {
                    AppToast.showError(EditorActivity.this, "Enter a commit message!");
                    return;
                }
                
                AppToast.showInfo(EditorActivity.this, "Committing...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final String result = GitManager.commit(treeCurrentDir, msg);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result.startsWith("Success")) {
                                    AppToast.showSuccess(EditorActivity.this, result);
                                    gitCommitMessage.setText("");
                                    btnGitStatus.performClick(); // Refresh list (should be empty now)
                                } else {
                                    AppToast.showError(EditorActivity.this, result);
                                }
                            }
                        });
                    }
                }).start();
            }
        });
        
        btnGitPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (treeCurrentDir == null) return;
                
                // Get credentials from SharedPreferences (saved during Clone)
                SharedPreferences prefs = getSharedPreferences("DebugMatePrefs", MODE_PRIVATE);
                final String user = prefs.getString("git_username", "");
                final String token = prefs.getString("git_token", "");
                
                if (user.isEmpty() || token.isEmpty()) {
                    AppToast.showError(EditorActivity.this, "No GitHub credentials found. Please clone a repo first.");
                    return;
                }
                
                AppToast.showInfo(EditorActivity.this, "Pushing to GitHub...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final String result = GitManager.push(treeCurrentDir, user, token);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result.startsWith("Success")) {
                                    AppToast.showSuccess(EditorActivity.this, result);
                                } else {
                                    AppToast.showError(EditorActivity.this, result);
                                }
                            }
                        });
                    }
                }).start();
            }
        });

        codeEditorWebView.getSettings().setJavaScriptEnabled(true);
        codeEditorWebView.getSettings().setDomStorageEnabled(true);

        codeEditorWebView.addJavascriptInterface(new WebAppInterface(), "AndroidBridge");

        codeEditorWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                view.evaluateJavascript("typeof setCode === 'function' && window.editorReady === true", new android.webkit.ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String result) {
                        editorReady = "true".equals(result);

                        if (editorReady) {
                            SharedPreferences prefs = getSharedPreferences("DebugMatePrefs", MODE_PRIVATE);
                            boolean wrap = prefs.getBoolean("editor_word_wrap", false);
                            boolean lines = prefs.getBoolean("editor_line_numbers", true);
                            
                            codeEditorWebView.evaluateJavascript("setWordWrap(" + wrap + ");", null);
                            codeEditorWebView.evaluateJavascript("setShowLineNumbers(" + lines + ");", null);
                            
                            updateEditorFontSize();
                            
                            if (pendingCode != null) {
                                sendCodeToEditor(pendingCode, pendingExtension);
                                pendingCode = null;
                            }
                            
                            if (pendingJumpToLine > 0) {
                                final int lineToJump = pendingJumpToLine;
                                codeEditorWebView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        goToLine(lineToJump);
                                    }
                                }, 500);
                            }
                        }
                    }
                });
            }
        });

        codeEditorWebView.loadUrl("file:///android_asset/www/editor.html");

        String filePath = getIntent().getStringExtra("file_path");

        if (filePath == null || filePath.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("DebugMatePrefs", MODE_PRIVATE);
            String recentStr = prefs.getString("recent_files", "");
            
            if (!recentStr.isEmpty()) {
                String[] paths = recentStr.split("\n");
                for (String path : paths) {
                    File f = new File(path);
                    if (f.exists() && f.isFile()) {
                        filePath = path;
                        break;
                    }
                }
            }
        }

        if (filePath != null && !filePath.isEmpty()) {
            loadFile(new File(filePath));
        }

        buildTabs();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish(); }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { saveCurrentFile(); }
        });

        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { runEditorCommand("find();"); }
        });

        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { runEditorCommand("undo();"); }
        });

        btnRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { runEditorCommand("redo();"); }
        });

        btnRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editorReady) {
                    AppToast.showInfo(EditorActivity.this, "Editor is still loading...");
                    return;
                }

                if (currentFile == null) {
                    AppToast.showInfo(EditorActivity.this, "No file open.");
                    return;
                }

                String ext = "";
                String name = currentFile.getName();
                int dotIndex = name.lastIndexOf('.');
                if (dotIndex != -1) {
                    ext = name.substring(dotIndex + 1).toLowerCase();
                }

                if (ext.equals("py")) {
                    codeEditorWebView.evaluateJavascript("getCode();", new android.webkit.ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String result) {
                            try {
                                String cleanCode = new JSONArray("[" + result + "]").getString(0);
                                android.content.Intent intent = new android.content.Intent(EditorActivity.this, ConsoleActivity.class);
                                intent.putExtra("python_code", cleanCode);
                                intent.putExtra("python_dir", currentFile.getParent()); 
                                startActivity(intent);
                            } catch (Exception e) {
                                AppToast.showError(EditorActivity.this, "Error getting code: " + e.getMessage());
                            }
                        }
                    });
                    return;
                }
                
                if (!ext.equals("html") && !ext.equals("css") && !ext.equals("js")) {
                    AppToast.showError(EditorActivity.this, "Running ." + ext + " is not supported yet.");
                    return;
                }

                codeEditorWebView.evaluateJavascript("getCode();", new android.webkit.ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String result) {
                        try {
                            if (result == null || "null".equals(result)) {
                                throw new Exception("Editor returned no content.");
                            }

                            String cleanCode = new JSONArray("[" + result + "]").getString(0);
                            
                            android.content.Intent intent = new android.content.Intent(EditorActivity.this, WebBrowserActivity.class);
                            intent.putExtra("code", cleanCode);
                            intent.putExtra("file_name", currentFile.getName());

                            String parentDir = currentFile.getParent();
                            intent.putExtra("base_url", "file://" + parentDir + "/");

                            String ext = "";
                            int dotIndex = currentFile.getName().lastIndexOf('.');
                            if (dotIndex != -1) {
                                ext = currentFile.getName().substring(dotIndex + 1).toLowerCase();
                            }
                            intent.putExtra("extension", ext);

                            startActivity(intent);

                        } catch (Exception e) {
                            AppToast.showError(EditorActivity.this, "Error launching preview: " + e.getMessage());
                        }
                    }
                });
            }
        });
    }

    // Handles switching between Activity Bar menu and panels ---
    private void showPanel(String panelName) {
        // Hide everything first
        activityBarLayout.setVisibility(View.GONE);
        panelExplorer.setVisibility(View.GONE);
        panelSourceControl.setVisibility(View.GONE);
        
        if (panelName.equals("menu")) {
            sideBarHeader.setVisibility(View.GONE);
            bottomPanel.setVisibility(View.GONE);
            activityBarLayout.setVisibility(View.VISIBLE);
            dropActivityBarBtn.setVisibility(View.GONE);
        } else if (panelName.equals("explorer")) {
            sideBarHeader.setVisibility(View.VISIBLE);
            bottomPanel.setVisibility(View.VISIBLE);
            treeRootName.setText(treeCurrentDir.getName());
            treeUpBtn.setText("\uf062");
            panelExplorer.setVisibility(View.VISIBLE);
            dropActivityBarBtn.setVisibility(View.VISIBLE);
        } else if (panelName.equals("git")) {
            sideBarHeader.setVisibility(View.VISIBLE);
            bottomPanel.setVisibility(View.GONE);
            treeRootName.setText("Source Control");
            treeUpBtn.setText("\uf126");
            panelSourceControl.setVisibility(View.VISIBLE);
            dropActivityBarBtn.setVisibility(View.VISIBLE);
        }
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void saveRequested(String content) {}

        @JavascriptInterface
        public void copyText(String text) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("DebugMate", text);
                clipboard.setPrimaryClip(clip);
            }
        }
    
        @JavascriptInterface
        public String getClipboardText() {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                ClipData clip = clipboard.getPrimaryClip();
                if (clip != null && clip.getItemCount() > 0) {
                    CharSequence text = clip.getItemAt(0).getText();
                    if (text != null) return text.toString();
                }
            }
            return "";
        }
        
        @JavascriptInterface
        public void updateLineCount(final int count) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (gotoLineInput != null) {
                        gotoLineInput.setHint("1 - " + count);
                    }
                }
            });
        }
    }
    
    private void loadTree(File dir) {
        if (dir == null) return;
        treeCurrentDir = dir;
        treeRootName.setText(dir.getName());
        treeFiles.clear();

        File[] files = dir.listFiles();
        if (files != null) {
            java.util.Arrays.sort(files, new java.util.Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    if (f1.isDirectory() && !f2.isDirectory()) return -1;
                    if (!f1.isDirectory() && f2.isDirectory()) return 1;
                    return f1.getName().compareToIgnoreCase(f2.getName());
                }
            });
            for (File f : files) {
                treeFiles.add(f);
            }
        }

        treeAdapter = new EditorTreeAdapter(this, treeFiles, currentFile);
        treeList.setAdapter(treeAdapter);
    }

    private void setupBottomIcon(TextView view, String unicode, Typeface font) {
        view.setTypeface(font);
        view.setText(unicode);
    }
    
    private void goToLine(int line) {
        if (editorReady) {
            codeEditorWebView.evaluateJavascript("goToLine(" + line + ");", null);
        }
    }

    private void runEditorCommand(String command) {
        if (editorReady) {
            codeEditorWebView.evaluateJavascript(command, null);
        }
    }

    private void sendCodeToEditor(String code, String extension) {
        if (!editorReady) {
            pendingCode = code;
            pendingExtension = extension;
            return;
        }

        String jsCommand = "setCode("
                + org.json.JSONObject.quote(code)
                + ", "
                + org.json.JSONObject.quote(extension)
                + ");";

        codeEditorWebView.evaluateJavascript(jsCommand, null);
    }
    
    private void updateEditorFontSize() {
        if (editorReady) {
            codeEditorWebView.evaluateJavascript("setEditorFontSize(" + currentEditorFontSize + ");", null);
        }
    }

    private void loadFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            AppToast.showError(EditorActivity.this, "File not found.");
            return;
        }

        currentFile = file;

        fileNameView.setText(file.getName());
        fileNamePath.setText(file.getAbsolutePath());

        try {
            StringBuilder text = new StringBuilder();

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    text.append(line).append('\n');
                }
            }

            String name = file.getName();
            String ext = "";
            int dotIndex = name.lastIndexOf('.');

            if (dotIndex != -1 && dotIndex < name.length() - 1) {
                ext = name.substring(dotIndex + 1).toLowerCase(java.util.Locale.ROOT);
            }

            pendingCode = text.toString();
            pendingExtension = ext;

            if (editorReady) {
                sendCodeToEditor(pendingCode, pendingExtension);
                pendingCode = null;
            }
            
            if (treeCurrentDir == null || !file.getParentFile().getAbsolutePath().equals(treeCurrentDir.getAbsolutePath())) {
                loadTree(file.getParentFile());
            } else {
                if (treeAdapter != null) {
                    treeAdapter.notifyDataSetChanged();
                }
            }

        } catch (Exception e) {
            AppToast.showError(EditorActivity.this, "Error reading file: " + e.getMessage());
        }
    }
    
    private void showCreateFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
        builder.setTitle("New Folder");
        final EditText input = new EditText(EditorActivity.this);
        input.setHint("e.g., Folder");
        input.setPadding(40, 40, 40, 40); 
        builder.setView(input);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String folderName = input.getText().toString().trim();
                if (folderName.isEmpty()) {
                    AppToast.showError(EditorActivity.this, "Folder name cannot be empty.");
                    return;
                }
                if (treeCurrentDir == null) {
                    AppToast.showInfo(EditorActivity.this, "No directory open.");
                    return;
                }
                File newFolder = new File(treeCurrentDir, folderName);
                if (newFolder.mkdir()) {
                    AppToast.showSuccess(EditorActivity.this, "Created: " + folderName);
                    loadTree(treeCurrentDir);
                } else {
                    AppToast.showError(EditorActivity.this, "Folder already exists!");
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void showCreateFileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
        builder.setTitle("New File");
        final EditText input = new EditText(EditorActivity.this);
        input.setHint("e.g., main.py");
        input.setPadding(40, 40, 40, 40); 
        builder.setView(input);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String fileName = input.getText().toString().trim();
                if (fileName.isEmpty()) {
                    AppToast.showError(EditorActivity.this, "File name cannot be empty.");
                    return;
                }
                if (treeCurrentDir == null) {
                    AppToast.showError(EditorActivity.this, "No directory open.");
                    return;
                }
                File newFile = new File(treeCurrentDir, fileName);
                try {
                    if (newFile.createNewFile()) {
                        AppToast.showSuccess(EditorActivity.this, "Created: " + fileName);
                        loadTree(treeCurrentDir);
                    } else {
                        AppToast.showError(EditorActivity.this, "File already exists!");
                    }
                } catch (IOException e) {
                    AppToast.showError(EditorActivity.this, "Error creating file: " + e.getMessage());
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveCurrentFile() {
        if (currentFile == null) {
            AppToast.showError(EditorActivity.this, "No file selected.");
            return;
        }

        if (!editorReady) {
            AppToast.showInfo(EditorActivity.this, "Editor is still loading.");
            return;
        }

        final File fileToSave = currentFile;

        codeEditorWebView.evaluateJavascript("getCode();", new android.webkit.ValueCallback<String>() {
            @Override
            public void onReceiveValue(String result) {
                try {
                    if (result == null || "null".equals(result)) {
                        throw new Exception("Editor returned no content.");
                    }

                    String cleanCode = new JSONArray("[" + result + "]").getString(0);

                    try (FileWriter writer = new FileWriter(fileToSave, false)) {
                        writer.write(cleanCode);
                    }

                    AppToast.showSuccess(EditorActivity.this, "Saved successfully! ✔️");

                } catch (Exception e) {
                    AppToast.showError(EditorActivity.this, "Error saving: " + e.getMessage());
                }
            }
        });
    }

    private void buildTabs() {
        tabContainer.removeAllViews();
        SharedPreferences prefs = getSharedPreferences("DebugMatePrefs", MODE_PRIVATE);
        String recentStr = prefs.getString("recent_files", "");

        if (recentStr.isEmpty()) return;

        String[] paths = recentStr.split("\n");

        for (final String path : paths) {
            final File f = new File(path);
            if (!f.exists() || !f.isFile()) continue;

            LinearLayout tabLayout = new LinearLayout(this);
            tabLayout.setOrientation(LinearLayout.HORIZONTAL);
            tabLayout.setGravity(Gravity.CENTER_VERTICAL);
            tabLayout.setPadding(24, 0, 9, 0);

            if (currentFile != null && currentFile.getAbsolutePath().equals(f.getAbsolutePath())) {
                tabLayout.setBackgroundResource(R.drawable.tab_active);
            }

            TextView tabName = new TextView(this);
            tabName.setText(f.getName());
            tabName.setTextColor(Color.parseColor("#FFFFFF"));
            tabName.setTextSize(12);
            tabName.setTypeface(appFont);

            TextView btnClose = new TextView(this);
            btnClose.setTypeface(solidFont);
            btnClose.setText("\uf00d");
            btnClose.setTextColor(Color.parseColor("#AAAAAA"));
            btnClose.setTextSize(16);
            btnClose.setPadding(15, 10, 15, 10);

            tabLayout.addView(tabName);
            tabLayout.addView(btnClose);

            tabLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadFile(f);
                    buildTabs();
                }
            });

            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeTab(path);
                }
            });

            tabContainer.addView(tabLayout);
        }
    }

    private void closeTab(String path) {
        SharedPreferences prefs = getSharedPreferences("DebugMatePrefs", MODE_PRIVATE);
        String recentStr = prefs.getString("recent_files", "");
        ArrayList<String> list = new ArrayList<>();

        for (String s : recentStr.split("\n")) {
            if (!s.equals(path) && !s.isEmpty()) {
                list.add(s);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s).append("\n");
        }

        prefs.edit().putString("recent_files", sb.toString().trim()).apply();

        if (currentFile != null && currentFile.getAbsolutePath().equals(path)) {
            if (list.isEmpty()) {
                finish();
                return;
            } else {
                loadFile(new File(list.get(0)));
            }
        }
        buildTabs();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("DebugMatePrefs", MODE_PRIVATE);
        int newSize = prefs.getInt("editor_font_size", 14);
        boolean wrap = prefs.getBoolean("editor_word_wrap", false);
        boolean lines = prefs.getBoolean("editor_line_numbers", true);

        if (newSize != currentEditorFontSize) {
            currentEditorFontSize = newSize;
            if (currentFontSize != null) {
                currentFontSize.setText(currentEditorFontSize + "px");
            }
            updateEditorFontSize();
        }
    
        if (editorReady) {
            codeEditorWebView.evaluateJavascript("setWordWrap(" + wrap + ");", null);
            codeEditorWebView.evaluateJavascript("setShowLineNumbers(" + lines + ");", null);
        }
    }
    
    @Override
    public void onBackPressed() {
        if (editorDrawer.isDrawerOpen(Gravity.LEFT)) {
            editorDrawer.closeDrawer(Gravity.LEFT);
        } else {
            super.onBackPressed();
        }
    }
}
