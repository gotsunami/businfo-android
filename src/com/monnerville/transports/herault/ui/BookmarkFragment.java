package com.monnerville.transports.herault.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.BusNetwork;
import com.monnerville.transports.herault.core.BusStation;
import com.monnerville.transports.herault.core.sql.SQLBusManager;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mathias
 */
public class BookmarkFragment extends ListFragment {
    private final SQLBusManager mManager = SQLBusManager.getInstance();
    private BusStationActivity.BookmarkStationListAdapter mAdapter;
    private List<BusStation> mStarredStations;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bookmarks, container, false);
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        mStarredStations = new ArrayList<BusStation>();
        mAdapter = new BusStationActivity.BookmarkStationListAdapter(getActivity(),
            R.layout.bus_line_bookmark_list_item, mStarredStations);
        setListAdapter(mAdapter);
    }

    @Override 
    public void onListItemClick(ListView l, View v, int position, long id) {
        // TODO
    }
}
