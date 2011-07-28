package com.monnerville.transports.herault.core.xml;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;
import com.monnerville.transports.herault.core.BusLine;
import com.monnerville.transports.herault.core.BusManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Handles everything related to buslines management. This is an
 * implementation using XML raw data and a XML SAX parser.
 *
 * @author mathias
 */
public class XMLBusManager implements BusManager {
    private static final XMLBusManager INSTANCE = new XMLBusManager();
    private XMLBusManager() {}

    private Resources mAppResources = null;
    private int mLinesId;

    public static XMLBusManager getInstance() { return INSTANCE; }
    /**
     * Sets manager application resources
     * @param appRes available application resources
     * @param resid resource id for all the bus lines
     */
    @Override
    public void setResources(Resources appRes, int resid) {
        mAppResources = appRes;
        mLinesId = resid;
    }

    public XmlResourceParser getResourceParser() {
        if (mAppResources == null) return null;
        return mAppResources.getXml(mLinesId);
    }

    @Override
    public List<BusLine> getBusLines() {
        if (mAppResources == null) return null;
        List<BusLine> lines = new ArrayList<BusLine>();
        XmlResourceParser xrp = mAppResources.getXml(mLinesId);
        try {
            while (xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlPullParser.START_TAG) {
                    String s = xrp.getName();
                    if (s.equals("line")) {
                        XMLBusLine line = new XMLBusLine(xrp.getAttributeValue(null, "id"));
                        lines.add(line);
                    }
                }
                try {
                    xrp.next();
                } catch (IOException ex) {
                    Logger.getLogger(XMLBusManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (XmlPullParserException ex) {
            Logger.getLogger(XMLBusManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        xrp.close();
        return lines;
    }

    /**
     * Gets a single bus line by name
     * @param name name of bus line
     * @return a BusLine instance or null if not found
     */
    @Override
    public BusLine getBusLine(String name) {
        if (mAppResources == null) return null;
        XmlResourceParser xrp = mAppResources.getXml(mLinesId);
        try {
            while (xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlPullParser.START_TAG) {
                    String s = xrp.getName();
                    if (s.equals("line")) {
                        if (xrp.getAttributeValue(null, "id").equals(name)) {
                            return new XMLBusLine(name);
                        }
                    }
                }
                try {
                    xrp.next();
                } catch (IOException ex) {
                    Logger.getLogger(XMLBusManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (XmlPullParserException ex) {
            Logger.getLogger(XMLBusManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        xrp.close();
        return null;
    }
}
