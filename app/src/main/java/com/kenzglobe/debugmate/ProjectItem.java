package com.kenzglobe.debugmate;

public class ProjectItem {
    public String name;
    public String type;
    public String path;        // The absolute file path or SAF URI
    public String storageType; // "Internal", "App Files", "App Cache", "SAF"

    public ProjectItem(String name, String type, String path, String storageType) {
        this.name = name;
        this.type = type;
        this.path = path;
        this.storageType = storageType;
    }
}