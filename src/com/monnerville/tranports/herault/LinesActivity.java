package com.monnerville.tranports.herault;

import android.app.Activity;
import android.app.ListActivity;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParserException;

public class LinesActivity extends ListActivity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        BusManager manager = BusManager.getInstance();
        try {
            manager.setResources(getResources(), R.xml.lines);
        } catch(Resources.NotFoundException err) {
            Log.e("RES", err.getMessage());
        } catch(XmlPullParserException err) {
            Log.e("RES", err.getMessage());
        } catch(IOException err) {
            Log.e("RES", err.getMessage());
        }

        try {
            List<BusLine> lines = manager.getBusLines();
            for (BusLine line : lines) {
                Log.d("TAG", "Line " + line.getName() + " has " + line.getStations().size()
                    + " stations");

            }
            ListAdapter adapter = new SimpleAdapter(this, getData(lines),
                android.R.layout.simple_list_item_1, new String[] {"name"},
                new int[] {android.R.id.text1});
            setListAdapter(adapter);
        } catch(XmlPullParserException err) {
        } catch(IOException err) {
        }
    }

    private List getData(List<BusLine> lines) {
        List<Map> data = new ArrayList<Map>();
        for (BusLine line : lines) {
            Map<String, String> m = new HashMap<String, String>();
            m.put("name", line.getName());
            data.add(m);
        }
        return data;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Toast.makeText(this, String.valueOf(position), Toast.LENGTH_SHORT).show();
    }
}
