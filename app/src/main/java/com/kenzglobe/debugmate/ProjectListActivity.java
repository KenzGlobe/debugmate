package com.kenzglobe.debugmate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;

public class ProjectListActivity extends Activity {

    private ListView projectListView;
    private TextView emptyText;
    private ProjectAdapter adapter;
    private ArrayList<ProjectItem> projectList;

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

        TextView btnClose = findViewById(R.id.btn_close_projects);
        btnClose.setTypeface(solidFont);
        btnClose.setText("\uf00d");

        projectListView = findViewById(R.id.project_list);
        emptyText = findViewById(R.id.empty_projects_text);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        projectListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ProjectItem item = projectList.get(position);
                File folder = new File(item.path);
                
                if (folder.exists()) {
                    // Open MainActivity in normal mode pointing to this folder
                    Intent intent = new Intent(ProjectListActivity.this, MainActivity.class);
                    intent.putExtra("START_DIRECTORY", item.path);
                    startActivity(intent);
                } else {
                    Toast.makeText(ProjectListActivity.this, "Project folder not found on disk.", Toast.LENGTH_LONG).show();
                }
            }
        });

        projectListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showOptionsDialog(projectList.get(position));
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
        
        if (projectList.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            projectListView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            projectListView.setVisibility(View.VISIBLE);
            adapter = new ProjectAdapter(this, projectList);
            projectListView.setAdapter(adapter);
        }
    }

    private void showOptionsDialog(final ProjectItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(item.name);
        
        final String[] options = {"Open Project", "Delete Project", "Remove from List"};
        
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    // Open
                    Intent intent = new Intent(ProjectListActivity.this, MainActivity.class);
                    intent.putExtra("START_DIRECTORY", item.path);
                    startActivity(intent);
                } else if (which == 1) {
                    // Delete from disk and list
                    deleteProjectFolder(new File(item.path));
                    ProjectManager.removeProject(ProjectListActivity.this, item.path);
                    loadData();
                } else if (which == 2) {
                    // Remove from list only
                    ProjectManager.removeProject(ProjectListActivity.this, item.path);
                    loadData();
                }
            }
        });
        builder.show();
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
}