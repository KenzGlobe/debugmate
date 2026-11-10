package com.kenzglobe.debugmate;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import java.io.File;

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
}