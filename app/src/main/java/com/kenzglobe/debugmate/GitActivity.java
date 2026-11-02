package com.kenzglobe.debugmate;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;

public class GitActivity extends Activity {

    private TextView statusText;
    private EditText commitMessageInput, remoteUrlInput, usernameInput, tokenInput;
    private File projectDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_git);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#8B5CF6"));
        }
        if (getActionBar() != null) getActionBar().hide();

        statusText = findViewById(R.id.git_status_text);
        commitMessageInput = findViewById(R.id.git_commit_message);
        remoteUrlInput = findViewById(R.id.git_remote_url);
        usernameInput = findViewById(R.id.git_username);
        tokenInput = findViewById(R.id.git_token);

        String path = getIntent().getStringExtra("project_path");
        if (path != null) {
            projectDir = new File(path);
            statusText.setText("Target: " + projectDir.getName());
        } else {
            statusText.setText("Error: No project folder provided.");
            statusText.setTextColor(Color.parseColor("#F85149"));
        }

        findViewById(R.id.btn_git_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.btn_git_init).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (projectDir == null) return;
                String res = GitManager.initRepo(projectDir);
                updateStatus(res);
            }
        });

        findViewById(R.id.btn_git_commit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (projectDir == null) return;
                String msg = commitMessageInput.getText().toString().trim();
                if (msg.isEmpty()) {
                    Toast.makeText(GitActivity.this, "Enter a commit message", Toast.LENGTH_SHORT).show();
                    return;
                }
                String res = GitManager.commitAll(projectDir, msg);
                updateStatus(res);
            }
        });

        findViewById(R.id.btn_git_push).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (projectDir == null) return;
                final String url = remoteUrlInput.getText().toString().trim();
                final String user = usernameInput.getText().toString().trim();
                final String token = tokenInput.getText().toString().trim();
                
                if (url.isEmpty() || user.isEmpty() || token.isEmpty()) {
                    Toast.makeText(GitActivity.this, "Fill all push credentials", Toast.LENGTH_SHORT).show();
                    return;
                }

                statusText.setText("Pushing... Please wait.");
                statusText.setTextColor(Color.parseColor("#58A6FF"));

                // Run in background because network calls freeze the UI
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final String res = GitManager.pushToRemote(projectDir, url, user, token);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateStatus(res);
                            }
                        });
                    }
                }).start();
            }
        });
    }

    private void updateStatus(String message) {
        statusText.setText(message);
        if (message.startsWith("Success")) {
            statusText.setTextColor(Color.parseColor("#3FB950")); // Green
        } else {
            statusText.setTextColor(Color.parseColor("#F85149")); // Red
        }
    }
}
