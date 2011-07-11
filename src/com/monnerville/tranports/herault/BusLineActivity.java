package com.monnerville.tranports.herault;

import android.util.Log;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xmlpull.v1.XmlPullParserException;

import static com.monnerville.tranports.herault.core.Application.TAG;

import com.monnerville.tranports.herault.core.BusLine;
import com.monnerville.tranports.herault.core.BusManager;
import com.monnerville.tranports.herault.core.BusStation;

/**
 *
 * @author mathias
 */
public class BusLineActivity extends ListActivity {
    private String mLine;
    private String mDirection;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.busline);

        final Intent intent = getIntent();
        final Bundle bun = intent.getExtras();
        if (bun != null) {
            mLine = bun.getString("line");
            mDirection = bun.getString("direction");
        }
        else
            finish();

        setTitle(mLine);
        BusManager manager = BusManager.getInstance();
        try {
            BusLine line = manager.getBusLine(mLine);
            List<BusStation> stations = line.getStations(mDirection);
            if (stations != null) {
                ListAdapter adapter = new SimpleAdapter(this, getData(stations),
                    R.layout.bus_line_list_item, new String[] {"station"},
                    new int[] {android.R.id.text1});
                setListAdapter(adapter);
            }
            else {
                Log.w(TAG, "Direction '" + mDirection + "' not found");
            }
        } catch (XmlPullParserException ex) {
            Logger.getLogger(BusLineActivity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BusLineActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private List getData(List<BusStation> stations) {
        List<Map> data = new ArrayList<Map>();
        for (BusStation st : stations) {
            Map<String, String> m = new HashMap<String, String>();
            m.put("station", st.getName());
            data.add(m);
        }
        return data;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, BusStationActivity.class);
        Map<String, String> map = (Map)getListView().getItemAtPosition(position);
        intent.putExtra("line", mLine);
        intent.putExtra("direction", mDirection);
        intent.putExtra("station", map.get("station"));
        startActivity(intent);
    }
}
