package com.kenzglobe.debugmate;

public class ActivityItem {
    public String type;
    public String message;
    public long timestamp;
    public String iconUnicode;
    public boolean isBrandIcon;

    public ActivityItem(String type, String message, long timestamp, String iconUnicode, boolean isBrandIcon) {
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
        this.iconUnicode = iconUnicode;
        this.isBrandIcon = isBrandIcon;
    }
}