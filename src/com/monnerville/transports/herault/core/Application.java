package com.monnerville.transports.herault.core;

import android.content.Context;

/**
 *
 * @author mathias
 */
public final class Application {
    public static final String TAG = "HT";
    /**
     * Gets string resource by name
     */
    public static String getStringResourceByName(Context ctx, String str) {
        String packageName = "com.monnerville.transports.herault";
        int resId = ctx.getResources().getIdentifier(str, "string", packageName);
        return ctx.getString(resId);
    }
}
