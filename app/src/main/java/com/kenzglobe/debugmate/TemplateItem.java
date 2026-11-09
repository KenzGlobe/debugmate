package com.kenzglobe.debugmate;

public class TemplateItem {
    public String name;
    public String description;
    public String type; // "web", "python", "java", "android"
    public String icon; // FontAwesome unicode

    public TemplateItem(String name, String description, String type, String icon) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.icon = icon;
    }
}