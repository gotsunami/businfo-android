package com.monnerville.transports.herault.ui;

import android.app.ActionBar;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.commonsware.android.listview.SectionedAdapter;
import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.Application;
import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.City;
import com.monnerville.transports.herault.core.QueryManager;
import com.monnerville.transports.herault.core.sql.SQLQueryManager;
import com.monnerville.transports.herault.core.sql.DBStation;
import com.monnerville.transports.herault.core.sql.SQLBusLine;
import com.monnerville.transports.herault.core.sql.SQLBusManager;
import com.monnerville.transports.herault.provider.SuggestionProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author mathias
 */
public class SearchableActivity extends ListActivity {
    final private SQLBusManager mManager = SQLBusManager.getInstance();

    // Matches in the result set
    private List<City> mCities;
    private List<DBStation> mStations;
    private List<BusLine> mLines;
    private List<List<String>> mDirections;

    private boolean mCanFinish = false;
    private ActionBar mActionBar = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Application.OSBeforeHoneyComb()) {
            requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        }
        setContentView(R.layout.simplelist);
        if (Application.OSBeforeHoneyComb()) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.search_title_bar);
            TextView tp = (TextView)findViewById(R.id.primary);
            tp.setText(getString(R.string.app_name));
        }
        else {
            mActionBar = getActionBar();
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
        getListView().setFastScrollEnabled(true);


        mDirections = new ArrayList<List<String>>();

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // Handle the normal search query case
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (Application.OSBeforeHoneyComb()) {
                TextView ts = (TextView)findViewById(R.id.secondary);
                ts.setText(getString(R.string.search_keyword, query));
            }
            else {
                mActionBar.setTitle(R.string.search_results_title);
                mActionBar.setSubtitle(getString(R.string.search_keyword, query));
            }
            new StartSearchingTask().execute(query);
        }
        else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // Handle a suggestions click (because the suggestions all use ACTION_VIEW)
            Uri data = intent.getData();
            mCanFinish = true;
            showResult(data);
        }
        else
            finish();

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
     * Connects search Uri to an Intent to show the appropriate result. This i an
     * activity switcher.
     *
     * @param target begins with a single letter that is one of {@link SuggestionProvider.BUS_LINE_PREFIX_ID},
     * {@link SuggestionProvider.BUS_STATION_PREFIX_ID} or {@link SuggestionProvider.BUS_CITY_PREFIX_ID},
     * followed a DB id for the matching table.
     *
     */
    private void showResult(Uri target) {
        String data = target.toString();
        // Is this a city?
        if (SuggestionProvider.isCityIntentData(data)) {
            Intent intent = new Intent(this, CityActivity.class);
            intent.putExtra("cityId", data.substring(1));
            startActivity(intent);
        }
        else if(SuggestionProvider.isStationIntentData(data)) {
            // This is a station
            Intent intent = new Intent(this, BusStationGlobalActivity.class);
            intent.putExtra("stationId", data.substring(1));
            startActivity(intent);
        }
        else if(SuggestionProvider.isLineIntentData(data)) {
            // This is a line
            Intent intent = new Intent(this, BusLineActivity.class);
            Cursor c = mManager.getDB().getReadableDatabase().query(getString(
                R.string.db_line_table_name), new String[] {"name"}, "id LIKE ?",
                new String[] {data.substring(1)}, null, null, null
            );
            c.moveToFirst();
            String name = c.getString(0);
            BusLine line = new SQLBusLine(name);
            c.close();
            String dirs[] = line.getDirections();

            intent.putExtra("line", name);
            intent.putExtra("direction", dirs[0]);
            intent.putExtra("showToast", false);
            startActivity(intent);
        }
    }

    private class StartSearchingTask extends AsyncTask<String, Void, Void> {
        private ProgressDialog mDialog;
        private long mStart;
        final QueryManager finder = SQLQueryManager.getInstance();

        @Override
        protected Void doInBackground(String... q) {
            String query = q[0];
            mCities = finder.findCities(query, false);
            mStations = (List<DBStation>)finder.findStations(query);
            mLines = finder.findMatchingLines(query);
            // Get directions
            for (BusLine line : mLines) {
                String[] dirs = line.getDirections();
                mDirections.add(Arrays.asList(dirs));
            }
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
            setAdapter(System.currentTimeMillis() - mStart);
        }
    }

    /**
     * Set list's adapter based on results
     * @param duration
     */
    private void setAdapter(long duration) {
        // Setup adapter
        if (mCities.isEmpty())
            mCities.add(new City(City.NOT_VALID, getString(R.string.result_no_match)));
        if (mStations.isEmpty())
            mStations.add(new DBStation(DBStation.NOT_VALID, getString(R.string.result_no_match)));

        if (mLines.isEmpty())
            mLines.add(new SQLBusLine(getString(R.string.result_no_match)));
            //mLines.add(getString(R.string.result_no_match));
        mAdapter.addSection(getString(R.string.result_city_header),
            new CityListAdapter(this, R.layout.result_city_list_item, mCities));
        mAdapter.addSection(getString(R.string.result_station_header),
            new StationListAdapter(this, R.layout.result_station_list_item, mStations));
        mAdapter.addSection(getString(R.string.result_lines_header),
            new AllLinesActivity.LineListAdapter(this, R.layout.line_list_item, mLines, mDirections));
        setListAdapter(mAdapter);
    }

    final SectionedAdapter mAdapter = new CounterSectionedAdapter(this) {
        @Override
        protected int getMatches(String caption) {
            int matches = 0;
            int ln = 0;
            // First match must be different from result_no_match
            String firstMatch = getString(R.string.result_no_match);
            if (caption.equals(getString(R.string.result_city_header))) {
                ln = mCities.size();
                firstMatch = mCities.get(0).getName();
            }
            else if (caption.equals(getString(R.string.result_lines_header))) {
                ln = mLines.size();
                firstMatch = mLines.get(0).getName();
            }
            else if (caption.equals(getString(R.string.result_station_header))) {
                ln = mStations.size();
                firstMatch = mStations.get(0).name;
            }

            if (ln > 0 && !firstMatch.equals(getString(R.string.result_no_match)))
                matches = ln;
            return matches;
        }
    };

    private class CityListAdapter extends ArrayAdapter<City> {
        private int mResource;
        private Context mContext;

        CityListAdapter(Context context, int resource, List<City> cities) {
            super(context, resource, cities);
            mResource = resource;
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout itemView;
            City city = getItem(position);

            if (convertView == null) {
                itemView = new LinearLayout(mContext);
                LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                li.inflate(mResource, itemView, true);
            }
            else
                itemView = (LinearLayout)convertView;

            TextView name = (TextView)itemView.findViewById(android.R.id.text1);
            name.setText(city.getName());
            if (city.getName().equals(getString(R.string.result_no_match))) {
                name.setTextColor(Color.GRAY);
                name.setTypeface(null, Typeface.ITALIC);
            }
            else {
                // We have some matches
                TextView tv = (TextView)itemView.findViewById(R.id.label_layout);
                QueryManager finder = SQLQueryManager.getInstance();
                List<String> lines = finder.findLinesInCity(city.getName());
                String strLines = Application.getJoinedList(lines, ",");
                String ls = getString(lines.size() == 1 ? R.string.city_served_by_line :
                    R.string.city_served_by_lines, strLines);
                tv.setText(ls);

                /*
                LinearLayout.LayoutParams zp = new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                zp.setMargins(2, 2, 2, 2);
                TextView tv;
                View[] vs = new View[lines.size()];
                int k = 0;
                for (String line : lines) {
                    tv = new TextView(mContext);
                    //tv.setTextSize(size);
                    tv.setPadding(4, 2, 4 , 2);
                    tv.setLayoutParams(zp);
//                    tv.setShadowLayer(1, 1, 1, Color.BLACK);
                    tv.setTextColor(Color.WHITE);
                    tv.setBackgroundResource(R.layout.tag_background);
                    tv.setText(line);
//                    lay.addView(tv);
                    vs[k] = tv;
                    k ++;
                }
                Log.d("POP", "POPULATE TEXT");
                populateText(lay, vs, mContext);
                 *
                 */
            }

            return itemView;
        }
    }

    private class StationListAdapter extends ArrayAdapter<DBStation> {
        private int mResource;
        private Context mContext;

        StationListAdapter(Context context, int resource, List<DBStation> stations) {
            super(context, resource, stations);
            mResource = resource;
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout itemView;
            DBStation station = getItem(position);

            if (convertView == null) {
                itemView = new LinearLayout(mContext);
                LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                li.inflate(mResource, itemView, true);
            }
            else
                itemView = (LinearLayout)convertView;

            TextView name = (TextView)itemView.findViewById(android.R.id.text1);
            name.setText(station.name);

            QueryManager finder = SQLQueryManager.getInstance();
            Map<String, List<String>> clines = finder.findLinesAndCityFromStation(
                station.name, Long.toString(station.id));

            if (clines.size() > 0) {
                TextView tv = (TextView)itemView.findViewById(R.id.label_layout);
                Set<String> keys = clines.keySet();
                Iterator itr = keys.iterator();
                String city = (String)itr.next();
                List<String> lines = clines.get(city);

                String strLines = Application.getJoinedList(clines.get(city), ",");
                String ls = getString(lines.size() == 1 ? R.string.city_served_by_line :
                    R.string.city_served_by_lines, strLines);
                tv.setText(getString(R.string.station_served_by_lines, city, ls));
            }

            if (station.name.equals(getString(R.string.result_no_match))) {
                name.setTextColor(Color.GRAY);
                name.setTypeface(null, Typeface.ITALIC);
            }

            return itemView;
        }
    }

    /**
     * Handles a click on any item in the list
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final Object obj = l.getItemAtPosition(position);
        mCanFinish = false;
        if (obj instanceof BusLine)
            AllLinesActivity.handleBusLineItemClick(this, l, v, position, id);
        else if(obj instanceof City) {
            // Cities
            City c = (City)obj;
            if (c.isValid()) {
                Intent intent = new Intent(this, CityActivity.class);
                intent.putExtra("cityId", String.valueOf(c.getPK()));
                startActivity(intent);
            }
        }
        else if(obj instanceof DBStation) {
            // Cities
            DBStation st = (DBStation)obj;
            if (st.isValid()) {
                Intent intent = new Intent(this, BusStationGlobalActivity.class);
                intent.putExtra("stationId", String.valueOf(st.id));
                startActivity(intent);
            }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.searchable, menu);
        return true;
    }
}
