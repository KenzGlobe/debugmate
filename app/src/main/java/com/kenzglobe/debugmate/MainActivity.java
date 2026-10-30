package com.kenzglobe.debugmate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.net.Uri;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.view.View;
import android.view.Gravity;
import android.view.View.OnClickListener;
import android.support.v4.widget.DrawerLayout;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.Date;
import java.util.Locale;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.text.SimpleDateFormat;

public class MainActivity extends Activity {

    private static final int REQUEST_SAF_DIRECTORY = 1001;
    private boolean isPickerMode = false;
    private String pendingTemplateType = "";
    private String pendingTemplateName = "";
    private TextView btnSelectFolder;
    private ListView fileListView; 
    private View popupDimView;
    private ArrayList<FileItem> fileList;
    private FileListAdapter adapter;
    private boolean isSelectionMode = false;
    private DrawerLayout drawerLayout;
    private File currentDirectory = null;
    private long backPressedTime = 0;
    
    private FrameLayout startUpLayout,
        tasksForFiles,
        moreFileTasks;
        
    private LinearLayout filesCopyBtnLayout,
        filesShareBtnLayout,
        filesCutBtnLayout,
        filesDeleteBtnLayout,
        filesMoreBtnLayout;
        
    private LinearLayout navItemFileManager,
        navItemAppFiles,
        navItemAppCache,
        navItemSAF,
        navItemProjects,
        navItemTerminal,
        navItemConsole,
        navItemWorkspace;
    private LinearLayout navItemLearn,
        navItemCheatsheets,
        navItemAppSettings,
        navItemEditorSettings,
        navItemTerminalSettings;
        
    private LinearLayout storage,
        shell,
        learn,
        settings;
        
    private TextView filesCopyBtn,
        filesShareBtn,
        filesCutBtn,
        filesDeleteBtn,
        filesMoreBtn;
        
    private TextView filesCopyText,
        filesShareText,
        filesCutText,
        filesDeleteText,
        filesMoreText;
        
    private TextView filesAddAsProjectBtn,
        filesRenameBtn,
        filesOpenWithBtn,
        filesCompressBtn,
        filesUncompressBtn,
        filesDetailsBtn;
    
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
        iconAppFiles,
        iconAppCache,
        iconSAF,
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
        
        btnSelectFolder = findViewById(R.id.btn_select_folder);
        
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
        
        storage = findViewById(R.id.storage);
        shell = findViewById(R.id.shell);
        learn = findViewById(R.id.learn);
        settings = findViewById(R.id.settings);
        
        isPickerMode = getIntent().getBooleanExtra("PICK_FOLDER_MODE", false);
        pendingTemplateType = getIntent().getStringExtra("TEMPLATE_TYPE");
        pendingTemplateName = getIntent().getStringExtra("TEMPLATE_NAME");

