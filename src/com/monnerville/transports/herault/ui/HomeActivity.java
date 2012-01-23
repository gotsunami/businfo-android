
package com.monnerville.transports.herault.ui;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.commonsware.android.listview.SectionedAdapter;
import com.monnerville.transports.herault.HeaderTitle;
import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.BusStation;
import com.monnerville.transports.herault.core.sql.SQLBusManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HomeActivity extends ListActivity implements HeaderTitle {
    private SharedPreferences mPrefs;
    private List<BusStation> mStarredStations;
    private List<Action> mMainActions;
    // Cached directions for all available lines

    private final SQLBusManager mManager = SQLBusManager.getInstance();
    /**
     * Used by onResume and updateBookmarks to ensure database is ready
     */
    private boolean mDBReady;
    private BookmarkHandler mBookmarkHandler;

    public static final int ACTION_UPDATE_BOOKMARKS = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTitle(R.string.lines_activity_title);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.search_title_bar);

        // Remove top parent padding (all but left padding)
        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        LinearLayout root = (LinearLayout) decorView.getChildAt(0);
        View titleContainer = root.getChildAt(0);
        titleContainer.setPadding(titleContainer.getPaddingLeft(), 0, 0, 0);

        setPrimaryTitle(getString(R.string.app_name));
        setSecondaryTitle(getString(R.string.slogan));

        new DBCreateOrUpdateTask().execute();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mStarredStations = new ArrayList<BusStation>();
        mMainActions = new ArrayList<Action>();
        mMainActions.add(new Action(getResources().getDrawable(android.R.drawable.ic_btn_speak_now),
            getString(R.string.action_speak_destination)));
        mMainActions.add(new Action(getResources().getDrawable(R.drawable.flatbus),
            getString(R.string.action_show_all_lines)));
        mMainActions.add(new Action(getResources().getDrawable(android.R.drawable.ic_menu_preferences),
            getString(R.string.action_show_settings)));

        Button searchButton = (Button)findViewById(R.id.btn_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // Open search dialog
                onSearchRequested();
            }
        });

        mBookmarkHandler = new BookmarkHandler(mAdapter, mStarredStations);
    }

    private class Action {
        private Drawable drawable;
        private String caption;
        public Action(Drawable icon, String caption) {
            this.drawable = icon;
            this.caption = caption;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Trick for slow emulator or device, in case the DB is not ready yet (or when DB is updating)
        if (!mDBReady) {
            try {
                Thread.sleep(70); // 70 ms
            } catch (InterruptedException ex) {
                Logger.getLogger(HomeActivity.class.getName()).log(Level.SEVERE, null, ex);
            } // 50 ms
        }

        if (mDBReady)
            updateBookmarks();

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
     * Handles bookmark stations
     */
    public static class BookmarkHandler extends Handler {
        private List<BusStation> stations;
        private SectionedAdapter adapter;

        public BookmarkHandler(SectionedAdapter adapter, List<BusStation> stations) {
            super();
            this.stations = stations;
            this.adapter = adapter;
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case HomeActivity.ACTION_UPDATE_BOOKMARKS:
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
    protected void onPause() {
        /* Overwrite existing saved stations with the current list
         * so that bookmark removal work as expected!
         */
        mManager.overwriteStarredStations(mStarredStations, this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mManager.getDB().close();
        super.onDestroy();
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
     * Asynchronous database creating/updating
     */
    private class DBCreateOrUpdateTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog mDialog;
        private long mStart; // benchmark
        private DBHandler mHandler;

        @Override
        protected Void doInBackground(Void... none) {
            mManager.initDB(HomeActivity.this, mHandler);
            return null;
        }

        @Override
        protected void onPreExecute() {
            // Executes on the UI thread
            // Prevents onResume() to update bookmarks before the DB is ready
            mDBReady = false;

            mDialog = new ProgressDialog(HomeActivity.this);
            mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mDialog.setMessage(getString(R.string.pd_updating_database));
            mDialog.setCancelable(false);
            mDialog.setProgress(0);
            mStart = System.currentTimeMillis();
            mHandler = new DBHandler(mDialog);
        }

        @Override
        protected void onPostExecute(Void none) {
            // Back to the UI thread
            Log.d("BENCH0", "DB update duration: " + (System.currentTimeMillis() - mStart) + "ms");
            mHandler = null;

            mDBReady = true;
            updateBookmarks();

            setupAdapter();
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Custom adapter with matches display
     */
    final SectionedAdapter mAdapter = new CounterSectionedAdapter(this) {
        @Override
        protected int getMatches(String caption) {
            if (caption.equals(getString(R.string.all_lines_header))) {
                return mManager.getBusLines().size();
            }
            else if (caption.equals(getString(R.string.all_lines_bookmarks_header))) {
                return mStarredStations.isEmpty() ? CounterSectionedAdapter.NO_MATCH :
                    mStarredStations.size();
            }
            return CounterSectionedAdapter.NO_MATCH;
        }
    };

    /**
     * Updates bookmarked stations with latest next stop
     */
    private void updateBookmarks() {
        List<BusStation> sts = mManager.getStarredStations(this);
        mStarredStations.clear();
        for (BusStation st : sts) {
            mStarredStations.add(st);
            st.getNextStop(); // Fresh, non-cached value
        }
        mAdapter.notifyDataSetChanged();
    }

    private class DBHandler extends Handler {
        ProgressDialog mPd;

        public DBHandler(ProgressDialog pd) {
            super();
            mPd = pd;
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case SQLBusManager.FLUSH_DATABASE_INIT:
                    mPd.show();
                    break;
                case SQLBusManager.FLUSH_DATABASE_PROGRESS:
                    int progress = (Integer)msg.obj;
                    mPd.setProgress(progress);
                    break;
                case SQLBusManager.FLUSH_DATABASE_UPGRADED:
                    mPd.setProgress(100);
                    mPd.cancel();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Sets up the adapter for the list
     */
    private void setupAdapter() {
        mAdapter.addSection(getString(R.string.quick_actions_header),
            new ActionListAdapter(this, R.layout.main_action_list_item, mMainActions));
        mAdapter.addSection(getString(R.string.all_lines_bookmarks_header),
            new BusStationActivity.BookmarkStationListAdapter(this,
            R.layout.bus_line_bookmark_list_item, mStarredStations));
        setListAdapter(mAdapter);
    }

    /**
     * List adapater for main actions (voice recognition etc.)
     */
    private class ActionListAdapter extends ArrayAdapter<Action> {
        private int mResource;
        private Context mContext;

        ActionListAdapter(Context context, int resource, List<Action> items) {
            super(context, resource, items);
            mResource = resource;
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout itemView;
            Action action = getItem(position);

            if (convertView == null) {
                itemView = new LinearLayout(mContext);
                LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                li.inflate(mResource, itemView, true);
            }
            else
                itemView = (LinearLayout)convertView;

            TextView name = (TextView)itemView.findViewById(android.R.id.text1);
            name.setText(action.caption);
            ImageView icon = (ImageView)itemView.findViewById(R.id.icon);
            icon.setBackgroundDrawable(action.drawable);

            return itemView;
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final Object obj = getListView().getItemAtPosition(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(obj instanceof BusStation) {
            final BusStation station = (BusStation)obj;
            builder.setTitle(R.string.bookmark_menu_title);
            builder.setItems(R.array.bookmark_options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    switch (item) {
                        case 0: { // Show station
                            Intent intent = new Intent(HomeActivity.this, BusStationActivity.class);
                            intent.putExtra("line", station.getLine().getName());
                            intent.putExtra("direction", station.getDirection());
                            intent.putExtra("station", station.getName());
                            startActivity(intent);
                            break;
                        }
                        case 1: { // Show line
                            Intent intent = new Intent(HomeActivity.this, BusLineActivity.class);
                            intent.putExtra("line", station.getLine().getName());
                            intent.putExtra("direction", station.getDirection());
                            startActivity(intent);
                            break;
                        }
                        case 2: { // Share
                            station.share(HomeActivity.this);
                            break;
                        }
                        case 3: // Remove
                            for (BusStation st : mStarredStations) {
                                if (st == obj) {
                                    mStarredStations.remove(st);
                                    break;
                                }
                            }
                            mAdapter.notifyDataSetChanged();
                            break;
                        default:
                            break;
                    }
                }
            });
            builder.show();
        }
        else if(obj instanceof Action) {
            final Action what = (Action)obj;
            if (what.caption.equals(getString(R.string.action_show_all_lines))) {
                Intent intent = new Intent(HomeActivity.this, AllLinesActivity.class);
                startActivity(intent);
            }
            else if(what.caption.equals(getString(R.string.action_speak_destination))) {
                // TODO
            }
            else if(what.caption.equals(getString(R.string.action_show_settings))) {
                startActivity(new Intent(this, AppPreferenceActivity.class));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                // Open search dialog
                onSearchRequested();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
