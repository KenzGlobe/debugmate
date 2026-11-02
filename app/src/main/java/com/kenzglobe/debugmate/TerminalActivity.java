package com.kenzglobe.debugmate;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TerminalActivity extends Activity {

    private LinearLayout outputLayout;
    private EditText input;
    private TextView promptView;
    private ScrollView scrollView;
    private LinearLayout extraKeysLayout;
    
    // --- FIX: Initialize with default values to prevent onCreate crash ---
    private int terminalFontSize = 14;
    private String terminalThemeColor = "#E6E6E6";
    
    private PackageManager pkgManager;
    
    private Typeface appFontBold,
        appFontRegular,
        terminalFont,
        solidFont,
        brandsRegularFont;
    
    private File currentDirectory = android.os.Environment.getExternalStorageDirectory(); 
    private String homePath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.terminal);

        pkgManager = new PackageManager(TerminalActivity.this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#8B5CF6"));
            getWindow().getDecorView().setSystemUiVisibility(0);
        }

        if (getActionBar() != null) getActionBar().hide();
        
        appFontBold = Typeface.createFromAsset(getAssets(), "fonts/SpaceMono-Bold.ttf");
        appFontRegular = Typeface.createFromAsset(getAssets(), "fonts/SpaceMono-Regular.ttf");
        terminalFont = Typeface.createFromAsset(getAssets(), "fonts/FiraCode-Regular.ttf");
        solidFont = Typeface.createFromAsset(getAssets(), "fonts/fa-solid-900-v5.ttf");
        brandsRegularFont = Typeface.createFromAsset(getAssets(), "fonts/fa-brands-regular-400-v5.ttf");

        outputLayout = findViewById(R.id.output_layout);
        input = findViewById(R.id.terminal_input);
        promptView = findViewById(R.id.terminal_prompt);
        scrollView = findViewById(R.id.scroll_view);
        extraKeysLayout = findViewById(R.id.extra_keys_layout);
        
        TextView backBtn = findViewById(R.id.back_btn);
        TextView sendBtn = findViewById(R.id.send_btn);
        TextView keyUp = findViewById(R.id.key_up);
        TextView keyDown = findViewById(R.id.key_down);
        TextView keyLeft = findViewById(R.id.key_left);
        TextView keyRight = findViewById(R.id.key_right);
        TextView settingsBtn = findViewById(R.id.btn_terminal_settings);
        
        input.setTypeface(terminalFont);
        promptView.setTypeface(terminalFont);

        setupIcon(backBtn, "\uf060", solidFont); // Arrow Left
        setupIcon(sendBtn, "\uf0da", solidFont); // Arrow Right (Enter)
        setupIcon(keyUp, "\uf062", solidFont);
        setupIcon(keyDown, "\uf063", solidFont);
        setupIcon(keyLeft, "\uf060", solidFont);
        setupIcon(keyRight, "\uf061", solidFont);
        setupIcon(settingsBtn, "\uf013", solidFont); // Gear icon

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TerminalActivity.this, TerminalSettingsActivity.class));
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitCommand();
            }
        });

        updatePrompt();
        printOutput("Welcome to DebugMate Terminal!\n\nWorking with packages:\n  - Search:  pkg search <query>\n  - Install: pkg install <package>\n  - Upgrade: pkg upgrade\n\nType 'help' for commands.\n");

        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    submitCommand();
                    return true;
                }
                return false;
            }
        });

        input.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    submitCommand();
                    return true;
                }
                return false;
            }
        });
    }

    private void submitCommand() {
        String command = input.getText().toString().trim();
        if (!command.isEmpty()) {
            printOutput(promptView.getText().toString() + " " + command);
            executeCommand(command);
            input.setText(""); 
        }
    }

    private void updatePrompt() {
        String path = currentDirectory.getAbsolutePath();
        String displayPath = "";
        
        if (path.equals(homePath)) {
            displayPath = ""; 
        } else if (path.startsWith(homePath)) {
            displayPath = path.substring(homePath.length());
        } else {
            displayPath = path;
        }

        if (displayPath.length() > 25) {
            displayPath = "..." + displayPath.substring(displayPath.length() - 22);
        }

        SpannableStringBuilder sb = new SpannableStringBuilder();
        
        int start = sb.length();
        sb.append("~");
        sb.setSpan(new ForegroundColorSpan(Color.parseColor("#3FB950")), start, sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (!displayPath.isEmpty()) {
            start = sb.length();
            sb.append(displayPath);
            sb.setSpan(new ForegroundColorSpan(Color.parseColor("#8B5CF6")), start, sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        start = sb.length();
        sb.append(" $ ");
        sb.setSpan(new ForegroundColorSpan(Color.parseColor("#3FB950")), start, sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        promptView.setText(sb);
    }
    
    private void executeCommand(String commandLine) {
        String[] parts = commandLine.split(" ");
        String mainCommand = parts[0].toLowerCase();

        if (mainCommand.equals("help")) {
            printOutput("Available commands:\n" +
                        "  help          - Show this menu\n" +
                        "  clear         - Clear the screen\n" +
                        "  cd <path>     - Change directory\n" +
                        "  (Any other standard Linux command like ls, pwd, echo, cat, etc. will be executed natively.)");
                    
        } else if (mainCommand.equals("clear")) {
            outputLayout.removeAllViews();
        
        } else if (mainCommand.equals("cd")) {
            String path = "";
            if (parts.length > 1) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < parts.length; i++) {
                    sb.append(parts[i]).append(" ");
                }
                path = sb.toString().trim().replace("\"", "").replace("'", "");
            }

            File newDir;
            if (path.isEmpty() || path.equals("~")) {
                newDir = new File(homePath);
            } else if (path.startsWith("/")) {
                newDir = new File(path);
            } else {
                newDir = new File(currentDirectory, path);
            }

            if (newDir.exists() && newDir.isDirectory()) {
                try {
                    currentDirectory = newDir.getCanonicalFile(); 
                    updatePrompt(); 
                } catch (Exception e) {
                    currentDirectory = newDir;
                    updatePrompt();
                }
            } else {
                printOutput("cd: no such file or directory: " + path);
            }

        } else if (mainCommand.equals("ls")) {
            executeLs(commandLine);
        
        } else if (mainCommand.equals("pkg")) {
            if (parts.length > 1 && parts[1].equals("install")) {
                if (parts.length > 2) {
                    printOutput("Installing " + parts[2] + "...");
                    pkgManager.installPackage(parts[2]);
                } else {
                    printOutput("Usage: pkg install <package>");
                }
            } else {
                printOutput("Usage: pkg install <package>");
            }
        
        } else if (mainCommand.equals("export")) {
            String exportCmd = commandLine.substring(6).trim();
            printOutput("Set environment variable: " + exportCmd);
        
        } else {
            File customExecutable = new File(pkgManager.getBinDir(), parts[0]);
        
            if (customExecutable.exists()) {
                StringBuilder newCmd = new StringBuilder();
            
                if (parts[0].endsWith(".sh")) {
                    newCmd.append("sh ").append(customExecutable.getAbsolutePath());
                } else {
                    newCmd.append("/system/bin/linker64 ").append(customExecutable.getAbsolutePath());
                }
            
                for (int i = 1; i < parts.length; i++) {
                    newCmd.append(" ").append(parts[i]);
                }
            
                runShellCommand(newCmd.toString());
            } else {
                runShellCommand(commandLine);
            }
        }
    }

    private void executeLs(String commandLine) {
        boolean showHidden = commandLine.contains("-a");
        
        File[] files = currentDirectory.listFiles();
        if (files == null) {
            printOutput("ls: cannot access directory");
            return;
        }
        
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                if (f1.isDirectory() && !f2.isDirectory()) return -1;
                if (!f1.isDirectory() && f2.isDirectory()) return 1;
                return f1.getName().compareToIgnoreCase(f2.getName());
            }
        });

        if (files.length == 0) {
            printOutput("(empty directory)");
            return;
        }

        for (File f : files) {
            if (!showHidden && f.getName().startsWith(".")) continue;
            
            String name = f.getName();
            String lowerName = name.toLowerCase();
            
            if (f.isDirectory()) {
                printOutput("\u001b[34m" + name + "\u001b[0m");
            } else if (f.canExecute()) {
                printOutput("\u001b[32m" + name + "\u001b[0m");
            } else if (lowerName.endsWith(".zip") || lowerName.endsWith(".apk") || lowerName.endsWith(".tar") || lowerName.endsWith(".gz")) {
                printOutput("\u001b[31m" + name + "\u001b[0m");
            } else if (isSymlink(f)) {
                printOutput("\u001b[36m" + name + "\u001b[0m");
            } else {
                printOutput(name);
            }
        }
    }

    private boolean isSymlink(File file) {
        try {
            File canon;
            if (file.getParent() == null) {
                canon = file;
            } else {
                File canonDir = file.getParentFile().getCanonicalFile();
                canon = new File(canonDir, file.getName());
            }
            return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
        } catch (Exception e) {
            return false;
        }
    }

    private void runShellCommand(final String commandLine) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessBuilder pb = new ProcessBuilder("/system/bin/sh", "-c", commandLine);
                    pb.directory(currentDirectory);
                    pb.redirectErrorStream(true); 
                    
                    pb.environment().put("TERM", "xterm-256color");
                    
                    String defaultPath = "/system/bin:/system/xbin";
                    String customBin = pkgManager.getBinDir().getAbsolutePath();
                    
                    pb.environment().put("PATH", customBin + ":" + defaultPath);
                    
                    Process process = pb.start();
                    
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        printOutput(line);
                    }
                    reader.close();
                    process.waitFor();
                    
                } catch (Exception e) {
                    printOutput("Shell Error: " + e.getMessage());
                }
            }
        }).start();
    }

    public void printOutput(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv = new TextView(TerminalActivity.this);
                tv.setTextSize(terminalFontSize);
                tv.setTypeface(terminalFont);
                
                tv.setText(parseAnsiColors(text));
                
                outputLayout.addView(tv);
                
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    private SpannableStringBuilder parseAnsiColors(String text) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        Pattern pattern = Pattern.compile("\u001b\\[([0-9;]+)m");
        Matcher matcher = pattern.matcher(text);
        
        int lastEnd = 0;
        // --- FIX: Ensure themeColor is not null ---
        int currentColor = Color.parseColor(terminalThemeColor != null ? terminalThemeColor : "#E6E6E6");

        while (matcher.find()) {
            int startSpan = sb.length();
            sb.append(text.substring(lastEnd, matcher.start()));
            if (sb.length() > startSpan) {
                sb.setSpan(new ForegroundColorSpan(currentColor), startSpan, sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            String code = matcher.group(1);
            if (code.equals("0") || code.equals("00")) {
                // Reset to the chosen theme color
                currentColor = Color.parseColor(terminalThemeColor != null ? terminalThemeColor : "#E6E6E6");
            } else if (code.contains("34")) {
                currentColor = Color.parseColor("#58A6FF");
            } else if (code.contains("32")) {
                currentColor = Color.parseColor("#3FB950");
            } else if (code.contains("31")) {
                currentColor = Color.parseColor("#F85149");
            } else if (code.contains("36")) {
                currentColor = Color.parseColor("#79C0FF");
            }

            lastEnd = matcher.end();
        }

        int startSpan = sb.length();
        sb.append(text.substring(lastEnd));
        if (sb.length() > startSpan) {
            sb.setSpan(new ForegroundColorSpan(currentColor), startSpan, sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return sb;
    }
    
    private void setupIcon(TextView view, String unicode, Typeface font) {
        view.setTypeface(font);
        view.setText(unicode);
    }
    
    @Override
    protected void onResume() {
        super.onResume(); // <--- FIX: super.onResume() goes here!
        
        SharedPreferences prefs = getSharedPreferences("DebugMatePrefs", MODE_PRIVATE);
        terminalFontSize = prefs.getInt("terminal_font_size", 14);
        boolean showKeys = prefs.getBoolean("terminal_extra_keys", true);
        String theme = prefs.getString("terminal_theme", "classic");

        if (showKeys) {
            extraKeysLayout.setVisibility(View.VISIBLE);
        } else {
            extraKeysLayout.setVisibility(View.GONE);
        }

        if (theme.equals("hacker")) {
            terminalThemeColor = "#3FB950"; // Green
        } else if (theme.equals("cyberpunk")) {
            terminalThemeColor = "#8B5CF6"; // Purple
        } else {
            terminalThemeColor = "#E6E6E6"; // Classic Gray
        }

        input.setTextSize(terminalFontSize);
        promptView.setTextSize(terminalFontSize);
        
        for (int i = 0; i < outputLayout.getChildCount(); i++) {
            View child = outputLayout.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextSize(terminalFontSize);
            }
        }
    }
}