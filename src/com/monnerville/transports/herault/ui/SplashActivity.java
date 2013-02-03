
package com.monnerville.transports.herault.ui;

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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.monnerville.transports.herault.HeaderTitle;
import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.Application;
import static com.monnerville.transports.herault.core.Application.TAG;
import com.monnerville.transports.herault.core.sql.SQLBusManager;
import java.util.List;

public class SplashActivity extends FragmentActivity implements HeaderTitle {
    private SharedPreferences mPrefs;
    private boolean mVoiceSupported = false;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    private boolean mDatabaseUpgrading = false;
    // Cached directions for all available lines

    private final SQLBusManager mManager = SQLBusManager.getInstance();
    private ViewPager mPager;
    private DynPagerAdapter mDynPagerAdapter;

    private BusNetworkFragment mBusNetworkFragment;
    private BookmarkFragment mBookmarkFragment;

    private boolean mDBReady;

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

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setVisibility(View.GONE);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Voice recognition supported?
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
            new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (!activities.isEmpty()) {
            mVoiceSupported = true;
        } else {
            Log.d(TAG, "Recognize not present: voice recognition not supported");
        }

        new DBCreateOrUpdateTask().execute();
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
            } catch (InterruptedException ex) {} 
        }
    }

    // Adapter for ViewPager
    public class DynPagerAdapter extends FragmentPagerAdapter 
        implements ViewPager.OnPageChangeListener {

        private Fragment[] mFrags = {mBusNetworkFragment, mBookmarkFragment};
        private int[] mPageTitles = {R.string.home_bus_networks, R.string.home_my_bookmarks};

        public DynPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return mFrags[i];
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(mPageTitles[position]);
        }
        @Override
        public void onPageSelected(int position) {
        }

        @Override
        public void onPageScrolled(int i, float f, int i1) {
        }

        @Override
        public void onPageScrollStateChanged(int i) {
        }
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
            Log.d(TAG, "DB update duration: " + (System.currentTimeMillis() - mStart) + "ms");
            mHandler = null;
            mDBReady = true;

            // Do most of UI init now so that the UI thread is not locked
            mBusNetworkFragment = new BusNetworkFragment();
            mBookmarkFragment = new BookmarkFragment();

            mDynPagerAdapter = new DynPagerAdapter(getSupportFragmentManager());
            mPager.setVisibility(View.VISIBLE);
            mPager.setAdapter(mDynPagerAdapter);
            mPager.setOnPageChangeListener(mDynPagerAdapter);

            setupAdapter();
        }
    }

    /**
     * Sets up the adapter for the list
     */
    private void setupAdapter() {
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
                    Log.d("TOOO", "Progress: " + progress);
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
}
