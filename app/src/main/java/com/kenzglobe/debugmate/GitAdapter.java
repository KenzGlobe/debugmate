package com.kenzglobe.debugmate;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;

public class GitAdapter extends BaseAdapter {

    private Context context;
    private List<GitFileItem> files;
    private Typeface solidFont, brandsFont, appFont;

    public GitAdapter(Context context, List<GitFileItem> files) {
        this.context = context;
        this.files = files;
        this.solidFont = Typeface.createFromAsset(context.getAssets(), "fonts/fa-solid-900-v5.ttf");
        this.brandsFont = Typeface.createFromAsset(context.getAssets(), "fonts/fa-brands-regular-400-v5.ttf");
        this.appFont = Typeface.createFromAsset(context.getAssets(), "fonts/SpaceMono-Bold.ttf");
    }

    @Override public int getCount() { return files.size(); }
    @Override public Object getItem(int position) { return files.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_git_file, parent, false);
        }

        GitFileItem item = files.get(position);

        TextView icon = convertView.findViewById(R.id.git_file_icon);
        TextView name = convertView.findViewById(R.id.git_file_name);
        TextView path = convertView.findViewById(R.id.git_file_path);
        TextView status = convertView.findViewById(R.id.git_file_status);

        name.setText(item.fileName);
        path.setText(item.filePath);
        status.setText(item.status);

        // Color and Text for Status Badge
        if (item.status.equals("M")) {
            status.setTextColor(Color.parseColor("#D29922")); // Yellow
        } else if (item.status.equals("U") || item.status.equals("A")) {
            status.setTextColor(Color.parseColor("#3FB950")); // Green
        } else if (item.status.equals("D")) {
            status.setTextColor(Color.parseColor("#F85149")); // Red
        }

        // Set Icons based on file extension
        String ext = "";
        int dot = item.fileName.lastIndexOf('.');
        if (dot != -1) ext = item.fileName.substring(dot + 1).toLowerCase();

        String iconCode = "\uf15b"; // Default file
        Typeface iconFont = solidFont;

        switch (ext) {
            case "py": iconCode = "\uf3e2"; iconFont = brandsFont; break;
            case "java": iconCode = "\uf4e4"; iconFont = brandsFont; break;
            case "js": iconCode = "\uf3b8"; iconFont = brandsFont; break;
            case "html": iconCode = "\uf13b"; iconFont = brandsFont; break;
            case "css": iconCode = "\uf13c"; iconFont = brandsFont; break;
            case "xml": iconCode = "\uf121"; break;
            case "json": iconCode = "\uf121"; break;
            case "md": iconCode = "\uf60f"; iconFont = brandsFont; break;
        }

        icon.setTypeface(iconFont);
        icon.setText(iconCode);
        icon.setTextColor(Color.parseColor("#8B5CF6"));

        return convertView;
    }
}