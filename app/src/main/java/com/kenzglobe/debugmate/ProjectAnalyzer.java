package com.kenzglobe.debugmate;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ArrayList;

public class ProjectAnalyzer {

    public static class ProjectInfo {
        public String type = "Generic";
        public String language = "Unknown";
        public String mainFile = "--";
        public String dependencies = "--";
        public String iconUnicode = "\uf07b"; 
        public boolean isBrandIcon = false;
        public int iconColor = Color.parseColor("#8B5CF6"); 
        
        // New Code Health & README Fields
        public int totalLines = 0;
        public int todoCount = 0;
        public boolean hasReadme = false;
        public String readmePreview = "No README.md found.";
        
        // Health Warnings
        public ArrayList<String> healthWarnings = new ArrayList<>();
        
        // Project Structure Preview
        public ArrayList<String> structurePreview = new ArrayList<>();
    }

    public interface AnalyzeCallback {
        void onAnalysisComplete(ProjectInfo info);
    }

    private static final Set<String> IGNORED_NAMES = new HashSet<>(Arrays.asList(
            ".git", ".gradle", ".idea", ".vscode", "build", "bin", "obj", "out", "dist", 
            "node_modules", "venv", ".env", "__pycache__", ".pytest_cache", ".DS_Store"
    ));

