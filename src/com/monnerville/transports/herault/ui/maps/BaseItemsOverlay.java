package com.monnerville.transports.herault.ui.maps;

import android.graphics.drawable.Drawable;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import java.util.ArrayList;

/**
 *
 * @author mathias
 */
public class BaseItemsOverlay extends ItemizedOverlay<OverlayItem> {
    private ArrayList<OverlayItem> mOverlayItemList = new ArrayList<OverlayItem>();
    
    public BaseItemsOverlay(Drawable marker) {
        super(boundCenterBottom(marker));
    }

    @Override
    protected OverlayItem createItem(int i) {
        return mOverlayItemList.get(i);
    }

    @Override
    public int size() {
        return mOverlayItemList.size();
    }

    public void addItem(GeoPoint p, String title, String snippet) {
        OverlayItem newItem = new OverlayItem(p, title, snippet);
        mOverlayItemList.add(newItem);
        populate();
    }
}
