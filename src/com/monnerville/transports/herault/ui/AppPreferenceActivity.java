package com.monnerville.transports.herault.ui;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;
import android.view.View;
import android.widget.TextView;

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

        dep = findPreference("pref_about_version");
        dep.setSummary(getString(R.string.pref_about_version_summary, getString(R.string.app_version), 
            getString(R.string.app_revision)));
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference pref) {
        String key = pref.getKey();
        /*
        if (key.equals("pref_about_release_notes")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AppPreferenceActivity.this);
            builder.setTitle(getString(R.string.pref_about_release_notes_title)).setCancelable(true);
            View log = getLayoutInflater().inflate(R.layout.releasenotes, null);
            ((TextView)log.findViewById(R.id.about_ht_version)).setText(String.format("%s %s",
                getString(R.string.app_name), getString(R.string.app_version)));
            builder.setView(log);
            builder.show();
            return true;
        }
         * 
         */
        return false;
   }
}
