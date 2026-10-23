package com.kenzglobe.debugmate;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class FileListAdapter extends ArrayAdapter<FileItem> {

    // Load the font ONCE here, so it doesn't lag the app
    private Typeface font;

    public FileListAdapter(Context ctx, List<FileItem> list){
        super(ctx, R.layout.item_file, list);
        // Initialize the font here
        try {
            this.font = Typeface.createFromAsset(ctx.getAssets(), "fonts/fa-solid-900-v5.ttf");
        } catch (Exception e) {
            this.font = null;
        }
    }

    static class ViewHolder {
        TextView icon, name, info, size;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        ViewHolder h;
        
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_file, parent, false);
            h = new ViewHolder();
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

        if(item.isDir){
            if (font != null) {
                h.icon.setTypeface(font);
            }
            h.icon.setText("\uf07b");
            h.icon.setTextColor(Color.parseColor("#8B5CF6"));
            h.info.setText("Folder");
            h.size.setText(">");
            h.size.setTextColor(Color.parseColor("#8B5CF6"));
        } else {
            h.info.setText(item.ext.toUpperCase() + " file");
            h.size.setText(formatSize(item.file.length()));
            if (font != null) {
                h.icon.setTypeface(font);
            }
            
            switch(item.ext) {
                case "html":
                    h.icon.setText("\uf1c9");
                    break;
                case "txt":
                    h.icon.setText("\uf15c");
                    h.icon.setTextColor(Color.parseColor("#FFDC5F"));
                    break;
                case "apk":
                    h.icon.setText("\uf1c6");
                    h.icon.setTextColor(Color.parseColor("#43FF8D"));
                    break;
                case "aac":
                    h.icon.setText("\uf001");
                    h.icon.setTextColor(Color.parseColor("#698CB7"));
                    break;
                case "mp3":
                    h.icon.setText("\uf001");
                    h.icon.setTextColor(Color.parseColor("#698CB7"));
                    break;
                case "wav":
                    h.icon.setText("\uf001");
                    h.icon.setTextColor(Color.parseColor("#698CB7"));
                    break;
                case "ogg":
                    h.icon.setText("\uf001");
                    h.icon.setTextColor(Color.parseColor("#698CB7"));
                    break;
                case "m4a":
                    h.icon.setText("\uf001");
                    h.icon.setTextColor(Color.parseColor("#698CB7"));
                    break;
                case "mp4":
                    h.icon.setText("\uf03d");
                    h.icon.setTextColor(Color.parseColor("#00B79E"));
                    break;
                case "mkv":
                    h.icon.setText("\uf008");
                    h.icon.setTextColor(Color.parseColor("#00B79E"));
                    break;
                case "avi":
                    h.icon.setText("\uf03d");
                    h.icon.setTextColor(Color.parseColor("#00B79E"));
                    break;
                case "png":
                    h.icon.setText("\uf03e");
                    h.icon.setTextColor(Color.parseColor("#72F3FF"));
                    break;
                case "jpg":
                    h.icon.setText("\uf03e");
                    h.icon.setTextColor(Color.parseColor("#72F3FF"));
                    break;
                case "jpeg":
                    h.icon.setText("\uf03e");
                    h.icon.setTextColor(Color.parseColor("#72F3FF"));
                    break;
                case "gif":
                    h.icon.setText("\uf03e");
                    h.icon.setTextColor(Color.parseColor("#72F3FF"));
                    break;
                case "webp":
                    h.icon.setText("\uf03e");
                    h.icon.setTextColor(Color.parseColor("#72F3FF"));
                    break;
                case "pdf":
                    h.icon.setText("\uf1c1");
                    h.icon.setTextColor(Color.parseColor("#FF5F5B"));
                    break;
                case "doc":
                    h.icon.setText("\uf1c2");
                    h.icon.setTextColor(Color.parseColor("#40A3FF"));
                    break;
                case "docx":
                    h.icon.setText("\uf1c2");
                    h.icon.setTextColor(Color.parseColor("#40A3FF"));
                    break;
                case "xls":
                    h.icon.setText("\uf1c3");
                    h.icon.setTextColor(Color.parseColor("#CFFF50"));
                    break;
                case "xlsx":
                    h.icon.setText("\uf1c3");
                    h.icon.setTextColor(Color.parseColor("#CFFF50"));
                    break;
                case "ppt":
                    h.icon.setText("\uf1c4");
                    h.icon.setTextColor(Color.parseColor("#FFB074"));
                    break;
                case "pptx":
                    h.icon.setText("\uf1c4");
                    h.icon.setTextColor(Color.parseColor("#FF7E46"));
                    break;
                case "zip":
                    h.icon.setText("\uf1c6");
                    h.icon.setTextColor(Color.parseColor("#9AD39F"));
                    break;
                case "rar":
                    h.icon.setText("\uf1c6");
                    h.icon.setTextColor(Color.parseColor("#9AD39F"));
                    break;
                case "7z":
                    h.icon.setText("\uf1c6");
                    h.icon.setTextColor(Color.parseColor("#9AD39F"));
                    break;
                case "tar":
                    h.icon.setText("\uf1c6");
                    h.icon.setTextColor(Color.parseColor("#9AD39F"));
                    break;
                case "gz":
                    h.icon.setText("\uf1c6");
                    h.icon.setTextColor(Color.parseColor("#9AD39F"));
                    break;
                case "py":
                    h.icon.setText("\uf1c9");
                    h.icon.setTextColor(Color.parseColor("#8B5CF6"));
                    break;
                case "js":
                    h.icon.setText("\uf1c9");
                    h.icon.setTextColor(Color.parseColor("#8B5CF6"));
                    break;
                case "java":
                    h.icon.setText("\uf1c9");
                    h.icon.setTextColor(Color.parseColor("#8B5CF6"));
                    break;
                case "kt":
                    h.icon.setText("\uf1c9");
                    h.icon.setTextColor(Color.parseColor("#8B5CF6"));
                    break;
                case "ts":
                    h.icon.setText("\uf1c9");
                    h.icon.setTextColor(Color.parseColor("#8B5CF6"));
                    break;
                case "htm":
                    h.icon.setText("\uf1c9");
                    h.icon.setTextColor(Color.parseColor("#8B5CF6"));
                    break;
                case "html":
                    h.icon.setText("\uf1c9");
                    h.icon.setTextColor(Color.parseColor("#8B5CF6"));
                    break;
                case "css":
                    h.icon.setText("\uf1c9");
                    h.icon.setTextColor(Color.parseColor("#8B5CF6"));
                    break;
                case "xml":
                    h.icon.setText("\uf1c9");
                    h.icon.setTextColor(Color.parseColor("#8B5CF6"));
                    break;
                case "json":
                    h.icon.setText("\uf1c9");
                    h.icon.setTextColor(Color.parseColor("#8B5CF6"));
                    break;
                case "sh":
                    h.icon.setText("\uf1c9");
                    h.icon.setTextColor(Color.parseColor("#8B5CF6"));
                    break;
                case "bat":
                    h.icon.setText("\uf1c9");
                    h.icon.setTextColor(Color.parseColor("#8B5CF6"));
                    break;
                case "c":
                    h.icon.setText("\uf1c9");
                    h.icon.setTextColor(Color.parseColor("#8B5CF6"));
                    break;
                case "cpp":
                    h.icon.setText("\uf1c9");
                    h.icon.setTextColor(Color.parseColor("#8B5CF6"));
                    break;
                case "h":
                    h.icon.setText("\uf1c9");
                    h.icon.setTextColor(Color.parseColor("#8B5CF6"));
                    break;
                case "php":
                    h.icon.setText("\uf1c9");
                    h.icon.setTextColor(Color.parseColor("#8B5CF6"));
                    break;
                case "rb":
                    h.icon.setText("\uf1c9");
                    h.icon.setTextColor(Color.parseColor("#8B5CF6"));
                    break;
                case "go":
                    h.icon.setText("\uf1c9");
                    h.icon.setTextColor(Color.parseColor("#8B5CF6"));
                    break;
                case "rs":
                    h.icon.setText("\uf1c9");
                    h.icon.setTextColor(Color.parseColor("#8B5CF6"));
                    break;
                default:
                    h.icon.setText("\uf15b");
                    h.icon.setTextColor(Color.parseColor("#999999"));
                    break;
            }
        }
        return convertView;
    }

    private String formatSize(long bytes){
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024*1024) return (bytes/1024) + " KB";
        return String.format("%.1f MB", bytes/1024f/1024f);
    }
}