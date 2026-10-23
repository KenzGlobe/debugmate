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
    private Typeface solidFont, brandsRegularFont;

    public FileListAdapter(Context ctx, List<FileItem> list){
        super(ctx, R.layout.item_file, list);
        
        try {
            this.solidFont = Typeface.createFromAsset(ctx.getAssets(), "fonts/fa-solid-900-v5.ttf");
            this.brandsRegularFont = Typeface.createFromAsset(ctx.getAssets(), "fonts/fa-brands-regular-400-v5.ttf");
        } catch (Exception e) {
            this.solidFont = null;
            this.brandsRegularFont = null;
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
            if (solidFont != null) {
                h.icon.setTypeface(solidFont);
            }
            h.icon.setText("\uf07b");
            h.icon.setTextColor(Color.parseColor("#8B5CF6"));
            h.info.setText("Folder");
            h.size.setText(">");
            h.size.setTextColor(Color.parseColor("#8B5CF6"));
        } else {
            h.info.setText(item.ext.toUpperCase() + " file");
            h.size.setText(formatSize(item.file.length()));
            
            switch(item.ext) {
                case "txt":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf15c");
                    h.icon.setTextColor(Color.parseColor("#FFDC5F"));
                    break;
                case "apk":
                    if (brandsRegularFont != null) {
              	  	    h.icon.setTypeface(brandsRegularFont);
            		    }
                    h.icon.setText("\uf17b");
                    h.icon.setTextColor(Color.parseColor("#43FF8D"));
                    break;
                case "aac":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf001");
                    h.icon.setTextColor(Color.parseColor("#698CB7"));
                    break;
                case "mp3":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf001");
                    h.icon.setTextColor(Color.parseColor("#698CB7"));
                    break;
                case "wav":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf001");
                    h.icon.setTextColor(Color.parseColor("#698CB7"));
                    break;
                case "ogg":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf001");
                    h.icon.setTextColor(Color.parseColor("#698CB7"));
                    break;
                case "m4a":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf001");
                    h.icon.setTextColor(Color.parseColor("#698CB7"));
                    break;
                case "mp4":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf03d");
                    h.icon.setTextColor(Color.parseColor("#00B79E"));
                    break;
                case "mkv":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf008");
                    h.icon.setTextColor(Color.parseColor("#00B79E"));
                    break;
                case "avi":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf03d");
                    h.icon.setTextColor(Color.parseColor("#00B79E"));
                    break;
                case "svg":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf03e");
                    h.icon.setTextColor(Color.parseColor("#72F3FF"));
                    break;
                case "png":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf03e");
                    h.icon.setTextColor(Color.parseColor("#72F3FF"));
                    break;
                case "jpg":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf03e");
                    h.icon.setTextColor(Color.parseColor("#72F3FF"));
                    break;
                case "jpeg":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf03e");
                    h.icon.setTextColor(Color.parseColor("#72F3FF"));
                    break;
                case "gif":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf03e");
                    h.icon.setTextColor(Color.parseColor("#72F3FF"));
                    break;
                case "webp":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf03e");
                    h.icon.setTextColor(Color.parseColor("#72F3FF"));
                    break;
                case "pdf":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf1c1");
                    h.icon.setTextColor(Color.parseColor("#FF5F5B"));
                    break;
                case "doc":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf1c2");
                    h.icon.setTextColor(Color.parseColor("#40A3FF"));
                    break;
                case "docx":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf1c2");
                    h.icon.setTextColor(Color.parseColor("#40A3FF"));
                    break;
                case "xls":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf1c3");
                    h.icon.setTextColor(Color.parseColor("#CFFF50"));
                    break;
                case "xlsx":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf1c3");
                    h.icon.setTextColor(Color.parseColor("#CFFF50"));
                    break;
                case "ppt":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf1c4");
                    h.icon.setTextColor(Color.parseColor("#FFB074"));
                    break;
                case "pptx":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf1c4");
                    h.icon.setTextColor(Color.parseColor("#FF7E46"));
                    break;
                case "zip":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf1c6");
                    h.icon.setTextColor(Color.parseColor("#9AD39F"));
                    break;
                case "rar":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf1c6");
                    h.icon.setTextColor(Color.parseColor("#9AD39F"));
                    break;
                case "7z":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf1c6");
                    h.icon.setTextColor(Color.parseColor("#9AD39F"));
                    break;
                case "tar":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf1c6");
                    h.icon.setTextColor(Color.parseColor("#9AD39F"));
                    break;
                case "gz":
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf1c6");
                    h.icon.setTextColor(Color.parseColor("#9AD39F"));
                    break;
                case "py":
            		    if (brandsRegularFont != null) {
              	  	    h.icon.setTypeface(brandsRegularFont);
            		    }
                    h.icon.setText("\uf3e2");
                    h.icon.setTextColor(Color.parseColor("#3776AB"));
                    break;
                case "js":
            		    if (brandsRegularFont != null) {
              	  	    h.icon.setTypeface(brandsRegularFont);
            		    }
                    h.icon.setText("\uf3b8");
                    h.icon.setTextColor(Color.parseColor("#FFFF00"));
                    break;
                case "java":
            		    if (brandsRegularFont != null) {
              	  	    h.icon.setTypeface(brandsRegularFont);
            		    }
                    h.icon.setText("\uf4e4");
                    h.icon.setTextColor(Color.parseColor("#ED8B00"));
                    break;
                case "kt":
            		    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf121");
                    h.icon.setTextColor(Color.parseColor("#7F52FF"));
                    break;
                case "ts":
            		    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf121");
                    h.icon.setTextColor(Color.parseColor("#3178C6"));
                    break;
                case "htm":
            		    if (brandsRegularFont != null) {
              	  	    h.icon.setTypeface(brandsRegularFont);
            		    }
                    h.icon.setText("\uf13b");
                    h.icon.setTextColor(Color.parseColor("#FF8800"));
                    break;
                case "html":
            		    if (brandsRegularFont != null) {
              	  	    h.icon.setTypeface(brandsRegularFont);
            		    }
                    h.icon.setText("\uf13b");
                    h.icon.setTextColor(Color.parseColor("#FF8800"));
                    break;
                case "css":
            		    if (brandsRegularFont != null) {
              	  	    h.icon.setTypeface(brandsRegularFont);
            		    }
                    h.icon.setText("\uf13c");
                    h.icon.setTextColor(Color.parseColor("#1572B6"));
                    break;
                case "xml":
            		    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf121");
                    h.icon.setTextColor(Color.parseColor("#E37933"));
                    break;
                case "json":
            		    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf121");
                    h.icon.setTextColor(Color.parseColor("#F0A000"));
                    break;
                case "sh":
            		    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf1c9");
                    h.icon.setTextColor(Color.parseColor("#8B5CF6"));
                    break;
                case "bat":
            		    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf1c9");
                    h.icon.setTextColor(Color.parseColor("#8B5CF6"));
                    break;
                case "c":
            		    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf121");
                    h.icon.setTextColor(Color.parseColor("#8B5CF6"));
                    break;
                case "cpp":
            		    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf121");
                    h.icon.setTextColor(Color.parseColor("#00599C"));
                    break;
                case "h":
            		    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf1c9");
                    h.icon.setTextColor(Color.parseColor("#8B5CF6"));
                    break;
                case "php":
            		    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf121");
                    h.icon.setTextColor(Color.parseColor("#8B5CF6"));
                    break;
                case "rb":
            		    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf121");
                    h.icon.setTextColor(Color.parseColor("#CC342D"));
                    break;
                case "go":
            		    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf121");
                    h.icon.setTextColor(Color.parseColor("#00ADD8"));
                    break;
                case "rs":
            		    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf121");
                    h.icon.setTextColor(Color.parseColor("#DEA584"));
                    break;
                case "swift":
            		    if (brandsRegularFont != null) {
              	  	    h.icon.setTypeface(brandsRegularFont);
            		    }
                    h.icon.setText("\uf8e1");
                    h.icon.setTextColor(Color.parseColor("#F05138"));
                    break;
                case "md":
            		    if (brandsRegularFont != null) {
              	  	    h.icon.setTypeface(brandsRegularFont);
            		    }
                    h.icon.setText("\uf60f");
                    h.icon.setTextColor(Color.parseColor("#608080"));
                    break;
                case "gradle":
            		    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
                    h.icon.setText("\uf7d9");
                    h.icon.setTextColor(Color.parseColor("#608080"));
                    break;
                default:
                    if (solidFont != null) {
              	  	    h.icon.setTypeface(solidFont);
            		    }
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
