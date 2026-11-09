package com.kenzglobe.debugmate;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;

public class CheatSheetActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cheat_sheet);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#8B5CF6"));
        }
        if (getActionBar() != null) getActionBar().hide();

        Typeface solidFont = Typeface.createFromAsset(getAssets(), "fonts/fa-solid-900-v5.ttf");
        Typeface appFont = Typeface.createFromAsset(getAssets(), "fonts/SpaceMono-Bold.ttf");

        TextView btnBack = findViewById(R.id.btn_cheat_back);
        btnBack.setTypeface(solidFont);
        btnBack.setText("\uf060");
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish(); }
        });
        
        String lang = getIntent().getStringExtra("language");
        if (lang == null) lang = "python";

        // 1. Define the data
        ArrayList<CheatItem> items = getCheatData(lang);

        // 2. Setup List and Adapter
        ListView listView = findViewById(R.id.cheat_list);
        final CheatAdapter adapter = new CheatAdapter(this, items);
        listView.setAdapter(adapter);

        // 3. Setup Search Bar
        EditText searchBar = findViewById(R.id.cheat_search);
        searchBar.setTypeface(appFont); // Apply font to search bar
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                adapter.filter(s.toString());
            }
        });
    }

    private ArrayList<CheatItem> getCheatData(String lang) {
        ArrayList<CheatItem> items = new ArrayList<>();
        
        ArrayList<CodeBlock> printBlocks = new ArrayList<>();
        printBlocks.add(new CodeBlock("main.py", "print(\"Hello, World!\")"));
        items.add(new CheatItem(
            "Print to Console",
            printBlocks,
            "Outputs text to the terminal. You can also print variables directly. " +
            "This is a core part of <a href=\"learn://variables\">Variables</a>."
        ));
        
        ArrayList<CodeBlock> loopBlocks = new ArrayList<>();
        loopBlocks.add(new CodeBlock("loops.py", "for i in range(5):\n    print(i)"));
        items.add(new CheatItem(
            "For Loop",
            loopBlocks,
            "Iterates 5 times. Loops are essential for <a href=\"learn://control_flow\">Control Flow</a>."
        ));
        
        ArrayList<CodeBlock> functionBlocks = new ArrayList<>();
        functionBlocks.add(new CodeBlock("functions.py", "def greet(name):\n    return \"Hello \" + name"));
        items.add(new CheatItem(
            "Functions",
            functionBlocks,
            "Defines a reusable block of code. Click here to learn about <a href=\"learn://functions\">Functions</a>."
        ));
        
        ArrayList<CodeBlock> cursorBlocks = new ArrayList<>();
        
        cursorBlocks.add(new CodeBlock("res/drawable/cursor_white.xml", 
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<shape xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
            "    android:shape=\"rectangle\">\n" +
            "    <solid android:color=\"#FFFFFF\" />\n" +
            "    <size android:width=\"2dp\" />\n" +
            "</shape>"));
            
        cursorBlocks.add(new CodeBlock("res/layout/activity_main.xml", 
            "<EditText\n" +
            "    android:id=\"@+id/my_input\"\n" +
            "    android:textCursorDrawable=\"@drawable/cursor_white\"\n" +
            "    android:layout_width=\"match_parent\"\n" +
            "    android:layout_height=\"wrap_content\" />"));
            
        items.add(new CheatItem(
            "Change EditText Cursor Color",
            cursorBlocks,
            "First create the drawable file to define the cursor shape and color. Then apply it to your EditText using the `textCursorDrawable` attribute."
        ));
        
        ArrayList<CodeBlock> textLayoutBlocks = new ArrayList<>();
        textLayoutBlocks.add(new CodeBlock("layout.xml", "<span>Hello</span>"));
        items.add(new CheatItem(
            "Text Layout",
            textLayoutBlocks,
            "The span is an inline container. It is a fundamental <a href=\"learn://ui_elements\">UI element</a>."
        ));
        
        
        ArrayList<CodeBlock> htmlBlocks = new ArrayList<>();
        htmlBlocks.add(new CodeBlock("index.html", "<h1>Hello World</h1>\n<p>This is HTML</p>"));
        items.add(new CheatItem(
            "Basic Layout",
            htmlBlocks,
            "A simple HTML structure. Learn more about <a href=\"learn://ui_elements\">UI Elements</a>."
        ));
        
        return items;
    }
}
