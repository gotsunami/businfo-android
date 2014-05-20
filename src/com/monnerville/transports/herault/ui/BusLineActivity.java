package com.monnerville.transports.herault.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.commonsware.android.listview.SectionedAdapter;
import com.google.android.maps.MapActivity;
import com.monnerville.transports.herault.HeaderTitle;
import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.AbstractBusLine;
import com.monnerville.transports.herault.core.Application;
import static com.monnerville.transports.herault.core.Application.TAG;
import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.BusManager;
import com.monnerville.transports.herault.core.BusStation;
import com.monnerville.transports.herault.core.BusStop;
import com.monnerville.transports.herault.core.City;

import com.monnerville.transports.herault.core.QueryManager;
import com.monnerville.transports.herault.core.sql.SQLBusManager;
import com.monnerville.transports.herault.core.sql.SQLBusNetwork;
import com.monnerville.transports.herault.core.sql.SQLQueryManager;
import com.monnerville.transports.herault.ui.maps.BaseItemsOverlay;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mathias
 */
public class BusLineActivity extends MapActivity implements HeaderTitle, OnItemClickListener {
    private String mNetwork;
    private String mLine;
    private String mDirection;
    private SharedPreferences mPrefs;
    private boolean mShowToast;
    private boolean mCanFinish = false;
    private ListView mList;

