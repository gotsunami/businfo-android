package com.monnerville.tranports.herault;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xmlpull.v1.XmlPullParserException;

public class LinesActivity extends ListActivity
{
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
            ListAdapter adapter = new SimpleAdapter(this, getData(lines),
                android.R.layout.simple_list_item_1, new String[] {"name"},
                new int[] {android.R.id.text1});
            setListAdapter(adapter);
        } catch(XmlPullParserException err) {
            Log.e("TAG", "Parsing error: " + err.getMessage());
        } catch(IOException err) {
            Log.e("TAG", "Error: " + err.getMessage());
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
        Intent intent = new Intent(this, BusLineActivity.class);
        Map<String, String> map = (Map)getListView().getItemAtPosition(position);
        intent.putExtra("line", map.get("name"));
        startActivity(intent);
    }
}
