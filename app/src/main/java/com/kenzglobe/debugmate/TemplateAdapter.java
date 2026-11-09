package com.kenzglobe.debugmate;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TemplateAdapter extends BaseAdapter {

    private Context context;
    private List<TemplateItem> items;
    private List<TemplateItem> filteredItems;
    private Typeface appFont, solidFont, brandsFont; // Added brandsFont

    public TemplateAdapter(Context context, List<TemplateItem> items) {
        this.context = context;
        this.items = items;
        this.filteredItems = new ArrayList<>(items);
        this.appFont = Typeface.createFromAsset(context.getAssets(), "fonts/SpaceMono-Bold.ttf");
        this.solidFont = Typeface.createFromAsset(context.getAssets(), "fonts/fa-solid-900-v5.ttf");
        this.brandsFont = Typeface.createFromAsset(context.getAssets(), "fonts/fa-brands-regular-400-v5.ttf"); // Load brands font
    }

    public void filter(String query) {
        filteredItems.clear();
        if (query.isEmpty()) {
            filteredItems.addAll(items);
        } else {
            for (TemplateItem item : items) {
                if (item.name.toLowerCase().contains(query.toLowerCase()) ||
                    item.description.toLowerCase().contains(query.toLowerCase())) {
                    filteredItems.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() { return filteredItems.size(); }

    @Override
    public TemplateItem getItem(int position) { return filteredItems.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_template, parent, false);
        }

        TemplateItem item = getItem(position);

        TextView iconView = convertView.findViewById(R.id.template_icon);
        TextView nameView = convertView.findViewById(R.id.template_name);
        TextView descView = convertView.findViewById(R.id.template_desc);

        nameView.setTypeface(appFont);
        descView.setTypeface(appFont);
        nameView.setText(item.name);
        descView.setText(item.description);

        int color;
        // Apply the correct font and color based on type
        switch (item.type) {
            case "web": 
                iconView.setTypeface(solidFont); 
                color = Color.parseColor("#3FB950"); 
                break;
            case "python": 
                iconView.setTypeface(brandsFont); // Use Brands font
                color = Color.parseColor("#3776AB"); 
                break;
            case "java": 
                iconView.setTypeface(brandsFont); // Use Brands font
                color = Color.parseColor("#ED8B00"); 
                break;
            case "android": 
                iconView.setTypeface(brandsFont); // Use Brands font
                color = Color.parseColor("#3DDC84"); 
                break;
            default: 
                iconView.setTypeface(solidFont); 
                color = Color.parseColor("#8B5CF6"); 
                break;
        }
        
        iconView.setText(item.icon);
        iconView.setTextColor(color);

        return convertView;
    }
}