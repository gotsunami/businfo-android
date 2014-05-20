package com.monnerville.transports.herault.ui;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.commonsware.android.listview.SectionedAdapter;
import com.monnerville.transports.herault.HeaderTitle;
import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.Application;
import com.monnerville.transports.herault.core.BusLine;
import java.util.ArrayList;
import java.util.List;

import com.monnerville.transports.herault.core.BusManager;
import com.monnerville.transports.herault.core.BusStation;
import com.monnerville.transports.herault.core.BusStop;
import com.monnerville.transports.herault.core.City;
import com.monnerville.transports.herault.core.TrafficPatternParser;
import com.monnerville.transports.herault.core.sql.SQLBusManager;
import com.monnerville.transports.herault.core.sql.SQLBusNetwork;
import java.util.Calendar;

/**
 *
 * @author mathias
 */
public class BusStationActivity extends ListActivity implements HeaderTitle {
    private String mNetwork;
    private String mLine = null;
    private String mStation = null;
    private String mDirection = null;
    private List<BusStop> mStops;
    private BusStation mCurrentStation;
    private final Calendar mNow = Calendar.getInstance();

    // For mapping week days in StopListAdapter
    private static final int[][] mWDays = {
        { R.id.monday,    TrafficPatternParser.MONDAY },
        { R.id.tuesday,   TrafficPatternParser.TUESDAY },
        { R.id.wednesday, TrafficPatternParser.WEDNESDAY },
        { R.id.thursday,  TrafficPatternParser.THURSDAY },
        { R.id.friday,    TrafficPatternParser.FRIDAY },
        { R.id.saturday,  TrafficPatternParser.SATURDAY },
        { R.id.sunday,    TrafficPatternParser.SUNDAY },
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (Application.OSBeforeHoneyComb()) {
            requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        }
        setContentView(R.layout.busstation);
        if (Application.OSBeforeHoneyComb()) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.bus_station_title_bar);
        }
        else {
            ActionBarHelper.setDisplayHomeAsUpEnabled(this, true);
        }

        TextView board = (TextView)findViewById(R.id.board);

        final Intent intent = getIntent();
        final Bundle bun = intent.getExtras();
        if (bun != null) {
            mNetwork = bun.getString("network");
            mStation = bun.getString("station");
            mLine = bun.getString("line");
            mDirection = bun.getString("direction");
        }
        else
            finish();

        BusManager manager = SQLBusManager.getInstance();
        BusLine line = manager.getBusLine(new SQLBusNetwork(mNetwork), mLine);
        if (Application.OSBeforeHoneyComb()) {
            setPrimaryTitle(mStation);
            setSecondaryTitle(getString(R.string.station_line_direction_title, mLine,
                line.getDirectionHumanReadableFor(mDirection)));
        }
        else {
            ActionBarHelper.setTitle(this, mStation);
            ActionBarHelper.setSubtitle(this, getString(R.string.station_line_direction_title, mLine,
                line.getDirectionHumanReadableFor(mDirection)));
        }

        setTitle(mLine + " - Station " + mStation);
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

        // Handles starred station status
        List<BusStation> starredStations = manager.getStarredStations(this);
        mCurrentStation.setStarred(starredStations.contains(mCurrentStation));

        if (Application.OSBeforeHoneyComb()) {
            final ImageView star = (ImageView)findViewById(R.id.station_star);
            star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCurrentStation.setStarred(!mCurrentStation.isStarred());
                    star.setImageResource(mCurrentStation.isStarred() ? android.R.drawable.btn_star_big_on :
                       android.R.drawable.btn_star_big_off);
                }
            });
            star.setImageResource(mCurrentStation.isStarred() ? android.R.drawable.btn_star_big_on :
               android.R.drawable.btn_star_big_off);
        }

        setupAdapter();
    }

    /**
     * Save current station starred status
     */
    @Override
    protected void onPause() {
        BusManager manager = SQLBusManager.getInstance();
        List <BusStation> starredStations = new ArrayList<BusStation>();
        if (mCurrentStation.isStarred())
            starredStations.add(mCurrentStation);
        manager.saveStarredStations(manager.getBusLine(new SQLBusNetwork(mNetwork), mLine), 
            mDirection, starredStations, this);

        super.onPause();
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
            info.setText(mContext.getString(R.string.bookmark_info, station.getLine().getName(), 
                City.removeSelfSuffix(station.getDirection())));
            TextView net = (TextView)itemView.findViewById(R.id.network);
            net.setText(station.getLine().getBusNetworkName());

            TextView sched = (TextView)itemView.findViewById(R.id.icon);
            // Get a cached value
            BusStop st = station.getNextStop(true);
            sched.setText(st == null ? mContext.getString(R.string.no_more_stop_short) :
                BusStop.TIME_FORMATTER.format(st.getTime()));

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
        String city = City.removeSelfSuffix(station.getCity());
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
                        feta = ctx.getString(pattern, city, "" + eta.getHours(), "" + eta.getMinutes());
                    }
                    else {
                        // 0 minute
                        pattern = R.string.bookmark_city_eta_hours_hp;
                        feta = ctx.getString(pattern, city, "" + eta.getHours());
                    }
                }
                else {
                    // 1 hour
                    if (eta.getMinutes() > 0) {
                        if (eta.getMinutes() == 1)
                            pattern = R.string.bookmark_city_eta_hours_hs_ms;
                        else if (eta.getMinutes() > 1)
                            pattern = R.string.bookmark_city_eta_hours_hs_mp;
                        feta = ctx.getString(pattern, city, "" + eta.getHours(), "" + eta.getMinutes());
                    }
                    else {
                        // 0 minute
                        pattern = R.string.bookmark_city_eta_hours_hs;
                        feta = ctx.getString(pattern, city, "" + eta.getHours());
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
                    feta = ctx.getString(pattern, city, "" + eta.getMinutes());
                }
            }
        }
        else {
            // No more stop today
            feta = ctx.getString(R.string.bookmark_city, city);
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

            TextView warn = (TextView)itemView.findViewById(R.id.warn);
            if ((st.getBinaryTrafficPattern() & TrafficPatternParser.SCHOOL) != 0) {
                warn.setVisibility(View.VISIBLE);
                warn.setText(getString(R.string.stop_info_school_days_only));
            }
            else if((st.getBinaryTrafficPattern() & TrafficPatternParser.RESTDAYS) != 0) {
                warn.setVisibility(View.VISIBLE);
                warn.setText(getString(R.string.stop_info_rest_days));
            }
            else if((st.getBinaryTrafficPattern() & TrafficPatternParser.HOLIDAYS) != 0) {
                warn.setVisibility(View.VISIBLE);
                warn.setText(getString(R.string.stop_info_holidays_only));
            }
            else {
                warn.setVisibility(View.GONE);
                warn.setText("");
            }

            TextView mark = (TextView)itemView.findViewById(R.id.mark);

            int pat = st.getBinaryTrafficPattern();
            TextView tvd;
            int lenab, ldis;
            for (int k=0; k < mWDays.length; k++) {
                tvd = (TextView)itemView.findViewById(mWDays[k][0]);
                if (TrafficPatternParser.calendarMap.get(mNow.get(Calendar.DAY_OF_WEEK)) ==
                    mWDays[k][1]) {
                    // Show today in a different way
                    tvd.setTypeface(Typeface.DEFAULT_BOLD);
                    lenab = R.layout.dow_bkg_today_enabled;
                    ldis = R.layout.dow_bkg_today_disabled;
                }
                else {
                    // Rest of the week, standard layouts
                    lenab = R.layout.dow_bkg_enabled;
                    ldis = R.layout.dow_bkg_disabled;
                }
                tvd.setBackgroundResource(((pat & mWDays[k][1]) != 0) ? lenab : ldis);
            }

            // We have a non-cached value
            TextView circmsg = (TextView)itemView.findViewById(R.id.circulmsg);
            if (st.isActive()) {
                time.setTextColor(getResources().getColor(R.color.list_item_bus_time));
                circmsg.setText("");
            }
            else {
                time.setTextColor(getResources().getColor(R.color.list_item_no_more_stop));
                circmsg.setText(R.string.does_not_circulate);
            }
            if (mCurrentStation.getNextStop(true) != null) {
                if (st.isActive() && mCurrentStation.getNextStop(true).getTime().equals(st.getTime())) {
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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final Object obj = getListView().getItemAtPosition(position);
        final BusStop st = (BusStop)obj;
        if (st.isActive()) shareStop(st);
    }

    private void shareStop(BusStop st) {
        if (st == null) return;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_msg, mCurrentStation.getName(),
            mCurrentStation.getLine().getName(), BusStop.TIME_FORMATTER.format(st.getTime()),
            getFormattedETA(mCurrentStation, st, this)));
        startActivity(Intent.createChooser(intent, getString(R.string.share_with_title)));
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
            case R.id.action_rating:
                mCurrentStation.setStarred(!mCurrentStation.isStarred());
                item.setIcon(mCurrentStation.isStarred() ? R.drawable.rating_important :
                   R.drawable.rating_not_important);
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
        inflater.inflate(R.menu.busstation, menu);
        // Sets the star icon state when activity launches (3.0+ only)
        if (!Application.OSBeforeHoneyComb()) {
            MenuItem starItem = menu.findItem(R.id.action_rating);
            starItem.setIcon(mCurrentStation.isStarred() ? R.drawable.rating_important :
               R.drawable.rating_not_important);
        }
        return true;
    }
}
