package com.kenzglobe.debugmate;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class OpenWithAdapter extends ArrayAdapter<ResolveInfo> {
    private Context context;
    private List<ResolveInfo> apps;
    private PackageManager pm;

    public OpenWithAdapter(Context context, List<ResolveInfo> apps) {
        super(context, 0, apps);
        this.context = context;
        this.apps = apps;
        this.pm = context.getPackageManager();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_open_with, parent, false);
        }
        
        ResolveInfo appInfo = apps.get(position);
        
        ImageView icon = convertView.findViewById(R.id.app_icon);
        TextView name = convertView.findViewById(R.id.app_name);
        
        icon.setImageDrawable(appInfo.loadIcon(pm));
        name.setText(appInfo.loadLabel(pm).toString());
        
        return convertView;
    }
}