package com.monnerville.transports.herault.ui;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.commonsware.android.listview.SectionedAdapter;
import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.QueryManager;
import com.monnerville.transports.herault.core.sql.SQLQueryManager;
import com.monnerville.transports.herault.core.sql.DBStation;
import com.monnerville.transports.herault.core.sql.SQLBusLine;
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
    // Matches in the result set
    private List<String> mCities;
    private List<DBStation> mStations;
    private List<BusLine> mLines;
    private List<List<String>> mDirections;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.search_title_bar);
        getListView().setFastScrollEnabled(true);

        TextView tp = (TextView)findViewById(R.id.primary);
        tp.setText(getString(R.string.app_name));
        TextView ts = (TextView)findViewById(R.id.secondary);

        mDirections = new ArrayList<List<String>>();

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            ts.setText(getString(R.string.search_keyword, query));
            new StartSearchingTask().execute(query);
        }
        else
            finish();

        Button searchButton = (Button)findViewById(R.id.btn_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // Open search dialog
                onSearchRequested();
            }
        });
    }

    private class StartSearchingTask extends AsyncTask<String, Void, Void> {
        private ProgressDialog mDialog;
        private long mStart;
        final QueryManager finder = SQLQueryManager.getInstance();

        @Override
        protected Void doInBackground(String... q) {
            String query = q[0];
            mCities = finder.findCities(query);
            mStations = (List<DBStation>)finder.findStations(query);
            mLines = finder.findLines(query);
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
            mCities.add(getString(R.string.result_no_match));
        if (mStations.isEmpty())
            mStations.add(new DBStation(0, getString(R.string.result_no_match)));

        if (mLines.isEmpty())
            mLines.add(new SQLBusLine(getString(R.string.result_no_match)));
            //mLines.add(getString(R.string.result_no_match));
        mAdapter.addSection(getString(R.string.result_city_header),
            new CityListAdapter(this, R.layout.result_city_list_item, mCities));
        mAdapter.addSection(getString(R.string.result_station_header),
            new StationListAdapter(this, R.layout.result_station_list_item, mStations));
        mAdapter.addSection(getString(R.string.result_line_header),
            new LineListAdapter(this, R.layout.result_line_list_item, mLines));
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
                firstMatch = mCities.get(0);
            }
            else if (caption.equals(getString(R.string.result_line_header))) {
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

    private class CityListAdapter extends ArrayAdapter<String> {
        private int mResource;
        private Context mContext;

        CityListAdapter(Context context, int resource, List<String> cities) {
            super(context, resource, cities);
            mResource = resource;
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout itemView;
            String city = getItem(position);

            if (convertView == null) {
                itemView = new LinearLayout(mContext);
                LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                li.inflate(mResource, itemView, true);
            }
            else
                itemView = (LinearLayout)convertView;

            TextView name = (TextView)itemView.findViewById(android.R.id.text1);
            name.setText(city);
            if (city.equals(getString(R.string.result_no_match))) {
                name.setTextColor(Color.GRAY);
                name.setTypeface(null, Typeface.ITALIC);
            }
            else {
                // We have some matches
                TextView tv = (TextView)itemView.findViewById(R.id.label_layout);
                QueryManager finder = SQLQueryManager.getInstance();
                List<String> lines = finder.findLinesInCity(city);
                String strLines = getJoinedList(lines, ",");
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

    /**
     * Joins a list's elements with a string separator
     */
    private String getJoinedList(List<String> data, String separator) {
        StringBuilder sb = new StringBuilder();
        for (String d : data) {
            sb.append(d).append(separator).append(" ");
        }
        sb.replace(sb.length()-1-separator.length(), sb.length(), "");
        return sb.toString();
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
                station.name, Integer.toString(station.id));

            if (clines.size() > 0) {
                TextView tv = (TextView)itemView.findViewById(R.id.label_layout);
                Set<String> keys = clines.keySet();
                Iterator itr = keys.iterator();
                String city = (String)itr.next();
                List<String> lines = clines.get(city);

                String strLines = getJoinedList(clines.get(city), ",");
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

    private class LineListAdapter extends ArrayAdapter<BusLine> {
        private int mResource;
        private Context mContext;

        LineListAdapter(Context context, int resource, List<BusLine> lines) {
            super(context, resource, lines);
            mResource = resource;
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout itemView;
            BusLine line = getItem(position);

            if (convertView == null) {
                itemView = new LinearLayout(mContext);
                LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                li.inflate(mResource, itemView, true);
            }
            else
                itemView = (LinearLayout)convertView;

            TextView name = (TextView)itemView.findViewById(android.R.id.text1);
            name.setText(line.getName());
            GradientDrawable gd;
            TextView col = (TextView)itemView.findViewById(R.id.line_color);

            if (line.getName().equals(getString(R.string.result_no_match))) {
                name.setTextColor(Color.GRAY);
                name.setTypeface(null, Typeface.ITALIC);
                int colors[] = { BusLine.DEFAULT_COLOR, AllLinesActivity.getLighterColor(BusLine.DEFAULT_COLOR, 2) };
                gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
                col.setText("?");
            }
            else {
                TextView direction = (TextView)itemView.findViewById(R.id.direction);

                if (line.getColor() != BusLine.DEFAULT_COLOR) {
                    col.setText("");
                    int colors[] = { line.getColor(), AllLinesActivity.getLighterColor(line.getColor(), 2) };
                    gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
                }
                else {
                    int colors[] = { BusLine.DEFAULT_COLOR, AllLinesActivity.getLighterColor(BusLine.DEFAULT_COLOR, 2) };
                    gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
                    col.setText("?");
                }

                try {
                    List<String> dirs = mDirections.get(position);
                    direction.setText(dirs.get(0) + " - " + dirs.get(1));
                } catch(IndexOutOfBoundsException ex) {}
            }
            gd.setCornerRadius(5);
            col.setBackgroundDrawable(gd);

            return itemView;
        }
    }
}
