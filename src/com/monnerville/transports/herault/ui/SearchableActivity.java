package com.monnerville.transports.herault.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.QueryManager;
import com.monnerville.transports.herault.core.sql.SQLQueryManager;
import java.util.List;

/**
 *
 * @author mathias
 */
public class SearchableActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
//            startSearching(query);
            new StartSearchingTask().execute(query);
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
        final QueryManager finder = SQLQueryManager.getInstance();
        Log.d("QUER", query);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final long start = System.currentTimeMillis();
                Log.d("CITIES", "" +  finder.findCities(query));
                Log.d("STATIONS", "" +  finder.findStations(query));
                Log.d("LINES", "" +  finder.findLines(query));
                final long end = System.currentTimeMillis();
                Log.d("BENCH", "Took " + (end-start) + "ms to compute");
            }
        }).start();
    }

    private class StartSearchingTask extends AsyncTask<String, Void, Void> {
        private ProgressDialog mDialog;
        private long mStart;
        final QueryManager finder = SQLQueryManager.getInstance();
        private List<String> c;
        private List<String> s;
        private List<String> li;

        @Override
        protected Void doInBackground(String... q) {
            String query = q[0];
            c = finder.findCities(query);
            s = finder.findStations(query);
            li = finder.findLines(query);
            return null;
        }

        @Override
        protected void onPreExecute() {
            // Executes on the UI thread
            mDialog = ProgressDialog.show(SearchableActivity.this, "",
                getString(R.string.pd_searching), true);
            mStart = System.currentTimeMillis();
        }

        @Override
        protected void onPostExecute(Void none) {
            //mAdapter.notifyDataSetChanged();
            mDialog.cancel();
            showResults(c, s, li, System.currentTimeMillis() - mStart);
        }
    }

    private void showResults(List<String> cities, List<String> stations, List<String> lines, long duration) {
        TextView tv = (TextView)findViewById(R.id.result);
        tv.setText("");
        tv.append("Cities:\n");
        for (String city : cities) {
            tv.append("    " + city + "\n");
        }
        tv.append("Stations:\n");
        for (String st : stations) {
            tv.append("    " + st + "\n");
        }
        tv.append("Lines:\n");
        for (String li : lines) {
            tv.append("    " + li + "\n");
        }
        tv.append("\n\nDuration: " + String.valueOf(duration) + " ms\n");
    }

}
