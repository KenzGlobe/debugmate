package com.kenzglobe.debugmate;

public class GitFileItem {
    public String fileName;
    public String filePath;
    public String status; // "M" (Modified), "U" (Untracked), "D" (Deleted), "A" (Added)

    public GitFileItem(String fileName, String filePath, String status) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.status = status;
    }
}