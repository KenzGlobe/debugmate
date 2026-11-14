package com.kenzglobe.debugmate;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageView;
import java.util.List;

public class FileListAdapter extends ArrayAdapter<FileItem> {
    private Typeface customFont, solidFont, brandsRegularFont;

    private boolean isSelectionMode = false;

    public void setSelectionMode(boolean selectionMode) {
        this.isSelectionMode = selectionMode;
        notifyDataSetChanged();
    }

    public FileListAdapter(Context ctx, List<FileItem> list){
        super(ctx, R.layout.item_file, list);
        
        try {
            this.customFont = Typeface.createFromAsset(ctx.getAssets(), "fonts/SpaceMono-Regular.ttf");
            this.solidFont = Typeface.createFromAsset(ctx.getAssets(), "fonts/fa-solid-900-v5.ttf");
            this.brandsRegularFont = Typeface.createFromAsset(ctx.getAssets(), "fonts/fa-brands-regular-400-v5.ttf");
        } catch (Exception e) {
            this.customFont = null;
            this.solidFont = null;
            this.brandsRegularFont = null;
        }
    }

    static class ViewHolder {
        ImageView marker;
        TextView icon, name, info, size;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        ViewHolder h;
        
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_file, parent, false);
            h = new ViewHolder();
            h.marker = convertView.findViewById(R.id.marker);
            h.icon = convertView.findViewById(R.id.icon);
            h.name = convertView.findViewById(R.id.file_name);
            h.info = convertView.findViewById(R.id.info);
            h.size = convertView.findViewById(R.id.file_size);
            convertView.setTag(h);
        } else {
            h = (ViewHolder) convertView.getTag();
        }

        FileItem item = getItem(pos);
        h.name.setText(item.name);
 
        if (isSelectionMode) {
            h.marker.setVisibility(View.VISIBLE);
            h.marker.setSelected(item.isSelected);
        } else {
            h.marker.setVisibility(View.GONE);
        }
        
        if (customFont != null) {
            h.name.setTypeface(customFont);
            h.info.setTypeface(customFont);
            h.size.setTypeface(customFont);
        }

        if(item.isDir){
            if (solidFont != null) {
                h.icon.setTypeface(solidFont);
                h.size.setTypeface(solidFont);
            }
            h.icon.setText("\uf07b");
            h.icon.setTextColor(Color.parseColor("#8B5CF6"));
            h.info.setText("Folder");
            h.size.setText("\uf054");
            h.size.setTextColor(Color.parseColor("#8B5CF6"));
        } else {
            h.info.setText(item.ext.toUpperCase() + " file");
            h.size.setText(formatSize(item.file.length()));
            
            Typeface iconFont = solidFont;
            String iconText = "\uf15b";
            int iconColor = Color.parseColor("#999999");
            
            switch(item.ext) {
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
                case "sql":
                    iconText = "\uf1c0";
                    iconColor = Color.parseColor("#F492FF");
                    break;
            }
            
            if (iconFont != null) {
                h.icon.setTypeface(iconFont);
            }
            h.icon.setText(iconText);
            h.icon.setTextColor(iconColor);
        }
        return convertView;
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024*1024) return (bytes/1024) + " KB";
        return String.format("%.1f MB", bytes/1024f/1024f);
    }
}
