package com.monnerville.transports.herault.ui;

import android.util.Log;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.commonsware.android.listview.SectionedAdapter;
import com.monnerville.transports.herault.HeaderTitle;
import com.monnerville.transports.herault.R;

import static com.monnerville.transports.herault.core.Application.TAG;
import com.monnerville.transports.herault.core.BusLine;

import com.monnerville.transports.herault.core.BusManager;
import com.monnerville.transports.herault.core.BusStation;
import com.monnerville.transports.herault.core.BusStop;
import com.monnerville.transports.herault.core.sql.SQLBusManager;
import com.monnerville.transports.herault.core.xml.XMLBusManager;

/**
 *
 * @author mathias
 */
public class BusLineActivity extends ListActivity implements HeaderTitle {
    private String mLine;
    private String mDirection;
    private SharedPreferences mPrefs;
    private boolean mShowToast;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.busline);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.bus_line_title_bar);
        getListView().setItemsCanFocus(true);

        final Intent intent = getIntent();
        final Bundle bun = intent.getExtras();
        if (bun != null) {
            mLine = bun.getString("line");
            mDirection = bun.getString("direction");
            mShowToast = bun.getBoolean("showToast");
        }
        else
            finish();

        setPrimaryTitle(getString(R.string.current_line_title, mLine));
        setSecondaryTitle(getString(R.string.line_direction_title, mDirection));

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        final BusManager manager = SQLBusManager.getInstance();
        BusLine line = manager.getBusLine(mLine);

        // Computes all next bus stops
        Map<String, List<BusStation>> stationsPerCity = line.getStationsPerCity(mDirection);
        if (!stationsPerCity.isEmpty()) {
            List<String> cities = line.getCities(mDirection);
            List<BusStation> allStations = new ArrayList<BusStation>();
            for (String city : cities) {
                List<BusStation> stations = stationsPerCity.get(city);
                allStations.addAll(stations);
                mAdapter.addSection(city, new StationListAdapter(this, R.layout.bus_line_list_item, stations));
            }
            setListAdapter(mAdapter);
            new StationsStopsRetreiverTask().execute(allStations);
        }
        else {
            Log.w(TAG, "Direction '" + mDirection + "' not found");
        }

        Button flipButton = (Button)findViewById(R.id.btn_flip_direction);
        flipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // Switch to other line direction
                Intent intent = new Intent(BusLineActivity.this, BusLineActivity.class);
                String[] directions = manager.getBusLine(mLine).getDirections();
                for (String dir : directions) {
                    if (!dir.equals(mDirection)) {
                        intent.putExtra("line", mLine);
                        intent.putExtra("direction", dir);
                        intent.putExtra("showToast", true);
                        startActivity(intent);
                    }
                }
            }
        });
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
        manager.saveStarredStations(manager.getBusLine(mLine), mDirection, starredStations, this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                time.setText(BusStop.TIME_FORMATTER.format(stop.getTime()));
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
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, BusStationActivity.class);
        BusStation station = (BusStation)getListView().getItemAtPosition(position);
        intent.putExtra("line", mLine);
        intent.putExtra("direction", mDirection);
        intent.putExtra("station", station.getName());
        startActivity(intent);
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

        @Override
        protected Void doInBackground(List<BusStation>... st) {
            List<BusStation> stations = st[0];
            for (BusStation station : stations) {
                // Get a fresh, non-cached value
                BusStop nextStop = station.getNextStop();
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
        }

        @Override
        protected void onPostExecute(Void none) {
            mAdapter.notifyDataSetChanged();
            mDialog.cancel();
            if (mShowToast) {
                Toast.makeText(BusLineActivity.this, getString(R.string.toast_current_direction,
                    mDirection), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
