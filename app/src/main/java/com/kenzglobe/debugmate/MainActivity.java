package com.kenzglobe.debugmate;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ListView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class MainActivity extends Activity {

    private ListView fileListView;
    private ArrayList<FileItem> fileList;
    private FileListAdapter adapter;
    
    // Keep track of where we are right now!
    private File currentDirectory; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (getActionBar() != null) {
            getActionBar().hide();
        }

        fileListView = findViewById(R.id.file_list);
        fileList = new ArrayList<>();
        adapter = new FileListAdapter(this, fileList);
        fileListView.setAdapter(adapter);
        
        // Start at the root directory
        currentDirectory = Environment.getExternalStorageDirectory();

        fileListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                FileItem item = fileList.get(position);
                if (item.isDir) {
                    // If it's a folder, load it!
                    loadDirectory(item.file);
                } else {
                    Toast.makeText(MainActivity.this, "Opening file: " + item.name, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Check for Permissions
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } else {
                loadDirectory(currentDirectory);
            }
        } else {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                loadDirectory(currentDirectory);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadDirectory(currentDirectory);
            } else {
                Toast.makeText(this, "Permission denied. Cannot read files.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // We only need ONE method now. It takes the directory we want to open.
    private void loadDirectory(File dir) {
        if (dir == null) return;
        
        currentDirectory = dir; // Update our current location
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
            for (File file : files) {
                fileList.add(new FileItem(file));
            }
            adapter.notifyDataSetChanged();
        } else {
             Toast.makeText(this, "Cannot read directory: " + dir.getName(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentDirectory != null && !currentDirectory.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
            loadDirectory(currentDirectory.getParentFile());
        } else {
            super.onBackPressed();
        }
    }
}