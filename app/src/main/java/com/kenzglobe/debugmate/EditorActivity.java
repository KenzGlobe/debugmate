package com.kenzglobe.debugmate;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Typeface;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class EditorActivity extends Activity {

    private File currentFile;
    private EditText codeEditor;
    private Typeface solidFont, brandsRegularFont;
    private TextView fileNameView, btnSave, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor);

        if (getActionBar() != null) {
            getActionBar().hide();
        }

		solidFont = Typeface.createFromAsset(getAssets(), "fonts/fa-solid-900-v5.ttf");
		brandsRegularFont = Typeface.createFromAsset(getAssets(), "fonts/fa-brands-regular-400-v5.ttf");

        fileNameView = findViewById(R.id.editor_file_name);
        codeEditor = findViewById(R.id.code_editor);
        btnSave = findViewById(R.id.btn_save);
        btnBack = findViewById(R.id.btn_back);
        
		btnSave.setTypeface(solidFont);
		btnSave.setText("\uf0c7");
		btnBack.setTypeface(solidFont);
		btnBack.setText("\uf060");

        String filePath = getIntent().getStringExtra("file_path");

        if (filePath != null) {
            currentFile = new File(filePath);
            fileNameView.setText(currentFile.getName());

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
            } catch (Exception e) {
                Toast.makeText(this, "Error reading file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        // 3. Handle the Save Button
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentFile != null) {
                    try {
                        FileWriter writer = new FileWriter(currentFile, false); // false = overwrite entirely
                        writer.write(codeEditor.getText().toString());
                        writer.close();
                        Toast.makeText(EditorActivity.this, "Saved successfully! ✔️", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(EditorActivity.this, "Error saving: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        // 4. Handle the Back Button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // This closes the Editor and goes back to MainActivity
            }
        });
    }
}