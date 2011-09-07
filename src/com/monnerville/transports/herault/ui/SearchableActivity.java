package com.monnerville.transports.herault.ui;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.monnerville.transports.herault.R;

/**
 *
 * @author mathias
 */
public class SearchableActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.search);

    // Get the intent, verify the action and get the query
    Intent intent = getIntent();
    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
        String query = intent.getStringExtra(SearchManager.QUERY);
        startSearching(query);
    }
    else
        finish();
    }

    /**
     * Starts the search according to the query. Can retreive a city, bus line,
     * stop name.
     *
     * @param query what to search for
     */
    void startSearching(String query) {
        Log.d("QUER", query);
    }
}
