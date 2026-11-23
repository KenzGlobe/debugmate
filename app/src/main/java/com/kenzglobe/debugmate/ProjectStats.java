package com.kenzglobe.debugmate;

public class ProjectStats {
    public int fileCount;
    public int folderCount;
    public long totalSize; // in bytes
    public long lastModified; // timestamp

    public ProjectStats() {
        this.fileCount = 0;
        this.folderCount = 0;
        this.totalSize = 0;
        this.lastModified = 0;
    }
}