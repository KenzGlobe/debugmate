package com.kenzglobe.debugmate;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
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
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.os.Handler;
import android.widget.EditText;

public class ProjectListActivity extends Activity {

    private GridView projectGridView;
    private LinearLayout emptyLayout;
    private TextView emptyIcon, btnShowHidden, btnClose, workspaceBtn;
    private ProjectAdapter adapter;
    private ArrayList<ProjectItem> projectList, displayList;
    private boolean showingHidden = false;
    
    private FingerprintManager fingerprintManager;
    private CancellationSignal cancellationSignal;
    private boolean isAuthenticating = false;
    private int fingerprintFailCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_list);
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#8B5CF6"));
            getWindow().getDecorView().setSystemUiVisibility(0);
        }
        if (getActionBar() != null) getActionBar().hide();
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
        }
        
        Typeface solidFont = Typeface.createFromAsset(getAssets(), "fonts/fa-solid-900-v5.ttf");
        
        btnClose = findViewById(R.id.btn_close_projects);
        btnClose.setTypeface(solidFont);
        btnClose.setText("\uf060");
        
        btnShowHidden = findViewById(R.id.btn_show_hidden);
        btnShowHidden.setTypeface(solidFont);
        btnShowHidden.setText("\uf06e");
        
        projectGridView = findViewById(R.id.project_list);
        emptyLayout = findViewById(R.id.empty_projects_layout);
        emptyIcon = findViewById(R.id.empty_icon_projects);
        emptyIcon.setTypeface(solidFont);
        emptyIcon.setText("\uf07b"); // Folder icon
        workspaceBtn = findViewById(R.id.workspace_btn);
        
        workspaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.content.Intent intent = new android.content.Intent(ProjectListActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish(); }
        });
        
        btnShowHidden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showingHidden) {
                    // If it's already showing, just lock it immediately
                    showingHidden = false;
                    btnShowHidden.setText("\uf06e"); // Eye icon
                    loadData();
                    return;
                }
        
                // If NOT showing, authenticate first
                SharedPreferences prefs = getSharedPreferences("DebugMatePrefs", MODE_PRIVATE);
                String savedPin = prefs.getString("vault_pin", null);
        
                if (savedPin == null || savedPin.isEmpty()) {
                    // PIN not set up yet. Ask them to create one.
                    showSetupPinDialog();
                } else {
                    // PIN is set. Try biometrics first, fallback to PIN.
                    authenticateVault(savedPin);
                }
            }
        });
        
        projectGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ProjectItem item = displayList.get(position);
                if (new File(item.path).exists()) {
                    Intent intent = new Intent(ProjectListActivity.this, ProjectDashboardActivity.class);
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
        ProjectManager.updateProject(this, item.path, item);
        
        String action = item.isPinned ? "State Changed: Project pinned" : "State Changed: Project unpinned";
        RecentActivityManager.log(this, item.path, "state", action, "\uf08d", false);
        
        loadData();
    }
    
    private void toggleHide(ProjectItem item) {
        item.isHidden = !item.isHidden;
        ProjectManager.updateProject(this, item.path, item);
        
        String action = item.isHidden ? "State Changed: Project hidden" : "State Changed: Project unhidden";
        String icon = item.isHidden ? "\uf023" : "\uf070"; // Lock vs Eye
        
        RecentActivityManager.log(this, item.path, "state", action, icon, false);
        
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
    
    private void authenticateVault(final String savedPin) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&
            fingerprintManager != null &&
            fingerprintManager.isHardwareDetected() && 
            fingerprintManager.hasEnrolledFingerprints()) {
            
            // Reset the counter for this session
            fingerprintFailCount = 0;

            final Dialog bioDialog = new Dialog(this);
            bioDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            bioDialog.setContentView(R.layout.dialog_vault_pin); 
            if (bioDialog.getWindow() != null) bioDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            
            TextView title = bioDialog.findViewById(R.id.tv_pin_title);
            EditText input = bioDialog.findViewById(R.id.et_vault_pin);
            
            title.setText("Touch Fingerprint Sensor");
            input.setVisibility(View.GONE);
            
            // ALLOW THE USER TO DISMISS IT
            bioDialog.setCancelable(true); 
            
            // WHAT HAPPENS IF THEY DISMISS IT?
            bioDialog.setOnCancelListener(new android.content.DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(android.content.DialogInterface dialog) {
                    // 1. Stop the fingerprint sensor
                    if (cancellationSignal != null) {
                        cancellationSignal.cancel();
                    }
                    isAuthenticating = false;
                    
                    // 2. Immediately show the PIN dialog
                    showPinEntryDialog(savedPin);
                }
            });
            
            bioDialog.show();

            cancellationSignal = new CancellationSignal();
            isAuthenticating = true;

            fingerprintManager.authenticate(null, cancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                    isAuthenticating = false;
                    bioDialog.dismiss();
                    unlockVault();
                }

                @Override
                public void onAuthenticationFailed() {
                    fingerprintFailCount++;
                    
                    if (fingerprintFailCount >= 3) {
                        isAuthenticating = false;
                        if (cancellationSignal != null) {
                            cancellationSignal.cancel();
                        }
                        bioDialog.dismiss();
                        AppToast.showError(ProjectListActivity.this, "Too many failed attempts. Please use PIN.");
                    } else {
                        AppToast.showInfo(ProjectListActivity.this, "Fingerprint not recognized. Attempts left: " + (3 - fingerprintFailCount));
                    }
                }
                
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    isAuthenticating = false;
                    if (bioDialog.isShowing()) {
                        bioDialog.dismiss();
                        showPinEntryDialog(savedPin);
                    }
                }
            }, null);
            
        } else {
            showPinEntryDialog(savedPin);
        }
    }
    
    private void unlockVault() {
        showingHidden = true;
        btnShowHidden.setText("\uf09c"); // Unlocked Lock icon
        loadData();
        AppToast.showSuccess(this, "Vault Unlocked");
    }
    
    private void showPinEntryDialog(final String savedPin) {
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setPadding(20, 20, 20, 20);
        input.setTextColor(Color.WHITE);
    
        AppDialog.show(this, "Vault Locked", "Enter 4-digit PIN:", input, "UNLOCK", "CANCEL",
            new AppDialog.OnDialogClickListener() {
                @Override
                public void onClick() {
                    String enteredPin = input.getText().toString().trim();
                    if (enteredPin.equals(savedPin)) {
                        unlockVault();
                    } else {
                        AppToast.showError(ProjectListActivity.this, "Incorrect PIN!");
                    }
                }
            }, null);
    }
    
    private void showSetupPinDialog() {
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setPadding(20, 20, 20, 20);
        input.setTextColor(Color.WHITE);
    
        AppDialog.show(this, "Setup Vault", "Create a 4-digit PIN to protect hidden projects:", input, "SAVE", "CANCEL",
            new AppDialog.OnDialogClickListener() {
                @Override
                public void onClick() {
                    String newPin = input.getText().toString().trim();
                    if (newPin.length() == 4) {
                        getSharedPreferences("DebugMatePrefs", MODE_PRIVATE).edit().putString("vault_pin", newPin).apply();
                        AppToast.showSuccess(ProjectListActivity.this, "Vault PIN setup complete!");
                        unlockVault();
                    } else {
                        AppToast.showError(ProjectListActivity.this, "PIN must be exactly 4 digits.");
                    }
                }
            }, null);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        // Auto-lock the vault when the user leaves the screen
        if (showingHidden) {
            showingHidden = false;
            btnShowHidden.setText("\uf06e"); // Eye icon
            loadData();
        }
        // Cancel any ongoing biometric scan
        if (isAuthenticating && cancellationSignal != null) {
            cancellationSignal.cancel();
            isAuthenticating = false;
        }
    }
}
