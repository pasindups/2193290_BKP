package com.kathri;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.kathri.R;
public class Prefs extends PreferenceActivity {		
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        addPreferencesFromResource(R.xml.preference);
    }	 
}