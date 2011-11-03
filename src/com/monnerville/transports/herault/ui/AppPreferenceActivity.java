package com.monnerville.transports.herault.ui;

import android.os.Bundle;
import android.preference.*;

import com.monnerville.transports.herault.R;

public class AppPreferenceActivity extends PreferenceActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
    }
}
