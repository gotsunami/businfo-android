
package com.monnerville.transports.herault.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.commonsware.android.listview.SectionedAdapter;
import com.monnerville.transports.herault.HeaderTitle;
import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.Application;
import static com.monnerville.transports.herault.core.Application.TAG;
import com.monnerville.transports.herault.core.BusStation;
import com.monnerville.transports.herault.core.sql.SQLBusManager;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SplashActivity extends Activity implements HeaderTitle {
    private SharedPreferences mPrefs;
    private List<BusStation> mStarredStations;
    private boolean mVoiceSupported = false;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    private boolean mDatabaseUpgrading = false;
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

        if (Application.OSBeforeHoneyComb()) {
            requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        }
        setContentView(R.layout.home);
        if (Application.OSBeforeHoneyComb()) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.search_title_bar);
        }

        if (Application.OSBeforeHoneyComb()) {
            // Remove top parent padding (all but left padding)
            ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
            LinearLayout root = (LinearLayout) decorView.getChildAt(0);
            View titleContainer = root.getChildAt(0);
            titleContainer.setPadding(titleContainer.getPaddingLeft(), 0, 0, 0);

            setPrimaryTitle(getString(R.string.app_name));
            setSecondaryTitle(getString(R.string.slogan));
        }
        else {
            ActionBarHelper.setHomeButtonEnabled(this, false);
            ActionBarHelper.setTitle(this, R.string.app_name);
            ActionBarHelper.setSubtitle(this, R.string.slogan);
        }

        new DBCreateOrUpdateTask().execute();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mStarredStations = new ArrayList<BusStation>();

        // Voice recognition supported?
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
            new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (!activities.isEmpty()) {
            mVoiceSupported = true;
        } else {
            Log.d(TAG, "Recognize not present: voice recognition not supported");
        }

        mBookmarkHandler = new BookmarkHandler(mAdapter, mStarredStations);
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
            case R.id.menu_settings:
                startActivity(new Intent(this, AppPreferenceActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void startHTNetwork(View view) {
            Intent intent = new Intent(this, AllLinesActivity.class);
            startActivity(intent);
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
            mManager.initDB(SplashActivity.this, mHandler);
            return null;
        }

        @Override
        protected void onPreExecute() {
            // Executes on the UI thread
            // Prevents onResume() to update bookmarks before the DB is ready
            mDBReady = false;

            mDialog = new ProgressDialog(SplashActivity.this);
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

            Log.d(TAG, "NETS:" + mManager.getBusNetworks());

            setupAdapter();
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Updates bookmarked stations with latest next stop
     */
    private void updateBookmarks() {
        List<BusStation> sts = mManager.getStarredStations(this);
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

    /**
     * Sets up the adapter for the list
     */
    private void setupAdapter() {
        /* FIXME
        mAdapter.addSection(getString(R.string.quick_actions_header),
            new ActionListAdapter(this, R.layout.main_action_list_item, mMainActions));
            */
        mAdapter.addSection(getString(R.string.all_lines_bookmarks_header),
            new BusStationActivity.BookmarkStationListAdapter(this,
            R.layout.bus_line_bookmark_list_item, mStarredStations));
        ListView bl = (ListView)findViewById(R.id.blist);
        bl.setAdapter(mAdapter);

        // Handle release notes
        final String releaseKey = "shown_release_notes_for_" + getString(R.string.app_version);
        if (!mPrefs.getBoolean(releaseKey, false)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.pref_about_release_notes_title))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences.Editor ed = mPrefs.edit();
                        ed.putBoolean(releaseKey, true);
                        ed.commit();
                        showTipsDialog();
                    }
                });
            View log = getLayoutInflater().inflate(R.layout.releasenotes, null);
            ((TextView)log.findViewById(R.id.about_ht_version))
                .setText(String.format("%s %s", getString(R.string.app_name), getString(R.string.app_version)));
            builder.setView(log);
            builder.show();
        }
        else {
            // Handle tips
            showTipsDialog();
        }
    }

    private void showTipsDialog() {
        if (mPrefs.getBoolean("pref_show_tips_at_startup", true)) {
            TipsDialog tips = new TipsDialog(this, false);
            tips.show();
        }
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
                    mDatabaseUpgrading = true;
                    mPd.show();
                    break;
                case SQLBusManager.FLUSH_DATABASE_PROGRESS:
                    int progress = (Integer)msg.obj;
                    mPd.setProgress(progress);
                    break;
                case SQLBusManager.FLUSH_DATABASE_UPGRADED:
                    mPd.setProgress(100);
                    mPd.cancel();
                    mDatabaseUpgrading = false;
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
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

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mManager.getDB().close();
        super.onDestroy();
    }

    /**
     * Force screen orientation to be in portrait mode while upgrading DB. Otherwise,
     * rotation is allowed and we will get into trouble :)
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDatabaseUpgrading) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
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
                case SplashActivity.ACTION_UPDATE_BOOKMARKS:
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

}