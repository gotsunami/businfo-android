package com.monnerville.transports.herault.ui;

import android.app.AlertDialog;
import android.app.Application;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.widget.ListView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.commonsware.android.listview.SectionedAdapter;
import com.monnerville.transports.herault.HeaderTitle;
import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.AbstractBusLine;
import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.BusStation;
import java.util.List;

import com.monnerville.transports.herault.core.BusManager;
import com.monnerville.transports.herault.core.sql.SQLBusManager;
import com.monnerville.transports.herault.core.xml.XMLBusManager;
import java.util.ArrayList;
import java.util.Arrays;
import javax.net.ssl.ManagerFactoryParameters;

public class AllLinesActivity extends ListActivity implements HeaderTitle {
    private SharedPreferences mPrefs;
    private List<BusStation> mStarredStations;
    private List<String> mMainActions;
    // Cached directions for all available lines
    private List<List<String>> mDirections;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setTitle(R.string.lines_activity_title);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.all_lines_title_bar);

        // Remove top parent padding (all but left padding)
        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        LinearLayout root = (LinearLayout) decorView.getChildAt(0);
        FrameLayout titleContainer = (FrameLayout) root.getChildAt(0);
        titleContainer.setPadding(titleContainer.getPaddingLeft(), 0, 0, 0);

        setPrimaryTitle(getString(R.string.app_name));
        setSecondaryTitle(getString(R.string.slogan));

        BusManager manager = SQLBusManager.getInstance();
        new DBCreateOrUpdateTask().execute();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mDirections = new ArrayList<List<String>>();
        mStarredStations = new ArrayList<BusStation>();
        mMainActions = new ArrayList<String>();
        mMainActions.add(getString(R.string.action_speak_destination));

        Button searchButton = (Button)findViewById(R.id.btn_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // Open search dialog
                onSearchRequested();
            }
        });
    }

    /**
     * Sets up the adapter for the list
     */
    private void setupAdapter() {
        BusManager manager = SQLBusManager.getInstance();
        List<BusLine> lines = manager.getBusLines();
        new DirectionsRetreiverTask().execute(lines);

        if (mStarredStations.size() > 0) {
        mAdapter.addSection(getString(R.string.all_lines_bookmarks_header),
            new BusStationActivity.BookmarkStationListAdapter(this,
                R.layout.bus_line_bookmark_list_item, mStarredStations));
        }
        mAdapter.addSection(getString(R.string.quick_actions_header),
            new ActionListAdapter(this, R.layout.main_action_list_item, mMainActions));
        mAdapter.addSection(getString(R.string.all_lines_header),
            new LineListAdapter(this, R.layout.all_lines_list_item, lines));
        setListAdapter(mAdapter);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // FIXME updateBookmarks();
    }

    @Override
    protected void onPause() {
        /** FIXME
        BusManager manager = XMLBusManager.getInstance();
        // Overwrite existing saved stations with the current list
        manager.overwriteStarredStations(mStarredStations, this);
         * */
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

    private void updateBookmarks() {
        BusManager manager = XMLBusManager.getInstance();
        List<BusStation> sts = manager.getStarredStations(this);
        mStarredStations.clear();
        for (BusStation st : sts) {
            mStarredStations.add(st);
        }
        mAdapter.notifyDataSetChanged();
    }

    private class LineListAdapter extends ArrayAdapter<BusLine> {
        private int mResource;
        private Context mContext;

        LineListAdapter(Context context, int resource, List<BusLine> items) {
            super(context, resource, items);
            mResource = resource;
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout itemView;
            BusLine line = getItem(position);

            if (convertView == null) {
                itemView = new LinearLayout(mContext);
                LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                li.inflate(mResource, itemView, true);
            }
            else
                itemView = (LinearLayout)convertView;

            TextView name = (TextView)itemView.findViewById(android.R.id.text1);
            name.setText(line.getName());
            TextView direction = (TextView)itemView.findViewById(android.R.id.text2);
            TextView col = (TextView)itemView.findViewById(R.id.line_color);

            GradientDrawable gd;
            if (line.getColor() != 0) {
                col.setText("");
                int colors[] = { line.getColor(), AllLinesActivity.getLighterColor(line.getColor(), 2) };
                gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
                col.setBackgroundDrawable(gd);
            }
            else {
                int colors[] = { BusLine.UNKNOWN_COLOR, AllLinesActivity.getLighterColor(BusLine.UNKNOWN_COLOR, 2) };
                gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
                col.setBackgroundDrawable(gd);
                col.setText("?");
            }
            gd.setCornerRadius(5);

            try {
                List<String> dirs = mDirections.get(position);
                direction.setText(dirs.get(0) + " - " + dirs.get(1));
            } catch(IndexOutOfBoundsException ex) {}

            return itemView;
        }
    }

    /**
     * Computes a lighter color
     * @param startCol source color to compute from
     * @param fn times factor
     * @return new color
     */
    public static int getLighterColor(int startCol, int fn) {
        return Color.rgb(
            fn*Color.red(startCol),
            fn*Color.green(startCol),
            fn*Color.blue(startCol));
    }

    /**
     * Computes a darker color
     * @param startCol source color to compute from
     * @param fn times factor
     * @return new color
     */
    public static int getDarkerColor(int startCol, int fn) {
        return Color.rgb(
            Color.red(startCol)/fn,
            Color.green(startCol)/fn,
            Color.blue(startCol)/fn);
    }

    private int getResourceId(Context ctx, String str) {
        String packageName = "com.monnerville.transports.herault";
        return ctx.getResources().getIdentifier(str, null, packageName);
    }

    /**
     * List adapater for main actions (voice recognition etc.)
     */
    private class ActionListAdapter extends ArrayAdapter<String> {
        private int mResource;
        private Context mContext;

        ActionListAdapter(Context context, int resource, List<String> items) {
            super(context, resource, items);
            mResource = resource;
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout itemView;
            String action = getItem(position);

            if (convertView == null) {
                itemView = new LinearLayout(mContext);
                LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                li.inflate(mResource, itemView, true);
            }
            else
                itemView = (LinearLayout)convertView;

            TextView name = (TextView)itemView.findViewById(android.R.id.text1);
            name.setText(action);

            return itemView;
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final Object obj = getListView().getItemAtPosition(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (obj instanceof BusLine) {
            final BusLine line = (BusLine)getListView().getItemAtPosition(position);
            final String[] directions;
            directions = line.getDirections();
            if (directions[0] == null || directions[1] == null) {
                Toast.makeText(this, R.string.toast_null_direction, Toast.LENGTH_SHORT).show();
                return;
            }
            builder.setTitle(R.string.pick_direction_title);
            builder.setItems(directions, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    Intent intent = new Intent(AllLinesActivity.this, BusLineActivity.class);
                    intent.putExtra("line", line.getName());
                    intent.putExtra("direction", directions[item]);
                    startActivity(intent);
                }
            });
            builder.show();
        }
        else if(obj instanceof BusStation) {
            final BusStation station = (BusStation)obj;
            builder.setTitle(R.string.bookmark_menu_title);
            builder.setItems(R.array.bookmark_options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    switch (item) {
                        case 0: { // Show station
                            Intent intent = new Intent(AllLinesActivity.this, BusStationActivity.class);
                            intent.putExtra("line", station.getLine().getName());
                            intent.putExtra("direction", station.getDirection());
                            intent.putExtra("station", station.getName());
                            startActivity(intent);
                            break;
                        }
                        case 1: { // Show line
                            Intent intent = new Intent(AllLinesActivity.this, BusLineActivity.class);
                            intent.putExtra("line", station.getLine().getName());
                            intent.putExtra("direction", station.getDirection());
                            startActivity(intent);
                            break;
                        }
                        case 2: // Remove
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
    }

    final SectionedAdapter mAdapter = new CounterSectionedAdapter(this) {
        @Override
        protected int getMatches(String caption) {
            if (caption.equals(getString(R.string.all_lines_header))) {
                BusManager manager = SQLBusManager.getInstance();
                return manager.getBusLines().size();
            }
            return CounterSectionedAdapter.NO_MATCH;
        }
    };

    /**
     * Retrieves all lines directions in a background thread
     */
    private class DirectionsRetreiverTask extends AsyncTask<List<BusLine>, Void, Void> {
        private List<BusStation> starredStations;
        private List<BusLine> mLines;

        @Override
        protected Void doInBackground(List<BusLine>... lis) {
            mLines = lis[0];
            for (BusLine line : mLines) {
                String[] dirs = line.getDirections();
                mDirections.add(Arrays.asList(dirs));
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            // Executes on the UI thread
        }

        @Override
        protected void onPostExecute(Void none) {
            // Back to the UI thread
            for (BusLine line : mLines) {
                int colorid = getResourceId(AllLinesActivity.this, "@color/line_" + line.getName());
                int buscol = colorid > 0 ? Color.parseColor(AllLinesActivity.this.getString(colorid)) : 0;
                line.setColor(buscol);
            }
            mAdapter.notifyDataSetChanged();
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
     * Asynchronous database creating/updating
     */
    private class DBCreateOrUpdateTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog mDialog;
        private long mStart; // benchmark
        private DBHandler mHandler;

        @Override
        protected Void doInBackground(Void... none) {
            SQLBusManager.getInstance().initDB(AllLinesActivity.this, mHandler);
            return null;
        }

        @Override
        protected void onPreExecute() {
            // Executes on the UI thread
            mDialog = new ProgressDialog(AllLinesActivity.this);
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
            Log.d("BENCH0", "DB create duration: " + (System.currentTimeMillis() - mStart) + "ms");
            mHandler = null;
            setupAdapter();
            mAdapter.notifyDataSetChanged();
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
            case R.id.menu_settings:
                startActivity(new Intent(this, AppPreferenceActivity.class));
                return true;
            case R.id.menu_search:
                // Open search dialog
                onSearchRequested();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
