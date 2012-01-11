package com.monnerville.transports.herault.ui;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.ListView;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.commonsware.android.listview.SectionedAdapter;
import com.monnerville.transports.herault.HeaderTitle;
import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.sql.SQLBusManager;

public class AllLinesActivity extends ListActivity implements HeaderTitle {
    // Cached directions for all available lines
    private List<List<String>> mDirections;

    private final SQLBusManager mManager = SQLBusManager.getInstance();
    /**
     * Used by onResume and updateBookmarks to ensure database is ready
     */
    private boolean mDBReady;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTitle(R.string.lines_activity_title);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.simplelist);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.search_title_bar);

        // Remove top parent padding (all but left padding)
        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        LinearLayout root = (LinearLayout) decorView.getChildAt(0);
        View titleContainer = root.getChildAt(0);
        titleContainer.setPadding(titleContainer.getPaddingLeft(), 0, 0, 0);

        setPrimaryTitle(getString(R.string.app_name));
        setSecondaryTitle(getString(R.string.slogan));

        mDirections = new ArrayList<List<String>>();

        Button searchButton = (Button)findViewById(R.id.btn_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // Open search dialog
                onSearchRequested();
            }
        });

        List<BusLine> lines = mManager.getBusLines();

        // Background line directions retreiver
        new DirectionsRetreiverTask().execute(lines);
    }

    /**
     * Sets up the adapter for the list
     */
    private void setupAdapter(List<BusLine> lines) {
        // mDirections is passed because LineListAdapter is a public 
        // static class used by other activities
        mAdapter.addSection(getString(R.string.all_lines_header),
            new LineListAdapter(this, R.layout.line_list_item, lines, mDirections));
        setListAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
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

    /**
     * Common line list adapter
     */
    public static class LineListAdapter extends ArrayAdapter<BusLine> {
        private int mResource;
        private Context mContext;
        private List<List<String>> mDirections;

        LineListAdapter(Context context, int resource, List<BusLine> lines, List<List<String>> directions) {
            super(context, resource, lines);
            mResource = resource;
            mContext = context;
            this.mDirections = directions;
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
            //GradientDrawable gd;
            TextView col = (TextView)itemView.findViewById(R.id.line_color);
            TextView avail = (TextView)itemView.findViewById(R.id.available);

            if (line.getAvailableFrom() != null && line.getAvailableTo() != null) {
                java.text.DateFormat dateFormat = DateFormat.getMediumDateFormat(mContext);

                avail.setVisibility(View.VISIBLE);
                avail.setText(mContext.getString(R.string.line_avail_range, 
                    dateFormat.format(line.getAvailableFrom()), 
                    dateFormat.format(line.getAvailableTo())
                ));
            }
            else
                avail.setVisibility(View.GONE);

            setLineTextViewStyle(mContext, line, col);

            if (!line.getName().equals(mContext.getString(R.string.result_no_match))) {
                TextView direction = (TextView)itemView.findViewById(R.id.direction);
                try {
                    List<String> dirs = this.mDirections.get(position);
                    direction.setText(dirs.get(0) + " - " + dirs.get(1));
                } catch(IndexOutOfBoundsException ex) {}
            }

            return itemView;
        }
    }

    /**
     * Changes a textview's style to match the line color attributes
     * @param ctx application's context
     * @param line bus line
     * @param tv TextView to change
     */
    public static void setLineTextViewStyle(final Context ctx, final BusLine line, TextView tv) {
        GradientDrawable gd;
        if (line.getName().equals(ctx.getString(R.string.result_no_match))) {
            int colors[] = { BusLine.DEFAULT_COLOR, AllLinesActivity.getLighterColor(BusLine.DEFAULT_COLOR, 2) };
            gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
            tv.setText("?");
        }
        else {
            if (line.getColor() != BusLine.DEFAULT_COLOR) {
                tv.setText("");
                int colors[] = { line.getColor(), AllLinesActivity.getLighterColor(line.getColor(), 2) };
                gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
            }
            else {
                int colors[] = { BusLine.DEFAULT_COLOR, AllLinesActivity.getLighterColor(BusLine.DEFAULT_COLOR, 2) };
                gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
                tv.setText("?");
            }
        }
        gd.setCornerRadius(5);
        tv.setBackgroundDrawable(gd);
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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final Object obj = l.getItemAtPosition(position);
        if (obj instanceof BusLine)
            handleBusLineItemClick(this, l, v, position, id);
    }

    /**
     * Handles bus line list item click
     * @param ctx context
     * @param l listview instance
     * @param v view
     * @param position position of item in the list
     * @param id
     */
    public static void handleBusLineItemClick(final Context ctx, ListView l, View v, int position, long id) {
        final BusLine line = (BusLine)l.getItemAtPosition(position);
        final String[] directions;
        directions = line.getDirections();
        // No direction, problem!
        if (directions[0] == null || directions[1] == null)
            return;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(R.string.pick_direction_title);
        builder.setItems(directions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                Intent intent = new Intent(ctx, BusLineActivity.class);
                intent.putExtra("line", line.getName());
                intent.putExtra("direction", directions[item]);
                ctx.startActivity(intent);
            }
        });
        builder.show();
    }

    /**
     * Custom adapter with matches display
     */
    final SectionedAdapter mAdapter = new CounterSectionedAdapter(this) {
        @Override
        protected int getMatches(String caption) {
            return mManager.getBusLines().size();
        }
    };

    /**
     * Retrieves all lines directions in a background thread
     */
    private class DirectionsRetreiverTask extends AsyncTask<List<BusLine>, Void, Void> {
        private ProgressDialog mDialog;
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
            mDialog = ProgressDialog.show(AllLinesActivity.this, "",
                getString(R.string.pd_searching), true);
        }

        @Override
        protected void onPostExecute(Void none) {
            // Back to the UI thread
            mDialog.cancel();
            setupAdapter(mLines);
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
            case R.id.menu_search:
                // Open search dialog
                onSearchRequested();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
