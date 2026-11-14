package com.kenzglobe.debugmate;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.io.File;
import java.util.List;

public class EditorTreeAdapter extends BaseAdapter {
    private Context context;
    private List<File> files;
    private File currentFile;
    private Typeface appFont, solidFont, brandsRegularFont;

    public EditorTreeAdapter(Context context, List<File> files, File currentFile) {
        this.context = context;
        this.files = files;
        this.currentFile = currentFile;
        this.appFont = Typeface.createFromAsset(context.getAssets(), "fonts/SpaceMono-Regular.ttf");
        this.solidFont = Typeface.createFromAsset(context.getAssets(), "fonts/fa-solid-900-v5.ttf");
        this.brandsRegularFont = Typeface.createFromAsset(context.getAssets(), "fonts/fa-brands-regular-400-v5.ttf");
    }

    @Override
    public int getCount() { return files.size(); }
    
    @Override
    public File getItem(int position) { return files.get(position); }
    
    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            // Pointing to the NEW layout
            convertView = LayoutInflater.from(context).inflate(R.layout.item_editor_tree, parent, false);
        }

        File file = getItem(position);

        // Using the NEW IDs
        TextView icon = convertView.findViewById(R.id.tree_icon);
        TextView name = convertView.findViewById(R.id.tree_name);
        TextView info = convertView.findViewById(R.id.tree_info);
        TextView size = convertView.findViewById(R.id.tree_size);

        name.setTypeface(appFont);
        info.setTypeface(appFont);
        name.setText(file.getName());
        
        // Highlight the currently open file
        if (currentFile != null && currentFile.getAbsolutePath().equals(file.getAbsolutePath())) {
            convertView.setBackgroundColor(Color.parseColor("#3A3A5A"));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        if (file.isDirectory()) {
            icon.setTypeface(solidFont);
            icon.setText("\uf07b");
            icon.setTextColor(Color.parseColor("#8B5CF6"));
            info.setText("Folder");
            size.setTypeface(solidFont);
            size.setText("\uf054");
            size.setTextColor(Color.parseColor("#8B5CF6"));
        } else {
            String fileName = file.getName();
            String ext = "";
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex != -1) {
                ext = fileName.substring(dotIndex + 1).toUpperCase();
            }

            if (ext.isEmpty()) {
                info.setText("File");
            } else {
                info.setText(ext + " file");
            }
            
            size.setText(formatSize(file.length()));
            size.setTextColor(Color.parseColor("#888888"));
            
            Typeface iconFont = solidFont;
            String iconText = "\uf15b";
            int iconColor = Color.parseColor("#999999");
            
            switch(ext.toLowerCase()) {
                case "txt":
                    iconText = "\uf15c";
                    iconColor = Color.parseColor("#FFDC5F");
                    break;
                case "apk":
                    iconFont = brandsRegularFont;
                    iconText = "\uf17b";
                    iconColor = Color.parseColor("#43FF8D");
                    break;
                case "aac":
                case "mp3":
                case "wav":
                case "ogg":
                case "m4a":
                    iconText = "\uf001";
                    iconColor = Color.parseColor("#698CB7");
                    break;
                case "mp4":
                case "avi":
                    iconText = "\uf03d";
                    iconColor = Color.parseColor("#00B79E");
                    break;
                case "mkv":
                    iconText = "\uf008";
                    iconColor = Color.parseColor("#00B79E");
                    break;
                case "svg":
                case "png":
                case "jpg":
                case "jpeg":
                case "gif":
                case "webp":
                    iconText = "\uf03e";
                    iconColor = Color.parseColor("#72F3FF");
                    break;
                case "pdf":
                    iconText = "\uf1c1";
                    iconColor = Color.parseColor("#FF5F5B");
                    break;
                case "doc":
                case "docx":
                    iconText = "\uf1c2";
                    iconColor = Color.parseColor("#40A3FF");
                    break;
                case "xls":
                case "xlsx":
                    iconText = "\uf1c3";
                    iconColor = Color.parseColor("#CFFF50");
                    break;
                case "ppt":
                    iconText = "\uf1c4";
                    iconColor = Color.parseColor("#FFB074");
                    break;
                case "pptx":
                    iconText = "\uf1c4";
                    iconColor = Color.parseColor("#FF7E46");
                    break;
                case "zip":
                case "rar":
                case "7z":
                case "tar":
                case "gz":
                    iconText = "\uf1c6";
                    iconColor = Color.parseColor("#9AD39F");
                    break;
                case "py":
                    iconFont = brandsRegularFont;
                    iconText = "\uf3e2";
                    iconColor = Color.parseColor("#3776AB");
                    break;
                case "js":
                    iconFont = brandsRegularFont;
                    iconText = "\uf3b8";
                    iconColor = Color.parseColor("#FFFF00");
                    break;
                case "java":
                    iconFont = brandsRegularFont;
                    iconText = "\uf4e4";
                    iconColor = Color.parseColor("#ED8B00");
                    break;
                case "kt":
                    iconText = "\uf121";
                    iconColor = Color.parseColor("#7F52FF");
                    break;
                case "ts":
                    iconText = "\uf121";
                    iconColor = Color.parseColor("#3178C6");
                    break;
                case "htm":
                case "html":
                    iconFont = brandsRegularFont;
                    iconText = "\uf13b";
                    iconColor = Color.parseColor("#FF8800");
                    break;
                case "css":
                    iconFont = brandsRegularFont;
                    iconText = "\uf13c";
                    iconColor = Color.parseColor("#1572B6");
                    break;
                case "xml":
                    iconText = "\uf121";
                    iconColor = Color.parseColor("#E37933");
                    break;
                case "json":
                    iconText = "\uf121";
                    iconColor = Color.parseColor("#F0A000");
                    break;
                case "sh":
                case "bat":
                case "h":
                    iconText = "\uf1c9";
                    iconColor = Color.parseColor("#8B5CF6");
                    break;
                case "c":
                case "php":
                    iconText = "\uf121";
                    iconColor = Color.parseColor("#8B5CF6");
                    break;
                case "cpp":
                    iconText = "\uf121";
                    iconColor = Color.parseColor("#00599C");
                    break;
                case "rb":
                    iconText = "\uf121";
                    iconColor = Color.parseColor("#CC342D");
                    break;
                case "go":
                    iconText = "\uf121";
                    iconColor = Color.parseColor("#00ADD8");
                    break;
                case "rs":
                    iconText = "\uf121";
                    iconColor = Color.parseColor("#DEA584");
                    break;
                case "swift":
                    iconFont = brandsRegularFont;
                    iconText = "\uf8e1";
                    iconColor = Color.parseColor("#F05138");
                    break;
                case "md":
                    iconFont = brandsRegularFont;
                    iconText = "\uf60f";
                    iconColor = Color.parseColor("#608080");
                    break;
                case "gradle":
                    iconText = "\uf7d9";
                    iconColor = Color.parseColor("#608080");
                    break;
            }
            
            if (iconFont != null) {
                icon.setTypeface(iconFont);
            }
            icon.setText(iconText);
            icon.setTextColor(iconColor);
        }

        return convertView;
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024*1024) return (bytes/1024) + " KB";
        return String.format("%.1f MB", bytes/1024f/1024f);
    }
}
