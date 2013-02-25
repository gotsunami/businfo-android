/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monnerville.transports.herault.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
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
import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.BusNetwork;
import com.monnerville.transports.herault.core.sql.SQLBusManager;
import java.util.List;

/**
 *
 * @author mathias
 */
public class BusNetworkFragment extends ListFragment {
    private final SQLBusManager mManager = SQLBusManager.getInstance();
    private BusNetworkListAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bus_networks, container, false);
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        mAdapter = new BusNetworkListAdapter(getActivity(), 
            R.layout.bus_network_list_item, mManager.getBusNetworks());
        setListAdapter(mAdapter);
    }

    private class BusNetworkListAdapter extends ArrayAdapter<BusNetwork> {
        private int mResource;
        private Context mContext;

        BusNetworkListAdapter(Context context, int resource, List<BusNetwork> networks) {
            super(context, resource, networks);
            mResource = resource;
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout itemView;
            BusNetwork net = getItem(position);

            if (convertView == null) {
                itemView = new LinearLayout(mContext);
                LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                li.inflate(mResource, itemView, true);
            }
            else {
                itemView = (LinearLayout)convertView;
            }

            TextView name = (TextView)itemView.findViewById(android.R.id.text1);
            name.setText(net.getName());

            TextView netinfo = (TextView)itemView.findViewById(R.id.network_info);
            netinfo.setText(getString(R.string.pref_about_network_summary, getString(R.string.num_lines)));

            TextView linecol = (TextView)itemView.findViewById(R.id.line_color);
            linecol.setBackgroundResource(R.drawable.busline_bkg);

            return itemView;
        }
    }

    @Override 
    public void onListItemClick(ListView l, View v, int position, long id) {
        // FIXME: support more networks
        Intent intent = new Intent(getActivity(), AllLinesActivity.class);
        startActivity(intent);
    }
}
