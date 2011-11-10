package com.monnerville.transports.herault.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;

import com.monnerville.transports.herault.R;

public class AppPreferenceActivity extends PreferenceActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

        Preference dep = findPreference("pref_about_db_version");
        SharedPreferences sp = dep.getSharedPreferences();
        dep.setSummary(getString(R.string.pref_about_db_version_summary,
            getString(R.string.dbversion), getString(R.string.num_lines),
            getString(R.string.num_cities), getString(R.string.num_stations)));
    }
}
