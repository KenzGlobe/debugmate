package com.kenzglobe.debugmate;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.File;

public class TerminalActivity extends Activity {

    private LinearLayout outputLayout;
    private EditText input;
    private ScrollView scrollView;
    
    // Start in the phone's external storage directory
    private File currentDirectory = android.os.Environment.getExternalStorageDirectory(); 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.terminal);

        if (getActionBar() != null) getActionBar().hide();

        outputLayout = findViewById(R.id.output_layout);
        input = findViewById(R.id.terminal_input);
        scrollView = findViewById(R.id.scroll_view);

        printOutput("Welcome to DebugMate Terminal!\nType 'help' for commands.\n");

        // Listen for the "Enter" key on the keyboard
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String command = input.getText().toString().trim();
                    if (!command.isEmpty()) {
                        printOutput("$ " + command);
                        executeCommand(command);
                        input.setText(""); // Clear the input box
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void executeCommand(String commandLine) {
        // Split the command by spaces
        String[] parts = commandLine.split(" ");
        String mainCommand = parts[0].toLowerCase();

        if (mainCommand.equals("help")) {
            printOutput("Available commands:\n" +
                        "  help          - Show this menu\n" +
                        "  echo <text>   - Print text back to the screen\n" +
                        "  ls            - List files in current directory\n" +
                        "  pwd           - Print current directory path\n" +
                        "  clear         - Clear the screen");
                        
        } else if (mainCommand.equals("echo")) {
            if (parts.length > 1) {
                // Join all words after "echo" back together
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < parts.length; i++) {
                    sb.append(parts[i]).append(" ");
                }
                printOutput(sb.toString().trim());
            } else {
                printOutput(""); // Echo with no text just prints a blank line
            }
            
        } else if (mainCommand.equals("ls")) {
            File[] files = currentDirectory.listFiles();
            if (files != null) {
                if (files.length == 0) {
                    printOutput("(empty directory)");
                } else {
                    for (File f : files) {
                        // Add a slash to folders so you can tell them apart
                        if (f.isDirectory()) {
                            printOutput(f.getName() + "/");
                        } else {
                            printOutput(f.getName());
                        }
                    }
                }
            } else {
                printOutput("Error: Cannot read directory (Permission denied).");
            }
            
        } else if (mainCommand.equals("pwd")) {
            printOutput(currentDirectory.getAbsolutePath());
            
        } else if (mainCommand.equals("clear")) {
            outputLayout.removeAllViews();
            
        } else {
            printOutput("Command not found: " + mainCommand);
        }
    }

    private void printOutput(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(0xFFFFFFFF); // White text
        tv.setTextSize(14);
        tv.setTypeface(android.graphics.Typeface.MONOSPACE);
        
        outputLayout.addView(tv);
        
        // Auto-scroll to the bottom
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
}