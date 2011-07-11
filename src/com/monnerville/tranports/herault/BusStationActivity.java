package com.monnerville.tranports.herault;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xmlpull.v1.XmlPullParserException;

import com.monnerville.tranports.herault.core.BusLine;
import com.monnerville.tranports.herault.core.BusManager;
import com.monnerville.tranports.herault.core.BusStation;
import com.monnerville.tranports.herault.core.BusStop;

/**
 *
 * @author mathias
 */
public class BusStationActivity extends ListActivity {
    private String mLine = null;
    private String mStation = null;
    private String mDirection = null;
    private List<BusStop> mStops;
    private BusStation mCurrentStation;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.busstation);

        final Intent intent = getIntent();
        final Bundle bun = intent.getExtras();
        if (bun != null) {
            mStation = bun.getString("station");
            mLine = bun.getString("line");
            mDirection = bun.getString("direction");
        }
        else
            finish();

        setTitle(mLine + " - Station " + mStation);
        BusManager manager = BusManager.getInstance();
        try {
            BusLine line = manager.getBusLine(mLine);
            List<BusStation> stations = line.getStations(mDirection);
            for (BusStation st : stations) {
                if (st.getName().equals(mStation)) {
                    mStops = st.getStops();
                    mCurrentStation = st;
                    break;
                }
            }
            TextView board = (TextView)findViewById(R.id.board);
            if (mCurrentStation != null) {
                BusStop nextStop = mCurrentStation.getNextStop();
                if (nextStop != null)
                    board.setText(BusStop.TIME_FORMATTER.format(mCurrentStation.getNextStop().getTime()));
                else
                    board.setText(R.string.no_more_stop);
            }
            ListAdapter adapter = new SimpleAdapter(this, getData(mStops),
                R.layout.bus_station_list_item, new String[] {"time"},
                new int[] {android.R.id.text1});
            setListAdapter(adapter);
        } catch (ParseException ex) {
            Logger.getLogger(BusStationActivity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XmlPullParserException ex) {
            Logger.getLogger(BusStationActivity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BusStationActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private List getData(List<BusStop> stops) {
        List<Map> data = new ArrayList<Map>();
        for (BusStop st : stops) {
            Map<String, String> m = new HashMap<String, String>();
            m.put("time", BusStop.TIME_FORMATTER.format(st.getTime()));
            m.put("line", st.getLine());
            data.add(m);
        }
        return data;
    }
}