    public static void analyze(final Context context, final String projectPath, final AnalyzeCallback callback) {
        final Handler mainHandler = new Handler(Looper.getMainLooper());
    
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ProjectInfo info = new ProjectInfo();
                File dir = new File(projectPath);
    
                if (dir.exists() && dir.isDirectory()) {
                    
                    // 1. ANDROID (Highest Priority)
                    // Look for AndroidManifest.xml or settings.gradle (safer than just build.gradle)
                    if (new File(dir, "AndroidManifest.xml").exists() || 
                        new File(dir, "settings.gradle").exists() || 
                        new File(dir, "app/src/main/AndroidManifest.xml").exists()) {
                        
                        info.type = "Android";
                        info.language = "Java/Kotlin";
                        info.mainFile = "MainActivity.java";
                        info.dependencies = "Gradle";
                        info.iconUnicode = "\uf17b"; // Android brand
                        info.isBrandIcon = true;
                        info.iconColor = Color.parseColor("#3DDC84"); // Android Green
                    } 
                    
                    // 2. JAVA (Maven or standalone Gradle)
                    else if (new File(dir, "pom.xml").exists() || new File(dir, "build.gradle").exists()) {
                        info.type = "Java";
                        info.language = "Java";
                        info.mainFile = new File(dir, "src/main/java/Main.java").exists() ? "Main.java" : "*.java";
                        info.dependencies = new File(dir, "pom.xml").exists() ? "Maven" : "Gradle";
                        info.iconUnicode = "\uf4e4"; // Java brand
                        info.isBrandIcon = true;
                        info.iconColor = Color.parseColor("#ED8B00"); // Java Orange
                    }
                    
                    // 3. NODE.JS / WEB (Both use package.json)
                    else if (new File(dir, "package.json").exists()) {
                        if (new File(dir, "index.html").exists()) {
                            // It's a Web project (React, Vue, Vanilla)
                            info.type = "Web";
                            info.language = "JavaScript/HTML";
                            info.mainFile = "index.html";
                            info.dependencies = "npm (package.json)";
                            info.iconUnicode = "\uf121"; // Code solid icon
                            info.isBrandIcon = false;
                            info.iconColor = Color.parseColor("#E34F26"); // HTML Orange
                        } else {
                            // It's a pure Node.js backend
                            info.type = "Node.js";
                            info.language = "JavaScript";
                            info.mainFile = new File(dir, "index.js").exists() ? "index.js" : "server.js";
                            info.dependencies = new File(dir, "node_modules").exists() ? "Installed" : "Not installed";
                            info.iconUnicode = "\uf3d3"; // Node brand
                            info.isBrandIcon = true;
                            info.iconColor = Color.parseColor("#339933"); // Node Green
                        }
                    }
                    
                    // 4. PYTHON
                    else if (new File(dir, "requirements.txt").exists() || 
                             new File(dir, "main.py").exists() || 
                             new File(dir, "app.py").exists() || 
                             containsFileWithExtension(dir, ".py")) {
                        info.type = "Python";
                        info.language = "Python";
                        info.mainFile = new File(dir, "main.py").exists() ? "main.py" : (new File(dir, "app.py").exists() ? "app.py" : "*.py");
                        info.dependencies = countDependencies(new File(dir, "requirements.txt"));
                        info.iconUnicode = "\uf3e2"; // Python brand
                        info.isBrandIcon = true;
                        info.iconColor = Color.parseColor("#3776AB"); // Python Blue
                    } 
                    
                    // 5. PURE WEB (Static HTML/CSS/JS without Node)
                    else if (new File(dir, "index.html").exists()) {
                        info.type = "Web";
                        info.language = "HTML/CSS/JS";
                        info.mainFile = "index.html";
                        info.dependencies = "None";
                        info.iconUnicode = "\uf121"; // Code solid icon
                        info.isBrandIcon = false;
                        info.iconColor = Color.parseColor("#E34F26"); // HTML Orange
                    }
                    
                    // 6. GENERIC FALLBACK
                    else {
                        info.type = "Generic";
                        info.language = "Unknown";
                        info.mainFile = "Unknown";
                        info.dependencies = "None";
                        info.iconUnicode = "\uf07b"; // Folder solid icon
                        info.isBrandIcon = false;
                        info.iconColor = Color.parseColor("#8B5CF6"); // Purple
                    }
    
                    // 7. Analyze Code Health & README
                    analyzeCodeHealth(dir, info);
                }
    
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onAnalysisComplete(info);
                    }
                });
            }
        }).start();
    }

    private static void analyzeCodeHealth(File dir, ProjectInfo info) {
        File readme = new File(dir, "README.md");
        if (!readme.exists()) readme = new File(dir, "readme.md");
        
        if (readme.exists() && readme.isFile()) {
            info.hasReadme = true;
            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(readme));
                String line;
                int linesRead = 0;
                while ((line = br.readLine()) != null && linesRead < 5) {
                    sb.append(line).append("\n");
                    linesRead++;
                }
                br.close();
                info.readmePreview = sb.toString().trim();
                if (info.readmePreview.isEmpty()) info.readmePreview = "(Empty README)";
            } catch (Exception e) {
                info.readmePreview = "Error reading README.";
            }
        }

        // Recursive scan for lines and TODOs
        scanDirectory(dir, info);
        checkHealth(dir, info);
        generateStructurePreview(dir, info);
    }
    
    private static void checkHealth(File dir, ProjectInfo info) {
        // 1. Universal checks
        if (!info.hasReadme) {
            info.healthWarnings.add("Missing README.md documentation.");
        }
        if (!new File(dir, ".gitignore").exists()) {
            info.healthWarnings.add("Missing .gitignore (recommended for version control).");
        }
    
        // 2. Project-specific checks
        if (info.type.equals("Python")) {
            if (!new File(dir, "requirements.txt").exists()) {
                info.healthWarnings.add("Missing requirements.txt (no dependencies tracked).");
            }
            if (!new File(dir, "main.py").exists() && !new File(dir, "app.py").exists()) {
                info.healthWarnings.add("No standard entry point (main.py or app.py) found.");
            }
        } else if (info.type.equals("Node.js")) {
            if (!new File(dir, "index.js").exists() && !new File(dir, "server.js").exists()) {
                info.healthWarnings.add("No standard entry point (index.js or server.js) found.");
            }
        } else if (info.type.equals("Web")) {
            if (!new File(dir, "index.html").exists()) {
                info.healthWarnings.add("Missing index.html entry point.");
            }
        } else if (info.type.equals("Android") || info.type.equals("Java")) {
            if (!new File(dir, "build.gradle").exists() && !new File(dir, "pom.xml").exists()) {
                info.healthWarnings.add("Missing build configuration (build.gradle or pom.xml).");
            }
        }
    }
    
    private static void generateStructurePreview(File dir, ProjectInfo info) {
        File[] files = dir.listFiles();
        if (files == null) return;
    
        // Sort: Directories first, then alphabetically
        Arrays.sort(files, new java.util.Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                if (f1.isDirectory() && !f2.isDirectory()) return -1;
                if (!f1.isDirectory() && f2.isDirectory()) return 1;
                return f1.getName().compareToIgnoreCase(f2.getName());
            }
        });
    
        int maxItems = 6;
        int displayedCount = 0;
        
        // First, count how many we will actually show
        ArrayList<File> toShow = new ArrayList<>();
        for (File f : files) {
            if (f.getName().startsWith(".")) continue; // Skip hidden files
            toShow.add(f);
        }
    
        int total = toShow.size();
        int limit = Math.min(total, maxItems);
    
        for (int i = 0; i < limit; i++) {
            File f = toShow.get(i);
            String prefix = (i == limit - 1 && total <= maxItems) ? "    └── " : "    ├── ";
            String suffix = f.isDirectory() ? "/" : "";
            info.structurePreview.add(prefix + f.getName() + suffix);
        }
    
        if (total > maxItems) {
            info.structurePreview.add("    └── ... (" + (total - maxItems) + " more items)");
        }
    }

    private static void scanDirectory(File dir, ProjectInfo info) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (IGNORED_NAMES.contains(f.getName())) continue;

            if (f.isDirectory()) {
                scanDirectory(f, info);
            } else {
                String name = f.getName().toLowerCase();
                if (name.endsWith(".py") || name.endsWith(".js") || name.endsWith(".java") || 
                    name.endsWith(".html") || name.endsWith(".css") || name.endsWith(".md") || 
                    name.endsWith(".xml") || name.endsWith(".txt")) {
                    
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(f));
                        String line;
                        while ((line = br.readLine()) != null) {
                            info.totalLines++;
                            String upperLine = line.toUpperCase();
                            if (upperLine.contains("TODO") || upperLine.contains("FIXME")) {
                                info.todoCount++;
                            }
                        }
                        br.close();
                    } catch (Exception e) {
                        // Ignore unreadable files
                    }
                }
            }
        }
    }

    private static boolean containsFileWithExtension(File dir, String ext) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile() && f.getName().endsWith(ext)) return true;
            }
        }
        return false;
    }

    private static String countDependencies(File reqFile) {
        if (!reqFile.exists()) return "0 detected";
        int count = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(reqFile));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.startsWith("#")) count++;
            }
            reader.close();
        } catch (Exception e) { return "Unknown"; }
        return count + " detected";
    }
}
