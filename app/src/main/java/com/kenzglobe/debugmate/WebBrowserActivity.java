package com.kenzglobe.debugmate;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class WebBrowserActivity extends Activity {

    private WebView webView;
    private LinearLayout consoleContainer;
    private LinearLayout consoleOutputContainer;
    private ScrollView consoleScroll;
    private Typeface solidFont, codeFont;
    private String baseUrl;
    private String intentFileName; // Store the intent file name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_browser);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#8B5CF6"));
            getWindow().getDecorView().setSystemUiVisibility(0);
        }

        if (getActionBar() != null) {
            getActionBar().hide();
        }

        solidFont = Typeface.createFromAsset(getAssets(), "fonts/fa-solid-900-v5.ttf");
        codeFont = Typeface.createFromAsset(getAssets(), "fonts/FiraCode-Regular.ttf");

        // --- Initialize Views ---
        webView = findViewById(R.id.browser_webview);
        TextView browserTitle = findViewById(R.id.browser_title);
        TextView btnBack = findViewById(R.id.btn_browser_back);
        
        consoleContainer = findViewById(R.id.console_container);
        consoleOutputContainer = findViewById(R.id.console_output_container);
        consoleScroll = findViewById(R.id.console_scroll);
        TextView btnToggleConsole = findViewById(R.id.btn_toggle_console);
        TextView btnCloseConsole = findViewById(R.id.btn_close_console);
        TextView btnClearConsole = findViewById(R.id.btn_clear_console);
        com.kenzglobe.debugmate.AppEditText consoleInput = findViewById(R.id.console_input);

        // --- Setup Icons & Listeners ---
        btnBack.setTypeface(solidFont);
        btnBack.setText("\uf060");
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish(); }
        });

        btnCloseConsole.setTypeface(solidFont);
        btnCloseConsole.setText("\uf078");
        btnCloseConsole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { consoleContainer.setVisibility(View.GONE); }
        });

        btnClearConsole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { consoleOutputContainer.removeAllViews(); }
        });

        btnToggleConsole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (consoleContainer.getVisibility() == View.VISIBLE) {
                    consoleContainer.setVisibility(View.GONE);
                } else {
                    consoleContainer.setVisibility(View.VISIBLE);
                }
            }
        });

        // --- Setup Console REPL Input ---
        consoleInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || 
                   (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    
                    String jsCode = v.getText().toString().trim();
                    if (!jsCode.isEmpty()) {
                        addLog(jsCode, "REPL", 0, ConsoleMessage.MessageLevel.LOG);
                        
                        webView.evaluateJavascript(jsCode, new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String result) {
                                if (result != null && !result.equals("null") && !result.equals("undefined")) {
                                    addLog(result, "REPL", 0, ConsoleMessage.MessageLevel.LOG);
                                }
                            }
                        });
                        v.setText(""); 
                    }
                    return true;
                }
                return false;
            }
        });

        // --- Configure WebView Settings ---
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);

        // --- ADD WEBCHROME CLIENT ---
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                String msg = consoleMessage.message();
                int line = consoleMessage.lineNumber();
                String sourceId = consoleMessage.sourceId();
                
                String fileName = "Unknown";
                if (sourceId != null && sourceId.contains("/")) {
                    fileName = sourceId.substring(sourceId.lastIndexOf("/") + 1);
                }

                if (consoleMessage.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                    addLog(msg, fileName, line, ConsoleMessage.MessageLevel.ERROR);
                } else if (consoleMessage.messageLevel() == ConsoleMessage.MessageLevel.WARNING) {
                    addLog(msg, fileName, line, ConsoleMessage.MessageLevel.WARNING);
                } else {
                    addLog(msg, fileName, line, ConsoleMessage.MessageLevel.LOG);
                }
                return true; 
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("file://")) {
                    view.loadUrl(url);
                    return true; 
                }
                return false; 
            }
        });

        // --- Load the Code ---
        String code = getIntent().getStringExtra("code");
        intentFileName = getIntent().getStringExtra("file_name");
        String ext = getIntent().getStringExtra("extension");
        baseUrl = getIntent().getStringExtra("base_url");

        if (intentFileName != null) {
            browserTitle.setText(intentFileName);
        }

        if (code != null) {
            if ("js".equals(ext)) {
                code = "<html><body><script>" + code + "</script></body></html>";
            } else if ("css".equals(ext)) {
                code = "<html><head><style>" + code + "</style></head><body>" +
                       "<h1>CSS Preview</h1><p>This paragraph is styled by your CSS.</p>" +
                       "</body></html>";
            }

            webView.loadDataWithBaseURL(baseUrl, code, "text/html", "UTF-8", null);
            addLog("Web preview loaded.", "System", 0, ConsoleMessage.MessageLevel.LOG);
        }
    }

    // --- UPDATED HELPER: Creates a UI row and handles Tap-to-Jump ---
    private void addLog(final String message, String msgFileName, final int lineNumber, final ConsoleMessage.MessageLevel level) {
        
        // FALLBACK: If sourceId was null/empty, use the file name from the intent
        if (msgFileName == null || msgFileName.isEmpty() || msgFileName.equals("Unknown")) {
            msgFileName = intentFileName;
        }
        
        final String finalFileName = (msgFileName != null) ? msgFileName : "Unknown";

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = LayoutInflater.from(WebBrowserActivity.this);
                View logView = inflater.inflate(R.layout.item_console_log, consoleOutputContainer, false);

                TextView iconView = logView.findViewById(R.id.console_log_icon);
                TextView textView = logView.findViewById(R.id.console_log_text);

                iconView.setTypeface(solidFont);
                
                // Append line number to the message if valid
                if (lineNumber > 0 && !finalFileName.equals("Unknown") && !finalFileName.equals("System") && !finalFileName.equals("REPL")) {
                    textView.setText(message + "\n(" + finalFileName + " : Line " + lineNumber + ")");
                } else {
                    textView.setText(message);
                }

                if (level == ConsoleMessage.MessageLevel.ERROR) {
                    iconView.setText("\uf057"); 
                    iconView.setTextColor(Color.parseColor("#F85149")); 
                    textView.setTextColor(Color.parseColor("#F85149"));
                } else if (level == ConsoleMessage.MessageLevel.WARNING) {
                    iconView.setText("\uf071"); 
                    iconView.setTextColor(Color.parseColor("#D29922")); 
                    textView.setTextColor(Color.parseColor("#D29922"));
                } else {
                    iconView.setText("\uf054"); 
                    iconView.setTextColor(Color.parseColor("#3FB950")); 
                    textView.setTextColor(Color.parseColor("#E6E6E6")); 
                }

                // --- MAGIC: Tap to jump to line ---
                if (lineNumber > 0 && baseUrl != null && !finalFileName.equals("Unknown")) {
                    logView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(WebBrowserActivity.this, EditorActivity.class);
                            
                            // Reconstruct the absolute path from baseUrl + fileName
                            String cleanBaseUrl = baseUrl.replace("file://", "");
                            // Ensure the base URL ends with a slash so concatenation is safe
                            if (!cleanBaseUrl.endsWith("/")) {
                                cleanBaseUrl += "/";
                            }
                            String filePath = cleanBaseUrl + finalFileName;
                            
                            intent.putExtra("file_path", filePath);
                            intent.putExtra("jump_to_line", lineNumber);
                            startActivity(intent);
                            finish(); // Close the browser
                        }
                    });
                }

                consoleOutputContainer.addView(logView);
                consoleScroll.post(new Runnable() {
                    @Override
                    public void run() { consoleScroll.fullScroll(View.FOCUS_DOWN); }
                });
            }
        });
    }
}
