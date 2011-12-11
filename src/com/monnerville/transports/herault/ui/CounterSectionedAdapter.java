package com.monnerville.transports.herault.ui;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.commonsware.android.listview.SectionedAdapter;
import com.monnerville.transports.herault.R;

/**
 * Sectioned adapter with counter on the right part of the header
 * @author mathias
 */
abstract public class CounterSectionedAdapter extends SectionedAdapter {
    private Activity mContext;

    public CounterSectionedAdapter(Activity ctx) {
        super();
        mContext = ctx;
    }

    @Override
    protected View getHeaderView(String caption, int index, View convertView, ViewGroup parent) {
        LinearLayout result = (LinearLayout)convertView;
        if (convertView == null) {
            result = (LinearLayout)mContext.getLayoutInflater().inflate(R.layout.list_counter_header, null);
        }
        TextView tv = (TextView)result.findViewById(R.id.result_title);
        tv.setText(caption);

        TextView num = (TextView)result.findViewById(R.id.result_num_match);
        num.setText(String.valueOf(getMatches(caption)));
        return result;
    }

    /**
     * Get result match count depending on caption header
     * @param caption name of header section
     * @return number of matches
     */
    abstract protected int getMatches(String caption);
}
