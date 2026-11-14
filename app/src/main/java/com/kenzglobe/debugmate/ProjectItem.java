package com.kenzglobe.debugmate;

public class ProjectItem {
    public String name;
    public String type;
    public String path;        
    public String storageType; 
    public boolean isHidden;
    public boolean isPinned;

    public ProjectItem(String name, String type, String path, String storageType) {
        this.name = name;
        this.type = type;
        this.path = path;
        this.storageType = storageType;
        this.isHidden = false;
        this.isPinned = false;
    }
}