package com.monnerville.tranports.herault;

import android.content.res.XmlResourceParser;
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
    private XmlResourceParser mXrp;

    public BusLine(XmlResourceParser xrp, String name) {
        mName = name;
        mXrp = xrp;
    }
    public String getName() { return mName; }

    public List getStations(String line) throws XmlPullParserException, IOException {
        List<String> stations = new ArrayList<String>();
        while(mXrp.getEventType() != XmlPullParser.END_DOCUMENT) {
            if (mXrp.getEventType() == XmlPullParser.START_TAG) {
                String s = mXrp.getName();
                if (s.equals("line")) {
                    if (mXrp.getAttributeValue(null, "id").equals(line)) {

                    }
                }
            }
            mXrp.next();
        }
        return stations;
    }
}
