package com.kenzglobe.debugmate;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import java.io.File;
import java.util.ArrayList;

public class GitManager {

    public static String cloneRepo(String remoteUrl, String destinationPath, String username, String token) {
        try {
            if (!remoteUrl.endsWith(".git")) {
                remoteUrl += ".git";
            }

            Git.cloneRepository()
               .setURI(remoteUrl)
               .setDirectory(new File(destinationPath))
               .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, token))
               .call();
               
            return "Success: Repository cloned successfully!";
            
        } catch (Exception e) {
            // Catch the generic exception to keep the AIDE compiler happy
            // Then check the type of exception using instanceof
            
            if (e instanceof org.eclipse.jgit.errors.TransportException) {
                String msg = e.getMessage();
                if (msg != null && msg.contains("git-upload-pack")) {
                    return "Error: Network failed or Repo not found. Check your internet and URL.";
                } else if (msg != null && msg.contains("not authorized")) {
                    return "Error: Authentication failed. Check Username and PAT.";
                }
                return "Error: Connection failed. Please check your internet.";
                
            } else if (e instanceof java.net.UnknownHostException) {
                return "Error: No internet connection. Please turn on your data or Wi-Fi.";
                
            } else {
                // Catch any other unexpected errors
                String errMsg = e.getMessage();
                return "Error: " + (errMsg != null ? errMsg : "Unknown error occurred.");
            }
        }
    }

    // 1. Get Changed Files
    public static ArrayList<GitFileItem> getStatus(File projectDir) {
        ArrayList<GitFileItem> changedFiles = new ArrayList<>();
        try {
            Git git = Git.open(projectDir);
            Status status = git.status().call();
            
            // Helper to extract just the directory path
            // e.g., "app/src/MainActivity.java" becomes "app/src"
            for (String s : status.getModified()) {
                changedFiles.add(new GitFileItem(getName(s), getDir(s), "M"));
            }
            for (String s : status.getUntracked()) {
                changedFiles.add(new GitFileItem(getName(s), getDir(s), "U"));
            }
            for (String s : status.getAdded()) {
                changedFiles.add(new GitFileItem(getName(s), getDir(s), "A"));
            }
            for (String s : status.getRemoved()) {
                changedFiles.add(new GitFileItem(getName(s), getDir(s), "D"));
            }
            
            git.close();
        } catch (Exception e) {
            // If there's an error, just return an empty list
        }
        return changedFiles;
    }
    
    // Helper methods to split the path
    private static String getName(String path) {
        if (path.contains("/")) {
            return path.substring(path.lastIndexOf("/") + 1);
        }
        return path;
    }
    
    private static String getDir(String path) {
        if (path.contains("/")) {
            return path.substring(0, path.lastIndexOf("/"));
        }
        return ""; // Root directory
    }

    // 2. Stage (Add) all files
    public static String stageAll(File projectDir) {
        try {
            Git git = Git.open(projectDir);
            git.add().addFilepattern(".").call();
            git.add().addFilepattern(".").setUpdate(true).call(); // Includes deleted files
            git.close();
            return "Success: Added all changes.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // 3. Commit changes
    public static String commit(File projectDir, String message) {
        try {
            Git git = Git.open(projectDir);
            git.commit().setMessage(message).call();
            git.close();
            return "Success: Committed locally.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // 4. Push to GitHub
    public static String push(File projectDir, String username, String token) {
        try {
            Git git = Git.open(projectDir);
            git.push()
               .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, token))
               .call();
            git.close();
            return "Success: Pushed to GitHub!";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}