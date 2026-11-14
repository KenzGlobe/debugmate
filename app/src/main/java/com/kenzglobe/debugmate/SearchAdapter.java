package com.kenzglobe.debugmate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.List;

public class SearchAdapter extends BaseAdapter {
    private Context context;
    private List<SearchResult> results;

    public SearchAdapter(Context context, List<SearchResult> results) {
        this.context = context;
        this.results = results;
    }

    @Override public int getCount() { return results.size(); }
    @Override public Object getItem(int position) { return results.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_search_result, parent, false);
        }
        
        SearchResult item = results.get(position);
        
        com.kenzglobe.debugmate.AppBoldTextView fileView = convertView.findViewById(R.id.search_item_file);
        com.kenzglobe.debugmate.AppCodeFontTextView lineView = convertView.findViewById(R.id.search_item_line);
        
        fileView.setText(item.fileName + " : Line " + item.lineNumber);
        lineView.setText(item.lineText.trim());
        
        return convertView;
    }
}