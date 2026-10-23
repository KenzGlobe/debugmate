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
    private TextView sideBarBtn, createFolderBtn, createFileBtn, terminalBtn;
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
        terminalBtn = findViewById(R.id.terminal_btn);

		Typeface solidFont = Typeface.createFromAsset(getAssets(), "fonts/fa-solid-900-v5.ttf");
		Typeface brandsRegularFont = Typeface.createFromAsset(getAssets(), "fonts/fa-brands-regular-400-v5.ttf");
				
		sideBarBtn.setTypeface(solidFont);
		sideBarBtn.setText("\uf0c9");
		createFolderBtn.setTypeface(solidFont);
		createFolderBtn.setText("\uf65e");
		createFileBtn.setTypeface(solidFont);
		createFileBtn.setText("\uf067");
		terminalBtn.setTypeface(solidFont);
		terminalBtn.setText("\uf120");

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
		
		terminalBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				android.content.Intent intent = new android.content.Intent(MainActivity.this, TerminalActivity.class);
				startActivity(intent);
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
					    // SAVE TO RECENT FILES
					    android.content.SharedPreferences prefs = getSharedPreferences("DebugMatePrefs", MODE_PRIVATE);
					    String recentStr = prefs.getString("recent_files", "");
					    ArrayList<String> recentList = new ArrayList<>();
					    
					    // Split existing string by newline, if it's not empty
					    if (!recentStr.isEmpty()) {
					        String[] arr = recentStr.split("\n");
					        for (String s : arr) recentList.add(s);
					    }
					    
					    // Remove if it already exists (so we move it to the front)
					    while (recentList.contains(filePath)) {
					        recentList.remove(filePath);
					    }
					    
					    // Add to the top of the list
					    recentList.add(0, filePath);
					    
					    // Limit to 20 files
					    while (recentList.size() > 20) {
					        recentList.remove(recentList.size() - 1);
					    }
					    
					    // Join back to string and save
					    StringBuilder sb = new StringBuilder();
					    for (String s : recentList) {
					        sb.append(s).append("\n");
					    }
					    prefs.edit().putString("recent_files", sb.toString().trim()).apply();
					
					    // Launch Editor
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
		        final FileItem item = fileList.get(position);
		
		        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		        builder.setTitle("Options for " + item.name);
		        
		        // This creates a popup menu with two options
		        builder.setItems(new CharSequence[]{"Rename", "Delete"}, new DialogInterface.OnClickListener() {
		            @Override
		            public void onClick(DialogInterface dialog, int which) {
		                if (which == 0) {
		                    // User tapped "Rename"
		                    showRenameDialog(item);
		                } else if (which == 1) {
		                    // User tapped "Delete"
		                    showDeleteDialog(item);
		                }
		            }
		        });
		
		        builder.show();
		        return true; 
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
    
    private boolean deleteRecursive(File file) {
	    if (file.isDirectory()) {
	        File[] children = file.listFiles();
	
	        if (children == null) {
	            return false;
	        }
	
	        for (File child : children) {
	            if (!deleteRecursive(child)) {
	                return false;
	            }
	        }
	    }
	
	    return file.delete();
	}
	
	private void showRenameDialog(final FileItem item) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
	    builder.setTitle("Rename");
	
	    final EditText input = new EditText(MainActivity.this);
	    input.setText(item.name); // Pre-fill with the current name
	    input.setSelection(input.getText().length()); // Put the cursor at the very end
	    input.setPadding(40, 40, 40, 40); 
	    builder.setView(input);
	
	    builder.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            String newName = input.getText().toString().trim();
	            
	            if (newName.isEmpty()) {
	                Toast.makeText(MainActivity.this, "Name cannot be empty.", Toast.LENGTH_SHORT).show();
	                return;
	            }
	
	            // Don't do anything if the name hasn't changed
	            if (newName.equals(item.name)) {
	                return;
	            }
	
	            // Create the new file object
	            File newFile = new File(currentDirectory, newName);
	
	            // renameTo() returns true if successful
	            if (item.file.renameTo(newFile)) {
	                Toast.makeText(MainActivity.this, "Renamed successfully! ✏️", Toast.LENGTH_SHORT).show();
	                loadDirectory(currentDirectory); 
	            } else {
	                Toast.makeText(MainActivity.this, "Rename failed. File might already exist.", Toast.LENGTH_LONG).show();
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
	
	private void showDeleteDialog(final FileItem item) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
	    builder.setTitle("Delete " + item.name + "?");
	    builder.setMessage("Are you sure you want to delete this?");
	
	    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            if (deleteRecursive(item.file)) {
	                Toast.makeText(MainActivity.this, "Deleted successfully! 🗑️", Toast.LENGTH_SHORT).show();
	                loadDirectory(currentDirectory);
	            } else {
	                Toast.makeText(MainActivity.this, "Failed to delete.", Toast.LENGTH_LONG).show();
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