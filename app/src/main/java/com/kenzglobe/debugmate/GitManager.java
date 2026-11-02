package com.kenzglobe.debugmate;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;

public class GitManager {
    
    // 1. Initialize a new Git repository
    public static String initRepo(File projectDir) {
        try {
            Git.init().setDirectory(projectDir).call().close();
            return "Success: Git repository initialized.";
        } catch (GitAPIException e) {
            return "Error: " + e.getMessage();
        }
    }

    // 2. Add all files and commit locally
    public static String commitAll(File projectDir, String message) {
        try {
            Git git = Git.open(projectDir);
            // Add all files (equivalent to git add .)
            git.add().addFilepattern(".").call();
            // Also add deleted files (equivalent to git add -u)
            git.add().addFilepattern(".").setUpdate(true).call();
            
            // Commit
            git.commit().setMessage(message).call();
            git.close();
            return "Success: Committed changes locally.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // 3. Push to GitHub (Requires a Personal Access Token, not a password)
    public static String pushToRemote(File projectDir, String remoteUrl, String username, String token) {
        try {
            Git git = Git.open(projectDir);
            
            // Push to the remote branch
            git.push()
               .setRemote(remoteUrl)
               .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, token))
               .call();
            
            git.close();
            return "Success: Pushed to GitHub!";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}