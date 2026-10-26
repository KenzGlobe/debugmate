package com.kenzglobe.debugmate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.graphics.Typeface;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import android.view.View;
import android.view.Gravity;
import android.view.View.OnClickListener;
import android.support.v4.widget.DrawerLayout;

public class MainActivity extends Activity {

    private ListView fileListView;
    private FrameLayout startUpLayout;
    private View popupDimView;
    private ArrayList<FileItem> fileList;
    private FileListAdapter adapter;
    private DrawerLayout drawerLayout;
    private File currentDirectory = null;
    private long backPressedTime = 0;
    
    private Typeface appFontBold,
        appFontRegular,
        solidFont,
        brandsRegularFont;
    
    private TextView welcomeMessage;
    
    // Sidebar Variables
    private TextView navBarSectionOne,
        navBarSectionTwo,
        navBarSectionThree,
        navBarSectionFour,
        navBarAppName;
        
    private TextView sideBarBtn,
        appName,
        haveAProject,
        fabAdd,
        createFolderBtn,
        createFileBtn;
        
    private TextView createProjectIcon,
        createProjectBtn,
        importProjectIcon,
        importProjectBtn,
        cloneFromGitIcon,
        cloneFromGitBtn;

    // Sidebar Icons & Text Items
    private TextView navFileManager,
        navProjects,
        navTerminal,
        navConsole,
        navWorkspace;
    private TextView navLearn,
        navCheatsheets,
        navAppSettings,
        navEditorSettings,
        navTerminalSettings;
    private TextView iconFileManager,
        iconProjects,
        iconTerminal,
        iconConsole,
        iconWorkspace;
    private TextView iconLearn,
        iconCheatsheets,
        iconAppSettings,
        iconEditorSettings,
        iconTerminalSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#8B5CF6"));
            getWindow().getDecorView().setSystemUiVisibility(0);
        }

        if (getActionBar() != null) getActionBar().hide();
        
        // Initialize Views
        welcomeMessage = findViewById(R.id.welcome_message);
        startUpLayout = findViewById(R.id.start_up_layout);
        popupDimView = findViewById(R.id.popup_dim_view);
        navBarSectionOne = findViewById(R.id.nav_bar_section_one);
        navBarSectionTwo = findViewById(R.id.nav_bar_section_two);
        navBarSectionThree = findViewById(R.id.nav_bar_section_three);
        navBarSectionFour = findViewById(R.id.nav_bar_section_four);
        sideBarBtn = findViewById(R.id.side_bar_btn);
        haveAProject = findViewById(R.id.have_a_project);
        fabAdd = findViewById(R.id.fab_add);
        createProjectIcon = findViewById(R.id.create_project_icon);
        createProjectBtn = findViewById(R.id.create_project_btn);
        importProjectIcon = findViewById(R.id.import_project_icon);
        importProjectBtn = findViewById(R.id.import_project_btn);
        cloneFromGitIcon = findViewById(R.id.clone_from_git_icon);
        cloneFromGitBtn = findViewById(R.id.clone_from_git_btn);
        appName = findViewById(R.id.app_name);
        navBarAppName = findViewById(R.id.nav_bar_app_name);
        createFolderBtn = findViewById(R.id.create_folder_btn);
        createFileBtn = findViewById(R.id.create_file_btn);
        drawerLayout = findViewById(R.id.drawer_layout);
        
        navFileManager = findViewById(R.id.nav_file_manager);
        navProjects = findViewById(R.id.nav_projects);
        navTerminal = findViewById(R.id.nav_terminal);
        navConsole = findViewById(R.id.nav_console);
        navWorkspace = findViewById(R.id.nav_workspace);
        navLearn = findViewById(R.id.nav_learn);
        navCheatsheets = findViewById(R.id.nav_cheatsheets);
        navAppSettings = findViewById(R.id.nav_app_settings);
        navEditorSettings = findViewById(R.id.nav_editor_settings);
        navTerminalSettings = findViewById(R.id.nav_terminal_settings);
        
        iconFileManager = findViewById(R.id.icon_file_manager);
        iconProjects = findViewById(R.id.icon_projects);
        iconTerminal = findViewById(R.id.icon_terminal);
        iconConsole = findViewById(R.id.icon_console);
        iconWorkspace = findViewById(R.id.icon_workspace);
        iconLearn = findViewById(R.id.icon_learn);
        iconCheatsheets = findViewById(R.id.icon_cheatsheets);
        iconAppSettings = findViewById(R.id.icon_app_settings);
        iconEditorSettings = findViewById(R.id.icon_editor_settings);
        iconTerminalSettings = findViewById(R.id.icon_terminal_settings);
        
        // Set Typefaces
        appFontBold = Typeface.createFromAsset(getAssets(), "fonts/SpaceMono-Bold.ttf");
        appFontRegular = Typeface.createFromAsset(getAssets(), "fonts/SpaceMono-Regular.ttf");
        solidFont = Typeface.createFromAsset(getAssets(), "fonts/fa-solid-900-v5.ttf");
        brandsRegularFont = Typeface.createFromAsset(getAssets(), "fonts/fa-brands-regular-400-v5.ttf");
        
        appName.setTypeface(appFontBold);
        haveAProject.setTypeface(appFontBold);
        navBarAppName.setTypeface(appFontBold);
        navBarSectionOne.setTypeface(appFontBold);
        navBarSectionTwo.setTypeface(appFontBold);
        navBarSectionThree.setTypeface(appFontBold);
        navBarSectionFour.setTypeface(appFontBold);
        sideBarBtn.setTypeface(solidFont);
        sideBarBtn.setText("\uf0c9");
        createFolderBtn.setTypeface(solidFont);
        createFolderBtn.setText("\uf65e");
        createFileBtn.setTypeface(solidFont);
        createFileBtn.setText("\uf067");
        fabAdd.setTypeface(solidFont);
        fabAdd.setText("\uf067");
        createProjectBtn.setTypeface(appFontRegular);
        importProjectBtn.setTypeface(appFontRegular);
        cloneFromGitBtn.setTypeface(appFontRegular);
        createProjectIcon.setTypeface(solidFont);
        importProjectIcon.setTypeface(solidFont);
        cloneFromGitIcon.setTypeface(brandsRegularFont);
        createProjectIcon.setText("\uf0fe");
        importProjectIcon.setText("\uf019");
        cloneFromGitIcon.setText("\uf09b");
        
        // Set Text Fonts
        navFileManager.setTypeface(appFontRegular);
        navProjects.setTypeface(appFontRegular);
        navTerminal.setTypeface(appFontRegular);
        navConsole.setTypeface(appFontRegular);
        navWorkspace.setTypeface(appFontRegular);
        navLearn.setTypeface(appFontRegular);
        navCheatsheets.setTypeface(appFontRegular);
        navAppSettings.setTypeface(appFontRegular);
        navEditorSettings.setTypeface(appFontRegular);
        navTerminalSettings.setTypeface(appFontRegular);
        
        // Set Icon Fonts & Unicode
        setupIcon(iconFileManager, "\uf07b");
        setupIcon(iconProjects, "\uf1b3");
        setupIcon(iconTerminal, "\uf120");
        setupIcon(iconConsole, "\uf120");
        setupIcon(iconWorkspace, "\uf121");
        setupIcon(iconLearn, "\uf19d");
        setupIcon(iconCheatsheets, "\uf46d");
        setupIcon(iconAppSettings, "\uf013");
        setupIcon(iconEditorSettings, "\uf121");
        setupIcon(iconTerminalSettings, "\uf013");

        fileListView = findViewById(R.id.file_list);
        fileList = new ArrayList<>();
        adapter = new FileListAdapter(this, fileList);
        fileListView.setAdapter(adapter);
        
        fabAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startUpLayout.setVisibility(View.VISIBLE);
            }
        });
        
        popupDimView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startUpLayout.setVisibility(View.GONE);
            }
        });
        
        sideBarBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sideBarBtn.getText().toString().equals("\uf0c9")) {
                    startUpLayout.setVisibility(View.GONE);
                    drawerLayout.openDrawer(Gravity.LEFT);
                } else {
                    loadDirectory(currentDirectory.getParentFile());
                }
            }
        });
        
        navFileManager.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(Gravity.LEFT);
                currentDirectory = Environment.getExternalStorageDirectory();
                loadDirectory(currentDirectory);
            }
        });
        
        navProjects.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(Gravity.LEFT);
                File projectsDir = new File(Environment.getExternalStorageDirectory(), "Projects");
                if (!projectsDir.exists()) {
                    projectsDir.mkdirs();
                }
                currentDirectory = projectsDir;
                loadDirectory(currentDirectory);
            }
        });
        
        navTerminal.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(Gravity.LEFT);
                startActivity(new android.content.Intent(MainActivity.this, TerminalActivity.class));
            }
        });

        createFolderBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    return;
                }
                
                if (currentDirectory == null) {
                    Toast.makeText(MainActivity.this, "Load a folder first!", Toast.LENGTH_SHORT).show();
                    return;
                }
                showCreateFolderDialog();
            }
        });
        
        createFileBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    return;
                }
                
                if (currentDirectory == null) {
                    Toast.makeText(MainActivity.this, "Load a folder first!", Toast.LENGTH_SHORT).show();
                    return;
                }
                showCreateFileDialog();
            }
        });

        fileListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    return;
                }
                
                FileItem item = fileList.get(position);
                if (item.isDir) {
                    loadDirectory(item.file);
                } else {
                    String filePath = item.file.getAbsolutePath();
                    String ext = item.ext.toLowerCase();
                    
                    boolean textFiles = ext.equals("py") || ext.equals("java") || ext.equals("xml") || ext.equals("txt") || ext.equals("md") || ext.equals("js") || ext.equals("json") || ext.equals("html") || ext.equals("css");
                    
                    if (textFiles) {
                        android.content.SharedPreferences prefs = getSharedPreferences("DebugMatePrefs", MODE_PRIVATE);
                        String recentStr = prefs.getString("recent_files", "");
                        ArrayList<String> recentList = new ArrayList<>();
                        
                        if (!recentStr.isEmpty()) {
                            String[] arr = recentStr.split("\n");
                            for (String s : arr) recentList.add(s);
                        }
                        
                        while (recentList.contains(filePath)) recentList.remove(filePath);
                        recentList.add(0, filePath);
                        while (recentList.size() > 20) recentList.remove(recentList.size() - 1);
                        
                        StringBuilder sb = new StringBuilder();
                        for (String s : recentList) sb.append(s).append("\n");
                        prefs.edit().putString("recent_files", sb.toString().trim()).apply();
                    
                        android.content.Intent intent = new android.content.Intent(MainActivity.this, EditorActivity.class);
                        intent.putExtra("file_path", filePath);
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "Feature not available yet.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        
        fileListView.setOnItemLongClickListener(new android.widget.AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    return true;
                }
                
                final FileItem item = fileList.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Options for " + item.name);
                builder.setItems(new CharSequence[]{"Rename", "Delete"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) showRenameDialog(item);
                        else if (which == 1) showDeleteDialog(item);
                    }
                });
                builder.show();
                return true; 
            }
        });

        // PERMISSIONS
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        } else {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
        
        // Show welcome screen on launch
        showWelcomeScreen();
    }

    private void setupIcon(TextView textView, String unicode) {
        textView.setTypeface(solidFont);
        textView.setText(unicode);
    }

    private void showWelcomeScreen() {
        welcomeMessage.setVisibility(View.VISIBLE);
        fileListView.setVisibility(View.GONE);
        appName.setText("DebugMate");
        sideBarBtn.setText("\uf0c9");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (currentDirectory != null) loadDirectory(currentDirectory);
        }
    }

    private void loadDirectory(File dir) {
        if (dir == null) {
            showWelcomeScreen();
            return;
        }
        
        currentDirectory = dir;
        File[] files = dir.listFiles();

        if (files != null) {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    if (f1.isDirectory() && !f2.isDirectory()) return -1;
                    if (!f1.isDirectory() && f2.isDirectory()) return 1;
                    return f1.getName().compareToIgnoreCase(f2.getName());
                }
            });

            fileList.clear();
            for (File file : files) fileList.add(new FileItem(file));
            adapter.notifyDataSetChanged();
            
            welcomeMessage.setVisibility(View.GONE);
            fileListView.setVisibility(View.VISIBLE);
            
            if (currentDirectory.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                sideBarBtn.setText("\uf0c9");
                appName.setText("DebugMate");
            } else {
                appName.setText(dir.getName());
                sideBarBtn.setText("\uf060");
            }
        } else {
             Toast.makeText(this, "Cannot read directory: " + dir.getName(), Toast.LENGTH_LONG).show();
        }
    }
    
    // DIALOG HELPERS
    private void showCreateFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("New Folder");
        final EditText input = new EditText(MainActivity.this);
        input.setHint("e.g., Folder");
        input.setPadding(40, 40, 40, 40); 
        builder.setView(input);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String folderName = input.getText().toString().trim();
                if (folderName.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Folder name cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                File newFolder = new File(currentDirectory, folderName);
                if (newFolder.mkdir()) {
                    Toast.makeText(MainActivity.this, "Created: " + folderName, Toast.LENGTH_SHORT).show();
                    loadDirectory(currentDirectory); 
                } else {
                    Toast.makeText(MainActivity.this, "Folder already exists!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void showCreateFileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("New File");
        final EditText input = new EditText(MainActivity.this);
        input.setHint("e.g., main.py");
        input.setPadding(40, 40, 40, 40); 
        builder.setView(input);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String fileName = input.getText().toString().trim();
                if (fileName.isEmpty()) {
                    Toast.makeText(MainActivity.this, "File name cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                File newFile = new File(currentDirectory, fileName);
                try {
                    if (newFile.createNewFile()) {
                        Toast.makeText(MainActivity.this, "Created: " + fileName, Toast.LENGTH_SHORT).show();
                        loadDirectory(currentDirectory); 
                    } else {
                        Toast.makeText(MainActivity.this, "File already exists!", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "Error creating file: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private boolean deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children == null) return false;
            for (File child : children) {
                if (!deleteRecursive(child)) return false;
            }
        }
        return file.delete();
    }
    
    private void showRenameDialog(final FileItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Rename");
        final EditText input = new EditText(MainActivity.this);
        input.setText(item.name);
        input.setSelection(input.getText().length());
        input.setPadding(40, 40, 40, 40); 
        builder.setView(input);
        builder.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString().trim();
                if (newName.isEmpty()) { Toast.makeText(MainActivity.this, "Name cannot be empty.", Toast.LENGTH_SHORT).show(); return; }
                if (newName.equals(item.name)) return;
                File newFile = new File(currentDirectory, newName);
                if (item.file.renameTo(newFile)) {
                    Toast.makeText(MainActivity.this, "Renamed successfully!", Toast.LENGTH_SHORT).show();
                    loadDirectory(currentDirectory); 
                } else {
                    Toast.makeText(MainActivity.this, "Rename failed. File might already exist.", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void showDeleteDialog(final FileItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Delete " + item.name + "?");
        builder.setMessage("Are you sure you want to delete this?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (deleteRecursive(item.file)) {
                    Toast.makeText(MainActivity.this, "Deleted successfully!", Toast.LENGTH_SHORT).show();
                    loadDirectory(currentDirectory);
                } else {
                    Toast.makeText(MainActivity.this, "Failed to delete.", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
            return;
        }

        if (currentDirectory != null) {
            // If we are in Projects root, go to Welcome Screen
            if (currentDirectory.getAbsolutePath().equals(new File(Environment.getExternalStorageDirectory(), "Projects").getAbsolutePath())) {
                currentDirectory = null;
                showWelcomeScreen();
            } 
            // If we are in Phone Storage root, go to Welcome Screen
            else if (currentDirectory.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                currentDirectory = null;
                showWelcomeScreen();
            } 
            // Otherwise, go up one folder
            else {
                loadDirectory(currentDirectory.getParentFile());
            }
        } else {
            // Double tap to exit from Welcome Screen
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                super.onBackPressed();
            } else {
                Toast.makeText(MainActivity.this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            }
            backPressedTime = System.currentTimeMillis();
        }
    }
}