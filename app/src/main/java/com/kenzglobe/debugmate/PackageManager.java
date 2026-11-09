package com.kenzglobe.debugmate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PackageManager {
    private TerminalActivity terminal;
    private File usrDir;
    private File binDir;

    public PackageManager(TerminalActivity terminal) {
        this.terminal = terminal;
        this.usrDir = new File(terminal.getFilesDir(), "usr");
        this.binDir = new File(usrDir, "bin");
        
        if (!binDir.exists()) {
            binDir.mkdirs();
        }
    }

    public File getBinDir() {
        return binDir;
    }

    public void installPackage(final String packageName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String downloadUrl = "";
                    
                    if (packageName.equals("busybox")) {
                        // PIE-compliant static Android ARM64 BusyBox
                        downloadUrl = "https://raw.githubusercontent.com/nelySLB/busybox-ndk/master/busybox-arm64";
                    } else {
                        terminal.printOutput("Package '" + packageName + "' not found in repository.");
                        return;
                    }

                    terminal.printOutput("Fetching " + packageName + " from network...");
                    URL url = new URL(downloadUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0"); 
                    connection.connect();

                    if (connection.getResponseCode() == 200) {
                        File outputFile = new File(binDir, packageName);
                        FileOutputStream fos = new FileOutputStream(outputFile);
                        InputStream is = connection.getInputStream();
                        
                        byte[] buffer = new byte[4096];
                        int length;
                        while ((length = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, length);
                        }
                        
                        fos.close();
                        is.close();
                        
                        outputFile.setExecutable(true);
                        
                        terminal.printOutput("Successfully installed " + packageName + " (" + outputFile.length() + " bytes)");
                        
                    } else {
                        terminal.printOutput("Download failed. HTTP Code: " + connection.getResponseCode());
                    }
                    connection.disconnect();
                    
                } catch (Exception e) {
                    terminal.printOutput("Error downloading package: " + e.getMessage());
                }
            }
        }).start();
    }
}