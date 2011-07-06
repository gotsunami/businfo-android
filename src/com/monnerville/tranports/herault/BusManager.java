package com.monnerville.tranports.herault;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Handles everything related to buslines management
 *
 * @author mathias
 */
class BusManager {
    private static final BusManager INSTANCE = new BusManager();
    private BusManager() {}

    private Resources mAppResources = null;
    private int mLinesId;

    public static BusManager getInstance() { return INSTANCE; }
    /**
     * Sets manager application resources
     * @param appRes available application resources
     * @param resid resource id for all the bus lines
     */
    public void setResources(Resources appRes, int resid) 
        throws Resources.NotFoundException, XmlPullParserException, IOException {
        mAppResources = appRes;
        mLinesId = resid;
    }

    public XmlResourceParser getResourceParser() { 
        return mAppResources.getXml(mLinesId);
    }

    public List<BusLine> getBusLines() throws XmlPullParserException, IOException {
        if (mAppResources == null) return null;
        List<BusLine> lines = new ArrayList<BusLine>();
        XmlResourceParser xrp = mAppResources.getXml(mLinesId);
        while(xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
            if (xrp.getEventType() == XmlPullParser.START_TAG) {
                String s = xrp.getName();
                if (s.equals("line")) {
                    BusLine line = new BusLine(this, xrp.getAttributeValue(null, "id"));
                    lines.add(line);
                }
            }
            xrp.next();
        }
        xrp.close();
        return lines;
    }

}
