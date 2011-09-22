package com.monnerville.transports.herault.ui;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.QueryManager;
import com.monnerville.transports.herault.core.xml.XMLQueryManager;

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
    void startSearching(final String query) {
        final QueryManager finder = XMLQueryManager.getInstance();
        Log.d("QUER", query);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final long start = System.currentTimeMillis();
                Log.d("RESCITIES", "" +  finder.findCities(query));
                final long end = System.currentTimeMillis();
                Log.d("BENCH", "Took " + (end-start) + "ms to compute");
            }
        }).start();
    }
}
