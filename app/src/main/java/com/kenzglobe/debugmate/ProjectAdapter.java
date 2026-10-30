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

        nameView.setTypeface(appFont);
        typeView.setTypeface(appFont);
        pathView.setTypeface(appFont);

        nameView.setText(item.name);
        typeView.setText(item.type.toUpperCase() + " Project");
        pathView.setText(item.path);

        int color;
        switch (item.type) {
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
            case "java": 
                iconView.setTypeface(brandsFont); 
                iconView.setText("\uf4e4");
                color = Color.parseColor("#ED8B00"); 
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