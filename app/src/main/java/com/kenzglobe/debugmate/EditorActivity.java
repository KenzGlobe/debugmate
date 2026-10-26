package com.kenzglobe.debugmate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
    private Typeface appFont, codeFont, solidFont;
    private ScrollView scrollNumbers;
    private TextView fileNameView, fileNamePath, sideBarBtn, lineNumbersView, btnSave, btnBack;
    private LinearLayout tabContainer;

    private final int COLOR_KEYWORD = Color.parseColor("#569CD6");
    private final int COLOR_STRING = Color.parseColor("#CE9178");
    private final int COLOR_COMMENT = Color.parseColor("#555555");
    private final int COLOR_NUMBER = Color.parseColor("#B5CEA8");
    private final int COLOR_TYPE = Color.parseColor("#4EC9B0");
    private final int COLOR_METHOD = Color.parseColor("#DCDCAA");
    private final int COLOR_ANNOTATION = Color.parseColor("#DCDCAA");
    private final int COLOR_CONSTANT = Color.parseColor("#4FC1FF");
    private final int COLOR_OPERATOR = Color.parseColor("#D4D4D4");
    private final int COLOR_CHARACTER = Color.parseColor("#CE9178");
    private final int COLOR_BRACKET = Color.parseColor("#FFD700");
    private final int COLOR_XML_TAG = Color.parseColor("#569CD6");
    
    // DEBOUNCE HANDLER
    private Handler highlightHandler = new Handler(Looper.getMainLooper());
    private Runnable highlightRunnable = new Runnable() {
        @Override
        public void run() {
            applySyntaxHighlighting(codeEditor.getText());
        }
    };

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
        
        appFont = Typeface.createFromAsset(getAssets(), "fonts/SpaceMono-Bold.ttf");
        codeFont = Typeface.createFromAsset(getAssets(), "fonts/FiraCode-Regular.ttf");
        solidFont = Typeface.createFromAsset(getAssets(), "fonts/fa-solid-900-v5.ttf");

        fileNameView = findViewById(R.id.editor_file_name);
        fileNamePath = findViewById(R.id.editor_file_path);
        sideBarBtn = findViewById(R.id.side_bar);
        scrollNumbers = findViewById(R.id.scroll_numbers);
        lineNumbersView = findViewById(R.id.line_numbers);
        codeEditor = findViewById(R.id.code_editor);
        btnSave = findViewById(R.id.btn_save);
        btnBack = findViewById(R.id.btn_back);
        tabContainer = findViewById(R.id.tab_container);
        
        fileNameView.setTypeface(appFont);
        fileNamePath.setTypeface(appFont);
        codeEditor.setTypeface(codeFont);
        codeEditor.setHorizontallyScrolling(true);
        lineNumbersView.setTypeface(codeFont);
        
        sideBarBtn.setTypeface(solidFont);
        sideBarBtn.setText("\uf0c9");
        
        btnSave.setTypeface(solidFont);
        btnSave.setText("\uf142");
        
        btnBack.setTypeface(solidFont);
        btnBack.setText("\uf060");

        // Sync the vertical scroll of the line numbers with the code editor
        codeEditor.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                scrollNumbers.scrollTo(0, scrollY);
            }
        });

        // FIX THE SNAPPING CURSOR BUG
        // This intercepts swipes so the cursor doesn't drag the view back
        codeEditor.setOnTouchListener(new View.OnTouchListener() {
            private float startX, startY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startX = event.getX();
                    startY = event.getY();
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    float dx = Math.abs(event.getX() - startX);
                    float dy = Math.abs(event.getY() - startY);
                    if (dx > 15 || dy > 15) {
                        // If it's a swipe, clear focus to stop the cursor snap back
                        v.clearFocus();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Give focus back when the user stops swiping
                    v.requestFocus();
                }
                return false;
            }
        });

        // ADD THE TEXT WATCHER FOR SYNTAX HIGHLIGHTING (DEBOUNCED)
        codeEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateLineNumbers(); 
                
                // Remove any pending highlight jobs
                highlightHandler.removeCallbacks(highlightRunnable);
                // Wait 300ms after the user stops typing, then highlight
                highlightHandler.postDelayed(highlightRunnable, 300);
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
            
            codeEditor.setText(text.toString());
            updateLineNumbers(); 
            
            // Force an immediate highlight when opening a new file
            highlightHandler.removeCallbacks(highlightRunnable);
            highlightHandler.postDelayed(highlightRunnable, 100);
            
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

        ForegroundColorSpan[] oldSpans = editable.getSpans(0, editable.length(), ForegroundColorSpan.class);
        for (ForegroundColorSpan span : oldSpans) {
            editable.removeSpan(span);
        }

        String text = editable.toString();

        if (ext.equals("py")) {
            highlightPattern(editable, text, "\\b(def|class|import|from|return|if|else|elif|for|while|in|try|except|print|True|False|None|and|or|not)\\b", COLOR_KEYWORD);
            highlightPattern(editable, text, "\"[^\"]*\"|'[^']*'", COLOR_STRING);
            highlightPattern(editable, text, "\\b\\d+\\b", COLOR_NUMBER);
            highlightPattern(editable, text, "#.*", COLOR_COMMENT);
            
        } else if (ext.equals("java")) {
		
		    // Numbers
		    highlightPattern(editable, text,
		        "\\b(?:0[xX][0-9a-fA-F_]+|0[bB][01_]+|\\d[\\d_]*(?:\\.\\d[\\d_]*)?(?:[eE][+-]?\\d[\\d_]*)?[fFdDlL]?)\\b",
		        COLOR_NUMBER);
		
		    highlightPattern(editable, text,
		        "'(?:\\\\.|[^'\\\\])'", COLOR_CHARACTER);
		
		    // Annotations
		    highlightPattern(editable, text,
		        "@[A-Za-z_$][A-Za-z0-9_$.]*", COLOR_ANNOTATION);
		
		    // Keywords
		    highlightPattern(editable, text,
		        "\\b(abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|extends|final|finally|float|for|if|implements|import|instanceof|int|interface|long|native|new|package|private|protected|public|return|short|static|strictfp|super|switch|synchronized|this|throw|throws|transient|try|void|volatile|while|var|yield|record|sealed|permits)\\b",
		        COLOR_KEYWORD);
		
		    // Constants
		    highlightPattern(editable, text,
		        "\\b(true|false|null)\\b", COLOR_CONSTANT);
		
		    // Common types and classes
		    highlightPattern(editable, text,
		        "\\b(String|Object|Integer|Long|Double|Float|Boolean|Character|Byte|Short|Void|Class|System|Thread|Math|Exception|ArrayList|HashMap|List|Map|Set|File|Scanner)\\b",
		        COLOR_TYPE);
		
		    // Method names
		    highlightPattern(editable, text,
		        "\\b[A-Za-z_$][A-Za-z0-9_$]*(?=\\s*\\()", COLOR_METHOD);
		
		    // Operators
		    highlightPattern(editable, text,
		        ">>>=|<<=|>>=|>>>|==|!=|<=|>=|&&|\\|\\||\\+\\+|--|\\+=|-=|\\*=|/=|%=|->|::|[+*/%=<>!&|^~?:-]",
		        COLOR_OPERATOR);
		
		    // Brackets
		    highlightPattern(editable, text,
		        "[(){}\\[\\]]", COLOR_BRACKET);

		    // Strings
		    highlightPattern(editable, text,
		        "\"(?:(?:\\\\.)|[^\"\\\\])*\"", COLOR_STRING);
		        
		    // Comments
		    highlightPattern(editable, text,
		        "/\\*[\\s\\S]*?\\*/|//.*", COLOR_COMMENT);
		        
        } else if (ext.equals("xml")) {
            highlightPattern(editable, text, "</?[a-zA-Z0-9_:-]+", COLOR_XML_TAG);
            highlightPattern(editable, text, "\"[^\"]*\"", COLOR_STRING);
        }
    }

    private void highlightPattern(Editable editable, String text, String regex, int color) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
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
            tabLayout.setPadding(24, 5, 24, 5);
            
            if (currentFile != null && currentFile.getAbsolutePath().equals(path)) {
                tabLayout.setBackgroundResource(R.drawable.tab_active);
            } else {
                tabLayout.setBackgroundResource(R.drawable.tab_inactive);
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
    
    private void updateLineNumbers() {
        String text = codeEditor.getText().toString();
        int lines = text.split("\n", -1).length; 
        
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lines; i++) {
            sb.append(i).append("\n");
        }
        
        lineNumbersView.setText(sb.toString());
    }
}
