package com.kenzglobe.debugmate;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

public class ConsoleActivity extends Activity {

    private TextView consoleOutput;
    private ScrollView consoleScroll;
    private EditText consoleInput;
    private WebView consoleWebView;

    private StringBuilder outputBuilder = new StringBuilder();
    private Typeface solidFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.console);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#262626"));
            getWindow().getDecorView().setSystemUiVisibility(0);
        }

        if (getActionBar() != null) {
            getActionBar().hide();
        }

        solidFont = Typeface.createFromAsset(getAssets(), "fonts/fa-solid-900-v5.ttf");

        consoleOutput = findViewById(R.id.console_output);
        consoleScroll = findViewById(R.id.console_scroll);
        consoleInput = findViewById(R.id.console_input);
        consoleWebView = findViewById(R.id.console_webview);
        
        TextView btnClose = findViewById(R.id.btn_console_close);
        TextView btnClear = findViewById(R.id.btn_console_clear);

        // Set Icons
        btnClose.setTypeface(solidFont);
        btnClose.setText("\uf00d"); // Close (X)
        btnClear.setTypeface(solidFont);
        btnClear.setText("\uf12d"); // Trash

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outputBuilder.setLength(0);
                consoleOutput.setText("");
                printToConsole("Console cleared.");
            }
        });

        // Setup Hidden WebView
        consoleWebView.getSettings().setJavaScriptEnabled(true);
        consoleWebView.addJavascriptInterface(new ConsoleBridge(), "AndroidConsole");

        // Capture console.log() from JavaScript
        consoleWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                String msg = consoleMessage.message() + " (Line " + consoleMessage.lineNumber() + ")";
                printToConsole("Log: " + msg);
                return true;
            }
        });

        // Load a blank page to initialize the JS environment
        consoleWebView.loadDataWithBaseURL(null, "<html><body></body></html>", "text/html", "UTF-8", null);

        // Handle the "Enter" key on the keyboard
        consoleInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || 
                   (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    
                    String code = consoleInput.getText().toString().trim();
                    if (!code.isEmpty()) {
                        runJavaScript(code);
                        consoleInput.setText("");
                    }
                    return true;
                }
                return false;
            }
        });

        printToConsole("DebugMate JavaScript Console\nType code and hit Enter to run.\n");
    }

    private void runJavaScript(String code) {
        printToConsole("> " + code);

        // Wrap the code in a try-catch and use the bridge to send the result back
        String js = "try { " +
                    "  var result = eval('" + code.replace("'", "\\'") + "'); " +
                    "  if (result !== undefined) { " +
                    "    AndroidConsole.onResult(String(result)); " +
                    "  } else { " +
                    "    AndroidConsole.onResult('undefined'); " +
                    "  } " +
                    "} catch (e) { " +
                    "  AndroidConsole.onError(e.message); " +
                    "}";

        consoleWebView.evaluateJavascript(js, null);
    }

    private void printToConsole(String text) {
        outputBuilder.append(text).append("\n");
        consoleOutput.setText(outputBuilder.toString());
        
        // Auto-scroll to bottom
        consoleScroll.post(new Runnable() {
            @Override
            public void run() {
                consoleScroll.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    // Bridge to receive results from JavaScript
    public class ConsoleBridge {
        @JavascriptInterface
        public void onResult(final String result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printToConsole(result);
                }
            });
        }

        @JavascriptInterface
        public void onError(final String error) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printToConsole("Error: " + error);
                }
            });
        }
    }
}