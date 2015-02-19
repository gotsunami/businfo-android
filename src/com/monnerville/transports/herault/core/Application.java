package com.monnerville.transports.herault.core;

import android.content.Context;
import android.os.Build;
import java.util.List;

/**
 *
 * @author mathias
 */
public final class Application {
    public static final String TAG = "Businfo";
    /**
     * Gets string resource by name
     */
    public static String getStringResourceByName(Context ctx, String str) {
        String packageName = "com.monnerville.transports.herault";
        int resId = ctx.getResources().getIdentifier(str, "string", packageName);
        return ctx.getString(resId);
    }

    /**
     * Joins a list's elements with a string separator
     * @param data String elements to join
     * @param separator separator string
     * @return merged list of strings
     */
    public static String getJoinedList(List<String> data, String separator) {
        StringBuilder sb = new StringBuilder();
        for (String d : data) {
            sb.append(d).append(separator).append(" ");
        }
        sb.replace(sb.length()-1-separator.length(), sb.length(), "");
        return sb.toString();
    }

    public static boolean OSBeforeHoneyComb() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB;
    }
}
