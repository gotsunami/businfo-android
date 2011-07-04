package com.monnerville.tranports.herault;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinesActivity extends ListActivity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        String[] lines = getResources().getStringArray(R.array.lines);
        ListAdapter adapter = new SimpleAdapter(this, getData(lines),
            android.R.layout.simple_list_item_1, new String[] {"name"},
            new int[] {android.R.id.text1});
        setListAdapter(adapter);
    }

    private List getData(String[] lines) {
        List<Map> data = new ArrayList<Map>();
        for (String line : lines) {
            Map<String, String> m = new HashMap<String, String>();
            m.put("name", line);
            data.add(m);
        }
        return data;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Toast.makeText(this, String.valueOf(position), Toast.LENGTH_SHORT).show();
    }
}
