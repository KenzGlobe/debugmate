package com.kenzglobe.debugmate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditorActivity extends Activity {

    private File currentFile;
    private EditText codeEditor;
    private Typeface codeFont, solidFont;
    private TextView fileNameView, fileNamePath, btnSave, btnBack;
    private LinearLayout tabContainer;

    // Syntax Highlighting Colors
    private final int COLOR_KEYWORD = Color.parseColor("#C586C0"); // Purple
    private final int COLOR_STRING = Color.parseColor("#CE9178");  // Orange
    private final int COLOR_COMMENT = Color.parseColor("#6A9955"); // Green
    private final int COLOR_NUMBER = Color.parseColor("#B5CEA8");  // Light Green
    private final int COLOR_XML_TAG = Color.parseColor("#569CD6"); // Blue

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor);
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#8B5CF6"));
            getWindow().getDecorView().setSystemUiVisibility(0);
        }

        if (getActionBar() != null) getActionBar().hide();
        
        codeFont = Typeface.createFromAsset(getAssets(), "fonts/FiraCode-Regular.ttf");
        solidFont = Typeface.createFromAsset(getAssets(), "fonts/fa-solid-900-v5.ttf");

        fileNameView = findViewById(R.id.editor_file_name);
        fileNamePath = findViewById(R.id.editor_file_path);
        codeEditor = findViewById(R.id.code_editor);
        btnSave = findViewById(R.id.btn_save);
        btnBack = findViewById(R.id.btn_back);
        tabContainer = findViewById(R.id.tab_container);
        
        codeEditor.setTypeface(codeFont);
        codeEditor.setHorizontallyScrolling(true); // Stop word wrap
        btnSave.setTypeface(solidFont);
        btnSave.setText("\uf0c7");
        btnBack.setTypeface(solidFont);
        btnBack.setText("\uf060");

        // ADD THE TEXT WATCHER FOR SYNTAX HIGHLIGHTING
        codeEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Call the highlighter every time the user types
                applySyntaxHighlighting(s);
            }
        });

        String filePath = getIntent().getStringExtra("file_path");
        if (filePath != null) {
            loadFile(new File(filePath));
        }

        buildTabs();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentFile != null) {
                    try {
                        FileWriter writer = new FileWriter(currentFile, false);
                        writer.write(codeEditor.getText().toString());
                        writer.close();
                        Toast.makeText(EditorActivity.this, "Saved successfully! ✔️", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(EditorActivity.this, "Error saving: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadFile(File file) {
        if (file == null || !file.exists()) return;
        
        currentFile = file;
        fileNameView.setText(currentFile.getName());
        fileNamePath.setText(currentFile.getAbsolutePath());

        try {
            StringBuilder text = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(currentFile));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
            
            // Setting the text will automatically trigger the TextWatcher and highlight it!
            codeEditor.setText(text.toString());
            
        } catch (Exception e) {
            Toast.makeText(this, "Error reading file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void applySyntaxHighlighting(Editable editable) {
        if (currentFile == null) return;

        String ext = currentFile.getName();
        int dotIndex = ext.lastIndexOf(".");
        if (dotIndex == -1) return;
        ext = ext.substring(dotIndex + 1).toLowerCase();

        // 1. Clear old highlights
        ForegroundColorSpan[] oldSpans = editable.getSpans(0, editable.length(), ForegroundColorSpan.class);
        for (ForegroundColorSpan span : oldSpans) {
            editable.removeSpan(span);
        }

        String text = editable.toString();

        // 2. Apply new highlights based on file type
        if (ext.equals("py")) {
            highlightPattern(editable, text, "\\b(def|class|import|from|return|if|else|elif|for|while|in|try|except|print|True|False|None|and|or|not)\\b", COLOR_KEYWORD);
            highlightPattern(editable, text, "#.*", COLOR_COMMENT);
            highlightPattern(editable, text, "\"[^\"]*\"|'[^']*'", COLOR_STRING);
            highlightPattern(editable, text, "\\b\\d+\\b", COLOR_NUMBER);
            
        } else if (ext.equals("java")) {
            highlightPattern(editable, text, "\\b(public|private|protected|class|void|int|String|boolean|if|else|for|while|return|new|static|final|import|package)\\b", COLOR_KEYWORD);
            highlightPattern(editable, text, "//.*", COLOR_COMMENT);
            highlightPattern(editable, text, "\"[^\"]*\"", COLOR_STRING);
            highlightPattern(editable, text, "\\b\\d+\\b", COLOR_NUMBER);
            
        } else if (ext.equals("xml")) {
            highlightPattern(editable, text, "</?[a-zA-Z0-9_:-]+", COLOR_XML_TAG);
            highlightPattern(editable, text, "\"[^\"]*\"", COLOR_STRING);
        }
    }

    private void highlightPattern(Editable editable, String text, String regex, int color) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            // setSpan applies the color without changing the actual text or cursor position
            editable.setSpan(
                new ForegroundColorSpan(color), 
                matcher.start(), 
                matcher.end(), 
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }

    private void buildTabs() {
        tabContainer.removeAllViews();
        SharedPreferences prefs = getSharedPreferences("DebugMatePrefs", MODE_PRIVATE);
        String recentStr = prefs.getString("recent_files", "");
        
        if (recentStr.isEmpty()) return;

        String[] paths = recentStr.split("\n");
        for (final String path : paths) {
            final File f = new File(path);
            if (!f.exists()) continue;

            LinearLayout tabLayout = new LinearLayout(this);
            tabLayout.setOrientation(LinearLayout.HORIZONTAL);
            tabLayout.setGravity(Gravity.CENTER_VERTICAL);
            tabLayout.setPadding(24, 16, 24, 16);
            
            if (currentFile != null && currentFile.getAbsolutePath().equals(path)) {
                tabLayout.setBackgroundColor(Color.parseColor("#262626"));
            } else {
                tabLayout.setBackgroundColor(Color.parseColor("#8B5CF6"));
            }

            TextView tabName = new TextView(this);
            tabName.setText(f.getName());
            tabName.setTextColor(Color.parseColor("#FFFFFF")); // Changed to white for better reading
            tabName.setTextSize(12);
            tabName.setTypeface(codeFont);
            
            TextView btnClose = new TextView(this);
            btnClose.setTypeface(solidFont);
            btnClose.setText("\uf00d");
            btnClose.setTextColor(Color.parseColor("#AAAAAA"));
            btnClose.setTextSize(14);
            btnClose.setPadding(16, 0, 0, 0);

            tabLayout.addView(tabName);
            tabLayout.addView(btnClose);

            tabLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadFile(f);
                    buildTabs();
                }
            });

            tabLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showFileInfo(f);
                    return true;
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
            if (!s.equals(path) && !s.isEmpty()) list.add(s);
        }

        StringBuilder sb = new StringBuilder();
        for (String s : list) sb.append(s).append("\n");
        prefs.edit().putString("recent_files", sb.toString().trim()).apply();

        if (currentFile != null && currentFile.getAbsolutePath().equals(path)) {
            if (list.isEmpty()) {
                finish();
            } else {
                loadFile(new File(list.get(0)));
            }
        }
        buildTabs();
    }

    private void showFileInfo(File file) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        String dateStr = sdf.format(new Date(file.lastModified()));
        
        long size = file.length();
        String sizeStr = size < 1024 ? size + " B" : (size < 1048576 ? (size / 1024) + " KB" : String.format("%.1f MB", size / 1048576f));

        new AlertDialog.Builder(this)
            .setTitle(file.getName())
            .setMessage("Path: " + file.getAbsolutePath() + 
                        "\n\nSize: " + sizeStr + 
                        "\n\nLast Modified: " + dateStr)
            .setPositiveButton("OK", null)
            .show();
    }
}