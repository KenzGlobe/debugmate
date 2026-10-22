package com.kenzglobe.debugmate;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
    	if (getActionBar() != null) {
    		getActionBar().hide();
		}
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}
