package com.kenzglobe.debugmate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.net.Uri;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.view.View;
import android.view.Gravity;
import android.view.View.OnClickListener;
import android.support.v4.widget.DrawerLayout;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.List;
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
    private TextView btnSelectFolder, emptyIcon, workspaceBtn;
    private ListView fileListView; 
    private View popupDimView;
    private ArrayList<FileItem> fileList;
    private ArrayList<android.support.v4.provider.DocumentFile> safHistory = new ArrayList<>();
    private FileListAdapter adapter;
    private boolean isSelectionMode = false;
    private DrawerLayout drawerLayout;
    private File currentDirectory = null;
    private String startDirectoryPath = null;
    private String savedSafUri = null;
    private long backPressedTime = 0;
    
    // --- For Copy/Move ---
    private ArrayList<FileItem> clipboardFiles = new ArrayList<>();
    private boolean isCopyMode = false;
    private boolean isMoveMode = false;
    
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
        navItemWorkspace,
        navItemGit,
        navItemLearn,
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
    
    private LinearLayout welcomeMessage,
        emptyFolderLayout;
    
    // Sidebar Variables
    private TextView navBarSectionOne,
        navBarSectionTwo,
        navBarSectionThree,
        navBarSectionFour;
    
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
        navWorkspace,
        navGit;
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
        iconWorkspace,
        iconGit;
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
        emptyFolderLayout = findViewById(R.id.empty_folder_layout);
        emptyIcon = findViewById(R.id.empty_icon);
        workspaceBtn = findViewById(R.id.workspace_btn);
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
        createFolderBtn = findViewById(R.id.create_folder_btn);
        createFileBtn = findViewById(R.id.create_file_btn);
        drawerLayout = findViewById(R.id.drawer_layout);
        savedSafUri = getSharedPreferences("DebugMatePrefs", MODE_PRIVATE).getString("saved_saf_uri", null);
        
        storage = findViewById(R.id.storage);
        shell = findViewById(R.id.shell);
        learn = findViewById(R.id.learn);
        settings = findViewById(R.id.settings);
        
        isPickerMode = getIntent().getBooleanExtra("PICK_FOLDER_MODE", false);
        pendingTemplateType = getIntent().getStringExtra("TEMPLATE_TYPE");
        pendingTemplateName = getIntent().getStringExtra("TEMPLATE_NAME");

        if (isPickerMode) {
            if (appName != null) appName.setText("Select Destination Folder");
            if (fabAdd != null) fabAdd.setVisibility(View.GONE);
            if (btnSelectFolder != null) btnSelectFolder.setVisibility(View.VISIBLE);
    
            if (btnSelectFolder != null) {
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
        }
        
        navItemFileManager = findViewById(R.id.nav_item_file_manager);
        navItemAppFiles = findViewById(R.id.nav_item_app_files);
        navItemAppCache = findViewById(R.id.nav_item_app_cache);
        navItemSAF = findViewById(R.id.nav_item_saf);
        navItemWorkspace = findViewById(R.id.nav_item_workspace);
        navItemGit = findViewById(R.id.nav_item_git);
        navItemProjects = findViewById(R.id.nav_item_projects);
        navItemTerminal = findViewById(R.id.nav_item_terminal);
        navItemConsole = findViewById(R.id.nav_item_console);
        navItemLearn = findViewById(R.id.nav_item_learn);
        navItemCheatsheets = findViewById(R.id.nav_item_cheatsheets);
        navItemAppSettings = findViewById(R.id.nav_item_app_settings);
        navItemEditorSettings = findViewById(R.id.nav_item_editor_settings);
        navItemTerminalSettings = findViewById(R.id.nav_item_terminal_settings);
        
        navFileManager = findViewById(R.id.nav_file_manager);
        navProjects = findViewById(R.id.nav_projects);
        navTerminal = findViewById(R.id.nav_terminal);
        navConsole = findViewById(R.id.nav_console);
        navWorkspace = findViewById(R.id.nav_workspace);
        navGit = findViewById(R.id.nav_git);
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
        iconGit = findViewById(R.id.icon_git);
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
        
        if (appName != null) appName.setTypeface(appFontBold);
        if (haveAProject != null) haveAProject.setTypeface(appFontBold);
        if (navBarSectionOne != null) navBarSectionOne.setTypeface(appFontBold);
        if (navBarSectionTwo != null) navBarSectionTwo.setTypeface(appFontBold);
        if (navBarSectionThree != null) navBarSectionThree.setTypeface(appFontBold);
        if (navBarSectionFour != null) navBarSectionFour.setTypeface(appFontBold);
        if (sideBarBtn != null) sideBarBtn.setTypeface(solidFont);
        if (createFolderBtn != null) createFolderBtn.setTypeface(solidFont);
        if (createFileBtn != null) createFileBtn.setTypeface(solidFont);
        if (fabAdd != null) fabAdd.setTypeface(solidFont);
        
        if (createProjectBtn != null) createProjectBtn.setTypeface(appFontRegular);
        if (importProjectBtn != null) importProjectBtn.setTypeface(appFontRegular);
        if (cloneFromGitBtn != null) cloneFromGitBtn.setTypeface(appFontRegular);
        
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
        if (navFileManager != null) navFileManager.setTypeface(appFontRegular);
        if (navProjects != null) navProjects.setTypeface(appFontRegular);
        if (navTerminal != null) navTerminal.setTypeface(appFontRegular);
        if (navConsole != null) navConsole.setTypeface(appFontRegular);
        if (navWorkspace != null) navWorkspace.setTypeface(appFontRegular);
        if (navGit != null) navGit.setTypeface(appFontRegular);
        if (navLearn != null) navLearn.setTypeface(appFontRegular);
        if (navCheatsheets != null) navCheatsheets.setTypeface(appFontRegular);
        if (navAppSettings != null) navAppSettings.setTypeface(appFontRegular);
        if (navEditorSettings != null) navEditorSettings.setTypeface(appFontRegular);
        if (navTerminalSettings != null) navTerminalSettings.setTypeface(appFontRegular);
        
        // Set Icon Fonts & Unicode
        setupIcon(iconFileManager, "\uf233", solidFont);
        setupIcon(iconProjects, "\uf1b3", solidFont);
        setupIcon(iconTerminal, "\uf120", solidFont);
        setupIcon(iconConsole, "\uf120", solidFont);
        setupIcon(iconWorkspace, "\uf5fc", solidFont);
        setupIcon(iconGit, "\uf1d2", brandsRegularFont);
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
        setupIcon(emptyIcon, "\uf07c", solidFont);
        setupIcon(workspaceBtn, "\uf5fc", solidFont);

        fileListView = findViewById(R.id.file_list);
        fileList = new ArrayList<>();
        adapter = new FileListAdapter(this, fileList);
        fileListView.setAdapter(adapter);
        
        if (fabAdd != null) {
            fabAdd.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isCopyMode || isMoveMode) {
                        performPaste();
                    } else {
                        startUpLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
        
        if (popupDimView != null) {
            popupDimView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    startUpLayout.setVisibility(View.GONE);
                }
            });
        }
        
        if (createProjectBtn != null) {
            createProjectBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    startUpLayout.setVisibility(View.GONE);
                    startActivity(new Intent(MainActivity.this, TemplateGalleryActivity.class));
                }
            });
        }
        
        if (cloneFromGitBtn != null) {
            cloneFromGitBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                        drawerLayout.closeDrawer(Gravity.LEFT);
                        return;
                    }
                    
                    if (currentDirectory == null) {
                        AppToast.showSuccess(MainActivity.this, "Load a folder first!");
                        return;
                    }
                    startUpLayout.setVisibility(View.GONE); // Hide the popup menu
                    showCloneDialog();
                }
            });
        }
        
        if (sideBarBtn != null) {
            sideBarBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String icon = sideBarBtn.getText().toString();
                    if (icon.equals("\uf0c9")) { // Hamburger
                        startUpLayout.setVisibility(View.GONE);
                        workspaceBtn.setVisibility(View.GONE);
                        drawerLayout.openDrawer(Gravity.LEFT);
                    } else if (icon.equals("\uf00d")) { // X (Selection mode)
                        exitSelectionMode();
                        workspaceBtn.setVisibility(View.GONE);
                    } else { // Back Arrow
                        handleBackNavigation();
                    }
                }
            });
        }    
        
        if (workspaceBtn != null) {
            workspaceBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    android.content.Intent intent = new android.content.Intent(MainActivity.this, EditorActivity.class);
                    startActivity(intent);
                }
            });
        }
        
        if (navItemFileManager != null) {
            navItemFileManager.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    startDirectoryPath = null; // Clear sandbox
                    currentDirectory = Environment.getExternalStorageDirectory();
                    loadDirectory(currentDirectory);
                }
            });
        }
        
        if (navItemAppFiles != null) {
            navItemAppFiles.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    startDirectoryPath = null; // Clear sandbox
                    File appFilesDir = getExternalFilesDir(null);
                    if (appFilesDir != null) {
                        if (!appFilesDir.exists()) appFilesDir.mkdirs();
                        currentDirectory = appFilesDir;
                        loadDirectory(currentDirectory);
                    } else {
                        AppToast.showSuccess(MainActivity.this, "App Files not available.");
                    }
                }
            });
        }
        
        if (navItemAppCache != null) {
            navItemAppCache.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    startDirectoryPath = null; // Clear sandbox
                    File cacheDir = getCacheDir();
                    if (cacheDir != null) {
                        currentDirectory = cacheDir;
                        loadDirectory(currentDirectory);
                    }
                }
            });
        }
        
        if (navItemSAF != null) {
            navItemSAF.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    
                    if (savedSafUri != null) {
                        // Load the SAF directory into MainActivity
                        loadSafDirectory(Uri.parse(savedSafUri));
                    } else {
                        // Launch the picker
                        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                            AppToast.showSuccess(MainActivity.this, "Storage picker requires Android 5.0 or later.");
                            return;
                        }
                        
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        startActivityForResult(intent, REQUEST_SAF_DIRECTORY);
                    }
                }
            });
        }
        
        if (navItemWorkspace != null) {
            navItemWorkspace.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    android.content.Intent intent = new android.content.Intent(MainActivity.this, EditorActivity.class);
                    startActivity(intent);
                }
            });
        }
        
        if (navItemGit != null) {
            navItemGit.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    if (currentDirectory != null) {
                        android.content.Intent intent = new android.content.Intent(MainActivity.this, GitActivity.class);
                        intent.putExtra("project_path", currentDirectory.getAbsolutePath());
                        startActivity(intent);
                    } else {
                        AppToast.showSuccess(MainActivity.this, "Open a folder first!");
                    }
                }
            });
        }
        
        if (navItemProjects != null) {
            navItemProjects.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    startActivity(new Intent(MainActivity.this, ProjectListActivity.class));
                }
            });
        }
        
        if (navItemConsole != null) {
            navItemConsole.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    startActivity(new android.content.Intent(MainActivity.this, ConsoleActivity.class));
                }
            });
        }
        
        if (navItemTerminal != null) {
            navItemTerminal.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    startActivity(new android.content.Intent(MainActivity.this, TerminalActivity.class));
                }
            });
        }
        
        if (navItemAppSettings != null) {
            navItemAppSettings.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    android.content.Intent intent = new android.content.Intent(MainActivity.this, AppSettingsActivity.class);
                    startActivity(intent);
                }
            });
        }
        
        if (navItemEditorSettings != null) {
            navItemEditorSettings.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    android.content.Intent intent = new android.content.Intent(MainActivity.this, EditorSettingsActivity.class);
                    startActivity(intent);
                }
            });
        }
        
        if (navItemTerminalSettings != null) {
            navItemTerminalSettings.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    android.content.Intent intent = new android.content.Intent(MainActivity.this, TerminalSettingsActivity.class);
                    startActivity(intent);
                }
            });
        }
        
        if (navItemLearn != null) {
            navItemLearn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    startActivity(new Intent(MainActivity.this, LearnHubActivity.class));
                }
            });
        }

        if (navItemCheatsheets != null) {
            navItemCheatsheets.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    startActivity(new Intent(MainActivity.this, CheatSheetActivity.class));
                }
            });
        }

        if (createFolderBtn != null) {
            createFolderBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                        drawerLayout.closeDrawer(Gravity.LEFT);
                        return;
                    }
                    
                    if (currentDirectory == null) {
                        AppToast.showSuccess(MainActivity.this, "Load a folder first!");
                        return;
                    }
                    startUpLayout.setVisibility(View.GONE);
                    showCreateFolderDialog();
                }
            });
        }
        
        if (createFileBtn != null) {
            createFileBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                        drawerLayout.closeDrawer(Gravity.LEFT);
                        return;
                    }
                    
                    if (currentDirectory == null) {
                        AppToast.showSuccess(MainActivity.this, "Load a folder first!");
                        return;
                    }
                    startUpLayout.setVisibility(View.GONE);
                    showCreateFileDialog();
                }
            });
        }

        if (filesShareBtnLayout != null) {
            filesShareBtnLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<Uri> uris = new ArrayList<>();
                    
                    for (FileItem item : fileList) {
                        if (item.isSelected && !item.isDir) {
                            Uri uri;
                            
                            if (item.isSaf) {
                                // SAF files already have a content:// URI
                                uri = item.docFile.getUri();
                            } else {
                                // Normal files need FileProvider
                                uri = android.support.v4.content.FileProvider.getUriForFile(
                                        MainActivity.this,
                                        getPackageName() + ".fileprovider",
                                        item.file
                                );
                            }
                            
                            uris.add(uri);
                        }
                    }

                    if (uris.isEmpty()) {
                        AppToast.showInfo(MainActivity.this, "Please select at least one file.");
                        return;
                    }
                    
                    // LOG THE ACTIVITIES
                    for (FileItem item : fileList) {
                        if (item.isSelected && !item.isDir && !item.isSaf) {
                            logProjectActivity(item.file.getAbsolutePath(), "share", "File shared: " + item.name, "\uf064");
                        }
                    }

                    Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    intent.setType("*/*");
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(intent, "Share files via"));
                    
                    exitSelectionMode(); // Good UX to exit selection mode after sharing
                }
            });
        }
        
        if (filesCopyBtnLayout != null) {
            filesCopyBtnLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    clipboardFiles.clear();
                    for (FileItem item : fileList) {
                        if (item.isSelected) clipboardFiles.add(item);
                    }
                    if (clipboardFiles.isEmpty()) return;
                    
                    isCopyMode = true;
                    isMoveMode = false;
                    
                    exitSelectionMode();
                    fabAdd.setVisibility(View.VISIBLE);
                    fabAdd.setTypeface(solidFont);
                    fabAdd.setText("\uf0ea"); // Paste Icon
                    
                    showPasteNavigationDialog();
                    
                    AppToast.showInfo(MainActivity.this, "Copied " + clipboardFiles.size() + " item(s). Navigate and tap Paste.");
                }
            });
        }
        
        if (filesCutBtnLayout != null) {
            filesCutBtnLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    clipboardFiles.clear();
                    for (FileItem item : fileList) {
                        if (item.isSelected) clipboardFiles.add(item);
                    }
                    if (clipboardFiles.isEmpty()) return;
                    
                    isCopyMode = false;
                    isMoveMode = true;
                    
                    exitSelectionMode();
                    fabAdd.setVisibility(View.VISIBLE);
                    fabAdd.setTypeface(solidFont);
                    fabAdd.setText("\uf0ea"); // Paste Icon
                    
                    showPasteNavigationDialog();
                    
                    AppToast.showInfo(MainActivity.this, "Cut " + clipboardFiles.size() + " item(s). Navigate and tap Paste.");
                }
            });
        }
    
        if (filesDeleteBtnLayout != null) {
            filesDeleteBtnLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int selectedCount = 0;
                    for (FileItem item : fileList) {
                        if (item.isSelected) selectedCount++;
                    }
        
                    if (selectedCount == 0) return;
        
                    String title = "Delete " + selectedCount + (selectedCount == 1 ? " item?" : " items?");
                    String message = "Are you sure you want to delete the selected items? This action cannot be undone.";
        
                    // Use your custom AppDialog
                    AppDialog.show(
                        MainActivity.this,
                        title,
                        message,
                        "DELETE",
                        "CANCEL",
                        new AppDialog.OnDialogClickListener() {
                            @Override
                            public void onClick() {
                                int deletedSuccessfully = 0;
                                int failedToDelete = 0;
        
                                for (FileItem item : fileList) {
                                    if (item.isSelected) {
                                        boolean success = false;
                                        
                                        if (item.isSaf) {
                                            // SAF Delete
                                            success = item.docFile.delete();
                                        } else {
                                            // Normal File Delete
                                            success = deleteRecursive(item.file);
                                        }
        
                                        if (success) {
                                            deletedSuccessfully++;
                                            
                                            // LOG THE ACTIVITY (Only for normal files, SAF uses URIs)
                                            if (!item.isSaf) {
                                                String typeStr = item.isDir ? "Folder" : "File";
                                                logProjectActivity(item.file.getAbsolutePath(), "delete", typeStr + " deleted: " + item.name, "\uf2ed");
                                            }
                                            
                                        } else {
                                            failedToDelete++;
                                        }
                                    }
                                }
        
                                if (failedToDelete == 0) {
                                    AppToast.showSuccess(MainActivity.this, "Successfully deleted " + deletedSuccessfully + " item(s).");
                                } else {
                                    AppToast.showSuccess(MainActivity.this, "Deleted " + deletedSuccessfully + ", but failed to delete " + failedToDelete + " item(s).");
                                }
        
                                exitSelectionMode();
                                if (currentDirectory != null) {
                                    loadDirectory(currentDirectory);
                                } else if (!safHistory.isEmpty()) {
                                    loadSafDirectory(safHistory.get(safHistory.size() - 1));
                                }
                            }
                        },
                        null
                    );
                }
            });
        }
        
        if (filesMoreBtnLayout != null) {
            filesMoreBtnLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (moreFileTasks != null) moreFileTasks.setVisibility(View.VISIBLE);
                }
            });
        }
        
        if (moreFileTasks != null) {
            moreFileTasks.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    moreFileTasks.setVisibility(View.GONE);
                }
            });
        }
        
        if (filesAddAsProjectBtn != null) {
            filesAddAsProjectBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final FileItem selectedItem;
                    FileItem tempItem = null;
                    
                    for (FileItem item : fileList) {
                        if (item.isSelected) {
                            tempItem = item;
                            break;
                        }
                    }
                    
                    if (tempItem == null) return;
                    selectedItem = tempItem;
        
                    if (!selectedItem.isDir) {
                        AppToast.showSuccess(MainActivity.this, "Please select a folder, not a file.");
                        return;
                    }
                    
                    if (isProjectFolder(selectedItem.file)) {
                        showAddAsProjectDialog(selectedItem);
                    } else {
                        AppDialog.show(
                            MainActivity.this,
                            "Not a Project Folder",
                            "This folder doesn't contain any recognizable project files or source code.\n\nAre you sure you want to add it?",
                            "ADD ANYWAY",
                            "CANCEL",
                            new AppDialog.OnDialogClickListener() {
                                @Override
                                public void onClick() {
                                    showAddAsProjectDialog(selectedItem); 
                                }
                            },
                            null
                        );
                    }
                }
            });
        }
        
        if (filesRenameBtn != null) {
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
        }
        
        if (filesOpenWithBtn != null) {
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
                        AppToast.showInfo(MainActivity.this, "Please select a file.");
                        return;
                    }
        
                    String ext = selectedItem.ext.toLowerCase();
                    String mimeType = java.net.URLConnection.guessContentTypeFromName(selectedItem.name);
        
                    if (mimeType == null) {
                        mimeType = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
                    }
                    if (mimeType == null) {
                        mimeType = "*/*"; 
                    }
        
                    try {
                        Uri uri;
                        // --- THE FIX: Check if SAF or Normal ---
                        if (selectedItem.isSaf) {
                            uri = selectedItem.docFile.getUri();
                        } else {
                            uri = android.support.v4.content.FileProvider.getUriForFile(
                                    MainActivity.this,
                                    getPackageName() + ".fileprovider",
                                    selectedItem.file
                            );
                        }
        
                        final Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, mimeType);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
                        // 1. Query Android for all apps that can handle this intent
                        final List<android.content.pm.ResolveInfo> apps = getPackageManager().queryIntentActivities(intent, 0);
        
                        if (apps.isEmpty()) {
                            AppToast.showError(MainActivity.this, "No app found to open this file type.");
                            return;
                        }
        
                        // 2. Build our custom dialog
                        final android.app.Dialog dialog = new android.app.Dialog(MainActivity.this);
                        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.dialog_open_with);
                        
                        // Apply the dark background shape to the dialog
                        if (dialog.getWindow() != null) {
                            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_custom_popup); 
                        }
                        
                        ListView listView = dialog.findViewById(R.id.open_with_list);
                        OpenWithAdapter adapter = new OpenWithAdapter(MainActivity.this, apps);
                        listView.setAdapter(adapter);
        
                        // 3. Handle clicks on the app list
                        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
                                android.content.pm.ResolveInfo info = apps.get(position);
                                
                                // Launch the specific app the user clicked
                                intent.setPackage(info.activityInfo.packageName);
                                startActivity(intent);
                                
                                dialog.dismiss();
                                exitSelectionMode();
                            }
                        });
        
                        dialog.show();
        
                    } catch (android.content.ActivityNotFoundException e) {
                        AppToast.showError(MainActivity.this, "No app found to open this file type.");
                    } catch (Exception e) {
                        AppToast.showError(MainActivity.this, "Error opening file: " + e.getMessage());
                    }
                }
            });
        }
        
        if (filesCompressBtn != null) {
            filesCompressBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    moreFileTasks.setVisibility(View.GONE);
                    compressSelectedFiles();
                }
            });
        }
        
        if (filesUncompressBtn != null) {
            filesUncompressBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    moreFileTasks.setVisibility(View.GONE);
                    uncompressSelectedFile();
                }
            });
        }
        
        if (filesDetailsBtn != null) {
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
        }

        fileListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    return;
                }
                
                FileItem item = fileList.get(position);
                
                if (isSelectionMode) {
                    // --- BLOCK PROTECTED FILES ---
                    if (item.name.equals(".debugmate_activity.json")) {
                        AppToast.showError(MainActivity.this, "This is a protected system file.");
                        return;
                    }
                    
                    item.isSelected = !item.isSelected;
                    adapter.notifyDataSetChanged();
                    updateSelectionCount();
                    return;
                }
                
                // --- BLOCK PROTECTED FILES ---
                if (item.name.equals(".debugmate_activity.json")) {
                    AppToast.showError(MainActivity.this, "This is a protected system file.");
                    return;
                }
                
                // --- SAF HANDLING ---
                if (item.isSaf) {
                    if (item.isDir) {
                        safHistory.add(item.docFile);
                        loadSafDirectory(item.docFile);
                    } else {
                        String safUriStr = item.docFile.getUri().toString();
                        
                        // Save to recent_files (Tab history)
                        android.content.SharedPreferences prefs = getSharedPreferences("DebugMatePrefs", MODE_PRIVATE);
                        String recentStr = prefs.getString("recent_files", "");
                        ArrayList<String> recentList = new ArrayList<>();
                        
                        if (!recentStr.isEmpty()) {
                            String[] arr = recentStr.split("\n");
                            for (String s : arr) recentList.add(s);
                        }
                        
                        while (recentList.contains(safUriStr)) recentList.remove(safUriStr);
                        recentList.add(0, safUriStr);
                        while (recentList.size() > 20) recentList.remove(recentList.size() - 1);
                        
                        StringBuilder sb = new StringBuilder();
                        for (String s : recentList) sb.append(s).append("\n");
                        prefs.edit().putString("recent_files", sb.toString().trim()).apply();

                        // Open Editor
                        Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                        intent.putExtra("saf_uri", safUriStr);
                        intent.putExtra("saf_name", item.name);
                        startActivity(intent);
                    }
                    return;
                }
                // --------------------
                
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
                        ext.equals("sql") ||
                        ext.equals("gitignore") ||
                        ext.equals("gradle") ||
                        ext.equals("pro") ||
                        ext.equals("svg") ||
                        ext.equals("sh") ||
                        ext.equals("");
                    
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
                        AppToast.showSuccess(MainActivity.this, "Feature not available yet.");
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
            navItemProjects.setVisibility(View.GONE);
            storage.setVisibility(View.VISIBLE);
            shell.setVisibility(View.GONE);
            learn.setVisibility(View.GONE);
            settings.setVisibility(View.GONE);
            currentDirectory = Environment.getExternalStorageDirectory();
            loadDirectory(currentDirectory);
        }
        
        // --- STARTUP BEHAVIOR LOGIC ---
        if (!isPickerMode) {
            int startupBehavior = getSharedPreferences("DebugMatePrefs", MODE_PRIVATE).getInt("startup_behavior", 0);
            
            startDirectoryPath = getIntent().getStringExtra("START_DIRECTORY");
            if (startDirectoryPath != null) {
                currentDirectory = new File(startDirectoryPath);
                return;
            }
            
            if (startupBehavior == 1) {
                // Load Last Opened Folder
                SharedPreferences prefs = getSharedPreferences("DebugMatePrefs", MODE_PRIVATE);
                String lastType = prefs.getString("last_location_type", "normal");
                String lastNormalFolder = prefs.getString("last_normal_folder", null);
                
                if (lastType.equals("saf") && savedSafUri != null) {
                    loadSafDirectory(Uri.parse(savedSafUri));
                } else if (lastType.equals("normal") && lastNormalFolder != null && new File(lastNormalFolder).exists()) {
                    currentDirectory = new File(lastNormalFolder);
                    loadDirectory(currentDirectory);
                } else {
                    showWelcomeScreen();
                }
                
            } else if (startupBehavior == 2) {
                // Open Projects Dashboard
                startActivity(new Intent(MainActivity.this, ProjectListActivity.class));
                
            } else if (startupBehavior == 3) {
                android.content.Intent intent = new android.content.Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
                
            } else {
                // 0: Welcome Screen (default behavior)
                showWelcomeScreen();
            }
        }
    }
    
    // Helper to check if a file is a recognized source code file
    private boolean isCodeFile(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.endsWith(".py") || lower.endsWith(".js") || lower.endsWith(".ts") || 
               lower.endsWith(".java") || lower.endsWith(".kt") || lower.endsWith(".html") || 
               lower.endsWith(".css") || lower.endsWith(".c") || lower.endsWith(".cpp") || 
               lower.endsWith(".cs") || lower.endsWith(".go") || lower.endsWith(".rs") || 
               lower.endsWith(".php") || lower.endsWith(".rb") || lower.endsWith(".swift") || 
               lower.endsWith(".sh");
    }
    
    // Helper to validate if a folder is a legitimate project
    private boolean isProjectFolder(File dir) {
        if (dir == null || !dir.isDirectory()) return false;
        
        // 1. Check for standard project config files / markers
        String[] configFiles = {
            "package.json", "requirements.txt", "pom.xml", "build.gradle", 
            "AndroidManifest.xml", "index.html", "Cargo.toml", "go.mod", 
            "composer.json", "Gemfile", "setup.py", "pyproject.toml", ".git"
        };
        
        for (String config : configFiles) {
            if (new File(dir, config).exists()) return true;
        }
    
        // 2. Check the root and one level deep for source code files
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile() && isCodeFile(f.getName())) {
                    return true; // Found code in root
                }
                if (f.isDirectory()) {
                    File[] subFiles = f.listFiles();
                    if (subFiles != null) {
                        for (File sf : subFiles) {
                            if (sf.isFile() && isCodeFile(sf.getName())) {
                                return true; // Found code one level deep (e.g., inside /src)
                            }
                        }
                    }
                }
            }
        }
        
        return false; // No config files or code found
    }
    
    // Helper method to log if the path belongs to a registered project
    private void logProjectActivity(String itemPath, String actionType, String message, String icon) {
        if (itemPath == null) return;
        
        ArrayList<ProjectItem> allProjects = ProjectManager.loadProjects(MainActivity.this);
        for (int i = 0; i < allProjects.size(); i++) {
            ProjectItem p = allProjects.get(i);
            if (itemPath.startsWith(p.path)) {
                RecentActivityManager.log(MainActivity.this, p.path, actionType, message, icon, false);
                break;
            }
        }
    }
    
    // Helper method specifically for Copy/Move operations to track project boundaries
    private void logPasteAction(FileItem item, String destPath, boolean isMoveMode) {
        if (item.isSaf || destPath == null) return; // Skip SAF paths for now
        
        String srcPath = item.file.getAbsolutePath();
        String action = isMoveMode ? "moved" : "copied";
        String typeStr = item.isDir ? "Folder" : "File";
        
        ArrayList<ProjectItem> allProjects = ProjectManager.loadProjects(MainActivity.this);
        String srcProjectPath = null;
        String destProjectPath = null;
        
        // 1. Find if source and destination belong to a project
        for (int i = 0; i < allProjects.size(); i++) {
            ProjectItem p = allProjects.get(i);
            if (srcPath.startsWith(p.path)) srcProjectPath = p.path;
            if (destPath.startsWith(p.path)) destProjectPath = p.path;
        }
        
        // 2. Log based on the combination
        if (srcProjectPath != null && destProjectPath != null) {
            if (srcProjectPath.equals(destProjectPath)) {
                // Same project
                RecentActivityManager.log(this, srcProjectPath, "paste", 
                    typeStr + " " + action + " within project: " + item.name, "\uf0c5", false);
            } else {
                // Cross-project (Project A to Project B)
                RecentActivityManager.log(this, srcProjectPath, "paste", 
                    typeStr + " " + action + " OUT to another project: " + item.name, "\uf0c5", false);
                RecentActivityManager.log(this, destProjectPath, "paste", 
                    typeStr + " " + action + " IN from another project: " + item.name, "\uf0c5", false);
            }
        } else if (srcProjectPath != null && destProjectPath == null) {
            // Outbound (Project to External)
            RecentActivityManager.log(this, srcProjectPath, "paste", 
                typeStr + " " + action + " OUT to: " + new File(destPath).getParent(), "\uf0c5", false);
        } else if (srcProjectPath == null && destProjectPath != null) {
            // Inbound (External to Project)
            RecentActivityManager.log(this, destProjectPath, "paste", 
                typeStr + " " + action + " IN from: " + new File(srcPath).getParent(), "\uf0c5", false);
        }
    }
    
    private void showPasteNavigationDialog() {
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    
        TextView titleView = dialog.findViewById(R.id.dialog_title);
        TextView messageView = dialog.findViewById(R.id.dialog_message);
        android.widget.FrameLayout customContainer = dialog.findViewById(R.id.dialog_custom_view_container);
        Button btnPositive = dialog.findViewById(R.id.dialog_btn_positive);
        Button btnNegative = dialog.findViewById(R.id.dialog_btn_negative);
    
        titleView.setText("Move/Copy Destination");
        messageView.setText("Choose a location to paste your " + clipboardFiles.size() + " item(s).");
        
        // Build the custom list of destinations
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        
        String[] options = {"📂 Internal Storage", "📁 App Files", "💾 App Cache", "🔒 SAF Storage"};
        
        for (int i = 0; i < options.length; i++) {
            TextView tv = new TextView(this);
            tv.setText(options[i]);
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(16);
            tv.setPadding(32, 32, 32, 32);
            tv.setBackgroundResource(android.R.drawable.list_selector_background);
            
            final int index = i;
            tv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    
                    if (index == 0) {
                        startDirectoryPath = null;
                        currentDirectory = Environment.getExternalStorageDirectory();
                        loadDirectory(currentDirectory);
                    } else if (index == 1) {
                        startDirectoryPath = null;
                        File appFilesDir = getExternalFilesDir(null);
                        if (appFilesDir != null) {
                            if (!appFilesDir.exists()) appFilesDir.mkdirs();
                            currentDirectory = appFilesDir;
                            loadDirectory(currentDirectory);
                        }
                    } else if (index == 2) {
                        startDirectoryPath = null;
                        File cacheDir = getCacheDir();
                        if (cacheDir != null) {
                            currentDirectory = cacheDir;
                            loadDirectory(currentDirectory);
                        }
                    } else if (index == 3) {
                        if (savedSafUri != null) {
                            loadSafDirectory(Uri.parse(savedSafUri));
                        } else {
                            AppToast.showInfo(MainActivity.this, "No SAF folder selected yet.");
                        }
                    }
                }
            });
            layout.addView(tv);
            
            // Add a small divider line
            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
            divider.setBackgroundColor(Color.parseColor("#333333"));
            layout.addView(divider);
        }
    
        customContainer.setVisibility(View.VISIBLE);
        customContainer.removeAllViews();
        customContainer.addView(layout);
    
        btnPositive.setText("CANCEL COPY");
        btnPositive.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                isCopyMode = false;
                isMoveMode = false;
                clipboardFiles.clear();
                fabAdd.setText("\uf067"); // Reset to Plus
                AppToast.showInfo(MainActivity.this, "Copy/Move cancelled.");
            }
        });
    
        btnNegative.setVisibility(View.GONE);
    
        dialog.show();
    }
    
    private void performPaste() {
        if (clipboardFiles.isEmpty()) return;
        
        AppLoader.show(this, "Processing...");
        
        // Determine Destination
        final File destFileDir = currentDirectory;
        final android.support.v4.provider.DocumentFile destSafDir = (currentDirectory == null && !safHistory.isEmpty()) ? safHistory.get(safHistory.size() - 1) : null;
        
        if (destFileDir == null && destSafDir == null) {
            AppToast.showError(this, "Cannot paste here.");
            AppLoader.hide();
            return;
        }
    
        new Thread(new Runnable() {
            @Override
            public void run() {
                int successCount = 0;
                try {
                    for (FileItem item : clipboardFiles) {
                        boolean success = false;
                        String destPath = null;

                        if (item.isSaf) {
                            if (destFileDir != null) {
                                success = copySafToFile(item.docFile, destFileDir);
                                destPath = new File(destFileDir, item.name).getAbsolutePath();
                            } else if (destSafDir != null) {
                                success = copySafToSaf(item.docFile, destSafDir);
                            }
                        } else {
                            if (destFileDir != null) {
                                File targetFile = new File(destFileDir, item.name);
                                success = copyFileToFile(item.file, targetFile);
                                destPath = targetFile.getAbsolutePath();
                            } else if (destSafDir != null) {
                                success = copyFileToSaf(item.file, destSafDir);
                            }
                        }
                        
                        if (success) {
                            successCount++;
                            
                            // LOG THE ACTIVITY USING THE NEW BOUNDARY HELPER
                            logPasteAction(item, destPath, isMoveMode);
                        }

                        // If move mode, delete the original
                        if (isMoveMode) {
                            if (item.isSaf) item.docFile.delete();
                            else deleteRecursive(item.file);
                        }
                    }
                    
                    final int finalSuccess = successCount;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AppLoader.hide();
                            AppDialog.show(MainActivity.this, "Pasted Successfully", "Successfully pasted " + finalSuccess + " item(s).", "OK", "", null, null);

                            isCopyMode = false;
                            isMoveMode = false;
                            clipboardFiles.clear();
                            
                            // Reset FAB
                            fabAdd.setText("\uf067"); // Plus Icon
                            if (currentDirectory != null) loadDirectory(currentDirectory);
                            else if (destSafDir != null) loadSafDirectory(destSafDir);
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AppLoader.hide();
                            AppDialog.show(MainActivity.this, "Paste Failed", "Error: " + e.getMessage(), "OK", "", null, null);
                        }
                    });
                }
            }
        }).start();
    }
    
    // --- Helper Stream Methods ---
    
    private boolean copyFileToFile(File src, File dest) {
        try {
            if (src.isDirectory()) {
                if (!dest.exists()) dest.mkdirs();
                File[] children = src.listFiles();
                if (children != null) {
                    for (File child : children) {
                        copyFileToFile(child, new File(dest, child.getName()));
                    }
                }
            } else {
                FileInputStream in = new FileInputStream(src);
                FileOutputStream out = new FileOutputStream(dest);
                byte[] buffer = new byte[4096];
                int len;
                while ((len = in.read(buffer)) > 0) out.write(buffer, 0, len);
                out.close();
                in.close();
            }
            return true;
        } catch (Exception e) { return false; }
    }
    
    private boolean copyFileToSaf(File src, android.support.v4.provider.DocumentFile destDir) {
        try {
            android.support.v4.provider.DocumentFile newDoc;
            
            // Use the correct method based on type
            if (src.isDirectory()) {
                newDoc = destDir.createDirectory(src.getName());
            } else {
                newDoc = destDir.createFile("application/octet-stream", src.getName());
            }
            
            if (newDoc == null) return false;

            if (src.isDirectory()) {
                File[] children = src.listFiles();
                if (children != null) {
                    for (File child : children) {
                        copyFileToSaf(child, newDoc);
                    }
                }
            } else {
                InputStream in = new FileInputStream(src);
                OutputStream out = getContentResolver().openOutputStream(newDoc.getUri());
                byte[] buffer = new byte[4096];
                int len;
                while ((len = in.read(buffer)) > 0) out.write(buffer, 0, len);
                out.close();
                in.close();
            }
            return true;
        } catch (Exception e) { return false; }
    }
    
    private boolean copySafToFile(android.support.v4.provider.DocumentFile srcDoc, File destDir) {
        try {
            File dest = new File(destDir, srcDoc.getName());
            if (srcDoc.isDirectory()) {
                dest.mkdirs();
                for (android.support.v4.provider.DocumentFile child : srcDoc.listFiles()) {
                    copySafToFile(child, dest);
                }
            } else {
                InputStream in = getContentResolver().openInputStream(srcDoc.getUri());
                FileOutputStream out = new FileOutputStream(dest);
                byte[] buffer = new byte[4096];
                int len;
                while ((len = in.read(buffer)) > 0) out.write(buffer, 0, len);
                out.close();
                in.close();
            }
            return true;
        } catch (Exception e) { return false; }
    }
    
    private boolean copySafToSaf(android.support.v4.provider.DocumentFile srcDoc, android.support.v4.provider.DocumentFile destDir) {
        try {
            android.support.v4.provider.DocumentFile newDoc;
            
            // Use the correct method based on type
            if (srcDoc.isDirectory()) {
                newDoc = destDir.createDirectory(srcDoc.getName());
            } else {
                newDoc = destDir.createFile("application/octet-stream", srcDoc.getName());
            }

            if (newDoc == null) return false;

            if (srcDoc.isDirectory()) {
                for (android.support.v4.provider.DocumentFile child : srcDoc.listFiles()) {
                    copySafToSaf(child, newDoc);
                }
            } else {
                InputStream in = getContentResolver().openInputStream(srcDoc.getUri());
                OutputStream out = getContentResolver().openOutputStream(newDoc.getUri());
                byte[] buffer = new byte[4096];
                int len;
                while ((len = in.read(buffer)) > 0) out.write(buffer, 0, len);
                out.close();
                in.close();
            }
            return true;
        } catch (Exception e) { return false; }
    }
    
    private void handleBackNavigation() {
        if (isSelectionMode) {
            exitSelectionMode();
            return;
        }
        
        if (isPickerMode) {
            if (currentDirectory == null || currentDirectory.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                setResult(RESULT_CANCELED);
                finish();
            } else {
                parentFolderNavigation(currentDirectory);
            }
            return;
        }
    
        // --- SAF NAVIGATION ---
        if (currentDirectory == null && savedSafUri != null && !appName.getText().toString().equals("DebugMate")) {
            if (safHistory.size() > 1) {
                // 1. Remove the current folder
                safHistory.remove(safHistory.size() - 1);
                
                // 2. Get the previous folder (DocumentFile object)
                android.support.v4.provider.DocumentFile previousDoc = safHistory.get(safHistory.size() - 1);
                
                // 3. Load it directly — no more fromSingleUri or fromTreeUri!
                loadSafDirectory(previousDoc);
            } else {
                // Go back to Internal Storage
                startDirectoryPath = null;
                currentDirectory = Environment.getExternalStorageDirectory();
                safHistory.clear();
                loadDirectory(currentDirectory);
            }
            return;
        }
    
        // --- NORMAL FILE NAVIGATION ---
        if (currentDirectory != null) {
            parentFolderNavigation(currentDirectory);
        } else {
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                super.onBackPressed();
            } else {
                AppToast.showSuccess(MainActivity.this, "Press back again to exit");
            }
            backPressedTime = System.currentTimeMillis();
        }
    }
    
    private void parentFolderNavigation(File dir) {
        
        if (dir == null) return;

        if (startDirectoryPath != null && dir.getAbsolutePath().equals(startDirectoryPath)) {
            finish();
            return;
        }

        File parentDir = dir.getParentFile();

        if (parentDir == null) {
            currentDirectory = null;
            showWelcomeScreen();
            workspaceBtn.setVisibility(View.GONE);
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
                workspaceBtn.setVisibility(View.GONE);
            }
        } else if (path.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
            currentDirectory = null;
            showWelcomeScreen();
            workspaceBtn.setVisibility(View.GONE);
        } else if (parent.equals("/storage/emulated/0/Android/data")) {
            loadDirectory(Environment.getExternalStorageDirectory());
            AppToast.showSuccess(MainActivity.this, "Can't read data. Redirected to root folder.");
        } else {
            loadDirectory(dir.getParentFile());
        }
    }
    
    private void showCloneDialog() {
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_clone_git);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        final EditText inputUrl = dialog.findViewById(R.id.clone_url);
        final EditText inputUser = dialog.findViewById(R.id.clone_username);
        final EditText inputToken = dialog.findViewById(R.id.clone_token);
        Button btnCancel = dialog.findViewById(R.id.btn_clone_cancel);
        Button btnClone = dialog.findViewById(R.id.btn_clone_start);
        TextView btnInfo = dialog.findViewById(R.id.btn_clone_info);

        // Setup Info Icon
        Typeface solidFont = Typeface.createFromAsset(getAssets(), "fonts/fa-solid-900-v5.ttf");
        btnInfo.setTypeface(solidFont);
        btnInfo.setText("\uf05a"); // Info circle icon

        btnInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String limitations = 
                    "1. SSH URLs are not supported. Please use HTTPS.\n\n" +
                    "2. Git LFS (Large File Storage) files will only download as text pointers.\n\n" +
                    "3. Submodules will not be downloaded automatically.\n\n" +
                    "4. Very large repositories may fail due to device memory limits.";
                    
                AppDialog.show(MainActivity.this, "Clone Limitations", limitations, "GOT IT", "", null, null);
            }
        });

        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnClone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final String url = inputUrl.getText().toString().trim();
                final String user = inputUser.getText().toString().trim();
                final String token = inputToken.getText().toString().trim();
                
                if (url.isEmpty()) {
                    AppToast.showError(MainActivity.this, "Repository URL is required.");
                    return;
                }

                dialog.dismiss();
                
                final File targetDir = currentDirectory != null ? currentDirectory : Environment.getExternalStorageDirectory();
                final String repoName = url.substring(url.lastIndexOf("/") + 1).replace(".git", "");
                final File destination = new File(targetDir, repoName);

                if (destination.exists()) {
                    AppToast.showError(MainActivity.this, "Folder '" + repoName + "' already exists!");
                    return;
                }

                AppToast.showInfo(MainActivity.this, "Cloning '" + repoName + "'... Please wait.");
                
                // Save credentials for future pushes
                SharedPreferences prefs = getSharedPreferences("DebugMatePrefs", MODE_PRIVATE);
                prefs.edit().putString("git_username", user).putString("git_token", token).apply();
                
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final String result = GitManager.cloneRepo(url, destination.getAbsolutePath(), user, token);
                        
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result.startsWith("Success")) {
                                    AppToast.showSuccess(MainActivity.this, "Cloned successfully!");
                                    
                                    // ==========================================
                                    // LOG CLONE ACTIVITY
                                    // ==========================================
                                    String platform = "Git";
                                    String lowerUrl = url.toLowerCase();
                                    if (lowerUrl.contains("github.com")) {
                                        platform = "GitHub";
                                    } else if (lowerUrl.contains("gitlab.com")) {
                                        platform = "GitLab";
                                    } else if (lowerUrl.contains("bitbucket.org")) {
                                        platform = "Bitbucket";
                                    }
                                    
                                    RecentActivityManager.log(
                                        MainActivity.this,
                                        destination.getAbsolutePath(),
                                        "git_clone",
                                        "Cloned from " + platform + ": " + repoName,
                                        "\uf09b", // Git brand icon
                                        true
                                    );
                                    // ==========================================
                                    
                                    loadDirectory(targetDir);
                                } else {
                                    AppToast.showError(MainActivity.this, result);
                                }
                            }
                        });
                    }
                }).start();
            }
        });

        dialog.show();
    }
    
    private void setupIcon(TextView textView, String unicode, Typeface font) {
        if (textView != null) {
            textView.setTypeface(font);
            textView.setText(unicode);
        }
    }

    private void showWelcomeScreen() {
        if (welcomeMessage != null) welcomeMessage.setVisibility(View.VISIBLE);
        if (fileListView != null) fileListView.setVisibility(View.GONE);
        if (emptyFolderLayout != null) emptyFolderLayout.setVisibility(View.GONE);
        if (!isPickerMode) {
            if (appName != null) appName.setText("DebugMate");
            if (sideBarBtn != null) sideBarBtn.setText("\uf0c9");
        }
    }
    
    private void showAddAsProjectDialog(final FileItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Project Type");
    
        final String[] types = {"web", "python", "java", "android", "custom"};
    
        builder.setItems(types, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedType = types[which];
            
                ProjectItem newProject = new ProjectItem(
                        item.name, 
                        selectedType, 
                        item.file.getAbsolutePath(), 
                        "Custom"
                );
            
                ProjectManager.saveProject(MainActivity.this, newProject);
            
                AppToast.showSuccess(MainActivity.this, "Added '" + item.name + "' to Projects!");
            
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
                selectedExt = item.ext.toLowerCase(); 
            }
            if (item.isSaf) {
                filesAddAsProjectBtn.setClickable(false);
                filesAddAsProjectBtn.setFocusable(false);
                filesAddAsProjectBtn.setTextColor(Color.parseColor("#888888"));
            } else {
                filesAddAsProjectBtn.setClickable(true);
                filesAddAsProjectBtn.setFocusable(true);
                filesAddAsProjectBtn.setTextColor(Color.parseColor("#FFFFFF"));
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
        if (tasksForFiles != null) tasksForFiles.setVisibility(View.GONE);
        if (moreFileTasks != null) moreFileTasks.setVisibility(View.GONE);
        if (fabAdd != null) fabAdd.setVisibility(View.VISIBLE);
    
        for (FileItem item : fileList) {
            item.isSelected = false;
        }
        adapter.notifyDataSetChanged();
    
        if (currentDirectory == null) {
            if (appName != null) appName.setText("DebugMate");
            if (sideBarBtn != null) sideBarBtn.setText("\uf0c9");
        } else if (currentDirectory.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
            if (appName != null) appName.setText("DebugMate");
            if (sideBarBtn != null) sideBarBtn.setText("\uf0c9");
        } else {
            if (appName != null) appName.setText(currentDirectory.getName());
            if (sideBarBtn != null) sideBarBtn.setText("\uf060");
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
        getSharedPreferences("DebugMatePrefs", MODE_PRIVATE).edit()
            .putString("last_normal_folder", dir.getAbsolutePath())
            .putString("last_location_type", "normal")
            .apply();
    
        File[] files = dir.listFiles();

        if (files != null) {
            // --- Check if the folder is empty ---
            if (files.length == 0) {
                if (welcomeMessage != null) welcomeMessage.setVisibility(View.GONE);
                if (fileListView != null) fileListView.setVisibility(View.GONE);
                
                if (emptyFolderLayout != null) {
                    emptyFolderLayout.setVisibility(View.VISIBLE);
                    
                    // Trigger the animation
                    android.view.animation.AlphaAnimation fadeIn = new android.view.animation.AlphaAnimation(0f, 1f);
                    fadeIn.setDuration(600);
                    emptyFolderLayout.startAnimation(fadeIn);
                }
                
            } else {
                // --- Folder has files ---
                if (emptyFolderLayout != null) emptyFolderLayout.setVisibility(View.GONE);
                
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        if (f1.isDirectory() && !f2.isDirectory()) return -1;
                        if (!f1.isDirectory() && f2.isDirectory()) return 1;
                        return f1.getName().compareToIgnoreCase(f2.getName());
                    }
                });

                fileList.clear();
                SharedPreferences prefs = getSharedPreferences("DebugMatePrefs", MODE_PRIVATE);
                boolean showHidden = prefs.getBoolean("show_hidden_files", false);

                for (File file : files) {
                    // If it's a hidden file and the setting is OFF, skip it
                    if (!showHidden && file.getName().startsWith(".")) {
                        continue;
                    }
                    fileList.add(new FileItem(file));
                }
                adapter.notifyDataSetChanged();
                
                if (welcomeMessage != null) welcomeMessage.setVisibility(View.GONE);
                if (fileListView != null) fileListView.setVisibility(View.VISIBLE);
                workspaceBtn.setVisibility(View.VISIBLE);
            }
            
            if (isPickerMode) {
                workspaceBtn.setVisibility(View.GONE);
                if (currentDirectory.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                    if (sideBarBtn != null) sideBarBtn.setText("\uf0c9");
                } else {
                    if (sideBarBtn != null) sideBarBtn.setText("\uf060");
                }
                if (appName != null) appName.setText("Select: " + dir.getName());
    
            } else {
                if (currentDirectory.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                    if (sideBarBtn != null) sideBarBtn.setText("\uf0c9");
                    if (appName != null) appName.setText("DebugMate");
                    workspaceBtn.setVisibility(View.GONE);
                } else {
                    if (appName != null) appName.setText(dir.getName());
                    if (sideBarBtn != null) sideBarBtn.setText("\uf060");
                    workspaceBtn.setVisibility(View.VISIBLE);
                }
            }
        } else {
             AppToast.showInfo(MainActivity.this, "Cannot read directory: " + dir.getName());
        }
    }
    
    private void loadSafDirectory(Uri treeUri) {
        android.support.v4.provider.DocumentFile rootDoc = android.support.v4.provider.DocumentFile.fromTreeUri(this, treeUri);
        if (rootDoc != null) {
            safHistory.clear();
            safHistory.add(rootDoc); // Store the DocumentFile directly
            loadSafDirectory(rootDoc);
        }
        
        if (rootDoc != null) {
            getSharedPreferences("DebugMatePrefs", MODE_PRIVATE).edit()
                .putString("last_location_type", "saf")
                .apply();
            safHistory.clear();
            safHistory.add(rootDoc);
            loadSafDirectory(rootDoc);
        }
    }
    
    private void loadSafDirectory(final android.support.v4.provider.DocumentFile rootDoc) {
        if (rootDoc == null || !rootDoc.isDirectory()) {
            AppToast.showError(MainActivity.this, "Invalid folder.");
            return;
        }
    
        currentDirectory = null; // Clear normal directory
        appName.setText(rootDoc.getName());
        
        // Change Icon based on history depth
        if (safHistory.size() > 1) {
            sideBarBtn.setText("\uf060"); // Back arrow
        } else {
            sideBarBtn.setText("\uf0c9"); // Hamburger menu
        }
        
        workspaceBtn.setVisibility(View.VISIBLE);
        welcomeMessage.setVisibility(View.GONE);
        fileListView.setVisibility(View.GONE);
        emptyFolderLayout.setVisibility(View.GONE);
        
        AppLoader.show(MainActivity.this, "Loading folder...");
    
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<FileItem> tempList = new ArrayList<>();
                android.support.v4.provider.DocumentFile[] files = rootDoc.listFiles();
    
                if (files != null) {
                    // 1. Sort in background
                    java.util.Arrays.sort(files, new java.util.Comparator<android.support.v4.provider.DocumentFile>() {
                        @Override
                        public int compare(android.support.v4.provider.DocumentFile f1, android.support.v4.provider.DocumentFile f2) {
                            if (f1.isDirectory() && !f2.isDirectory()) return -1;
                            if (!f1.isDirectory() && f2.isDirectory()) return 1;
                            String n1 = f1.getName() != null ? f1.getName() : "";
                            String n2 = f2.getName() != null ? f2.getName() : "";
                            return n1.compareToIgnoreCase(n2);
                        }
                    });
    
                    // 2. Fetch data in background
                    for (android.support.v4.provider.DocumentFile f : files) {
                        String fileName = f.getName();
                        if (fileName != null && !fileName.startsWith(".")) {
                            boolean isDir = f.isDirectory();
                            long size = isDir ? 0 : f.length();
                            tempList.add(new FileItem(f, fileName, isDir, size));
                        }
                    }
                }
    
                // 3. Update UI instantly
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AppLoader.hide();
                        fileList.clear();
                        fileList.addAll(tempList);
                        adapter.notifyDataSetChanged();
                        
                        if (fileList.isEmpty()) {
                            emptyFolderLayout.setVisibility(View.VISIBLE);
                        } else {
                            fileListView.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }).start();
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
                    AppToast.showSuccess(MainActivity.this, "Folder name cannot be empty.");
                    return;
                }
                
                File newFolder = new File(currentDirectory, folderName);
                if (newFolder.mkdir()) {
                    AppToast.showSuccess(MainActivity.this, "Created: " + folderName);
                    
                    // ==========================================
                    // LOG FOLDER CREATION TO PROJECT DASHBOARD
                    // ==========================================
                    String newFolderPath = newFolder.getAbsolutePath();
                    ArrayList<ProjectItem> allProjects = ProjectManager.loadProjects(MainActivity.this);
                    
                    for (int i = 0; i < allProjects.size(); i++) {
                        ProjectItem p = allProjects.get(i);
                        if (newFolderPath.startsWith(p.path)) {
                            RecentActivityManager.log(
                                MainActivity.this,
                                p.path,
                                "folder_created",
                                "Folder created: " + folderName,
                                "\uf07b", // FontAwesome folder solid icon
                                false
                            );
                            break;
                        }
                    }
                    // ==========================================
                
                    loadDirectory(currentDirectory); 
                } else {
                    AppToast.showSuccess(MainActivity.this, "Folder already exists!");
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
                    AppToast.showSuccess(MainActivity.this, "File name cannot be empty.");
                    return;
                }
                
                File newFile = new File(currentDirectory, fileName);
                try {
                    if (newFile.createNewFile()) {
                        AppToast.showSuccess(MainActivity.this, "Created: " + fileName);
                        
                        // ==========================================
                        // LOG FILE CREATION TO PROJECT DASHBOARD
                        // ==========================================
                        String newFilePath = newFile.getAbsolutePath();
                        ArrayList<ProjectItem> allProjects = ProjectManager.loadProjects(MainActivity.this);
                        
                        for (int i = 0; i < allProjects.size(); i++) {
                            ProjectItem p = allProjects.get(i);
                            // Check if this new file is inside a registered project
                            if (newFilePath.startsWith(p.path)) {
                                RecentActivityManager.log(
                                    MainActivity.this,
                                    p.path,
                                    "file_created",
                                    "File created: " + fileName,
                                    "\uf15b", // FontAwesome file solid icon
                                    false
                                );
                                break; // Found the project, stop looping
                            }
                        }
                        // ==========================================
                
                        loadDirectory(currentDirectory); 
                    } else {
                        AppToast.showSuccess(MainActivity.this, "File already exists!");
                    }
                } catch (IOException e) {
                    AppToast.showSuccess(MainActivity.this, "Error creating file: " + e.getMessage());
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
        // 1. Inflate the custom XML layout
        android.view.LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_rename_input, null);
        
        // 2. Find the EditText and set the current name
        final EditText input = dialogView.findViewById(R.id.dialog_rename_input);
        input.setText(item.name);
        input.setSelection(input.getText().length());

        // 3. Show the AppDialog with the custom view
        AppDialog.show(
            MainActivity.this,
            "Rename",
            "Enter a new name for this item.",
            dialogView,
            "RENAME",
            "CANCEL",
            new AppDialog.OnDialogClickListener() {
                @Override
                public void onClick() {
                    String newName = input.getText().toString().trim();
                    
                    if (newName.isEmpty()) {
                        AppToast.showError(MainActivity.this, "Name cannot be empty.");
                        return;
                    }
                    if (newName.equals(item.name)) {
                        return; // No change
                    }

                    // --- SAF LOGIC ---
                    if (item.isSaf) {
                        if (item.docFile.renameTo(newName)) {
                            AppToast.showSuccess(MainActivity.this, "Renamed successfully!");
                            exitSelectionMode();
                            // Refresh the SAF directory
                            if (!safHistory.isEmpty()) {
                                loadSafDirectory(safHistory.get(safHistory.size() - 1));
                            }
                        } else {
                            AppToast.showError(MainActivity.this, "Rename failed. The provider might not allow this.");
                        }
                        return;
                    }

                    // --- NORMAL FILE LOGIC ---
                    if (currentDirectory == null) return;
                    
                    File newFile = new File(currentDirectory, newName);
                    
                    if (item.file.renameTo(newFile)) {
                        AppToast.showSuccess(MainActivity.this, "Renamed successfully!");
                        
                        // LOG THE ACTIVITY
                        String typeStr = item.isDir ? "Folder" : "File";
                        logProjectActivity(newFile.getAbsolutePath(), "rename", typeStr + " renamed: " + item.name + " -> " + newName, "\uf044");
                        
                        exitSelectionMode();
                        loadDirectory(currentDirectory); 
                    } else {
                        AppToast.showError(MainActivity.this, "Rename failed. File might already exist.");
                    }
                }
            },
            null
        );
    }
    
    private void showDeleteDialog(final FileItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Delete " + item.name + "?");
        builder.setMessage("Are you sure you want to delete this?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (deleteRecursive(item.file)) {
                    AppToast.showSuccess(MainActivity.this, "Deleted successfully!");
                    loadDirectory(currentDirectory);
                } else {
                    AppToast.showSuccess(MainActivity.this, "Failed to delete.");
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
    
        AppLoader.show(this, "Compressing...");
    
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ZipOutputStream zos;
                    File finalNormalZip = null;
                    String finalZipName = "";
    
                    // --- 1. Create Destination Zip File ---
                    if (currentDirectory != null) {
                        // Normal Destination
                        finalNormalZip = new File(currentDirectory, "archive.zip");
                        int count = 1;
                        while (finalNormalZip.exists()) {
                            finalNormalZip = new File(currentDirectory, "archive(" + count + ").zip");
                            count++;
                        }
                        finalZipName = finalNormalZip.getName();
                        zos = new ZipOutputStream(new FileOutputStream(finalNormalZip));
                    } else {
                        // SAF Destination
                        android.support.v4.provider.DocumentFile destSafDir = safHistory.get(safHistory.size() - 1);
                        android.support.v4.provider.DocumentFile zipDoc = destSafDir.createFile("application/zip", "archive.zip");
                        finalZipName = zipDoc.getName();
                        zos = new ZipOutputStream(getContentResolver().openOutputStream(zipDoc.getUri()));
                    }
    
                    // --- 2. Add files to Zip ---
                    for (FileItem item : selectedFiles) {
                        if (item.isSaf) {
                            zipSafFile(item.docFile, item.name, zos);
                        } else {
                            zipNormalFile(item.file, item.name, zos);
                        }
                    }
                    zos.close();
    
                    // --- 3. Update UI ---
                    final String finalZipNameCopy = finalZipName;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AppLoader.hide();
                            AppDialog.show(MainActivity.this, "Compressed", "Successfully created " + finalZipNameCopy, "OK", "", null, null);
                            exitSelectionMode();
                            if (currentDirectory != null) loadDirectory(currentDirectory);
                            else if (!safHistory.isEmpty()) loadSafDirectory(safHistory.get(safHistory.size() - 1));
                        }
                    });
    
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AppLoader.hide();
                            AppDialog.show(MainActivity.this, "Failed", "Compression failed: " + e.getMessage(), "OK", "", null, null);
                        }
                    });
                }
            }
        }).start();
    }

    private void zipNormalFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws Exception {
        if (fileToZip.isHidden()) return;
        
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
                    zipNormalFile(childFile, fileName + "/" + childFile.getName(), zipOut);
                }
            }
            return;
        }
        
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        
        byte[] bytes = new byte[4096];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
        zipOut.closeEntry();
    }
    
    private void zipSafFile(android.support.v4.provider.DocumentFile docFile, String fileName, ZipOutputStream zipOut) throws Exception {
        if (docFile.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
            }
            zipOut.closeEntry();
            
            android.support.v4.provider.DocumentFile[] children = docFile.listFiles();
            if (children != null) {
                for (android.support.v4.provider.DocumentFile child : children) {
                    zipSafFile(child, fileName + "/" + child.getName(), zipOut);
                }
            }
            return;
        }
        
        InputStream is = getContentResolver().openInputStream(docFile.getUri());
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        
        byte[] bytes = new byte[4096];
        int length;
        while ((length = is.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        is.close();
        zipOut.closeEntry();
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
    
        if (!selectedItem.name.toLowerCase().endsWith(".zip")) {
            AppToast.showSuccess(MainActivity.this, "Only .zip files are supported for extraction.");
            return;
        }
    
        final FileItem finalItem = selectedItem;
        AppLoader.show(this, "Uncompressing...");
    
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream is;
                    if (finalItem.isSaf) {
                        is = getContentResolver().openInputStream(finalItem.docFile.getUri());
                    } else {
                        is = new FileInputStream(finalItem.file);
                    }
    
                    if (currentDirectory != null) {
                        // Normal Target
                        unzipStream(is, currentDirectory, null);
                    } else {
                        // SAF Target
                        android.support.v4.provider.DocumentFile targetSafDir = safHistory.get(safHistory.size() - 1);
                        unzipStream(is, null, targetSafDir);
                    }
                    is.close();
    
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AppLoader.hide();
                            AppDialog.show(MainActivity.this, "Uncompressed", "Successfully extracted files.", "OK", "", null, null);
                            exitSelectionMode();
                            if (currentDirectory != null) loadDirectory(currentDirectory);
                            else if (!safHistory.isEmpty()) loadSafDirectory(safHistory.get(safHistory.size() - 1));
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AppLoader.hide();
                            AppDialog.show(MainActivity.this, "Failed", "Error: " + e.getMessage(), "OK", "", null, null);
                        }
                    });
                }
            }
        }).start();
    }
    
    private void unzipStream(InputStream is, File normalDir, android.support.v4.provider.DocumentFile safDir) throws Exception {
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
        ZipEntry ze;
        byte[] buffer = new byte[8192];
        int count;
    
        while ((ze = zis.getNextEntry()) != null) {
            String entryName = ze.getName();
            
            if (normalDir != null) {
                // --- NORMAL FILE EXTRACTION ---
                File file = new File(normalDir, entryName);
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs()) {
                    throw new Exception("Failed to ensure directory: " + dir.getAbsolutePath());
                }
                if (!ze.isDirectory()) {
                    FileOutputStream fout = new FileOutputStream(file);
                    BufferedOutputStream bos = new BufferedOutputStream(fout, 1024);
                    while ((count = zis.read(buffer)) != -1) bos.write(buffer, 0, count);
                    bos.flush(); bos.close(); fout.close();
                }
            } else if (safDir != null) {
                // --- SAF EXTRACTION ---
                String[] parts = entryName.split("/");
                android.support.v4.provider.DocumentFile current = safDir;
                
                // Traverse and create directories if needed
                for (int i = 0; i < parts.length - 1; i++) {
                    if (parts[i].isEmpty()) continue; 
                    android.support.v4.provider.DocumentFile next = findOrCreateSafDir(current, parts[i]);
                    if (next != null) current = next;
                }
                
                if (!ze.isDirectory()) {
                    String fileName = parts[parts.length - 1];
                    if (fileName.isEmpty()) continue;
                    
                    // Delete existing file if it exists to overwrite safely
                    android.support.v4.provider.DocumentFile existingFile = findSafChild(current, fileName);
                    if (existingFile != null) existingFile.delete();
                    
                    android.support.v4.provider.DocumentFile newFile = current.createFile("application/octet-stream", fileName);
                    if (newFile != null) {
                        OutputStream os = getContentResolver().openOutputStream(newFile.getUri());
                        while ((count = zis.read(buffer)) != -1) os.write(buffer, 0, count);
                        os.close();
                    }
                }
            }
        }
        zis.close();
    }
    
    private android.support.v4.provider.DocumentFile findOrCreateSafDir(android.support.v4.provider.DocumentFile parent, String dirName) {
        android.support.v4.provider.DocumentFile[] children = parent.listFiles();
        if (children != null) {
            for (android.support.v4.provider.DocumentFile child : children) {
                if (dirName.equals(child.getName()) && child.isDirectory()) return child;
            }
        }
        return parent.createDirectory(dirName);
    }
    
    private android.support.v4.provider.DocumentFile findSafChild(android.support.v4.provider.DocumentFile parent, String name) {
        android.support.v4.provider.DocumentFile[] children = parent.listFiles();
        if (children != null) {
            for (android.support.v4.provider.DocumentFile child : children) {
                if (name.equals(child.getName())) return child;
            }
        }
        return null;
    }
    
    private void showDetailsDialog(FileItem item) {
        String dateString;
        String sizeString;
        String typeString;
        String pathString;

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        
        if (item.isSaf) {
            // --- SAF LOGIC ---
            dateString = sdf.format(new Date(item.docFile.lastModified()));
            // We use the cached size from FileItem to avoid locking the UI
            sizeString = item.isDir ? "Folder" : formatSize(item.size);
            typeString = item.isDir ? "Folder" : (item.ext.toUpperCase() + " File");
            pathString = item.docFile.getUri().toString();
        } else {
            // --- NORMAL FILE LOGIC ---
            dateString = sdf.format(new Date(item.file.lastModified()));
            long sizeInBytes = item.isDir ? getFolderSize(item.file) : item.file.length();
            sizeString = formatSize(sizeInBytes);
            typeString = item.isDir ? "Folder" : (item.ext.toUpperCase() + " File");
            pathString = item.file.getAbsolutePath();
        }

        // Inflate the custom XML layout
        android.view.LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_file_details, null);

        // Populate the TextViews
        ((TextView) dialogView.findViewById(R.id.detail_name)).setText(item.name);
        ((TextView) dialogView.findViewById(R.id.detail_type)).setText(typeString);
        ((TextView) dialogView.findViewById(R.id.detail_size)).setText(sizeString);
        ((TextView) dialogView.findViewById(R.id.detail_date)).setText(dateString);
        ((TextView) dialogView.findViewById(R.id.detail_path)).setText(pathString);

        // Show it using your AppDialog
        AppDialog.show(
            MainActivity.this,
            "File Details",
            "Information about this item.",
            dialogView,
            "OK",
            "", 
            null,
            null
        );
    }

    private long getFolderSize(File dir) {
        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    size += file.length();
                } else {
                    size += getFolderSize(file); 
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    
        if (requestCode == REQUEST_SAF_DIRECTORY && resultCode == RESULT_OK && data != null) {
            Uri treeUri = data.getData();
            if (treeUri == null) return;
    
            final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            try {
                getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
                
                // Save the URI permanently!
                savedSafUri = treeUri.toString();
                getSharedPreferences("DebugMatePrefs", MODE_PRIVATE).edit().putString("saved_saf_uri", savedSafUri).apply();
                
                // Load the directory
                loadSafDirectory(treeUri);
    
            } catch (SecurityException e) {
                AppToast.showError(MainActivity.this, "Could not retain storage access.");
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the directory if we have one open to apply new settings
        if (currentDirectory != null && !isSelectionMode) {
            loadDirectory(currentDirectory);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
            return;
        }
        handleBackNavigation();
    }
}
