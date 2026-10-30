package com.kenzglobe.debugmate;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class ProjectManager {

    private static final String FILE_NAME = "debugmate_projects.json";

    public static void saveProject(Context context, ProjectItem newProject) {
        ArrayList<ProjectItem> projects = loadProjects(context);
        
        // Remove if a project with the same path already exists
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).path.equals(newProject.path)) {
                projects.remove(i);
                break;
            }
        }
        
        projects.add(newProject);
        writeToFile(context, projects);
    }

    public static void removeProject(Context context, String path) {
        ArrayList<ProjectItem> projects = loadProjects(context);
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).path.equals(path)) {
                projects.remove(i);
                break;
            }
        }
        writeToFile(context, projects);
    }

    public static ArrayList<ProjectItem> loadProjects(Context context) {
        ArrayList<ProjectItem> list = new ArrayList<>();
        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            if (!file.exists()) return list;

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
                list.add(new ProjectItem(
                        obj.getString("name"),
                        obj.getString("type"),
                        obj.getString("path"),
                        obj.getString("storageType")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private static void writeToFile(Context context, ArrayList<ProjectItem> projects) {
        try {
            JSONArray array = new JSONArray();
            for (ProjectItem p : projects) {
                JSONObject obj = new JSONObject();
                obj.put("name", p.name);
                obj.put("type", p.type);
                obj.put("path", p.path);
                obj.put("storageType", p.storageType);
                array.put(obj);
            }

            File file = new File(context.getFilesDir(), FILE_NAME);
            FileWriter writer = new FileWriter(file);
            writer.write(array.toString());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}