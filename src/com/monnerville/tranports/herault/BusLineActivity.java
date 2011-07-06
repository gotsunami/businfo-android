package com.monnerville.tranports.herault;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author mathias
 */
public class BusLineActivity extends ListActivity {
    private String mLine = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.busline);

        final Intent intent = getIntent();
        final Bundle bun = intent.getExtras();
        if (bun != null) {
            mLine = bun.getString("line");
        }
        else
            finish();

        setTitle(mLine);
        BusManager manager = BusManager.getInstance();
        try {
            BusLine line = manager.getBusLine(mLine);
            List<BusStation> stations = line.getStations();
            ListAdapter adapter = new SimpleAdapter(this, getData(stations),
                android.R.layout.simple_list_item_1, new String[] {"name"},
                new int[] {android.R.id.text1});
            setListAdapter(adapter);
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
            m.put("name", st.getName());
            data.add(m);
        }
        return data;
    }
}
