package com.monnerville.transports.herault.ui;

import android.util.Log;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import java.util.List;

import com.commonsware.android.listview.SectionedAdapter;
import com.monnerville.transports.herault.HeaderTitle;
import com.monnerville.transports.herault.R;

import static com.monnerville.transports.herault.core.Application.TAG;

import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.BusManager;
import com.monnerville.transports.herault.core.QueryManager;

import com.monnerville.transports.herault.core.sql.SQLBusManager;
import com.monnerville.transports.herault.core.sql.SQLQueryManager;
import java.util.ArrayList;
import java.util.Arrays;

public class CityActivity extends ListActivity implements HeaderTitle {
    private String mCityId;

    // Cached directions for matching lines
    private List<List<String>> mDirections;
    private List<BusLine> mLines;

    final BusManager mManager = SQLBusManager.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.simplelist);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.city_title_bar);
        getListView().setItemsCanFocus(true);

        final Intent intent = getIntent();
        final Bundle bun = intent.getExtras();
        // Sent directly from the suggestion search entry or from the SearchableActivity
        mCityId = bun.getString("cityId");

        mLines = new ArrayList<BusLine>();
        mDirections = new ArrayList<List<String>>();

        QueryManager finder = SQLQueryManager.getInstance();
        String cityName = finder.getCityFromId(mCityId);
        List<String> lines = finder.findLinesInCity(cityName);

        setPrimaryTitle(cityName);
        setSecondaryTitle("Dooo");

        new DirectionsRetreiverTask().execute(lines);
    }

    @Override
    public void setPrimaryTitle(String title) {
        TextView t= (TextView)findViewById(R.id.primary);
        t.setText(title);
    }

    @Override
    public void setSecondaryTitle(String title) {
        TextView t= (TextView)findViewById(R.id.secondary);
        t.setText(title);
    }

    /**
     * Sets up the adapter for the list
     */
    private void setupAdapter(List<BusLine> lines) {
        mAdapter.addSection(getString(R.string.result_lines_header),
            new AllLinesActivity.LineListAdapter(this, R.layout.line_list_item, lines, mDirections));
        setListAdapter(mAdapter);
    }

	final SectionedAdapter mAdapter = new CounterSectionedAdapter(this) {
        @Override
        protected int getMatches(String caption) {
            return mLines.size();
        }
	};

    /**
     * Retrieves all lines directions in a background thread
     */
    private class DirectionsRetreiverTask extends AsyncTask<List<String>, Void, Void> {
        @Override
        protected Void doInBackground(List<String>... lis) {
            for (String li : lis[0]) {
                BusLine line = mManager.getBusLine(li);
                mDirections.add(Arrays.asList(line.getDirections()));
                mLines.add(line);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            // Executes on the UI thread
        }

        @Override
        protected void onPostExecute(Void none) {
            // Back to the UI thread
            setupAdapter(mLines);
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Handles a click on any item in the list
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final Object obj = l.getItemAtPosition(position);
        if (obj instanceof BusLine)
            AllLinesActivity.handleBusLineItemClick(this, l, v, position, id);
    }
}
