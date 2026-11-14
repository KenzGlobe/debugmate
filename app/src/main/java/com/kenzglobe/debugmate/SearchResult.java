package com.kenzglobe.debugmate;

public class SearchResult {
    public String fileName;
    public String filePath;
    public String lineText;
    public int lineNumber;

    public SearchResult(String fileName, String filePath, String lineText, int lineNumber) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.lineText = lineText;
        this.lineNumber = lineNumber;
    }
}