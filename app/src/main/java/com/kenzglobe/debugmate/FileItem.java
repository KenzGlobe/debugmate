package com.kenzglobe.debugmate;

import java.io.File;

public class FileItem {
    public File file;
    public String name;
    public boolean isDir;
    public String ext;

    public FileItem(File file){
        this.file = file;
        this.name = file.getName();
        this.isDir = file.isDirectory();
        if(!isDir && name.contains(".")){
            ext = name.substring(name.lastIndexOf(".")+1).toLowerCase();
        } else {
            ext = "";
        }
    }
}