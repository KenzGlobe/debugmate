package com.kenzglobe.debugmate;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class RecentActivityManager {

    private static final String FILE_NAME = ".debugmate_activity.json";
    private static final int MAX_ITEMS = 20; // Keep the last 20 activities

    public static void log(Context context, String projectPath, String type, String message, String iconUnicode, boolean isBrandIcon) {
        ArrayList<ActivityItem> activities = getActivities(projectPath);
        
        // Add new activity to the top
        activities.add(0, new ActivityItem(type, message, System.currentTimeMillis(), iconUnicode, isBrandIcon));
        
        // Trim to MAX_ITEMS
        while (activities.size() > MAX_ITEMS) {
            activities.remove(activities.size() - 1);
        }
        
        saveActivities(projectPath, activities);
    }
    
    public static void logFileModified(Context context, String projectPath, String fileName) {
        ArrayList<ActivityItem> activities = getActivities(projectPath);
        long now = System.currentTimeMillis();
        
        if (!activities.isEmpty()) {
            ActivityItem lastItem = activities.get(0);
            if (lastItem.type.equals("modify") && lastItem.message.contains(fileName)) {
                if (now - lastItem.timestamp < 300000) { 
                    return; 
                }
            }
        }
        
        log(context, projectPath, "modify", "File modified: " + fileName, "\uf044", false);
    }

    public static ArrayList<ActivityItem> getActivities(String projectPath) {
        ArrayList<ActivityItem> list = new ArrayList<>();
        File file = new File(projectPath, FILE_NAME);
        if (!file.exists()) return list;
        
        try {
            FileReader reader = new FileReader(file);
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[1024];
            int length;
            while ((length = reader.read(buffer)) > 0) {
                sb.append(buffer, 0, length);
            }
            reader.close();
        
            JSONArray array = new JSONArray(sb.toString());
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                list.add(new ActivityItem(
                        obj.getString("type"),
                        obj.getString("message"),
                        obj.getLong("timestamp"),
                        obj.getString("icon"),
                        obj.getBoolean("isBrand")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private static void saveActivities(String projectPath, ArrayList<ActivityItem> list) {
        try {
            JSONArray array = new JSONArray();
            for (ActivityItem item : list) {
                JSONObject obj = new JSONObject();
                obj.put("type", item.type);
                obj.put("message", item.message);
                obj.put("timestamp", item.timestamp);
                obj.put("icon", item.iconUnicode);
                obj.put("isBrand", item.isBrandIcon);
                array.put(obj);
            }
            FileWriter writer = new FileWriter(new File(projectPath, FILE_NAME));
            writer.write(array.toString());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
