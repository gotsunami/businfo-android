/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.monnerville.transports.herault.ui.maps;

import android.graphics.drawable.Drawable;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

/**
 *
 * @author mathias
 */
public class BusLinesOverlay extends ItemizedOverlay<OverlayItem> {

    public BusLinesOverlay(Drawable marker) {
        super(boundCenterBottom(marker));
    }

    @Override
    protected OverlayItem createItem(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
