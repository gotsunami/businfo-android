package com.monnerville.transports.herault.ui;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.Application;

public class AppPreferenceActivity extends PreferenceActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

        if (!Application.OSBeforeHoneyComb()) {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

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
        if (key.equals("pref_about_about")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AppPreferenceActivity.this);
            builder.setTitle(getString(R.string.pref_about_about_title)).setCancelable(true);
            View about = getLayoutInflater().inflate(R.layout.about, null);
            ((TextView)about.findViewById(R.id.about_ht_version)).setText(String.format("%s %s", getString(R.string.app_name),
                getString(R.string.app_version)));
            builder.setView(about);
            builder.show();
            return true;
        }
        else if(key.equals("pref_about_release_notes")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AppPreferenceActivity.this);
            builder.setTitle(getString(R.string.pref_about_release_notes_title)).setCancelable(true);
            View log = getLayoutInflater().inflate(R.layout.releasenotes, null);
            ((TextView)log.findViewById(R.id.about_ht_version)).setText(String.format("Version %s",
            getString(R.string.app_version)));
            builder.setView(log);
            builder.show();
            return true;
        }
        else if(key.equals("pref_about_changelog")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AppPreferenceActivity.this);
            builder.setTitle(getString(R.string.pref_about_changelog_title)).setCancelable(true);
            View log = getLayoutInflater().inflate(R.layout.changelog, null);
            builder.setView(log);
            builder.show();
            return true;
        }
        else if(key.equals("pref_tips_show")) {
            AlertDialog.Builder tipsDialog = new TipsDialog(this, true);
            tipsDialog.show();
            return true;
        }
        return false;
   }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
}
