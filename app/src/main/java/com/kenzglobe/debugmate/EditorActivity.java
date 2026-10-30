
package com.kenzglobe.debugmate;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Gravity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class EditorActivity extends Activity {

    private File currentFile;
    private WebView codeEditorWebView;

    private boolean editorReady = false;
    private String pendingCode = null;
    private String pendingExtension = "text";

    private Typeface appFont, solidFont;

    private TextView fileNameView, fileNamePath, sideBarBtn,
            btnBack, btnSave, btnFind, btnUndo, btnRedo, btnRun;

    private LinearLayout tabContainer;

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

        appFont = Typeface.createFromAsset(
                getAssets(), "fonts/SpaceMono-Bold.ttf");

        solidFont = Typeface.createFromAsset(
                getAssets(), "fonts/fa-solid-900-v5.ttf");

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

        setupBottomIcon(sideBarBtn, "\uf07b", solidFont);
        setupBottomIcon(btnBack, "\uf060", solidFont);
        setupBottomIcon(btnSave, "\uf0c7", solidFont);
        setupBottomIcon(btnFind, "\uf002", solidFont);
        setupBottomIcon(btnUndo, "\uf0e2", solidFont);
        setupBottomIcon(btnRedo, "\uf01e", solidFont);
        setupBottomIcon(btnRun, "\uf04b", solidFont);

        fileNameView.setTypeface(appFont);
        fileNamePath.setTypeface(appFont);

        // Configure WebView before loading the editor page.
        codeEditorWebView.getSettings().setJavaScriptEnabled(true);
        codeEditorWebView.getSettings().setDomStorageEnabled(true);

        codeEditorWebView.addJavascriptInterface(
                new WebAppInterface(), "AndroidBridge");

        codeEditorWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // Confirm that Ace and setCode() are initialized.
                view.evaluateJavascript(
                        "typeof setCode === 'function' && window.editorReady === true",
                        new android.webkit.ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String result) {
                                editorReady = "true".equals(result);

                                if (editorReady && pendingCode != null) {
                                    sendCodeToEditor(
                                            pendingCode, pendingExtension);

                                    pendingCode = null;
                                }
                            }
                        }
                );
            }
        });

        codeEditorWebView.loadUrl(
                "file:///android_asset/www/editor.html");

        String filePath = getIntent().getStringExtra("file_path");

        // If no file path was passed (e.g., opened via Workspace), find the most recent file
        if (filePath == null || filePath.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("DebugMatePrefs", MODE_PRIVATE);
            String recentStr = prefs.getString("recent_files", "");
            
            if (!recentStr.isEmpty()) {
                String[] paths = recentStr.split("\n");
                // Loop through recent files and find the first one that still exists
                for (String path : paths) {
                    File f = new File(path);
                    if (f.exists() && f.isFile()) {
                        filePath = path;
                        break; // Stop once we find the most recent valid file
                    }
                        }
            }
        }

        // Load the file if we found one (either from Intent or SharedPreferences)
        if (filePath != null && !filePath.isEmpty()) {
            loadFile(new File(filePath));
        }

        buildTabs();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentFile();
            }
        });

        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runEditorCommand("find();");
            }
        });

        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runEditorCommand("undo();");
            }
        });

        btnRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runEditorCommand("redo();");
            }
        });

        btnRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editorReady) {
                    Toast.makeText(EditorActivity.this, "Editor is still loading...", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (currentFile == null) {
                    Toast.makeText(EditorActivity.this, "No file open.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String ext = "";
                String name = currentFile.getName();
                int dotIndex = name.lastIndexOf('.');
                if (dotIndex != -1) {
                    ext = name.substring(dotIndex + 1).toLowerCase();
                }

                // Only run web files for now
                if (!ext.equals("html") && !ext.equals("css") && !ext.equals("js")) {
                    Toast.makeText(EditorActivity.this, "Running ." + ext + " is not supported yet.", Toast.LENGTH_LONG).show();
                    return;
                }

                // Get the code from WebView and start the preview
                codeEditorWebView.evaluateJavascript("getCode();", new android.webkit.ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String result) {
                        try {
                            if (result == null || "null".equals(result)) {
                                throw new Exception("Editor returned no content.");
                            }

                            // Decode the JSON string
                            String cleanCode = new JSONArray("[" + result + "]").getString(0);
                            
                            // Launch the new Activity
                            android.content.Intent intent = new android.content.Intent(EditorActivity.this, WebBrowserActivity.class);
                            intent.putExtra("code", cleanCode);
                            intent.putExtra("file_name", currentFile.getName());

                            // Pass the directory so the WebView knows where to find local files
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
                            Toast.makeText(EditorActivity.this, "Error launching preview: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    // JavaScript bridge
    public class WebAppInterface {

        @JavascriptInterface
        public void saveRequested(String content) {
            // Reserved for future auto-save functionality.
        }

        @JavascriptInterface
        public void copyText(String text) {
            ClipboardManager clipboard =
                (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText(
                    "DebugMate", text
                );
                clipboard.setPrimaryClip(clip);
            }
        }
    
        @JavascriptInterface
        public String getClipboardText() {
            ClipboardManager clipboard =
                (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                ClipData clip = clipboard.getPrimaryClip();
    
                if (clip != null && clip.getItemCount() > 0) {
                    CharSequence text = clip.getItemAt(0).getText();
    
                    if (text != null) {
                        return text.toString();
                    }
                }
            }
    
            return "";
        }
    }


    private void setupBottomIcon(
            TextView view, String unicode, Typeface font) {

        view.setTypeface(font);
        view.setText(unicode);
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

    private void loadFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            Toast.makeText(
                    this, "File not found.", Toast.LENGTH_SHORT
            ).show();
            return;
        }

        currentFile = file;

        fileNameView.setText(file.getName());
        fileNamePath.setText(file.getAbsolutePath());

        try {
            StringBuilder text = new StringBuilder();

            try (BufferedReader br = new BufferedReader(
                    new FileReader(file))) {

                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line).append('\n');
                }
            }

            String name = file.getName();
            String ext = "";
            int dotIndex = name.lastIndexOf('.');

            if (dotIndex != -1 && dotIndex < name.length() - 1) {
                ext = name.substring(dotIndex + 1).toLowerCase(
                        java.util.Locale.ROOT);
            }

            pendingCode = text.toString();
            pendingExtension = ext;

            if (editorReady) {
                sendCodeToEditor(pendingCode, pendingExtension);
                pendingCode = null;
            }

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Error reading file: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void saveCurrentFile() {
        if (currentFile == null) {
            Toast.makeText(
                    this, "No file selected.", Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (!editorReady) {
            Toast.makeText(
                    this, "Editor is still loading.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        final File fileToSave = currentFile;

        codeEditorWebView.evaluateJavascript(
                "getCode();",
                new android.webkit.ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String result) {
                        try {
                            if (result == null || "null".equals(result)) {
                                throw new Exception(
                                        "Editor returned no content.");
                            }

                            // Decode the JSON string returned by WebView.
                            String cleanCode = new JSONArray(
                                    "[" + result + "]"
                            ).getString(0);

                            try (FileWriter writer =
                                    new FileWriter(fileToSave, false)) {
                                writer.write(cleanCode);
                            }

                            Toast.makeText(
                                    EditorActivity.this,
                                    "Saved successfully! ✔️",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } catch (Exception e) {
                            Toast.makeText(
                                    EditorActivity.this,
                                    "Error saving: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                }
        );
    }

    private void buildTabs() {
        tabContainer.removeAllViews();

        SharedPreferences prefs = getSharedPreferences(
                "DebugMatePrefs", MODE_PRIVATE);

        String recentStr = prefs.getString("recent_files", "");

        if (recentStr.isEmpty()) {
            return;
        }

        String[] paths = recentStr.split("\n");

        for (final String path : paths) {
            final File f = new File(path);

            if (!f.exists() || !f.isFile()) {
                continue;
            }

            LinearLayout tabLayout = new LinearLayout(this);
            tabLayout.setOrientation(LinearLayout.HORIZONTAL);
            tabLayout.setGravity(Gravity.CENTER_VERTICAL);
            tabLayout.setPadding(24, 0, 9, 0);

            if (currentFile != null
                    && currentFile.getAbsolutePath().equals(
                            f.getAbsolutePath())) {
                tabLayout.setBackgroundResource(R.drawable.tab_active);
            } /*else {
                tabLayout.setBackgroundResource(R.drawable.tab_inactive);
            }*/

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
        SharedPreferences prefs = getSharedPreferences(
                "DebugMatePrefs", MODE_PRIVATE);

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

        prefs.edit()
                .putString("recent_files", sb.toString().trim())
                .apply();

        if (currentFile != null
                && currentFile.getAbsolutePath().equals(path)) {

            if (list.isEmpty()) {
                finish();
                return;
            } else {
                loadFile(new File(list.get(0)));
            }
        }

        buildTabs();
    }
}
