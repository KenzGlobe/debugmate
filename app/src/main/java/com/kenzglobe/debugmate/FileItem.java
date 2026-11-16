package com.kenzglobe.debugmate;

import java.io.File;
import android.support.v4.provider.DocumentFile;

public class FileItem {
    public File file;
    public DocumentFile docFile;
    public String name;
    public boolean isDir;
    public String ext;
    public boolean isSelected = false;
    public boolean isSaf = false;
    public long size = 0; // Cache the size!

    // File Constructor
    public FileItem(File file) {
        this.file = file;
        this.name = file.getName();
        this.isDir = file.isDirectory();
        this.size = isDir ? 0 : file.length(); // Cache size
        if (!isDir && name.contains(".")) {
            ext = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
        } else {
            ext = "";
        }
    }

    // SAF Constructor (Now takes cached data to prevent lag)
    public FileItem(DocumentFile docFile, String name, boolean isDir, long size) {
        this.docFile = docFile;
        this.name = name != null ? name : "Unknown";
        this.isDir = isDir;
        this.size = size;
        this.isSaf = true;
        if (!isDir && name.contains(".")) {
            ext = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
        } else {
            ext = "";
        }
    }
}