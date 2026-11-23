package com.kenzglobe.debugmate;

public class ProjectConfig {
    public String projectName;
    public String projectType;
    public String packageName; // Used for Android, Java, etc.

    public ProjectConfig(String projectName, String projectType) {
        this.projectName = projectName;
        this.projectType = projectType;
    }
}