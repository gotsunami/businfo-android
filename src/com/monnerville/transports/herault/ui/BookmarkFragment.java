package com.monnerville.transports.herault.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.commonsware.android.listview.SectionedAdapter;
import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.BusStation;
import com.monnerville.transports.herault.core.City;
import com.monnerville.transports.herault.core.QueryManager;
import com.monnerville.transports.herault.core.sql.SQLBusManager;
import com.monnerville.transports.herault.core.sql.SQLQueryManager;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mathias
 */
public class BookmarkFragment extends ListFragment {
    private final SQLBusManager mManager = SQLBusManager.getInstance();
    private BusStationActivity.BookmarkStationListAdapter mAdapter;
    private List<BusStation> mStarredStations;
    private BookmarkHandler mBookmarkHandler;
    private Context mContext;
    /**
     * Used by onResume and updateBookmarks to ensure database is ready
     */
    private boolean mDBReady;

    public static final int ACTION_UPDATE_BOOKMARKS = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bookmarks, container, false);
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        mContext = null;
        mDBReady= false;
        mStarredStations = new ArrayList<BusStation>();
    }

    public void setDatabaseReady(boolean ready) {
        mDBReady = ready;
    }

    /**
     * Handler called by a thread every minute to update stations schedules.
     */
    public class BookmarkHandler extends Handler {
        public BookmarkHandler(ArrayAdapter adapter, List<BusStation> stations) {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case ACTION_UPDATE_BOOKMARKS:
                    new ComputeNextStopsTask().execute();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Handles bookmark stations
     */
    public static class SectionedBookmarkHandler extends Handler {
        private final List<BusStation> stations;
        private SectionedAdapter adapter;

        public SectionedBookmarkHandler(SectionedAdapter adapter, List<BusStation> stations) {
            super();
            this.stations = stations;
            this.adapter = adapter;
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case ACTION_UPDATE_BOOKMARKS:
                    for (BusStation st : stations) {
                        st.getNextStop(); // Fresh, non-cached value
                    }
                    this.adapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        if (mDBReady) {
            updateBookmarks();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        // Update bookmark info every minute
                        Thread.sleep(1000*60);
                        mBookmarkHandler.sendEmptyMessage(ACTION_UPDATE_BOOKMARKS);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(SplashActivity.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();
        mDBReady = true;
        mAdapter = new BusStationActivity.BookmarkStationListAdapter(mContext,
            R.layout.bus_line_bookmark_list_item, mStarredStations);
        setListAdapter(mAdapter);
        mBookmarkHandler = new BookmarkHandler(mAdapter, mStarredStations);
    }

    /**
     * Updates bookmarked stations with latest next stop
     */
    private void updateBookmarks() {
        if (mContext == null) {
            return;
        }
        new ComputeFreshNextStopsTask().execute();
    }

    private class ComputeFreshNextStopsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... none) {
            List<BusStation> sts = mManager.getStarredStations(mContext);
            mStarredStations.clear();
            for (BusStation st : sts) {
                mStarredStations.add(st);
                try {
                    st.getNextStop(); // Fresh, non-cached value
                } catch (Exception ex) {
                    // Could not restore station? Delete it.
                    mStarredStations.remove(st);
                    // FIXME
                    Log.e("ST", "Could not restore station!");
                } 
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Void none) {
            // Back to the UI thread
            mAdapter.notifyDataSetChanged();
        }
    }

    private class ComputeNextStopsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... none) {
            for (BusStation st : mStarredStations) {
                st.getNextStop(); // Fresh, non-cached value
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Void none) {
            // Back to the UI thread
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    
        final Object obj = l.getItemAtPosition(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if(obj instanceof BusStation) {
            final BusStation station = (BusStation)obj;
            builder.setTitle(R.string.bookmark_menu_title);
            builder.setItems(R.array.bookmark_options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    switch (item) {
                        case 0: { // All schedules (show station)
                            Intent intent = new Intent(getActivity(), BusStationActivity.class);
                            intent.putExtra("line", station.getLine().getName());
                            intent.putExtra("direction", station.getDirection());
                            intent.putExtra("station", station.getName());
                            intent.putExtra("network", station.getLine().getBusNetworkName());
                            startActivity(intent);
                            break;
                        }
                        case 1: { // All line stations (show line)
                            Intent intent = new Intent(getActivity(), BusLineActivity.class);
                            intent.putExtra("line", station.getLine().getName());
                            intent.putExtra("direction", station.getDirection());
                            intent.putExtra("network", station.getLine().getBusNetworkName());
                            startActivity(intent);
                            break;
                        }
                        case 2: { // All lines in city (show city)
                            QueryManager finder = SQLQueryManager.getInstance();
                            List<City> cs = finder.findCities(station.getCity(), true);
                            if (cs.isEmpty()) return;
                            City c = cs.get(0);
                            if (c.isValid()) {
                                Intent intent = new Intent(getActivity(), CityActivity.class);
                                intent.putExtra("cityId", String.valueOf(c.getPK()));
                                startActivity(intent);
                            }
                            break;
                        }
                        case 3: { // Share
                            station.share(getActivity());
                            break;
                        }
                        case 4: // Remove
                            for (BusStation st : mStarredStations) {
                                if (st == obj) {
                                    mStarredStations.remove(st);
                                    break;
                                }
                            }
                            mManager.overwriteStarredStations(mStarredStations, getActivity());
                            mAdapter.notifyDataSetChanged();
                            break;
                        default:
                            break;
                    }
                }
            });
            builder.show();
        }
    }
}
