package com.kenzglobe.debugmate;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.io.File;
import java.util.ArrayList;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
    	if (getActionBar() != null) {
    		getActionBar().hide();
		}
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
		ListView fileListView = findViewById(R.id.file_list);
		ArrayList<String> fileList = new ArrayList<>();
		File root = Environment.getExternalStorageDirectory();
		File[] files = root.listFiles();
		
		if (files != null) {
		    for (File file : files) {
		        if (file.isDirectory()) {
		            fileList.add("📁 " + file.getName());
		        } else {
		            fileList.add("📄 " + file.getName());
		        }
		    }
		} else {
		    fileList.add("Permission denied or empty folder.");
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<>(
		        this, 
		        android.R.layout.simple_list_item_1, 
		        fileList
		);
		fileListView.setAdapter(adapter);
    }
}
