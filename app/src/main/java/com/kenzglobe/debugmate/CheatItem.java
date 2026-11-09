package com.kenzglobe.debugmate;

import java.util.ArrayList;

public class CheatItem {
    public String title;
    public ArrayList<CodeBlock> codeBlocks;
    public String explanationHtml;
    public boolean isExpanded;

    public CheatItem(String title, ArrayList<CodeBlock> codeBlocks, String explanationHtml) {
        this.title = title;
        this.codeBlocks = codeBlocks;
        this.explanationHtml = explanationHtml;
        this.isExpanded = false;
    }
}