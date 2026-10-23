package com.kenzglobe.debugmate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ListView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Typeface;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity {

    private ListView fileListView;
    private ArrayList<FileItem> fileList;
    private FileListAdapter adapter;
    private TextView sideBarBtn, createFolderBtn, createFileBtn;
    private File currentDirectory; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.parseColor("#8B5CF6"));
            getWindow().getDecorView().setSystemUiVisibility(0);
		}

        if (getActionBar() != null) {
            getActionBar().hide();
        }
        
        sideBarBtn = findViewById(R.id.side_bar_btn);
        createFolderBtn = findViewById(R.id.create_folder_btn);
        createFileBtn = findViewById(R.id.create_file_btn);

		Typeface solidFont = Typeface.createFromAsset(getAssets(), "fonts/fa-solid-900-v5.ttf");
		Typeface brandsRegularFont = Typeface.createFromAsset(getAssets(), "fonts/fa-brands-regular-400-v5.ttf");
				
		sideBarBtn.setTypeface(solidFont);
		sideBarBtn.setText("\uf0c9");
		createFolderBtn.setTypeface(solidFont);
		createFolderBtn.setText("\uf65e");
		createFileBtn.setTypeface(solidFont);
		createFileBtn.setText("\uf067");

        fileListView = findViewById(R.id.file_list);
        fileList = new ArrayList<>();
        adapter = new FileListAdapter(this, fileList);
        fileListView.setAdapter(adapter);
        
        currentDirectory = Environment.getExternalStorageDirectory();
		
		sideBarBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (sideBarBtn.getText().toString().equals("\uf0c9")) {
					Toast.makeText(MainActivity.this, "Side Bar.", Toast.LENGTH_LONG).show();
				} else {
					loadDirectory(currentDirectory.getParentFile());
					if (currentDirectory.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
            			sideBarBtn.setText("\uf0c9");
            		}
				}
			}
		});
		
		createFolderBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
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
		
		              builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		                  @Override
		                  public void onClick(DialogInterface dialog, int which) {
		                      dialog.cancel();
		                  }
		              });
		
		              builder.show();
			}
		});
		
		createFileBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
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
		
		              builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		                  @Override
		                  public void onClick(DialogInterface dialog, int which) {
		                      dialog.cancel();
		                  }
		              });
		
		              builder.show();
			}
		});

        fileListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
			    FileItem item = fileList.get(position);
			    if (item.isDir) {
			        loadDirectory(item.file);
			        sideBarBtn.setText("\uf060");
			    } else {
			        String filePath = item.file.getAbsolutePath();
			        String ext = item.ext.toLowerCase();
			        
			        boolean textFiles = ext.equals("py") || ext.equals("java") || ext.equals("xml") || ext.equals("txt") || ext.equals("md") || ext.equals("js") || ext.equals("json") || ext.equals("html") || ext.equals("css");
			        
			        if (textFiles) {
			            android.content.Intent intent = new android.content.Intent(MainActivity.this, EditorActivity.class);
			            intent.putExtra("file_path", filePath);
			            startActivity(intent);
			        } else {
			            Toast.makeText(MainActivity.this, "Feature not available yet.", Toast.LENGTH_LONG).show();
			        }
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

    private void loadDirectory(File dir) {
        if (dir == null) return;
        
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
            
            if (currentDirectory.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
            	sideBarBtn.setText("\uf0c9");
            }
            
        } else {
            super.onBackPressed();
        }
    }
}