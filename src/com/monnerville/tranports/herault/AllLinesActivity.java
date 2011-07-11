package com.monnerville.tranports.herault;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
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

import com.monnerville.tranports.herault.core.BusLine;
import com.monnerville.tranports.herault.core.BusManager;

public class AllLinesActivity extends ListActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setTitle(R.string.lines_activity_title);

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
                R.layout.all_lines_list_item, new String[] {"name", "directions"},
                new int[] {android.R.id.text1, android.R.id.text2});
            setListAdapter(adapter);
        } catch(XmlPullParserException err) {
            Log.e("TAG", "Parsing error: " + err.getMessage());
        } catch(IOException err) {
            Log.e("TAG", "Error: " + err.getMessage());
        }
    }

    private List getData(List<BusLine> lines) throws XmlPullParserException, IOException {
        List<Map> data = new ArrayList<Map>();
        for (BusLine line : lines) {
            Map<String, String> m = new HashMap<String, String>();
            m.put("name", line.getName());
            String[] d = line.getDirections();
            m.put("directions", d[0] + " - " + d[1]);
            m.put("direction1", d[0]);
            m.put("direction2", d[1]);
            data.add(m);
        }
        return data;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final Map<String, String> map = (Map)getListView().getItemAtPosition(position);
        final String[] directions = new String[2];
        directions[0] = map.get("direction1");
        directions[1] = map.get("direction2");

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
                intent.putExtra("line", map.get("name"));
                intent.putExtra("direction", directions[item]);
                startActivity(intent);
            }
        });
        builder.show();
    }
}