        if (isPickerMode) {
            appName.setText("Select Destination Folder");
            fabAdd.setVisibility(View.GONE);
            btnSelectFolder.setVisibility(View.VISIBLE);
    
            btnSelectFolder.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentDirectory != null) {
                        android.content.Intent returnIntent = new android.content.Intent();
                        returnIntent.putExtra("SELECTED_PATH", currentDirectory.getAbsolutePath());
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    }
                }
            });
        }
        
        String startDir = getIntent().getStringExtra("START_DIRECTORY");
        if (startDir != null) {
            currentDirectory = new File(startDir);
        }
        
        navItemFileManager = findViewById(R.id.nav_item_file_manager);
        navItemAppFiles = findViewById(R.id.nav_item_app_files);
        navItemAppCache = findViewById(R.id.nav_item_app_cache);
        navItemSAF = findViewById(R.id.nav_item_saf);
        navItemWorkspace = findViewById(R.id.nav_item_workspace);
        navItemProjects = findViewById(R.id.nav_item_projects);
        navItemTerminal = findViewById(R.id.nav_item_terminal);
        navItemConsole = findViewById(R.id.nav_item_console);
        
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
        iconAppFiles = findViewById(R.id.icon_app_files);
        iconAppCache = findViewById(R.id.icon_app_cache);
        iconSAF = findViewById(R.id.icon_saf);
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
        createFolderBtn.setTypeface(solidFont);
        createFileBtn.setTypeface(solidFont);
        fabAdd.setTypeface(solidFont);
        createProjectBtn.setTypeface(appFontRegular);
        importProjectBtn.setTypeface(appFontRegular);
        cloneFromGitBtn.setTypeface(appFontRegular);
        createProjectIcon.setTypeface(solidFont);
        importProjectIcon.setTypeface(solidFont);
        cloneFromGitIcon.setTypeface(brandsRegularFont);
        
        tasksForFiles = findViewById(R.id.tasks_for_files);
        filesShareBtnLayout = findViewById(R.id.files_share_btn_layout);
        filesCopyBtnLayout = findViewById(R.id.files_copy_btn_layout);
        filesCutBtnLayout = findViewById(R.id.files_cut_btn_layout);
        filesDeleteBtnLayout = findViewById(R.id.files_delete_btn_layout);
        filesMoreBtnLayout = findViewById(R.id.files_more_btn_layout);
        filesShareBtn = findViewById(R.id.files_share_btn);
        filesCopyBtn = findViewById(R.id.files_copy_btn);
        filesCutBtn = findViewById(R.id.files_cut_btn);
        filesDeleteBtn = findViewById(R.id.files_delete_btn);
        filesMoreBtn = findViewById(R.id.files_more_btn);
        filesShareText = findViewById(R.id.files_share_text);
        filesCopyText = findViewById(R.id.files_copy_text);
        filesCutText = findViewById(R.id.files_cut_text);
        filesDeleteText = findViewById(R.id.files_delete_text);
        filesMoreText = findViewById(R.id.files_more_text);
        moreFileTasks = findViewById(R.id.more_file_tasks);
        filesAddAsProjectBtn = findViewById(R.id.files_add_as_project_btn);
        filesRenameBtn = findViewById(R.id.files_rename_btn);
        filesOpenWithBtn = findViewById(R.id.files_open_with_btn);
        filesCompressBtn = findViewById(R.id.files_compress_btn);
        filesUncompressBtn = findViewById(R.id.files_uncompress_btn);
        filesDetailsBtn = findViewById(R.id.files_details_btn);
        
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
        setupIcon(iconFileManager, "\uf233", solidFont);
        setupIcon(iconProjects, "\uf1b3", solidFont);
        setupIcon(iconTerminal, "\uf120", solidFont);
        setupIcon(iconConsole, "\uf120", solidFont);
        setupIcon(iconWorkspace, "\uf121", solidFont);
        setupIcon(iconLearn, "\uf19d", solidFont);
        setupIcon(iconCheatsheets, "\uf46d", solidFont);
        setupIcon(iconAppSettings, "\uf013", solidFont);
        setupIcon(iconEditorSettings, "\uf121", solidFont);
        setupIcon(iconTerminalSettings, "\uf013", solidFont);
        setupIcon(iconAppFiles, "\uf07b", solidFont);
        setupIcon(iconAppCache, "\uf07b", solidFont);
        setupIcon(iconSAF, "\uf0a0", solidFont);

        setupIcon(filesShareBtn, "\uf064", solidFont);
        setupIcon(filesCopyBtn, "\uf24d", solidFont);
        setupIcon(filesCutBtn, "\uf0c4", solidFont);
        setupIcon(filesDeleteBtn, "\uf2ed", solidFont);
        setupIcon(filesMoreBtn, "\uf142", solidFont);

        setupIcon(createProjectIcon, "\uf0fe", solidFont);
        setupIcon(importProjectIcon, "\uf019", solidFont);
        setupIcon(cloneFromGitIcon, "\uf09b", brandsRegularFont); 

        setupIcon(sideBarBtn, "\uf0c9", solidFont);
        setupIcon(createFolderBtn, "\uf65e", solidFont);
        setupIcon(createFileBtn, "\uf067", solidFont);
        setupIcon(fabAdd, "\uf067", solidFont);

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
        
        createProjectBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startUpLayout.setVisibility(View.GONE);
                startActivity(new Intent(MainActivity.this, TemplateGalleryActivity.class));
            }
        });
        
        sideBarBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sideBarBtn.getText().toString().equals("\uf0c9")) {
                    startUpLayout.setVisibility(View.GONE);
                    drawerLayout.openDrawer(Gravity.LEFT);
                } else if (sideBarBtn.getText().toString().equals("\uf00d")) {
                    exitSelectionMode();
                } else {
                    parentFolderNavigation(currentDirectory);
                }
            }
        });
        
        navItemFileManager.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(Gravity.LEFT);
                currentDirectory = Environment.getExternalStorageDirectory();
                loadDirectory(currentDirectory);
            }
        });
        
        navItemAppFiles.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(Gravity.LEFT);
                File appFilesDir = getExternalFilesDir(null);
                if (appFilesDir != null) {
                    if (!appFilesDir.exists()) appFilesDir.mkdirs();
                    currentDirectory = appFilesDir;
                    loadDirectory(currentDirectory);
                } else {
                    Toast.makeText(MainActivity.this, "App Files not available.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        navItemAppCache.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(Gravity.LEFT);
                File cacheDir = getCacheDir();
                if (cacheDir != null) {
                    currentDirectory = cacheDir;
                    loadDirectory(currentDirectory);
                }
            }
        });
        
        navItemSAF.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(Gravity.LEFT);
                
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Toast.makeText(MainActivity.this, "Storage picker requires Android 5.0 or later.", Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(
                        Intent.ACTION_OPEN_DOCUMENT_TREE
                );

                startActivityForResult(intent, REQUEST_SAF_DIRECTORY);
            }
        });
        
        navItemWorkspace.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(Gravity.LEFT);
                android.content.Intent intent = new android.content.Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        
        navItemProjects.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(Gravity.LEFT);
                startActivity(new Intent(MainActivity.this, ProjectListActivity.class));
            }
        });
        
        navItemConsole.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(Gravity.LEFT);
                startActivity(new android.content.Intent(MainActivity.this, ConsoleActivity.class));
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

        filesShareBtnLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Uri> uris = new ArrayList<>();
                for (FileItem item : fileList) {
                    if (item.isSelected && !item.isDir) {
                        // Create a content:// URI using FileProvider
                        Uri uri = android.support.v4.content.FileProvider.getUriForFile(
                                MainActivity.this,
                                getPackageName() + ".fileprovider",
                                item.file
                        );
                        uris.add(uri);
                    }
                }

                if (uris.isEmpty()) return;

                Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                intent.setType("*/*");
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, "Share files via"));
            }
        });
    
        filesDeleteBtnLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Count selected items
                int selectedCount = 0;
                for (FileItem item : fileList) {
                    if (item.isSelected) selectedCount++;
                }

                if (selectedCount == 0) return;

                // 2. Show Confirmation Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Delete " + selectedCount + (selectedCount == 1 ? " item?" : " items?"));
                builder.setMessage("Are you sure you want to delete the selected items? This action cannot be undone.");
        
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int deletedSuccessfully = 0;
                        int failedToDelete = 0;

                        // 3. Loop through the list and delete selected items
                        for (FileItem item : fileList) {
                            if (item.isSelected) {
                                if (deleteRecursive(item.file)) {
                                    deletedSuccessfully++;
                                } else {
                                    failedToDelete++;
                                }
                            }
                        }

                        // 4. Show the result
                        if (failedToDelete == 0) {
                            Toast.makeText(MainActivity.this, "Successfully deleted " + deletedSuccessfully + " item(s).", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Deleted " + deletedSuccessfully + ", but failed to delete " + failedToDelete + " item(s).", Toast.LENGTH_LONG).show();
                        }

                        // 5. Exit selection mode and refresh the directory
                        exitSelectionMode();
                        loadDirectory(currentDirectory);
                    }
                });
        
                builder.setNegativeButton("Cancel", null);
                builder.show();
            }
        });
        
        filesMoreBtnLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                moreFileTasks.setVisibility(View.VISIBLE);
            }
        });
        
        moreFileTasks.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                moreFileTasks.setVisibility(View.GONE);
            }
        });
        
        filesAddAsProjectBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FileItem selectedItem = null;
                for (FileItem item : fileList) {
                    if (item.isSelected) {
                        selectedItem = item;
                        break;
                    }
                }
        
                if (selectedItem == null) return;

                // Only allow folders to be added as projects
                if (!selectedItem.isDir) {
                    Toast.makeText(MainActivity.this, "Please select a folder, not a file.", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                showAddAsProjectDialog(selectedItem);
            }
        });
        
        filesRenameBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FileItem selectedItem = null;
                for (FileItem item : fileList) {
                    if (item.isSelected) {
                        selectedItem = item;
                        break;
                    }
                }
        
                if (selectedItem != null) {
                    showRenameDialog(selectedItem);
                }
            }
        });
        
        filesOpenWithBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FileItem selectedItem = null;
        
                for (FileItem item : fileList) {
                    if (item.isSelected) {
                        selectedItem = item;
                        break;
                    }
                }
        
                if (selectedItem == null || selectedItem.isDir) {
                    Toast.makeText(MainActivity.this, "Please select a file.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get the MIME type (e.g., text/plain, image/png, application/pdf)
                String ext = selectedItem.ext.toLowerCase();
                // String mimeType = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
                String mimeType = java.net.URLConnection.guessContentTypeFromName(selectedItem.name);

                if (mimeType == null) {
                    mimeType = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
                }

                // If it STILL doesn't know, just use */*
                if (mimeType == null) {
                    mimeType = "*/*"; 
                }

                try {
                    // Get the secure content:// URI
                    Uri uri = android.support.v4.content.FileProvider.getUriForFile(
                            MainActivity.this,
                            getPackageName() + ".fileprovider",
                            selectedItem.file
                    );

                    // Create the Intent to view the file
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, mimeType);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    startActivity(Intent.createChooser(intent, "Open with"));
                    exitSelectionMode();

                } catch (android.content.ActivityNotFoundException e) {
                    Toast.makeText(MainActivity.this, "No app found to open this file type (" + ext + ")", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Error opening file: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        
        filesCompressBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                moreFileTasks.setVisibility(View.GONE);
                compressSelectedFiles();
            }
        });
        
        filesUncompressBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                moreFileTasks.setVisibility(View.GONE);
                uncompressSelectedFile();
            }
        });
        
        filesDetailsBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                moreFileTasks.setVisibility(View.GONE);
        
                FileItem selectedItem = null;
                for (FileItem item : fileList) {
                    if (item.isSelected) {
                        selectedItem = item;
                        break;
                    }
                }
                
                if (selectedItem != null) {
                    showDetailsDialog(selectedItem);
                }
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
                
                if (isSelectionMode) {
                    item.isSelected = !item.isSelected;
                    adapter.notifyDataSetChanged();
                    updateSelectionCount();
                    return;
                }
                
                if (item.isDir) {
                    loadDirectory(item.file);
                } else {
                    String filePath = item.file.getAbsolutePath();
                    String ext = item.ext.toLowerCase();
                    
                    boolean textFiles = ext.equals("py") ||
                        ext.equals("java") ||
                        ext.equals("xml") ||
                        ext.equals("txt") ||
                        ext.equals("md") ||
                        ext.equals("js") ||
                        ext.equals("json") ||
                        ext.equals("html") ||
                        ext.equals("css") ||
                        ext.equals("gitignore") ||
                        ext.equals("gradle") ||
                        ext.equals("pro") ||
                        ext.equals("svg");
                    
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
            
                if (!isSelectionMode) {
                    isSelectionMode = true;
                    adapter.setSelectionMode(true);
                    sideBarBtn.setText("\uf00d");
                    tasksForFiles.setVisibility(View.VISIBLE);
                    fabAdd.setVisibility(View.GONE);
                }
        
                FileItem item = fileList.get(position);
                item.isSelected = !item.isSelected;
                adapter.notifyDataSetChanged();
                updateSelectionCount();
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
        
        if (!isPickerMode) {
            if (currentDirectory != null) {
                loadDirectory(currentDirectory);
            } else {
                showWelcomeScreen();
            }
        } else {
            storage.setVisibility(View.VISIBLE);
            shell.setVisibility(View.GONE);
            learn.setVisibility(View.GONE);
            settings.setVisibility(View.GONE);
            currentDirectory = Environment.getExternalStorageDirectory();
            loadDirectory(currentDirectory);
        }
    }
    
    private void parentFolderNavigation(File dir) {
        if (dir == null) return;
        File parentDir = dir.getParentFile();

        if (parentDir == null) {
            currentDirectory = null;
            showWelcomeScreen();
            return;
        }

        String path = dir.getAbsolutePath();
        String parent = parentDir.getAbsolutePath();
        
        if (parent.equals("/data/user/0")) {
            if (isPickerMode) {
                loadDirectory(Environment.getExternalStorageDirectory());
            } else {
                currentDirectory = null;
                showWelcomeScreen();
            }
        } else if (path.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
            currentDirectory = null;
            showWelcomeScreen();
        } else if (parent.equals("/storage/emulated/0/Android/data")) {
            loadDirectory(Environment.getExternalStorageDirectory());
            Toast.makeText(MainActivity.this, "Can't read data. Redirected to root folder.", Toast.LENGTH_LONG).show();
        } else {
            loadDirectory(dir.getParentFile());
        }
    }
    
    private void setupIcon(TextView textView, String unicode, Typeface font) {
        textView.setTypeface(font);
        textView.setText(unicode);
    }

    private void showWelcomeScreen() {
        welcomeMessage.setVisibility(View.VISIBLE);
        fileListView.setVisibility(View.GONE);
        if (!isPickerMode) {
            appName.setText("DebugMate");
            sideBarBtn.setText("\uf0c9");
        }
    }
    
    private void showAddAsProjectDialog(final FileItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Project Type");
    
        // The available project types
        final String[] types = {"web", "python", "java", "android", "custom"};
    
        builder.setItems(types, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedType = types[which];
            
                // Create the ProjectItem using the folder's name and path
                ProjectItem newProject = new ProjectItem(
                        item.name, 
                        selectedType, 
                        item.file.getAbsolutePath(), 
                        "Custom"
                );
            
                // Save it to the JSON file
                ProjectManager.saveProject(MainActivity.this, newProject);
            
                Toast.makeText(MainActivity.this, "Added '" + item.name + "' to Projects!", Toast.LENGTH_SHORT).show();
            
                // Exit selection mode and refresh
                exitSelectionMode();
            }
        });
    
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void updateSelectionCount() {
        int count = 0;
        boolean hasFolder = false;
        String selectedExt = "";
        
        for (FileItem item : fileList) {
            if (item.isSelected) {
                count++;
                if (item.isDir) {
                    hasFolder = true;
                }
                selectedExt = item.ext.toLowerCase(); // Keep updating, will hold the last selected item's ext
            }
        }

        if (count == 0) {
            exitSelectionMode();
        } else {
            if (count == 1) {
                appName.setText(count + " item selected");
                filesRenameBtn.setClickable(true);
                filesRenameBtn.setFocusable(true);
                filesRenameBtn.setTextColor(Color.parseColor("#FFFFFF"));
                filesDetailsBtn.setClickable(true);
                filesDetailsBtn.setFocusable(true);
                filesDetailsBtn.setTextColor(Color.parseColor("#FFFFFF"));
            } else {
                appName.setText(count + " items selected");
                filesRenameBtn.setClickable(false);
                filesRenameBtn.setFocusable(false);
                filesRenameBtn.setTextColor(Color.parseColor("#888888"));
                filesDetailsBtn.setClickable(false);
                filesDetailsBtn.setFocusable(false);
                filesDetailsBtn.setTextColor(Color.parseColor("#888888"));
            }
        
            if (hasFolder) {
                filesShareBtnLayout.setAlpha(0.5f);
                filesShareBtnLayout.setClickable(false);
                filesShareBtnLayout.setFocusable(false);
                filesShareBtn.setTextColor(Color.parseColor("#888888"));
                filesShareText.setTextColor(Color.parseColor("#888888"));
            
                filesOpenWithBtn.setClickable(false);
                filesOpenWithBtn.setFocusable(false);
                filesOpenWithBtn.setTextColor(Color.parseColor("#888888"));
                
                filesUncompressBtn.setClickable(false);
                filesUncompressBtn.setFocusable(false);
                filesUncompressBtn.setTextColor(Color.parseColor("#888888"));
            } else {
                filesShareBtnLayout.setAlpha(1.0f);
                filesShareBtnLayout.setClickable(true);
                filesShareBtnLayout.setFocusable(true);
                filesShareBtn.setTextColor(Color.parseColor("#FFFFFF"));
                filesShareText.setTextColor(Color.parseColor("#FFFFFF"));
            
                if (count == 1) {
                    filesOpenWithBtn.setClickable(true);
                    filesOpenWithBtn.setFocusable(true);
                    filesOpenWithBtn.setTextColor(Color.parseColor("#FFFFFF"));
                
                    // if (selectedExt.equals("zip") || selectedExt.equals("rar") || selectedExt.equals("7z") || selectedExt.equals("tar") || selectedExt.equals("gz")) {
                    if (selectedExt.equals("zip")) {
                        filesUncompressBtn.setClickable(true);
                        filesUncompressBtn.setFocusable(true);
                        filesUncompressBtn.setTextColor(Color.parseColor("#FFFFFF"));
                    } else {
                        filesUncompressBtn.setClickable(false);
                        filesUncompressBtn.setFocusable(false);
                        filesUncompressBtn.setTextColor(Color.parseColor("#888888"));
                    }
                } else {
                    filesOpenWithBtn.setClickable(false);
                    filesOpenWithBtn.setFocusable(false);
                    filesOpenWithBtn.setTextColor(Color.parseColor("#888888"));
                
                    filesUncompressBtn.setClickable(false);
                    filesUncompressBtn.setFocusable(false);
                    filesUncompressBtn.setTextColor(Color.parseColor("#888888"));
                }
            }
        }
    }
    
    private void exitSelectionMode() {
        isSelectionMode = false;
        adapter.setSelectionMode(false);
        tasksForFiles.setVisibility(View.GONE);
        moreFileTasks.setVisibility(View.GONE);
        fabAdd.setVisibility(View.VISIBLE);
    
        // Clear all selections
        for (FileItem item : fileList) {
            item.isSelected = false;
        }
        adapter.notifyDataSetChanged();
    
        // Restore the App Name and Sidebar button
        if (currentDirectory == null) {
            appName.setText("DebugMate");
            sideBarBtn.setText("\uf0c9");
        } else if (currentDirectory.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
            appName.setText("DebugMate");
            sideBarBtn.setText("\uf0c9");
        } else {
            appName.setText(currentDirectory.getName());
            sideBarBtn.setText("\uf060");
        }
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
            
            if (isPickerMode) {
                // Keep the picker title, but update the sidebar icon
                if (currentDirectory.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                    sideBarBtn.setText("\uf0c9");
                } else {
                    sideBarBtn.setText("\uf060");
                }
                // Show current folder name in the picker title
                appName.setText("Select: " + dir.getName());
    
            } else {
                // Normal Mode
                if (currentDirectory.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                    sideBarBtn.setText("\uf0c9");
                    appName.setText("DebugMate");
                } else {
                    appName.setText(dir.getName());
                    sideBarBtn.setText("\uf060");
                }
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
                    exitSelectionMode();
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
    
    private void compressSelectedFiles() {
        final ArrayList<FileItem> selectedFiles = new ArrayList<>();
        for (FileItem item : fileList) {
            if (item.isSelected) selectedFiles.add(item);
        }

        if (selectedFiles.isEmpty()) return;
    
        // Create a unique zip name (e.g., archive.zip, archive(1).zip)
        File zipFile = new File(currentDirectory, "archive.zip");
        int count = 1;
        while (zipFile.exists()) {
            zipFile = new File(currentDirectory, "archive(" + count + ").zip");
            count++;
        }
        final File finalZipFile = zipFile;

        Toast.makeText(this, "Compressing... Please wait.", Toast.LENGTH_LONG).show();

        // Run in background thread to prevent UI freeze
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileOutputStream fos = new FileOutputStream(finalZipFile);
                    ZipOutputStream zos = new ZipOutputStream(fos);

                    for (FileItem item : selectedFiles) {
                        zipFile(item.file, item.name, zos);
                    }

                    zos.close();
                    fos.close();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Compressed successfully to " + finalZipFile.getName(), Toast.LENGTH_SHORT).show();
                            exitSelectionMode();
                            loadDirectory(currentDirectory);
                        }
                    });

                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Compression failed!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws Exception {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
            }
            zipOut.closeEntry();
            File[] children = fileToZip.listFiles();
            if (children != null) {
                for (File childFile : children) {
                    zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
                }
            }
            return;
        }
    
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
    
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
    
        fis.close();
    }
    
    private void uncompressSelectedFile() {
        FileItem selectedItem = null;
        for (FileItem item : fileList) {
            if (item.isSelected) {
                selectedItem = item;
                break;
            }
        }

        if (selectedItem == null || selectedItem.isDir) return;

        final File zipFile = selectedItem.file;
    
        if (!zipFile.getName().toLowerCase().endsWith(".zip")) {
            Toast.makeText(this, "Only .zip files are supported for extraction.", Toast.LENGTH_LONG).show();
            return;
        }
    
        Toast.makeText(this, "Uncompressing... Please wait.", Toast.LENGTH_LONG).show();

        // Run in background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    unzipFile(zipFile, currentDirectory);
                
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Uncompressed successfully!", Toast.LENGTH_SHORT).show();
                            exitSelectionMode();
                            loadDirectory(currentDirectory);
                        }
                    });

                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Failed to uncompress: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }
    
    private void unzipFile(File zipFile, File targetDirectory) throws Exception {
        InputStream is = new FileInputStream(zipFile);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
        ZipEntry ze;
        byte[] buffer = new byte[1024];
        int count;

        while ((ze = zis.getNextEntry()) != null) {
            File file = new File(targetDirectory, ze.getName());
            File dir = ze.isDirectory() ? file : file.getParentFile();
        
            if (!dir.isDirectory() && !dir.mkdirs()) {
                throw new Exception("Failed to ensure directory: " + dir.getAbsolutePath());
            }
        
            if (ze.isDirectory()) {
                continue;
            }
        
            FileOutputStream fout = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fout, 1024);
        
            while ((count = zis.read(buffer)) != -1) {
                bos.write(buffer, 0, count);
            }
        
            bos.flush();
            bos.close();
            fout.close();
        }
        zis.close();
        is.close();
    }
    
    private void showDetailsDialog(FileItem item) {
        // 1. Format the Last Modified Date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        String dateString = sdf.format(new Date(item.file.lastModified()));

        // 2. Calculate Size (If it's a folder, we need to calculate the total size recursively)
        long sizeInBytes = item.isDir ? getFolderSize(item.file) : item.file.length();
        String sizeString = formatSize(sizeInBytes);

        // 3. Format Type
        String typeString = item.isDir ? "Folder" : item.ext.toUpperCase() + " File";

        // 4. Build the message
        String details = 
                "Name:\n" + item.name + "\n\n" +
                "Type:\n" + typeString + "\n\n" +
                "Size:\n" + sizeString + "\n\n" +
                "Last Modified:\n" + dateString + "\n\n" +
                "Path:\n" + item.file.getAbsolutePath();

        // 5. Show the Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("File Details");
        builder.setMessage(details);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    // Helper to calculate folder size recursively
    private long getFolderSize(File dir) {
        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    size += file.length();
                } else {
                    size += getFolderSize(file); // Recursive call for subfolders
                }
            }
        }
        return size;
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(Locale.getDefault(), "%.1f KB", bytes / 1024f);
        if (bytes < 1024 * 1024 * 1024) return String.format(Locale.getDefault(), "%.1f MB", bytes / 1048576f);
        return String.format(Locale.getDefault(), "%.1f GB", bytes / 1073741824f);
    }
    
    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {
    
        super.onActivityResult(requestCode, resultCode, data);
    
        if (requestCode == REQUEST_SAF_DIRECTORY
                && resultCode == RESULT_OK
                && data != null) {
    
            Uri treeUri = data.getData();
    
            if (treeUri == null) {
                Toast.makeText(
                        this,
                        "No directory was selected.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }
    
            final int takeFlags = data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    
            try {
                getContentResolver().takePersistableUriPermission(
                        treeUri,
                        takeFlags
                );
    
                Toast.makeText(
                        this,
                        "Storage access granted.",
                        Toast.LENGTH_SHORT
                ).show();
    
            } catch (SecurityException e) {
                    Toast.makeText(
                        this,
                        "Could not retain storage access.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isSelectionMode) {
            exitSelectionMode();
            return;
        }
        
        if (isPickerMode) {
            if (currentDirectory == null || currentDirectory.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                // If we are at the root, cancel the picker
                setResult(RESULT_CANCELED);
                finish();
            } else {
                parentFolderNavigation(currentDirectory);
            }
            return;
        }
    
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
            return;
        }
    
        if (currentDirectory != null) {
            parentFolderNavigation(currentDirectory);
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
