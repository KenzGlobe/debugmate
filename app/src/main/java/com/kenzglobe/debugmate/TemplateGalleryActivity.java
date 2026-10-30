
package com.kenzglobe.debugmate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TemplateGalleryActivity extends Activity {
    private String pendingTemplateType = "";
    private String pendingTemplateName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.template_gallery);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#8B5CF6"));
            getWindow().getDecorView().setSystemUiVisibility(0);
        }

        if (getActionBar() != null) getActionBar().hide();

        Typeface solidFont = Typeface.createFromAsset(getAssets(), "fonts/fa-solid-900-v5.ttf");

        TextView btnClose = findViewById(R.id.btn_close_templates);
        btnClose.setTypeface(solidFont);
        btnClose.setText("\uf00d");

        EditText search = findViewById(R.id.template_search);
        GridView grid = findViewById(R.id.template_grid);

        // TEMPLATE DATA
        ArrayList<TemplateItem> templates = new ArrayList<>();

        templates.add(new TemplateItem("Basic Web", "HTML, CSS, JavaScript", "web", "\uf121"));
        templates.add(new TemplateItem("Python Console", "Basic Python", "python", "\uf3e2"));
        templates.add(new TemplateItem("Java Class", "Standard Java", "java", "\uf4e4"));
        templates.add(new TemplateItem("Android Empty", "Basic Activity", "android", "\uf17b"));

        final TemplateAdapter adapter = new TemplateAdapter(this, templates);
        grid.setAdapter(adapter);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TemplateItem selected = adapter.getItem(position);
                pendingTemplateType = selected.type;
                pendingTemplateName = selected.name;
                
                // Launch MainActivity in Picker Mode
                android.content.Intent intent = new android.content.Intent(TemplateGalleryActivity.this, MainActivity.class);
                intent.putExtra("PICK_FOLDER_MODE", true);
                intent.putExtra("TEMPLATE_TYPE", selected.type);
                intent.putExtra("TEMPLATE_NAME", selected.name);
                startActivityForResult(intent, 2001);
            }
        });
    }

    private void showProjectNameDialog(final String targetPath, final String type, final String templateName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Project Name (" + templateName + ")");

        final EditText input = new EditText(this);
        input.setHint("e.g., MyProject");
        input.setPadding(40, 40, 40, 40);
        builder.setView(input);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String projectName = input.getText().toString().trim();

                if (!isValidProjectName(projectName)) {
                    Toast.makeText(TemplateGalleryActivity.this, "Invalid project name.", Toast.LENGTH_SHORT).show();
                    return;
                }

                createProject(targetPath, type, projectName);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private boolean isValidProjectName(String name) {
        if (name.isEmpty() || name.equals(".") || name.equals("..")) return false;

        return !name.matches(".*[\\\\/:*?\"<>|].*");
    }

    private void createProject(String targetPath, String type, String projectName) {
        File projectFolder = new File(targetPath, projectName);

        if (projectFolder.exists()) {
            showError("A project with this name already exists.");
            return;
        }

        if (!projectFolder.mkdirs()) {
            showError("Failed to create project folder.");
            return;
        }

        try {
            if ("web".equals(type)) {
                createBasicWebProject(projectFolder, projectName);
            } else if ("python".equals(type)) {
                writeFile(projectFolder, "main.py",
                    "# " + projectName + "\n\n" +
                    "def main():\n" +
                    "    print('Hello from " + projectName + "!')\n\n" +
                    "if __name__ == '__main__':\n" +
                    "    main()\n"
                );
            } else if ("java".equals(type)) {
                writeFile(projectFolder, "Main.java",
                    "public class Main {\n" +
                    "    public static void main(String[] args) {\n" +
                    "        System.out.println(\"Hello from " + projectName + "!\");\n" +
                    "    }\n" +
                    "}\n"
                );
            } else if ("android".equals(type)) {
                writeFile(projectFolder, "README.md", "# " + projectName + "\n\nAndroid project starter.\n");
            }

            // SAVE TO JSON METADATA
            ProjectItem newProject = new ProjectItem(projectName, type, projectFolder.getAbsolutePath(), "Custom");
            ProjectManager.saveProject(this, newProject);

            Toast.makeText(this, "Project '" + projectName + "' created successfully!", Toast.LENGTH_LONG).show();
            finish();

        } catch (IOException e) {
            deleteRecursive(projectFolder);
            showError("Project creation failed: " + e.getMessage());
        }
    }

    private void createBasicWebProject(File root, String projectName) throws IOException {
        // Create folders
        createDirectory(root, "css");
        createDirectory(root, "js");

        File assets = createDirectory(root, "assets");
        createDirectory(assets, "images");
        createDirectory(assets, "icons");

        // index.html
        writeFile(root, "index.html",
            "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>" + projectName + "</title>\n" +
            "    <link rel=\"stylesheet\" href=\"css/style.css\">\n" +
            "</head>\n" +
            "<body>\n\n" +
            "    <h1>Welcome to " + projectName + "</h1>\n" +
            "    <p>Your web project is ready.</p>\n\n" +
            "    <script src=\"js/script.js\"></script>\n" +
            "</body>\n" +
            "</html>\n"
        );

        // css/style.css
        File cssDir = new File(root, "css");

        writeFile(cssDir, "style.css",
            "* {\n" +
            "    margin: 0;\n" +
            "    padding: 0;\n" +
            "    box-sizing: border-box;\n" +
            "}\n\n" +
            "body {\n" +
            "    font-family: Arial, sans-serif;\n" +
            "    background: #ffffff;\n" +
            "    color: #222222;\n" +
            "    padding: 20px;\n" +
            "}\n"
        );

        // js/script.js
        File jsDir = new File(root, "js");

        writeFile(jsDir, "script.js",
            "document.addEventListener(\"DOMContentLoaded\", function() {\n" +
            "    console.log(\"" + projectName + " is ready!\");\n" +
            "});\n"
        );

        // README.md
        writeFile(root, "README.md",
            "# " + projectName + "\n\n" +
            "A basic HTML, CSS and JavaScript project.\n\n" +
            "## Getting Started\n\n" +
            "Open `index.html` in a web browser.\n"
        );

        // .gitignore
        writeFile(root, ".gitignore",
            ".DS_Store\n" +
            "Thumbs.db\n" +
            "*.log\n" +
            ".vscode/\n" +
            ".idea/\n"
        );
    }

    private File createDirectory(File parent, String name) throws IOException {
        File directory = new File(parent, name);

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create directory: " + name);
        }

        return directory;
    }

    private void writeFile(File parent, String name, String content) throws IOException {
        File file = new File(parent, name);
        FileWriter writer = null;

        try {
            writer = new FileWriter(file);
            writer.write(content);
            writer.flush();

        } finally {
            if (writer != null) writer.close();
        }
    }

    private void deleteRecursive(File file) {
        if (file == null || !file.exists()) return;

        if (file.isDirectory()) {
            File[] children = file.listFiles();

            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }

        file.delete();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2001 && resultCode == RESULT_OK && data != null) {
            String selectedPath = data.getStringExtra("SELECTED_PATH");
            if (selectedPath != null) {
                showProjectNameDialog(selectedPath, pendingTemplateType, pendingTemplateName);
            }
        }
    }
}
