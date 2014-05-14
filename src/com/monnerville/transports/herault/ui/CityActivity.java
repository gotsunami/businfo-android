package com.monnerville.transports.herault.ui;

import android.util.Log;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import java.util.List;

import com.commonsware.android.listview.SectionedAdapter;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.monnerville.transports.herault.HeaderTitle;
import com.monnerville.transports.herault.R;

import com.monnerville.transports.herault.core.Application;
import static com.monnerville.transports.herault.core.Application.TAG;

import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.BusManager;
import com.monnerville.transports.herault.core.City;
import com.monnerville.transports.herault.core.GPSPoint;
import com.monnerville.transports.herault.core.QueryManager;
import com.monnerville.transports.herault.core.sql.SQLBusLine;

import com.monnerville.transports.herault.core.sql.SQLBusManager;
import com.monnerville.transports.herault.core.sql.SQLBusNetwork;
import com.monnerville.transports.herault.core.sql.SQLQueryManager;
import com.monnerville.transports.herault.ui.maps.BaseItemsOverlay;
import java.util.ArrayList;
import java.util.Arrays;

public class CityActivity extends MapActivity implements HeaderTitle, OnItemClickListener {
    private String mNetwork;
    private String mCityId;
    private boolean mCanFinish = false;
    private SharedPreferences mPrefs;
    private boolean mShowGMap;

    // Cached directions for matching lines
    private List<List<String>> mDirections;
    private List<BusLine> mLines;
    private ListView mList;
    
    private final int DEFAULT_MAP_ZOOM = 13;

    final BusManager mManager = SQLBusManager.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mShowGMap = mPrefs.getBoolean("pref_use_city_map", true);

        if (Application.OSBeforeHoneyComb()) {
            requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        }
        setContentView(mShowGMap ? R.layout.city : R.layout.city_no_map);
        if (Application.OSBeforeHoneyComb()) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.city_title_bar);
        }
        else {
            ActionBarHelper.setDisplayHomeAsUpEnabled(this, true);
        }

        mList = (ListView)findViewById(R.id.lineslist);
        mList.setItemsCanFocus(true);
        mList.setOnItemClickListener(this);

        final Intent intent = getIntent();
        final Bundle bun = intent.getExtras();
        // Sent directly from the suggestion search entry or from the SearchableActivity
        mCityId = bun.getString("cityId");
        mNetwork = bun.getString("network");

        mLines = new ArrayList<BusLine>();
        mDirections = new ArrayList<List<String>>();

        QueryManager finder = SQLQueryManager.getInstance();
        String cityName = finder.getCityFromId(mCityId);
        List<City> cities = finder.findCities(cityName, true);
        List<String> lines = finder.findLinesInCity(cityName);

        if (Application.OSBeforeHoneyComb()) {
            setPrimaryTitle(cityName);
            setSecondaryTitle(getString(R.string.city_title));
        }
        else {
            ActionBarHelper.setTitle(this, cityName);
        }

        new DirectionsRetreiverTask().execute(lines);

        if (Application.OSBeforeHoneyComb()) {
            Button searchButton = (Button)findViewById(R.id.btn_search);
            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // Open search dialog
                    mCanFinish = true;
                    onSearchRequested();
                }
            });
        }

        if (mShowGMap) {
            MapView mapView = (MapView) findViewById(R.id.mapview);
            MapController controller = mapView.getController();

            // Display city on map
            Drawable marker = getResources().getDrawable(android.R.drawable.star_big_on);
            int markerWidth = marker.getIntrinsicWidth();
            int markerHeight = marker.getIntrinsicHeight();
            marker.setBounds(0, markerHeight, markerWidth, 0);

            BaseItemsOverlay busOverlay = new BaseItemsOverlay(marker);
            GPSPoint pt = finder.getCityGPSCoordinates(cities.get(0));
            GeoPoint cityPoint = new GeoPoint(pt.getLatitude(), pt.getLongitude());
            busOverlay.addItem(cityPoint, "doo", "Kilo");
            mapView.getOverlays().add(busOverlay);

            // Center map on the city
            controller.setCenter(cityPoint);
            controller.setZoom(DEFAULT_MAP_ZOOM);
        }
    }

    @Override
    public void setPrimaryTitle(String title) {
        TextView t = (TextView)findViewById(R.id.primary);
        t.setText(title);
    }

    @Override
    public void setSecondaryTitle(String title) {
        TextView t= (TextView)findViewById(R.id.secondary);
        t.setText(title);
    }

    @Override
    protected void onPause() {
        // Force finishing the activity so it's not in the activity stack if
        // performing multiple searches
        if (mCanFinish)
            finish();
        super.onPause();
    }

    /**
     * Sets up the adapter for the list
     */
    private void setupAdapter(List<BusLine> lines) {
        mAdapter.addSection(getString(R.string.result_lines_header),
            new AllLinesActivity.LineListAdapter(this, R.layout.line_list_item, lines, mDirections));
        mList.setAdapter(mAdapter);
    }

	final SectionedAdapter mAdapter = new CounterSectionedAdapter(this) {
        @Override
        protected int getMatches(String caption) {
            return mLines.size();
        }
	};

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    /**
     * Retrieves all lines directions in a background thread
     */
    private class DirectionsRetreiverTask extends AsyncTask<List<String>, Void, Void> {
        private ProgressDialog mDialog;

        @Override
        protected Void doInBackground(List<String>... lis) {
            for (String li : lis[0]) {
                BusLine line = mManager.getBusLine(new SQLBusNetwork(mNetwork), li);
                mDirections.add(Arrays.asList(line.getDirections()));
                mLines.add(line);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            // Executes on the UI thread
            mDialog = ProgressDialog.show(CityActivity.this, "",
                getString(R.string.pd_searching), true);
        }

        @Override
        protected void onPostExecute(Void none) {
            // Back to the UI thread
            try {
                mDialog.cancel();
                mDialog = null;
                setupAdapter(mLines);
                mAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                /* NOP */
                // Prevents random "IllegalArgumentException: View not attached to window manager"
                // exception
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> av, View v, int position, long id) {
        mCanFinish = false;
        final Object obj = mList.getItemAtPosition(position);
        if (obj instanceof BusLine) {
            BusLine line = (SQLBusLine)obj;
            AllLinesActivity.handleBusLineItemClick(this, new SQLBusNetwork(line.getBusNetworkName()), mList, v, position, id);
        }
    }

    /**
     * Asynchronous database creating/updating
     */
    private class PopulateMapTask extends AsyncTask<Void, Void, Void> {
        final QueryManager finder = SQLQueryManager.getInstance();
        Drawable marker;

        @Override
        protected Void doInBackground(Void... none) {
            return null;
        }

        @Override
        protected void onPreExecute() {
            // Executes on the UI thread
            marker = getResources().getDrawable(android.R.drawable.star_big_on);
            int markerWidth = marker.getIntrinsicWidth();
            int markerHeight = marker.getIntrinsicHeight();
            marker.setBounds(0, markerHeight, markerWidth, 0);
        }

        @Override
        protected void onPostExecute(Void none) {
            // Back to the UI thread
            MapView mapView = (MapView)findViewById(R.id.mapview);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                // Open search dialog
                onSearchRequested();
                return true;
            case android.R.id.home:
                // App icon in action bar clicked; go home
                finish();
                return true;
            case R.id.menu_settings:
                startActivity(new Intent(this, AppPreferenceActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Creating menu options
     * @param menu object
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.city, menu);
        return true;
    }

}
