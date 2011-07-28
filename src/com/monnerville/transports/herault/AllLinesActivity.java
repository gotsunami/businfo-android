package com.monnerville.transports.herault;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.ListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.commonsware.android.listview.SectionedAdapter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.BusManager;
import com.monnerville.transports.herault.core.XMLBusManager;

public class AllLinesActivity extends ListActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setTitle(R.string.lines_activity_title);

        BusManager manager = XMLBusManager.getInstance();
        manager.setResources(getResources(), R.xml.lines);

        List<BusLine> lines = manager.getBusLines();
        mAdapter.addSection(getString(R.string.all_lines_header, lines.size()),
            new LineListAdapter(this, R.layout.all_lines_list_item, lines));
        setListAdapter(mAdapter);
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
            try {
                String[] dirs = line.getDirections();
                direction.setText(dirs[0] + " - " + dirs[1]);
            } catch (XmlPullParserException ex) {
                Logger.getLogger(AllLinesActivity.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(AllLinesActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
            return itemView;
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BusLine line = (BusLine)getListView().getItemAtPosition(position);
        final String[] directions;
        try {
            directions = line.getDirections();
            if (directions[0] == null || directions[1] == null) {
                Toast.makeText(this, R.string.toast_null_direction, Toast.LENGTH_SHORT).show();
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        } catch (XmlPullParserException ex) {
            Logger.getLogger(AllLinesActivity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AllLinesActivity.class.getName()).log(Level.SEVERE, null, ex);
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

}