    private final int DEFAULT_MAP_ZOOM = 13;
    private QueryManager mFinder = SQLQueryManager.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (Application.OSBeforeHoneyComb()) {
            requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        }
        setContentView(R.layout.busline);
        if (Application.OSBeforeHoneyComb()) 
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.bus_line_title_bar);
        else {
            ActionBarHelper.setDisplayHomeAsUpEnabled(this, true);
        }

        mList = (ListView)findViewById(R.id.stationslist);
        mList.setItemsCanFocus(true);
        mList.setOnItemClickListener(this);

        final Intent intent = getIntent();
        final Bundle bun = intent.getExtras();
        if (bun != null) {
            mNetwork = bun.getString("network");
            mLine = bun.getString("line");
            mDirection = bun.getString("direction");
            mShowToast = bun.getBoolean("showToast");
        }
        else
            finish();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        final BusManager manager = SQLBusManager.getInstance();
        BusLine line = manager.getBusLine(new SQLBusNetwork(mNetwork), mLine);
        if (Application.OSBeforeHoneyComb()) {
            setPrimaryTitle(getString(R.string.current_line_title, mLine));
            setSecondaryTitle(getString(R.string.line_direction_title, line.getDirectionHumanReadableFor(mDirection)));
        }
        else {
            ActionBarHelper.setTitle(this, getString(R.string.current_line_title, mLine));
            ActionBarHelper.setSubtitle(this, getString(R.string.line_direction_title, line.getDirectionHumanReadableFor(mDirection)));
        }


        TextView lineIcon = (TextView)findViewById(R.id.line_icon);
        if (Application.OSBeforeHoneyComb()) {
            AllLinesActivity.setLineTextViewStyle(this, line, lineIcon);
        }

        /**
        MapView mapView = (MapView) findViewById(R.id.mapview);
        MapController controller = mapView.getController();
         */ 

        // Display city on map
        Drawable marker = getResources().getDrawable(android.R.drawable.star_on);
        int markerWidth = marker.getIntrinsicWidth();
        int markerHeight = marker.getIntrinsicHeight();
        marker.setBounds(0, markerHeight, markerWidth, 0);

        BaseItemsOverlay busOverlay = new BaseItemsOverlay(marker);

        // Computes all next bus stops
        Map<String, List<BusStation>> stationsPerCity = line.getStationsPerCity(mDirection);
        if (!stationsPerCity.isEmpty()) {
            List<String> cities = line.getCities(mDirection);
            List<BusStation> allStations = new ArrayList<BusStation>();
            for (String city : cities) {
                List<BusStation> stations = stationsPerCity.get(city);
                allStations.addAll(stations);
                mAdapter.addSection(City.removeSelfSuffix(city),
                    new StationListAdapter(this, R.layout.bus_line_list_item, stations));
            }
            mList.setAdapter(mAdapter);
            new StationsStopsRetreiverTask().execute(allStations);
        }
        else {
            Log.w(TAG, "Direction '" + mDirection + "' not found");
        }

        if (Application.OSBeforeHoneyComb()) {
            Button flipButton = (Button)findViewById(R.id.btn_flip_direction);
            flipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    flipLineDirection();
                }
            });

            Button searchButton = (Button)findViewById(R.id.btn_search);
            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // Open search dialog
                    onSearchRequested();
                }
            });
        }
    }

    /**
     * Flip line's direction
     */
    private void flipLineDirection() {
        // Switch to other line direction
        BusManager manager = SQLBusManager.getInstance();
        Intent intent = new Intent(this, BusLineActivity.class);
        List<City> directions = manager.getBusLine(new SQLBusNetwork(mNetwork), mLine).getDirections();
        for (City dir : directions) {
            if (!dir.equals(mDirection)) {
                intent.putExtra("line", mLine);
                intent.putExtra("direction", dir.getName());
                intent.putExtra("showToast", true);
                intent.putExtra("network", mNetwork);
                mCanFinish = true;
                startActivity(intent);
                break;
            }
        }
    }

    /**
     * Save any starred bus station (permanent storage)
     */
    @Override
    protected void onPause() {
        BusManager manager = SQLBusManager.getInstance();
        List <BusStation> starredStations = new ArrayList<BusStation>();
        for (int i=0; i < mAdapter.getCount(); i++) {
            Object o = mAdapter.getItem(i);
            if (o instanceof BusStation) {
                BusStation st = (BusStation)mAdapter.getItem(i);
                if (st.isStarred())
                    starredStations.add(st);
            }
        }
        manager.saveStarredStations(manager.getBusLine(new SQLBusNetwork(mNetwork), mLine), 
            mDirection, starredStations, this);

        // Force finishing the activity so it's not in the activity stack if
        // performing multiple searches
        if (mCanFinish)
            finish();
        super.onPause();
    }

    @Override
    protected void onResume() {
        // Updates starred stations
        BusManager manager = SQLBusManager.getInstance();
        List<BusStation> sts = manager.getStarredStations(BusLineActivity.this);

        for (int i=0; i < mAdapter.getCount(); i++) {
            Object o = mAdapter.getItem(i);
            if (o instanceof BusStation) {
                BusStation st = (BusStation)mAdapter.getItem(i);
                for (BusStation star : sts) {
                    if (st.equals(star)) {
                        st.setStarred(true);
                        break;
                    }
                }
            }
        }

        mAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    private class StationListAdapter extends ArrayAdapter<BusStation> {
        private int mResource;
        private Context mContext;

        StationListAdapter(Context context, int resource, List<BusStation> items) {
            super(context, resource, items);
            mResource = resource;
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout itemView;
            final BusStation station = getItem(position);
            final int j = position;

            if (convertView == null) {
                itemView = new LinearLayout(mContext);
                LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                li.inflate(mResource, itemView, true);
            }
            else
                itemView = (LinearLayout)convertView;

            TextView name = (TextView)itemView.findViewById(android.R.id.text1);
            name.setText(station.getName());
            TextView time = (TextView)itemView.findViewById(R.id.time);
            ImageView star = (ImageView)itemView.findViewById(R.id.star);
            star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    station.setStarred(!station.isStarred());
                    mAdapter.notifyDataSetChanged();
                }
            });
            star.setImageResource(station.isStarred() ? android.R.drawable.btn_star_big_on :
               android.R.drawable.btn_star_big_off);

            BusStop stop = station.getNextStop(true);
            // We have a non-cached value
            if (stop != null) {
                time.setTextColor(getResources().getColor(R.color.list_item_bus_time));
                time.setText(BusStop.TIME_FORMATTER.format(stop.getTime()));
            }
            else {
                time.setTextColor(getResources().getColor(R.color.list_item_no_more_stop));
                time.setText(R.string.no_more_stop);
            }
            return itemView;
        }
    }

	final SectionedAdapter mAdapter = new SectionedAdapter() {
        @Override
		protected View getHeaderView(String caption, int index, View convertView, ViewGroup parent) {
			TextView result = (TextView)convertView;
			if (convertView == null) {
				result = (TextView)getLayoutInflater().inflate(R.layout.list_header, null);
			}
			result.setText(caption);
			return(result);
		}
	};

    @Override
    public void onItemClick(AdapterView<?> av, View v, int position, long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final BusStation station = (BusStation)mList.getItemAtPosition(position);

        builder.setTitle(station.getName());
        builder.setItems(R.array.show_or_share_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0: { // Show all schedules
                        Intent intent = new Intent(BusLineActivity.this, BusStationActivity.class);
                        intent.putExtra("line", mLine);
                        intent.putExtra("direction", mDirection);
                        intent.putExtra("station", station.getName());
                        startActivity(intent);
                        break;
                    }
                    case 1: { // Share
                        station.share(BusLineActivity.this);
                        break;
                    }
                    case 2: { // Show city activity with all lines
                        List<City> cs = mFinder.findCities(station.getCity(), true);
                        if (cs.isEmpty()) return;
                        City c = cs.get(0);
                        if (c.isValid()) {
                            Intent intent = new Intent(BusLineActivity.this, CityActivity.class);
                            intent.putExtra("cityId", String.valueOf(c.getPK()));
                            startActivity(intent);
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        });
        builder.show();
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
     * Retrieves next stops for all bus stations in a background thread
     */
    private class StationsStopsRetreiverTask extends AsyncTask<List<BusStation>, Void, Void> {
        private List<BusStation> starredStations;
        private ProgressDialog mDialog;
        private long mStart;

        @Override
        protected Void doInBackground(List<BusStation>... st) {
            List<BusStation> stations = st[0];
            for (BusStation station : stations) {
                // Get a fresh, non-cached value
                station.getNextStop();
                station.setStarred(starredStations.contains(station));
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            // Executes on the UI thread
            BusManager manager = SQLBusManager.getInstance();
            starredStations = manager.getStarredStations(BusLineActivity.this);
            mDialog = ProgressDialog.show(BusLineActivity.this, "",
                getString(R.string.pd_loading_bus_schedules), true);
            mStart = System.currentTimeMillis();
        }

        @Override
        protected void onPostExecute(Void none) {
            Log.d(TAG, "duration: " + (System.currentTimeMillis() - mStart) + "ms");
            try {
                mAdapter.notifyDataSetChanged();
                mDialog.cancel();
                if (mShowToast) {
                    Toast.makeText(BusLineActivity.this, getString(R.string.toast_current_direction,
                        City.removeSelfSuffix(mDirection)), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                /* NOP */
                // Prevents random "IllegalArgumentException: View not attached to window manager"
                // exception
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
            case R.id.action_flip_direction:
                flipLineDirection();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.busline, menu);
        return true;
    }
}
