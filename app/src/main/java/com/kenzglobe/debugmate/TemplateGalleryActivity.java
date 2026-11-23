
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
import android.widget.*;
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
        
        // ==========================================
        // 1. POPULAR / FRONTEND
        // ==========================================
        templates.add(new TemplateItem("Basic Web", "HTML, CSS, JavaScript", "web", "\uf121"));
        templates.add(new TemplateItem("Python Script", "Basic Python console app", "python", "\uf3e2"));
        
        // // ==========================================
        // // 2. WEB FRAMEWORKS
        // // ==========================================
        // templates.add(new TemplateItem("React App", "Modern UI library starter", "react", "\uf41b"));
        // templates.add(new TemplateItem("Vue App", "Progressive framework starter", "vue", "\uf41f"));
        // templates.add(new TemplateItem("HTML5 Canvas", "Interactive graphics setup", "canvas", "\uf1c8"));
        
        // // ==========================================
        // // 3. BACKEND & SCRIPTING
        // // ==========================================
        // templates.add(new TemplateItem("Python Flask", "Lightweight web API", "flask", "\uf3e2"));
        // templates.add(new TemplateItem("PHP Script", "Server-side scripting", "php", "\uf457"));
        // templates.add(new TemplateItem("Ruby Script", "Basic Ruby environment", "ruby", "\uf41d"));
        // templates.add(new TemplateItem("Go Console", "Fast, compiled backend", "go", "\uf3d1"));
        // templates.add(new TemplateItem("Rust Console", "Safe, systems programming", "rust", "\uf47b"));
        
        // // ==========================================
        // // 4. COMPILED & SYSTEMS
        // // ==========================================
        // templates.add(new TemplateItem("C++ Console", "Standard C++ program", "cpp", "\uf121"));
        // templates.add(new TemplateItem("C Console", "Standard C program", "c", "\uf121"));
        // templates.add(new TemplateItem("Java Console", "Standard Java application", "java", "\uf4e4"));
        // templates.add(new TemplateItem("Kotlin Console", "Modern JVM language", "kotlin", "\uf3d1"));
        // templates.add(new TemplateItem("C# Console", "Microsoft .NET setup", "csharp", "\uf4e4"));
        // templates.add(new TemplateItem("Swift Console", "Apple ecosystem starter", "swift", "\uf41e"));
        
        // ==========================================
        // 5. MOBILE & GENERAL
        // ==========================================
        templates.add(new TemplateItem("Android Activity", "Basic Android UI", "android", "\uf17b"));
        templates.add(new TemplateItem("Empty Project", "Start from scratch", "generic", "\uf07b"));

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
        // 1. Inflate the custom layout
        android.view.LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_create_project, null);
    
        final EditText inputName = dialogView.findViewById(R.id.input_project_name);
        final EditText inputPackage = dialogView.findViewById(R.id.input_package_name);
        LinearLayout packageLayout = dialogView.findViewById(R.id.layout_package_name);
    
        // 2. Show Package Name only for Android
        if (type.equals("android")) {
            packageLayout.setVisibility(View.VISIBLE);
            // Auto-fill a default package name based on the template
            inputName.setText("MyApp"); 
            inputPackage.setText("com.example.myapp");
        } else {
            packageLayout.setVisibility(View.GONE);
        }
    
        // 3. Show the AppDialog
        AppDialog.show(
            TemplateGalleryActivity.this,
            "Create " + templateName,
            "Location: " + targetPath,
            dialogView,
            "CREATE",
            "CANCEL",
            new AppDialog.OnDialogClickListener() {
                @Override
                public void onClick() {
                    String projectName = inputName.getText().toString().trim();
                    String packageName = inputPackage.getText().toString().trim();
    
                    if (!isValidProjectName(projectName)) {
                        AppToast.showError(TemplateGalleryActivity.this, "Invalid project name.");
                        return;
                    }
    
                    if (type.equals("android") && (packageName.isEmpty() || !packageName.contains("."))) {
                        AppToast.showError(TemplateGalleryActivity.this, "Package name must contain a dot (e.g., com.example.app)");
                        return;
                    }
    
                    // Create the config object
                    ProjectConfig config = new ProjectConfig(projectName, type);
                    config.packageName = packageName;
    
                    createProject(targetPath, config);
                }
            },
            null
        );
    }

    private boolean isValidProjectName(String name) {
        if (name.isEmpty() || name.equals(".") || name.equals("..")) return false;
        return !name.matches(".*[\\\\/:*?\"<>|].*");
    }

    private void createProject(String targetPath, ProjectConfig config) {
        File projectFolder = new File(targetPath, config.projectName);
    
        if (projectFolder.exists()) {
            showError("A project with this name already exists.");
            return;
        }
    
        if (!projectFolder.mkdirs()) {
            showError("Failed to create project folder.");
            return;
        }
    
        try {
            // --- 1. GENERATE TEMPLATE-SPECIFIC FILES ---
            if ("android".equals(config.projectType)) {
                ProjectTemplate template = new Template_AndroidEmpty();
                template.generate(projectFolder, config);
                
                ProjectItem newProject = new ProjectItem(config.projectName, config.projectType, projectFolder.getAbsolutePath(), "Custom");
                ProjectManager.saveProject(this, newProject);
                AppToast.showSuccess(this, "Project '" + config.projectName + "' created!");
                finish();
                return; // Exit early!
            }
    
            // --- Legacy Fallback for Web, Python, etc ---
            String type = config.projectType;
            String projectName = config.projectName;
            
            if ("web".equals(type)) {
                createBasicWebProject(projectFolder, projectName);
            } else if ("react".equals(type)) {
                writeFile(projectFolder, "package.json", "{\n  \"name\": \"" + projectName.toLowerCase().replace(" ", "-") + "\",\n  \"version\": \"1.0.0\",\n  \"dependencies\": {\n    \"react\": \"^18.0.0\",\n    \"react-dom\": \"^18.0.0\"\n  }\n}\n");
                writeFile(projectFolder, "App.js", "import React from 'react';\n\nfunction App() {\n  return (\n    <div>\n      <h1>Hello from " + projectName + "!</h1>\n    </div>\n  );\n}\n\nexport default App;\n");
                writeFile(projectFolder, "index.html", "<!DOCTYPE html>\n<html>\n<head>\n  <title>" + projectName + "</title>\n</head>\n<body>\n  <div id=\"root\"></div>\n  <script src=\"App.js\"></script>\n</body>\n</html>\n");
            }
            else if ("vue".equals(type)) {
                writeFile(projectFolder, "package.json", "{\n  \"name\": \"" + projectName.toLowerCase().replace(" ", "-") + "\",\n  \"version\": \"1.0.0\",\n  \"dependencies\": {\n    \"vue\": \"^3.0.0\"\n  }\n}\n");
                writeFile(projectFolder, "App.vue", "<template>\n  <div>\n    <h1>Hello from " + projectName + "!</h1>\n  </div>\n</template>\n\n<script>\nexport default {\n  name: 'App'\n}\n</script>\n");
            }
            else if ("canvas".equals(type)) {
                writeFile(projectFolder, "index.html", "<!DOCTYPE html>\n<html>\n<head>\n  <title>" + projectName + "</title>\n  <style>body { margin: 0; overflow: hidden; }</style>\n</head>\n<body>\n  <canvas id=\"gameCanvas\"></canvas>\n  <script src=\"game.js\"></script>\n</body>\n</html>\n");
                writeFile(projectFolder, "game.js", "const canvas = document.getElementById('gameCanvas');\nconst ctx = canvas.getContext('2d');\n\ncanvas.width = window.innerWidth;\ncanvas.height = window.innerHeight;\n\nfunction animate() {\n  ctx.clearRect(0, 0, canvas.width, canvas.height);\n  requestAnimationFrame(animate);\n}\n\nanimate();\n");
            }
            else if ("node".equals(type)) {
                writeFile(projectFolder, "package.json", "{\n  \"name\": \"" + projectName.toLowerCase().replace(" ", "-") + "\",\n  \"version\": \"1.0.0\",\n  \"main\": \"server.js\",\n  \"scripts\": {\n    \"start\": \"node server.js\"\n  },\n  \"dependencies\": {}\n}\n");
                writeFile(projectFolder, "server.js", "const http = require('http');\n\nconst server = http.createServer((req, res) => {\n  res.writeHead(200, {'Content-Type': 'text/plain'});\n  res.end('Hello from " + projectName + "!\\n');\n});\n\nconst PORT = 3000;\nserver.listen(PORT, () => {\n  console.log(`Server running at http://localhost:${PORT}/`);\n});\n");
            }
            else if ("python".equals(type)) {
                writeFile(projectFolder, "main.py", "# " + projectName + "\n\ndef main():\n    print('Hello from " + projectName + "!')\n\nif __name__ == '__main__':\n    main()\n");
                writeFile(projectFolder, "requirements.txt", "# Add your dependencies here\n");
            }
            else if ("flask".equals(type)) {
                writeFile(projectFolder, "app.py", "from flask import Flask\n\napp = Flask(__name__)\n\n@app.route('/')\ndef hello():\n    return 'Hello from " + projectName + "!'\n\nif __name__ == '__main__':\n    app.run(debug=True)\n");
                writeFile(projectFolder, "requirements.txt", "Flask==2.0.1\n");
            }
            else if ("php".equals(type)) {
                writeFile(projectFolder, "index.php", "<?php\necho \"Hello from " + projectName + "!\";\n?>\n");
            }
            else if ("ruby".equals(type)) {
                writeFile(projectFolder, "main.rb", "puts 'Hello from " + projectName + "!'\n");
            }
            else if ("go".equals(type)) {
                writeFile(projectFolder, "main.go", "package main\n\nimport \"fmt\"\n\nfunc main() {\n    fmt.Println(\"Hello from " + projectName + "!\")\n}\n");
                writeFile(projectFolder, "go.mod", "module " + projectName.toLowerCase().replace(" ", "-") + "\n\ngo 1.20\n");
            }
            else if ("rust".equals(type)) {
                writeFile(projectFolder, "main.rs", "fn main() {\n    println!(\"Hello from " + projectName + "!\");\n}\n");
            }
            else if ("cpp".equals(type)) {
                writeFile(projectFolder, "main.cpp", "#include <iostream>\n\nint main() {\n    std::cout << \"Hello from " + projectName + "!\" << std::endl;\n    return 0;\n}\n");
            }
            else if ("c".equals(type)) {
                writeFile(projectFolder, "main.c", "#include <stdio.h>\n\nint main() {\n    printf(\"Hello from " + projectName + "!\\n\");\n    return 0;\n}\n");
            }
            else if ("java".equals(type)) {
                writeFile(projectFolder, "Main.java", "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello from " + projectName + "!\");\n    }\n}\n");
            }
            else if ("kotlin".equals(type)) {
                writeFile(projectFolder, "Main.kt", "fun main() {\n    println(\"Hello from " + projectName + "!\")\n}\n");
            }
            else if ("csharp".equals(type)) {
                writeFile(projectFolder, "Program.cs", "using System;\n\nclass Program {\n    static void Main() {\n        Console.WriteLine(\"Hello from " + projectName + "!\");\n    }\n}\n");
            }
            else if ("swift".equals(type)) {
                writeFile(projectFolder, "main.swift", "print(\"Hello from " + projectName + "!\")\n");
            }
    
            // --- 2. STANDARD BOILERPLATE (README & .gitignore) ---
            File readmeFile = new File(projectFolder, "README.md");
            if (!readmeFile.exists()) {
                writeFile(projectFolder, "README.md", "# " + projectName + "\n\nCreated with DebugMate.\n");
            }
    
            File gitignoreFile = new File(projectFolder, ".gitignore");
            if (!gitignoreFile.exists()) {
                String gitignoreContent = ".DS_Store\nThumbs.db\n*.log\n.vscode/\n.idea/\n";
                if ("python".equals(type) || "flask".equals(type)) gitignoreContent += "__pycache__/\n*.pyc\nvenv/\n";
                if ("node".equals(type) || "react".equals(type) || "vue".equals(type)) gitignoreContent += "node_modules/\n.env\n";
                if ("java".equals(type) || "kotlin".equals(type)) gitignoreContent += "*.class\nbin/\n";
                if ("cpp".equals(type) || "c".equals(type)) gitignoreContent += "*.exe\n*.o\n";
                if ("go".equals(type)) gitignoreContent += "*.exe\n";
                if ("rust".equals(type)) gitignoreContent += "target/\n";
                if ("csharp".equals(type)) gitignoreContent += "bin/\nobj/\n";
                
                writeFile(projectFolder, ".gitignore", gitignoreContent);
            }
    
            // --- 3. SAVE TO PROJECT MANAGER ---
            ProjectItem newProject = new ProjectItem(projectName, type, projectFolder.getAbsolutePath(), "Custom");
            ProjectManager.saveProject(this, newProject);
    
            AppToast.showSuccess(this, "Project '" + projectName + "' created!");
            finish();
    
        } catch (IOException e) {
            deleteRecursive(projectFolder);
            showError("Project creation failed: " + e.getMessage());
        }
    }

    private void createBasicWebProject(File root, String projectName) throws IOException {
        // 1. Create standard folders
        createDirectory(root, "css");
        createDirectory(root, "js");
        File assets = createDirectory(root, "assets");
        createDirectory(assets, "images");
        createDirectory(assets, "icons");

        // 2. index.html
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
            "    <div class=\"container\">\n" +
            "        <h1>Welcome to <span class=\"highlight\">" + projectName + "</span></h1>\n" +
            "        <p>Your modern web project is ready to go.</p>\n" +
            "        <button id=\"actionBtn\">Click Me</button>\n" +
            "    </div>\n\n" +
            "    <script src=\"js/script.js\" defer></script>\n" +
            "</body>\n" +
            "</html>\n"
        );

        // 3. css/style.css
        File cssDir = new File(root, "css");
        writeFile(cssDir, "style.css",
            ":root {\n" +
            "    --bg-dark: #0f0f13;\n" +
            "    --bg-card: #1a1a24;\n" +
            "    --primary: #8B5CF6;\n" +
            "    --text-main: #f3f4f6;\n" +
            "    --text-muted: #9ca3af;\n" +
            "}\n\n" +
            "* {\n" +
            "    margin: 0;\n" +
            "    padding: 0;\n" +
            "    box-sizing: border-box;\n" +
            "    font-family: 'Segoe UI', system-ui, sans-serif;\n" +
            "}\n\n" +
            "body {\n" +
            "    background-color: var(--bg-dark);\n" +
            "    color: var(--text-main);\n" +
            "    display: flex;\n" +
            "    justify-content: center;\n" +
            "    align-items: center;\n" +
            "    min-height: 100vh;\n" +
            "}\n\n" +
            ".container {\n" +
            "    background: var(--bg-card);\n" +
            "    padding: 40px;\n" +
            "    border-radius: 16px;\n" +
            "    box-shadow: 0 10px 30px rgba(0,0,0,0.5);\n" +
            "    text-align: center;\n" +
            "    border: 1px solid rgba(255,255,255,0.05);\n" +
            "    max-width: 400px;\n" +
            "}\n\n" +
            "h1 {\n" +
            "    margin-bottom: 12px;\n" +
            "    font-size: 24px;\n" +
            "}\n\n" +
            ".highlight {\n" +
            "    color: var(--primary);\n" +
            "}\n\n" +
            "p {\n" +
            "    color: var(--text-muted);\n" +
            "    margin-bottom: 24px;\n" +
            "    font-size: 14px;\n" +
            "}\n\n" +
            "button {\n" +
            "    background-color: var(--primary);\n" +
            "    color: white;\n" +
            "    border: none;\n" +
            "    padding: 12px 28px;\n" +
            "    border-radius: 8px;\n" +
            "    font-size: 15px;\n" +
            "    font-weight: bold;\n" +
            "    cursor: pointer;\n" +
            "    transition: transform 0.2s, background 0.2s;\n" +
            "}\n\n" +
            "button:hover {\n" +
            "    background-color: #7c3aed;\n" +
            "    transform: translateY(-2px);\n" +
            "}\n"
        );

        // 4. js/script.js
        File jsDir = new File(root, "js");
        writeFile(jsDir, "script.js",
            "document.addEventListener('DOMContentLoaded', () => {\n" +
            "    const btn = document.getElementById('actionBtn');\n" +
            "    \n" +
            "    btn.addEventListener('click', () => {\n" +
            "        btn.innerText = 'Awesome! 🚀';\n" +
            "        btn.style.backgroundColor = '#10b981'; // Green\n" +
            "        setTimeout(() => {\n" +
            "            btn.innerText = 'Click Me';\n" +
            "            btn.style.backgroundColor = '';\n" +
            "        }, 2000);\n" +
            "    });\n" +
            "    \n" +
            "    console.log('" + projectName + " loaded successfully.');\n" +
            "});\n"
        );

        // 5. robots.txt
        writeFile(root, "robots.txt", "User-agent: *\nAllow: /\n");

        // 6. README.md
        writeFile(root, "README.md",
            "# " + projectName + "\n\n" +
            "A modern, responsive web project generated by DebugMate.\n\n" +
            "## Features\n" +
            "- CSS Variables for easy theming\n" +
            "- Flexbox centering\n" +
            "- Interactive JavaScript button\n\n" +
            "## Usage\n" +
            "Open `index.html` in your browser.\n"
        );

        // 7. .gitignore
        writeFile(root, ".gitignore", ".DS_Store\nThumbs.db\n*.log\n.vscode/\n.idea/\n");
    };

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
        AppToast.showError(this, message);
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
