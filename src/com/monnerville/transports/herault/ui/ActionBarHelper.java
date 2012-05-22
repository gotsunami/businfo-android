package com.monnerville.transports.herault.ui;

import android.app.ActionBar;
import android.app.Activity;

/**
 * Wrapper class for ActionBar available on API 11+
 * @author mathias
 */
final class ActionBarHelper {
    public static ActionBar getActionBar(Activity act) {
        return act.getActionBar();
    }
    public static void setHomeButtonEnabled(Activity a, boolean enabled) {
        a.getActionBar().setHomeButtonEnabled(enabled);
    }
    public static void setTitle(Activity a, int title) {
        a.getActionBar().setTitle(title);
    }
    public static void setSubtitle(Activity a, int sub) {
        a.getActionBar().setSubtitle(sub);
    }
}
