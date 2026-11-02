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
import java.io.File;

public class ConsoleActivity extends Activity {

    private TextView consoleOutput;
    private ScrollView consoleScroll;
    private EditText consoleInput;
    private WebView consoleWebView;
    private String pendingPythonCode = null;
    private String pendingPythonDir = null;

    private StringBuilder outputBuilder = new StringBuilder();
    private Typeface terminalFont, solidFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.console);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#8B5CF6"));
            getWindow().getDecorView().setSystemUiVisibility(0);
        }

        if (getActionBar() != null) {
            getActionBar().hide();
        }

        terminalFont = Typeface.createFromAsset(getAssets(), "fonts/FiraCode-Regular.ttf");
        solidFont = Typeface.createFromAsset(getAssets(), "fonts/fa-solid-900-v5.ttf");

        consoleOutput = findViewById(R.id.console_output);
        consoleScroll = findViewById(R.id.console_scroll);
        consoleInput = findViewById(R.id.console_input);
        consoleWebView = findViewById(R.id.console_webview);
        
        TextView btnClose = findViewById(R.id.btn_console_close);
        TextView btnClear = findViewById(R.id.btn_console_clear);

        consoleOutput.setTypeface(terminalFont);
        consoleInput.setTypeface(terminalFont);
        
        pendingPythonCode = getIntent().getStringExtra("python_code");
        pendingPythonDir = getIntent().getStringExtra("python_dir");
        
        // Set Icons
        btnClose.setTypeface(solidFont);
        btnClose.setText("\uf060");
        btnClear.setTypeface(solidFont);
        btnClear.setText("\uf12d");

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

        // Setup Hidden WebView for Pyodide
        consoleWebView.getSettings().setJavaScriptEnabled(true);
        consoleWebView.getSettings().setAllowFileAccess(true);
        consoleWebView.addJavascriptInterface(new ConsoleBridge(), "AndroidConsole");

        // Capture print() from Python (which routes to console.log)
        consoleWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                printToConsole(consoleMessage.message());
                return true;
            }

            // --- NEW: Intercept JS prompt() for Python input() ---
            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final android.webkit.JsPromptResult result) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ConsoleActivity.this);
                builder.setTitle("Python Input");
                builder.setMessage(message);

                final EditText input = new EditText(ConsoleActivity.this);
                input.setTextColor(Color.WHITE);
                input.setHint("Enter value...");
                input.setText(defaultValue);
                builder.setView(input);

                builder.setPositiveButton("OK", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        result.confirm(input.getText().toString());
                    }
                });
                builder.setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });
                builder.setOnCancelListener(new android.content.DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(android.content.DialogInterface dialog) {
                        result.cancel();
                    }
                });
                builder.show();
                return true;
            }
        });

        printToConsole("DebugMate Python Console (Pyodide)\nBooting up Python engine... Please wait.");
        
        // Load the Pyodide HTML environment
        consoleWebView.loadDataWithBaseURL("https://cdn.jsdelivr.net/", getPyodideHtml(), "text/html", "UTF-8", null);

        // Handle the "Enter" key on the keyboard
        consoleInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || 
                   (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    
                    String code = consoleInput.getText().toString();
                    if (!code.trim().isEmpty()) {
                        printToConsole(">>> " + code);
                        runPython(code);
                        consoleInput.setText("");
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void runPython(String code) {
        String pythonCode = code;

        // --- NEW: Intercept 'pip install' and route to micropip ---
        if (code.trim().startsWith("pip install ")) {
            String packageName = code.trim().substring(12).trim();
            // Remove any quotes the user might have typed
            packageName = packageName.replace("'", "").replace("\"", "");
            
            printToConsole("Fetching package '" + packageName + "' from PyPI...");
            
            // Pyodide uses micropip. We must use 'await' because it downloads over the network.
            pythonCode = "import micropip\n" +
                         "await micropip.install('" + packageName + "')\n" +
                         "print('Successfully installed " + packageName + "')";
        }

        // Send the Python code to our JavaScript bridge
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            json.put("code", pythonCode);
            String safeCode = json.toString();

            String js = "runPythonCode(" + safeCode + ");";
            consoleWebView.evaluateJavascript(js, null);
        } catch (Exception e) {
            printToConsole("Error passing code: " + e.getMessage());
        }
    }
    
    private void loadProjectFilesToPyodide(String dirPath) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null) return;

        try {
            org.json.JSONObject filesJson = new org.json.JSONObject();
            for (File f : files) {
                if (f.isFile() && f.getName().endsWith(".py")) {
                    StringBuilder sb = new StringBuilder();
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(f));
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    br.close();
                    filesJson.put(f.getName(), sb.toString());
                }
            }
        
            // Send the files to the hidden WebView
            String js = "loadFilesToFS(" + filesJson.toString() + ");";
            consoleWebView.evaluateJavascript(js, null);
        
        } catch (Exception e) {
            printToConsole("Warning: Could not load local modules. " + e.getMessage());
        }
    }

    public void printToConsole(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
        });
    }

    // Bridge to receive results from Pyodide
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
                    printToConsole("Traceback (most recent call last):\n" + error);
                }
            });
        }
        
        @JavascriptInterface
        public void onReady() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printToConsole("Python engine ready! Type your code below.\n");
                    
                    if (pendingPythonCode != null && !pendingPythonCode.isEmpty()) {
                        printToConsole("--- Running script from Editor ---");
                        
                        if (pendingPythonDir != null) {
                            loadProjectFilesToPyodide(pendingPythonDir);
                        }
                        
                        runPython(pendingPythonCode);
                        pendingPythonCode = null;
                    }
                }
            });
        }
    }

    // This is the HTML document that loads Pyodide and handles Python execution
    private String getPyodideHtml() {
        return "<!DOCTYPE html><html><head>" +
               "<script src=\"https://cdn.jsdelivr.net/pyodide/v0.25.1/full/pyodide.js\"></script>" +
               "</head><body>" +
               "<script>" +
               "let pyodideReady = false;" +
               "let pyodide = null;" +
               "" +
               "async function initPyodide() {" +
               "  try {" +
               "    pyodide = await loadPyodide();" +
               "    await pyodide.loadPackage(\"micropip\");" +
               "" +
               "    pyodide.runPython(`" +
               "        import js\n" +
               "        import builtins\n" +
               "        def debugmate_input(prompt=''):\n" +
               "            val = js.prompt(prompt)\n" +
               "            if val is None: raise EOFError('Input cancelled')\n" +
               "            return val\n" +
               "        builtins.input = debugmate_input\n" +
               "    `);" +
               "" +
               "    pyodideReady = true;" +
               "    AndroidConsole.onReady();" +
               "  } catch (e) {" +
               "    AndroidConsole.onError('Failed to load Pyodide: ' + e.message);" +
               "  }" +
               "}" +
               "" +
               "async function runPythonCode(data) {" +
               "  if (!pyodideReady) {" +
               "    AndroidConsole.onError('Python is still loading. Please wait.');" +
               "    return;" +
               "  }" +
               "  try {" +
               "    let result = await pyodide.runPythonAsync(data.code);" +
               "    if (result !== undefined && result !== null) {" +
               "      AndroidConsole.onResult(String(result));" +
               "    }" +
               "  } catch (e) {" +
               "    AndroidConsole.onError(e.message);" +
               "  }" +
               "}" +
               "" +
               "window.loadFilesToFS = function(filesObj) {" +
               "  for (let filename in filesObj) {" +
               "    try {" +
               "      pyodide.FS.writeFile('/home/pyodide/' + filename, filesObj[filename], { encoding: 'utf8' });" +
               "    } catch (e) {" +
               "      console.error('Failed to write ' + filename + ': ' + e.message);" +
               "    }" +
               "  }" +
               "};" +
               "" +
               "initPyodide();" +
               "</script>" +
               "</body></html>";
    }
}
