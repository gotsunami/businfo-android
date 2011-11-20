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
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.commonsware.android.listview.SectionedAdapter;
import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.QueryManager;
import com.monnerville.transports.herault.core.sql.SQLQueryManager;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mathias
 */
public class SearchableActivity extends ListActivity {
    // Matches in the result set
    private List<String> mCities;
    private List<String> mStations;
    private List<String> mLines;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.search);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            new StartSearchingTask().execute(query);
        }
        else
            finish();

        getListView().setFastScrollEnabled(true);
    }

    private class StartSearchingTask extends AsyncTask<String, Void, Void> {
        private ProgressDialog mDialog;
        private long mStart;
        final QueryManager finder = SQLQueryManager.getInstance();

        @Override
        protected Void doInBackground(String... q) {
            String query = q[0];
            mCities = finder.findCities(query);
            mStations = finder.findStations(query);
            mLines = finder.findLines(query);
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
            mStations.add(getString(R.string.result_no_match));
        if (mLines.isEmpty())
            mLines.add(getString(R.string.result_no_match));
        mAdapter.addSection(getString(R.string.result_city_header),
            new CityListAdapter(this, R.layout.result_city_list_item, mCities));
        mAdapter.addSection(getString(R.string.result_station_header),
            new StationListAdapter(this, R.layout.result_station_list_item, mStations));
        mAdapter.addSection(getString(R.string.result_line_header),
            new LineListAdapter(this, R.layout.result_line_list_item, mLines));
        setListAdapter(mAdapter);
    }

    /**
     * Get result match count depending on caption header
     * @param caption name of header section
     * @return number of matches
     */
    private int getMatches(String caption) {
        int matches = 0;
        int ln;
        List<String> results = new ArrayList<String>();
        if (caption.equals(getString(R.string.result_city_header)))
            results = mCities;
        else if (caption.equals(getString(R.string.result_line_header)))
            results = mLines;
        else if (caption.equals(getString(R.string.result_station_header)))
            results = mStations;

        ln = results.size();
        if (ln > 0 && !results.get(0).equals(getString(R.string.result_no_match)))
            matches = ln;
        return matches;
    }

	final SectionedAdapter mAdapter = new SectionedAdapter() {
        @Override
		protected View getHeaderView(String caption, int index, View convertView, ViewGroup parent) {
			LinearLayout result = (LinearLayout)convertView;
			if (convertView == null) {
				result = (LinearLayout)getLayoutInflater().inflate(R.layout.list_counter_header, null);
			}
            TextView tv = (TextView)result.findViewById(R.id.result_title);
			tv.setText(caption);

            TextView num = (TextView)result.findViewById(R.id.result_num_match);
            num.setText(String.valueOf(getMatches(caption)));
			return result;
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
                QueryManager finder = SQLQueryManager.getInstance();
                TextView tv = (TextView)itemView.findViewById(R.id.label_layout);
                List<String> lines = finder.findLinesInCity(city);
                StringBuilder sb = new StringBuilder();
                for (String line : lines) {
                    sb.append(line).append(", ");
                }
                sb.replace(sb.length()-2, sb.length(), "");

                String ls = getString(lines.size() == 1 ? R.string.city_served_by_line :
                    R.string.city_served_by_lines, sb.toString());
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

    private class StationListAdapter extends ArrayAdapter<String> {
        private int mResource;
        private Context mContext;

        StationListAdapter(Context context, int resource, List<String> stations) {
            super(context, resource, stations);
            mResource = resource;
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout itemView;
            String station = getItem(position);

            if (convertView == null) {
                itemView = new LinearLayout(mContext);
                LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                li.inflate(mResource, itemView, true);
            }
            else
                itemView = (LinearLayout)convertView;

            TextView name = (TextView)itemView.findViewById(android.R.id.text1);
            name.setText(station);
            if (station.equals(getString(R.string.result_no_match))) {
                name.setTextColor(Color.GRAY);
                name.setTypeface(null, Typeface.ITALIC);
            }

            return itemView;
        }
    }

    private class LineListAdapter extends ArrayAdapter<String> {
        private int mResource;
        private Context mContext;

        LineListAdapter(Context context, int resource, List<String> lines) {
            super(context, resource, lines);
            mResource = resource;
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout itemView;
            String line = getItem(position);

            if (convertView == null) {
                itemView = new LinearLayout(mContext);
                LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                li.inflate(mResource, itemView, true);
            }
            else
                itemView = (LinearLayout)convertView;

            TextView name = (TextView)itemView.findViewById(android.R.id.text1);
            name.setText(line);
            if (line.equals(getString(R.string.result_no_match))) {
                name.setTextColor(Color.GRAY);
                name.setTypeface(null, Typeface.ITALIC);
            }

            return itemView;
        }
    }

    /*
    private void populateText(LinearLayout ll, View[] views, Context ctx) {
        Display display = getWindowManager().getDefaultDisplay();
        ll.removeAllViews();
        //int maxWidth = display.getWidth() - 100;
        int maxWidth = ll.getMeasuredWidth() - 10;

        LinearLayout LL;
        LinearLayout.LayoutParams params;
        LinearLayout newLL = new LinearLayout(ctx);
        newLL.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));
        newLL.setGravity(Gravity.LEFT);
        newLL.setOrientation(LinearLayout.HORIZONTAL);

        int widthSoFar = 0;

        for (int i = 0 ; i < views.length ; i++ ){
            LL = new LinearLayout(ctx);
            LL.setOrientation(LinearLayout.HORIZONTAL);
            LL.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);
            LL.setLayoutParams(new ListView.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            views[i].measure(0,0);
            params = new LinearLayout.LayoutParams(views[i].getMeasuredWidth(),
                    LayoutParams.WRAP_CONTENT);
            params.setMargins(2, 2, 2, 2);
            LL.addView(views[i], params);
            LL.measure(0, 0);
            widthSoFar += views[i].getMeasuredWidth();
            if (widthSoFar >= maxWidth) {
                ll.addView(newLL);

                newLL = new LinearLayout(ctx);
                newLL.setLayoutParams(new LayoutParams(
                        LayoutParams.FILL_PARENT,
                        LayoutParams.WRAP_CONTENT));
                newLL.setOrientation(LinearLayout.HORIZONTAL);
                newLL.setGravity(Gravity.LEFT);
                params = new LinearLayout.LayoutParams(LL
                        .getMeasuredWidth(), LL.getMeasuredHeight());
                newLL.addView(LL, params);
                widthSoFar = LL.getMeasuredWidth();
            } else {
                newLL.addView(LL);
            }
        }
        ll.addView(newLL);
    }
     *
     */
}
