package com.monnerville.transports.herault.ui;

import android.util.Log;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.List;

import com.commonsware.android.listview.SectionedAdapter;
import com.monnerville.transports.herault.HeaderTitle;
import com.monnerville.transports.herault.R;

import static com.monnerville.transports.herault.core.Application.TAG;

import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.BusManager;
import com.monnerville.transports.herault.core.BusStation;
import com.monnerville.transports.herault.core.BusStop;

import com.monnerville.transports.herault.core.sql.SQLBusManager;

public class CityActivity extends ListActivity implements HeaderTitle {
    private String cityId;
    private String mDirection;
    private SharedPreferences mPrefs;
    private boolean mShowToast;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.simplelist);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.city_title_bar);
        getListView().setItemsCanFocus(true);

        final Intent intent = getIntent();
        final Bundle bun = intent.getExtras();
        cityId = bun.getString("cityId");

        setPrimaryTitle("CITY");
        setSecondaryTitle("Dooo");

        final BusManager manager = SQLBusManager.getInstance();
//        BusLine line = manager.getBusLine(mLine);
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
            final BusStation station = getItem(position);
            final int j = position;

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
            ImageView star = (ImageView)itemView.findViewById(R.id.star);
            star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    station.setStarred(!station.isStarred());
                    mAdapter.notifyDataSetChanged();
                }
            });
            star.setImageResource(station.isStarred() ? android.R.drawable.btn_star_big_on :
               android.R.drawable.btn_star_big_off);

            BusStop stop = station.getNextStop(true);
            // We have a non-cached value
            if (stop != null) {
                time.setTextColor(getResources().getColor(R.color.list_item_bus_time));
                time.setText(BusStop.TIME_FORMATTER.format(stop.getTime()));
            }
            else {
                time.setTextColor(getResources().getColor(R.color.list_item_no_more_stop));
                time.setText(R.string.no_more_stop);
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
        /*
        Intent intent = new Intent(this, BusStationActivity.class);
        BusStation station = (BusStation)getListView().getItemAtPosition(position);
        intent.putExtra("line", mLine);
        intent.putExtra("direction", mDirection);
        intent.putExtra("station", station.getName());
        startActivity(intent);
         *
         */
    }
}
