package com.monnerville.transports.herault.ui;

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
import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.BusStation;
import com.monnerville.transports.herault.core.sql.SQLBusManager;
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

        mDBReady= false;
        mStarredStations = new ArrayList<BusStation>();
        mAdapter = new BusStationActivity.BookmarkStationListAdapter(getActivity(),
            R.layout.bus_line_bookmark_list_item, mStarredStations);
        setListAdapter(mAdapter);

        mBookmarkHandler = new BookmarkHandler(mAdapter, mStarredStations);
    }

    @Override 
    public void onListItemClick(ListView l, View v, int position, long id) {
        // TODO
    }

    public void setDatabaseReady(boolean ready) {
        mDBReady = ready;
        if (ready) {
            updateBookmarks();
        }
    }

    /**
     * Handles bookmark stations
     */
    public static class BookmarkHandler extends Handler {
        private List<BusStation> stations;
        private ArrayAdapter adapter;

        public BookmarkHandler(ArrayAdapter adapter, List<BusStation> stations) {
            super();
            this.stations = stations;
            this.adapter = adapter;
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case BookmarkFragment.ACTION_UPDATE_BOOKMARKS:
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
        /* TODO
         * Trick for slow emulator or device, in case the DB is not ready yet (or when DB is updating)
         * Strangely, removing this statement on slow device won't display the upgrading process...
         */
        if (!mDBReady) {
            try {
                Thread.sleep(70); // 70 ms
            } catch (InterruptedException ex) {
                Logger.getLogger(HomeActivity.class.getName()).log(Level.SEVERE, null, ex);
            } // 50 ms
        }

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
                    Logger.getLogger(HomeActivity.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    /**
     * Updates bookmarked stations with latest next stop
     */
    private void updateBookmarks() {
        List<BusStation> sts = mManager.getStarredStations(getActivity());
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
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        mManager.getDB().close();
        super.onDestroy();
    }
}
