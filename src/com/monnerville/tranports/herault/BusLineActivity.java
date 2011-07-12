package com.monnerville.tranports.herault;

import android.util.Log;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xmlpull.v1.XmlPullParserException;

import com.commonsware.android.listview.SectionedAdapter;

import static com.monnerville.tranports.herault.core.Application.TAG;

import com.monnerville.tranports.herault.core.BusLine;
import com.monnerville.tranports.herault.core.BusManager;
import com.monnerville.tranports.herault.core.BusStation;
import com.monnerville.tranports.herault.core.BusStop;

/**
 *
 * @author mathias
 */
public class BusLineActivity extends ListActivity implements HeaderTitle {
    private String mLine;
    private String mDirection;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.busline);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.bus_line_title_bar);

        final Intent intent = getIntent();
        final Bundle bun = intent.getExtras();
        if (bun != null) {
            mLine = bun.getString("line");
            mDirection = bun.getString("direction");
        }
        else
            finish();

        setPrimaryTitle(getString(R.string.current_line_title, mLine));
        setSecondaryTitle(getString(R.string.line_direction_title, mDirection));

        BusManager manager = BusManager.getInstance();
        try {
            BusLine line = manager.getBusLine(mLine);
            // Computes all next bus stops
            Map<String, List<BusStation>> stationsPerCity = line.getStationsPerCity(mDirection);
            if (!stationsPerCity.isEmpty()) {
                List<String> cities = line.getCities(mDirection);
                for (String city : cities) {
                    List<BusStation> stations = stationsPerCity.get(city);
                    mAdapter.addSection(city, new StationListAdapter(this, R.layout.bus_line_list_item, stations));
                    new StationsStopsRetreiverTask().execute(stations);
                }
                setListAdapter(mAdapter);
            }
            else {
                Log.w(TAG, "Direction '" + mDirection + "' not found");
            }
        } catch (XmlPullParserException ex) {
            Logger.getLogger(BusLineActivity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BusLineActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
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
            BusStation station = getItem(position);

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
            try {
                BusStop stop = station.getNextStop(true);
                // We have a non-cached value
                if (stop != null) {
                    time.setText(BusStop.TIME_FORMATTER.format(stop.getTime()));
                }
            } catch (XmlPullParserException ex) {
                Logger.getLogger(BusLineActivity.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(BusLineActivity.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(BusLineActivity.class.getName()).log(Level.SEVERE, null, ex);
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
        @Override
        protected Void doInBackground(List<BusStation>... st) {
            List<BusStation> stations = st[0];
            try {
                for (BusStation station : stations) {
                    try {
                        // Get a fresh, non-cached value
                        BusStop nextStop = station.getNextStop();
                    } catch (ParseException ex) {
                        Logger.getLogger(BusLineActivity.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (XmlPullParserException ex) {
                Logger.getLogger(BusLineActivity.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(BusLineActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void none) {
            mAdapter.notifyDataSetChanged();
        }
    }
}
