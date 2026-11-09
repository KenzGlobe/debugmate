package com.kenzglobe.debugmate;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheatAdapter extends BaseAdapter {

    private Context context;
    private List<CheatItem> originalList;
    private List<CheatItem> filteredList;
    private Typeface appFont, solidFont;

    public CheatAdapter(Context context, List<CheatItem> list) {
        this.context = context;
        this.originalList = list;
        this.filteredList = new ArrayList<>(list);
        this.appFont = Typeface.createFromAsset(context.getAssets(), "fonts/SpaceMono-Bold.ttf");
        this.solidFont = Typeface.createFromAsset(context.getAssets(), "fonts/fa-solid-900-v5.ttf");
    }

    @Override public int getCount() { return filteredList.size(); }
    @Override public Object getItem(int position) { return filteredList.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_cheat, parent, false);
        }

        final CheatItem item = filteredList.get(position);

        TextView title = convertView.findViewById(R.id.cheat_item_title);
        TextView explanation = convertView.findViewById(R.id.cheat_item_explanation);
        TextView expandIcon = convertView.findViewById(R.id.cheat_expand_icon);
        LinearLayout detailsContainer = convertView.findViewById(R.id.cheat_details_container);
        LinearLayout codeContainer = convertView.findViewById(R.id.cheat_code_container);

        title.setTypeface(appFont);
        title.setText(item.title);
        
        // Set expand/collapse icon
        expandIcon.setTypeface(solidFont);
        if (item.isExpanded) {
            expandIcon.setText("\uf078"); // Chevron up
            detailsContainer.setVisibility(View.VISIBLE);
        } else {
            expandIcon.setText("\uf054"); // Chevron down
            detailsContainer.setVisibility(View.GONE);
        }

        // --- DYNAMICALLY ADD CODE BLOCKS ---
        codeContainer.removeAllViews(); // Clear old views
        LayoutInflater inflater = LayoutInflater.from(context);
        
        for (final CodeBlock block : item.codeBlocks) {
            View codeView = inflater.inflate(R.layout.item_code_block, codeContainer, false);
            
            TextView fileNameView = codeView.findViewById(R.id.code_block_filename);
            TextView codeViewText = codeView.findViewById(R.id.code_block_text);
            TextView copyBtn = codeView.findViewById(R.id.code_block_copy_btn);

            if (block.fileName != null && !block.fileName.isEmpty()) {
                fileNameView.setText(block.fileName);
            } else {
                fileNameView.setVisibility(View.GONE);
            }
            
            codeViewText.setText(block.code);
            
            // Setup Copy Button
            copyBtn.setTypeface(solidFont);
            copyBtn.setText("\uf0c5"); // Copy icon
            copyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(ClipData.newPlainText("Code", block.code));
                        AppToast.showSuccess(context, "Code copied!");
                    }
                }
            });

            codeContainer.addView(codeView);
        }

        explanation.setText(Html.fromHtml(item.explanationHtml));
        explanation.setMovementMethod(new CustomLinkMovementMethod(context));

        // Toggle expansion on click
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.isExpanded = !item.isExpanded;
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        filteredList.clear();
        if (charText.length() == 0) {
            filteredList.addAll(originalList);
        } else {
            for (CheatItem item : originalList) {
                if (item.title.toLowerCase(Locale.getDefault()).contains(charText)) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }
}