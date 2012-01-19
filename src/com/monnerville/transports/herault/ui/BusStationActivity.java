package com.monnerville.transports.herault.ui;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.commonsware.android.listview.SectionedAdapter;
import com.monnerville.transports.herault.HeaderTitle;
import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.BusLine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.monnerville.transports.herault.core.BusManager;
import com.monnerville.transports.herault.core.BusStation;
import com.monnerville.transports.herault.core.BusStop;
import com.monnerville.transports.herault.core.TrafficPatternParser;
import com.monnerville.transports.herault.core.sql.SQLBusManager;

/**
 *
 * @author mathias
 */
public class BusStationActivity extends ListActivity implements HeaderTitle {
    private String mLine = null;
    private String mStation = null;
    private String mDirection = null;
    private List<BusStop> mStops;
    private BusStation mCurrentStation;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.busstation);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.bus_station_title_bar);

        TextView board = (TextView)findViewById(R.id.board);

        final Intent intent = getIntent();
        final Bundle bun = intent.getExtras();
        if (bun != null) {
            mStation = bun.getString("station");
            mLine = bun.getString("line");
            mDirection = bun.getString("direction");
        }
        else
            finish();

        setPrimaryTitle(mStation);
        setSecondaryTitle(getString(R.string.station_line_direction_title, mLine, mDirection));

        setTitle(mLine + " - Station " + mStation);
        BusManager manager = SQLBusManager.getInstance();
        BusLine line = manager.getBusLine(mLine);
        List<BusStation> stations = line.getStations(mDirection);
        for (BusStation st : stations) {
            if (st.getName().equals(mStation)) {
                mStops = st.getStops();
                mCurrentStation = st;
                break;
            }
        }
        if (mCurrentStation != null) {
            BusStop nextStop = mCurrentStation.getNextStop();
            if (nextStop != null)
                board.setText(BusStop.TIME_FORMATTER.format(mCurrentStation.getNextStop().getTime()));
            else
                board.setText(R.string.no_more_stop_short);
        }

        setupAdapter();
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

    static public class BookmarkStationListAdapter extends ArrayAdapter<BusStation> {
        private int mResource;
        private Context mContext;

        BookmarkStationListAdapter(Context context, int resource, List<BusStation> items) {
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
            TextView info = (TextView)itemView.findViewById(android.R.id.text2);
            info.setText(mContext.getString(R.string.bookmark_info, station.getLine().getName(), station.getDirection()));

            TextView sched = (TextView)itemView.findViewById(R.id.icon);
            // Get a cached value
            BusStop st = station.getNextStop(true);
            sched.setText(st == null ? mContext.getString(R.string.no_more_stop_short) :
                BusStop.TIME_FORMATTER.format(st.getTime()));

            int colors[] = { 0x00ffffff, st == null ? 0xffff0000 : 0xff039900, 0x00ffffff };
            GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
//            sched.setBackgroundDrawable(gd);

            TextView city = (TextView)itemView.findViewById(R.id.city);
            city.setText(getFormattedETA(station, st, mContext));

            return itemView;
        }
    }

    /**
     * Returns an i18n formatted estimated time of arrival for a bus stop
     * 
     * @param station bus station
     * @param st bus stop
     * @param ctx application's context
     * @return a fully formatted string
     */
    static public String getFormattedETA(BusStation station, BusStop st, Context ctx) {
        // FIXME: remove station param to clean up the code

        String feta; // formatted ETA
        if (st != null) {
            BusStop.EstimatedTime eta = st.getETA();
            int pattern = -1;
            if (eta.getHours() > 0) {
                if (eta.getHours() > 1) {
                    if (eta.getMinutes() > 0) {
                        if (eta.getMinutes() == 1)
                            pattern = R.string.bookmark_city_eta_hours_hp_ms;
                        else if (eta.getMinutes() > 1)
                            pattern = R.string.bookmark_city_eta_hours_hp_mp;
                        feta = ctx.getString(pattern, station.getCity(), "" + 
                            eta.getHours(), "" + eta.getMinutes());
                    }
                    else {
                        // 0 minute
                        pattern = R.string.bookmark_city_eta_hours_hp;
                        feta = ctx.getString(pattern, station.getCity(), "" + eta.getHours());
                    }
                }
                else {
                    // 1 hour
                    if (eta.getMinutes() > 0) {
                        if (eta.getMinutes() == 1)
                            pattern = R.string.bookmark_city_eta_hours_hs_ms;
                        else if (eta.getMinutes() > 1)
                            pattern = R.string.bookmark_city_eta_hours_hs_mp;
                        feta = ctx.getString(pattern, station.getCity(), "" + 
                            eta.getHours(), "" + eta.getMinutes());
                    }
                    else {
                        // 0 minute
                        pattern = R.string.bookmark_city_eta_hours_hs;
                        feta = ctx.getString(pattern, station.getCity(), "" + eta.getHours());
                    }
                }
            }
            else {
                // 0 hour
                if(eta.getMinutes() == 0)  {
                    feta = ctx.getString(R.string.bookmark_city_eta_ms_now, station.getCity());
                }
                else {
                    pattern = eta.getMinutes() > 1 ? R.string.bookmark_city_eta_mp : R.string.bookmark_city_eta_ms;
                    feta = ctx.getString(pattern, station.getCity(), "" + eta.getMinutes());
                }
            }
        }
        else {
            // No more stop today
            feta = ctx.getString(R.string.bookmark_city, station.getCity());
        }
        return feta;
    }

    /**
     * Custom adapter with matches display
     */
    final SectionedAdapter mAdapter = new CounterSectionedAdapter(this) {
        @Override
        protected int getMatches(String caption) {
            return CounterSectionedAdapter.NO_MATCH;
        }
    };

    /**
     * Sets up the adapter for the list
     */
    private void setupAdapter() {
        mAdapter.addSection(getString(R.string.stop_all_schedules_title),
            new StopListAdapter(this, R.layout.bus_station_list_item, mStops));
        setListAdapter(mAdapter);
    }

    private class StopListAdapter extends ArrayAdapter<BusStop> {
        private int mResource;
        private Context mContext;

        StopListAdapter(Context context, int resource, List<BusStop> stops) {
            super(context, resource, stops);
            mResource = resource;
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout itemView;
            final BusStop st = getItem(position);
            final int j = position;

            if (convertView == null) {
                itemView = new LinearLayout(mContext);
                LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                li.inflate(mResource, itemView, true);
            }
            else
                itemView = (LinearLayout)convertView;

            TextView time = (TextView)itemView.findViewById(android.R.id.text1);
            time.setText(BusStop.TIME_FORMATTER.format(st.getTime()));
            TextView detail = (TextView)itemView.findViewById(android.R.id.text2);
            detail.setText("(" + st.isActive() + ") :: " + TrafficPatternParser.parse(st.getTrafficPattern())
                + " :: "+ st.getTrafficPattern());
            TextView mark = (TextView)itemView.findViewById(R.id.mark);

            // We have a non-cached value
            if (st.isActive()) {
                time.setTextColor(getResources().getColor(R.color.list_item_bus_time));
            }
            else {
                time.setTextColor(getResources().getColor(R.color.list_item_no_more_stop));
            }
            if (mCurrentStation.getNextStop(true) != null) {
                if (mCurrentStation.getNextStop(true).getTime().equals(st.getTime())) {
                    mark.setBackgroundResource(R.color.ht_blue);
                    time.setTypeface(Typeface.DEFAULT_BOLD);
                }
                else {
                    mark.setBackgroundResource(R.color.board_background);
                    time.setTypeface(Typeface.DEFAULT);
                }
            }
            else {
                mark.setBackgroundResource(R.color.board_background);
                time.setTypeface(Typeface.DEFAULT);
            }
            return itemView;
        }
    }
}
