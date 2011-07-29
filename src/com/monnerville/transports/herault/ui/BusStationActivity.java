package com.monnerville.transports.herault.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.monnerville.transports.herault.HeaderTitle;
import com.monnerville.transports.herault.R;
import com.monnerville.transports.herault.core.BusLine;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xmlpull.v1.XmlPullParserException;

import com.monnerville.transports.herault.core.BusManager;
import com.monnerville.transports.herault.core.BusStation;
import com.monnerville.transports.herault.core.BusStop;
import com.monnerville.transports.herault.core.xml.XMLBusManager;

/**
 *
 * @author mathias
 */
public class BusStationActivity extends ListActivity implements HeaderTitle {
    private String mLine = null;
    private String mStation = null;
    private String mDirection = null;
    private List<BusStop> mStops;
    private BusStation mCurrentStation;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.busstation);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.bus_station_title_bar);

        TextView board = (TextView)findViewById(R.id.board);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/lcd.ttf");
        board.setTypeface(tf);

        final Intent intent = getIntent();
        final Bundle bun = intent.getExtras();
        if (bun != null) {
            mStation = bun.getString("station");
            mLine = bun.getString("line");
            mDirection = bun.getString("direction");
        }
        else
            finish();

        setPrimaryTitle(mStation);
        setSecondaryTitle(getString(R.string.station_line_direction_title, mLine, mDirection));

        setTitle(mLine + " - Station " + mStation);
        BusManager manager = XMLBusManager.getInstance();
        BusLine line = manager.getBusLine(mLine);
        List<BusStation> stations = line.getStations(mDirection);
        for (BusStation st : stations) {
            if (st.getName().equals(mStation)) {
                mStops = st.getStops();
                mCurrentStation = st;
                break;
            }
        }
        if (mCurrentStation != null) {
            BusStop nextStop = mCurrentStation.getNextStop();
            if (nextStop != null)
                board.setText(BusStop.TIME_FORMATTER.format(mCurrentStation.getNextStop().getTime()));
            else
                board.setText(R.string.no_more_stop);
        }
        ListAdapter adapter = new SimpleAdapter(this, getData(mStops),
            R.layout.bus_station_list_item, new String[] {"time", "line"},
            new int[] {android.R.id.text1, android.R.id.text2});
        setListAdapter(adapter);
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

    @Override
    public void setPrimaryTitle(String title) {
        TextView t= (TextView)findViewById(R.id.primary);
        t.setText(title);
    }

    @Override
    public void setSecondaryTitle(String title) {
        TextView t= (TextView)findViewById(R.id.secondary);
        t.setText(title);
    }
}
