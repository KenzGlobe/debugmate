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

public class ProjectAdapter extends BaseAdapter {

    private Context context;
    private List<ProjectItem> projects;
    private Typeface appFont, solidFont, brandsFont;

    public ProjectAdapter(Context context, List<ProjectItem> projects) {
        this.context = context;
        this.projects = projects;
        this.appFont = Typeface.createFromAsset(context.getAssets(), "fonts/SpaceMono-Bold.ttf");
        this.solidFont = Typeface.createFromAsset(context.getAssets(), "fonts/fa-solid-900-v5.ttf");
        this.brandsFont = Typeface.createFromAsset(context.getAssets(), "fonts/fa-brands-regular-400-v5.ttf");
    }

    @Override
    public int getCount() { return projects.size(); }

    @Override
    public ProjectItem getItem(int position) { return projects.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_project, parent, false);
        }

        ProjectItem item = getItem(position);

        TextView iconView = convertView.findViewById(R.id.project_icon);
        TextView nameView = convertView.findViewById(R.id.project_name);
        TextView typeView = convertView.findViewById(R.id.project_type);
        TextView pathView = convertView.findViewById(R.id.project_path);
        
        TextView pinned = convertView.findViewById(R.id.pinned);
        TextView hidden = convertView.findViewById(R.id.hidden);

        nameView.setTypeface(appFont);
        typeView.setTypeface(appFont);
        pathView.setTypeface(appFont);
        
        pinned.setTypeface(solidFont);
        hidden.setTypeface(solidFont);
        pinned.setText("\uf08d");
        hidden.setText("\uf023");

        nameView.setText(item.name);
        typeView.setText(item.type.toUpperCase() + " Project");
        
        // --- FIXED: Show the parent directory instead of the project name again ---
        File parentDir = new File(item.path).getParentFile();
        if (parentDir != null) {
            pathView.setText("in " + parentDir.getName());
        } else {
            pathView.setText(item.path);
        }
        
        if (item.isPinned) {
            pinned.setVisibility(View.VISIBLE);
        } else {
            pinned.setVisibility(View.GONE);
        }

        if (item.isHidden) {
            hidden.setVisibility(View.VISIBLE);
        } else {
            hidden.setVisibility(View.GONE);
        }

        int color;
        switch (item.type.toLowerCase()) {
            case "web":
                iconView.setTypeface(solidFont); 
                iconView.setText("\uf121");
                color = Color.parseColor("#3FB950"); 
                break;
            case "python": 
                iconView.setTypeface(brandsFont); 
                iconView.setText("\uf3e2");
                color = Color.parseColor("#3776AB"); 
                break;
            case "node": 
                iconView.setTypeface(brandsFont); 
                iconView.setText("\uf3d3");
                color = Color.parseColor("#339933"); 
                break;
            case "java": 
                iconView.setTypeface(brandsFont); 
                iconView.setText("\uf4e4");
                color = Color.parseColor("#ED8B00"); 
                break;
            case "kotlin":
                iconView.setTypeface(brandsFont); 
                iconView.setText("\uf3d1");
                color = Color.parseColor("#7F52FF"); 
                break;
            case "android": 
                iconView.setTypeface(brandsFont); 
                iconView.setText("\uf17b");
                color = Color.parseColor("#3DDC84"); 
                break;
            default: 
                iconView.setTypeface(solidFont); 
                iconView.setText("\uf07b");
                color = Color.parseColor("#8B5CF6"); 
                break;
        }
        iconView.setTextColor(color);

        return convertView;
    }
}
