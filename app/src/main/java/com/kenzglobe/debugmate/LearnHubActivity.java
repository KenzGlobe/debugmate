package com.kenzglobe.debugmate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import java.util.ArrayList;

public class LearnHubActivity extends Activity {

    private GridView gridView;
    private TextView btnBack;
    private Typeface solidFont, brandsFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- NEW: Handle deep links from Cheat Sheets ---
        String targetTopic = getIntent().getStringExtra("TARGET_TOPIC");
        if (targetTopic != null && !targetTopic.isEmpty()) {
            // The user clicked a learn:// link. Route them to the Concept Activity!
            Intent intent = new Intent(LearnHubActivity.this, ConceptActivity.class);
            intent.putExtra("topic", targetTopic);
            startActivity(intent);
            finish(); // Close the Hub so pressing Back goes to the Cheat Sheet
            return;
        }
        // ------------------------------------------------

        setContentView(R.layout.learn_hub);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#8B5CF6"));
        }
        if (getActionBar() != null) getActionBar().hide();

        solidFont = Typeface.createFromAsset(getAssets(), "fonts/fa-solid-900-v5.ttf");
        brandsFont = Typeface.createFromAsset(getAssets(), "fonts/fa-brands-regular-400-v5.ttf");

        btnBack = findViewById(R.id.btn_learn_back);
        btnBack.setTypeface(solidFont);
        btnBack.setText("\uf060");
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish(); }
        });

        gridView = findViewById(R.id.learn_grid);
        
        ArrayList<LearnItem> items = new ArrayList<>();
        items.add(new LearnItem("HTML", "Web Structure", "html", "#E34F26", "\uf13b", brandsFont));
        items.add(new LearnItem("CSS", "Web Styling", "css", "#264DE4", "\uf13c", brandsFont));
        items.add(new LearnItem("JavaScript", "Web Logic", "js", "#F7DF1E", "\uf3b8", brandsFont));
        items.add(new LearnItem("Python", "Data & AI", "python", "#3776AB", "\uf3e2", brandsFont));
        items.add(new LearnItem("Java", "Android & Backend", "java", "#ED8B00", "\uf4e4", brandsFont));
        items.add(new LearnItem("Git", "Version Control", "git", "#F05032", "\uf1d3", brandsFont));
        items.add(new LearnItem("Linux", "Terminal Basics", "linux", "#3FB950", "\uf17c", brandsFont));

        gridView.setAdapter(new LearnAdapter(this, items));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LearnItem item = (LearnItem) parent.getItemAtPosition(position);
                // Open the Concept Activity for the whole language (e.g., "python_overview")
                Intent intent = new Intent(LearnHubActivity.this, ConceptActivity.class);
                intent.putExtra("topic", item.topicCode + "_overview");
                startActivity(intent);
            }
        });
    }

    private static class LearnItem {
        String title, desc, topicCode, color, unicode;
        Typeface font;

        LearnItem(String title, String desc, String topicCode, String color, String unicode, Typeface font) {
            this.title = title; 
            this.desc = desc; 
            this.topicCode = topicCode;
            this.color = color; 
            this.unicode = unicode; 
            this.font = font;
        }
    }

    private class LearnAdapter extends BaseAdapter {
        ArrayList<LearnItem> items;
        LayoutInflater inflater;

        LearnAdapter(Context context, ArrayList<LearnItem> items) {
            this.items = items;
            this.inflater = LayoutInflater.from(context);
        }

        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_learn_category, parent, false);
            }
            LearnItem item = items.get(position);
            
            TextView icon = convertView.findViewById(R.id.learn_icon);
            TextView title = convertView.findViewById(R.id.learn_title);
            TextView desc = convertView.findViewById(R.id.learn_desc);

            icon.setTypeface(item.font);
            icon.setText(item.unicode);
            icon.setTextColor(Color.parseColor(item.color));

            title.setText(item.title);
            desc.setText(item.desc);

            return convertView;
        }
    }
}