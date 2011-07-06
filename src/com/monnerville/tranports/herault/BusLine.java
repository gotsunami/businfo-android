package com.monnerville.tranports.herault;

import android.content.res.XmlResourceParser;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author mathias
 */
class BusLine {
    private String mName;
    private BusManager mManager;

    public BusLine(BusManager manager, String name) {
        mName = name;
        mManager = manager;
    }
    public String getName() { return mName; }

    public List<BusStation> getStations() throws XmlPullParserException, IOException {
        List<BusStation> stations = new ArrayList<BusStation>();
        XmlResourceParser xrp = mManager.getResourceParser();
        boolean match = false;
        while(xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
            if (xrp.getEventType() == XmlPullParser.START_TAG) {
                String s = xrp.getName();
                if (s.equals("line")) {
                    String id = xrp.getAttributeValue(null, "id");
                    if (id.equals(mName))
                        match = true;
                }
                if (s.equals("station")) {
                    if (match)
                        stations.add(new BusStation(this, xrp.getAttributeValue(null, "id")));
                }
            }
            else if(xrp.getEventType() == XmlPullParser.END_TAG) {
                // Matches end of line
                String s = xrp.getName();
                if (s.equals("line")) {
                    if (match)
                        match = false;
                }
            }
            xrp.next();
        }
        return stations;
    }
}
