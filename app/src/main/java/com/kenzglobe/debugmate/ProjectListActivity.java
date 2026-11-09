package com.kenzglobe.debugmate;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ProjectListActivity extends Activity {

    private GridView projectGridView;
    private LinearLayout emptyLayout;
    private TextView emptyIcon, btnShowHidden, btnClose;
    private ProjectAdapter adapter;
    private ArrayList<ProjectItem> projectList, displayList;
    private boolean showingHidden = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_list);
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#8B5CF6"));
            getWindow().getDecorView().setSystemUiVisibility(0);
        }
        if (getActionBar() != null) getActionBar().hide();
        
        Typeface solidFont = Typeface.createFromAsset(getAssets(), "fonts/fa-solid-900-v5.ttf");
        
        btnClose = findViewById(R.id.btn_close_projects);
        btnClose.setTypeface(solidFont);
        btnClose.setText("\uf060");
        
        // Dynamically add the "Show Hidden" eye icon to the top bar
        btnShowHidden = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(55, LinearLayout.LayoutParams.MATCH_PARENT);
        btnShowHidden.setLayoutParams(params);
        btnShowHidden.setGravity(Gravity.CENTER);
        btnShowHidden.setTextColor(Color.WHITE);
        btnShowHidden.setTextSize(20f);
        btnShowHidden.setTypeface(solidFont);
        btnShowHidden.setText("\uf06e"); // Eye icon
        ((LinearLayout)btnClose.getParent()).addView(btnShowHidden);
        
        projectGridView = findViewById(R.id.project_list);
        emptyLayout = findViewById(R.id.empty_projects_layout);
        emptyIcon = findViewById(R.id.empty_icon_projects);
        emptyIcon.setTypeface(solidFont);
        emptyIcon.setText("\uf07b"); // Folder icon
        
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish(); }
        });
        
        btnShowHidden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showingHidden = !showingHidden;
                btnShowHidden.setTextColor(showingHidden ? Color.parseColor("#8B5CF6") : Color.WHITE);
                loadData();
            }
        });
        
        projectGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ProjectItem item = displayList.get(position);
                if (new File(item.path).exists()) {
                    Intent intent = new Intent(ProjectListActivity.this, MainActivity.class);
                    intent.putExtra("START_DIRECTORY", item.path);
                    startActivity(intent);
                } else {
                    AppToast.showError(ProjectListActivity.this, "Project folder not found!");
                }
            }
        });
        
        projectGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showOptionsDialog(displayList.get(position));
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        projectList = ProjectManager.loadProjects(this);
        displayList = new ArrayList<>();

        for (ProjectItem p : projectList) {
            if (showingHidden || !p.isHidden) {
                displayList.add(p);
            }
        }

        // Sort: Pinned projects first, then alphabetically
        Collections.sort(displayList, new Comparator<ProjectItem>() {
            @Override
            public int compare(ProjectItem p1, ProjectItem p2) {
                if (p1.isPinned && !p2.isPinned) return -1;
                if (!p1.isPinned && p2.isPinned) return 1;
                return p1.name.compareToIgnoreCase(p2.name);
            }
        });
        
        if (displayList.isEmpty()) {
            emptyLayout.setVisibility(View.VISIBLE);
            projectGridView.setVisibility(View.GONE);
        } else {
            emptyLayout.setVisibility(View.GONE);
            projectGridView.setVisibility(View.VISIBLE);
            adapter = new ProjectAdapter(this, displayList);
            projectGridView.setAdapter(adapter);
        }
    }

    private void showOptionsDialog(final ProjectItem item) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_project_options);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        TextView title = dialog.findViewById(R.id.options_title);
        title.setText(item.name + " Options");
        
        ListView list = dialog.findViewById(R.id.options_list);
        String[] options = {
            item.isPinned ? "Unpin Project" : "Pin Project",
            item.isHidden ? "Unhide Project" : "Hide Project",
            "Rename Project",
            "Copy Project",
            "Compress to ZIP",
            "Share Project",
            "Project Info",
            "Remove from List",
            "Delete from Device"
        };
        
        ArrayAdapter<String> optionsAdapter = new ArrayAdapter<>(
                this, 
                R.layout.item_dialog_option, 
                android.R.id.text1, 
                options
        );
        list.setAdapter(optionsAdapter);
        
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();
                switch (position) {
                    case 0: togglePin(item); break;
                    case 1: toggleHide(item); break;
                    case 2: showRenameDialog(item); break;
                    case 3: showCopyDialog(item); break;
                    case 4: compressProject(item, false); break;
                    case 5: compressProject(item, true); break;
                    case 6: showInfoDialog(item); break;
                    case 7: 
                        ProjectManager.removeProject(ProjectListActivity.this, item.path);
                        loadData();
                        break;
                    case 8: confirmDelete(item); break;
                }
            }
        });
        dialog.show();
    }

    private void togglePin(ProjectItem item) {
        item.isPinned = !item.isPinned;
        // Pass item.path for both oldPath and current item
        ProjectManager.updateProject(this, item.path, item);
        loadData();
    }

    private void toggleHide(ProjectItem item) {
        item.isHidden = !item.isHidden;
        ProjectManager.updateProject(this, item.path, item);
        loadData();
    }

    private void showRenameDialog(final ProjectItem item) {
        final EditText input = new EditText(this);
        input.setText(item.name);
        input.setTextColor(Color.WHITE);
        input.setPadding(20, 20, 20, 20);
        
        AppDialog.show(this, "Rename Project", "Enter a new name:", input, "RENAME", "CANCEL",
            new AppDialog.OnDialogClickListener() {
                @Override
                public void onClick() {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) return;
                    
                    String oldPath = item.path; // <--- Save old path before changing it
                    File oldFile = new File(item.path);
                    File newFile = new File(oldFile.getParent(), newName);
                    
                    if (oldFile.renameTo(newFile)) {
                        item.name = newName;
                        item.path = newFile.getAbsolutePath();
                        
                        // Update using the oldPath so the Manager can find it
                        ProjectManager.updateProject(ProjectListActivity.this, oldPath, item);
                        
                        AppToast.showSuccess(ProjectListActivity.this, "Renamed successfully!");
                        loadData();
                    } else {
                        AppToast.showError(ProjectListActivity.this, "Rename failed.");
                    }
                }
            }, null);
    }

    private void showCopyDialog(final ProjectItem item) {
        final EditText input = new EditText(this);
        input.setText(item.name + "_copy");
        input.setTextColor(Color.WHITE);
        input.setPadding(20, 20, 20, 20);
        
        AppDialog.show(this, "Copy Project", "Enter name for the copy:", input, "COPY", "CANCEL",
            new AppDialog.OnDialogClickListener() {
                @Override
                public void onClick() {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) return;
                    File source = new File(item.path);
                    File dest = new File(source.getParent(), newName);
                    try {
                        copyDirectory(source, dest);
                        ProjectManager.saveProject(ProjectListActivity.this, new ProjectItem(newName, item.type, dest.getAbsolutePath(), item.storageType));
                        AppToast.showSuccess(ProjectListActivity.this, "Copied successfully!");
                        loadData();
                    } catch (Exception e) {
                        AppToast.showError(ProjectListActivity.this, "Copy failed.");
                    }
                }
            }, null);
    }

    private void showInfoDialog(final ProjectItem item) {
        File f = new File(item.path);
        String info = "Type: " + item.type.toUpperCase() + "\n" +
                      "Storage: " + item.storageType + "\n" +
                      "Size: " + (getFolderSize(f) / 1024) + " KB\n\n" +
                      "Path:\n" + item.path;
        
        AppDialog.show(this, "Project Info", info, "COPY PATH", "OK",
            new AppDialog.OnDialogClickListener() {
                @Override
                public void onClick() {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(ClipData.newPlainText("Project Path", item.path));
                        AppToast.showSuccess(ProjectListActivity.this, "Path copied!");
                    }
                }
            }, null);
    }

    private void confirmDelete(final ProjectItem item) {
        AppDialog.show(this, "Delete '" + item.name + "'?",
            "Are you sure you want to permanently delete this project?\n\nAll files will be erased. This CANNOT be undone.",
            "DELETE", "CANCEL",
            new AppDialog.OnDialogClickListener() {
                @Override
                public void onClick() {
                    deleteProjectFolder(new File(item.path));
                    ProjectManager.removeProject(ProjectListActivity.this, item.path);
                    AppToast.showSuccess(ProjectListActivity.this, "Project deleted.");
                    loadData();
                }
            }, null);
    }

    private void compressProject(final ProjectItem item, final boolean share) {
        AppToast.showInfo(this, "Compressing... Please wait.");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File source = new File(item.path);
                    final File zipFile = new File(source.getParent(), item.name + ".zip");
                    
                    FileOutputStream fos = new FileOutputStream(zipFile);
                    ZipOutputStream zos = new ZipOutputStream(fos);
                    
                    zipDirectory(source, source.getName(), zos);
                    zos.close();
                    fos.close();
                    
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (share) {
                                AppToast.showSuccess(ProjectListActivity.this, "Zipped! Opening share menu...");
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("application/zip");
                                
                                // Use FileProvider so it doesn't crash on Android 7+
                                Uri uri = android.support.v4.content.FileProvider.getUriForFile(
                                        ProjectListActivity.this,
                                        getPackageName() + ".fileprovider",
                                        zipFile
                                );
                                intent.putExtra(Intent.EXTRA_STREAM, uri);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(Intent.createChooser(intent, "Share Project"));
                            } else {
                                AppToast.showSuccess(ProjectListActivity.this, "Compressed to " + zipFile.getName());
                            }
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AppToast.showError(ProjectListActivity.this, "Compression failed: " + e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    private void deleteProjectFolder(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteProjectFolder(child);
                }
            }
        }
        file.delete();
    }

    private void copyDirectory(File source, File dest) throws IOException {
        if (!dest.exists()) dest.mkdirs();
        File[] files = source.listFiles();
        if (files != null) {
            for (File f : files) {
                File newFile = new File(dest, f.getName());
                if (f.isDirectory()) {
                    copyDirectory(f, newFile);
                } else {
                    FileInputStream in = new FileInputStream(f);
                    FileOutputStream out = new FileOutputStream(newFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) out.write(buffer, 0, length);
                    in.close(); 
                    out.close();
                }
            }
        }
    }

    private void zipDirectory(File dir, String baseName, ZipOutputStream zos) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File f : files) {
            if (f.isDirectory()) {
                zipDirectory(f, baseName + "/" + f.getName(), zos);
            } else {
                FileInputStream fis = new FileInputStream(f);
                zos.putNextEntry(new ZipEntry(baseName + "/" + f.getName()));
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) zos.write(buffer, 0, length);
                zos.closeEntry();
                fis.close();
            }
        }
    }

    private long getFolderSize(File dir) {
        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                size += f.isFile() ? f.length() : getFolderSize(f);
            }
        }
        return size;
    }
}
