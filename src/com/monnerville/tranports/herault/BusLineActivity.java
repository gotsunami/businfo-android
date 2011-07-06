package com.monnerville.tranports.herault;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;

/**
 *
 * @author mathias
 */
public class BusLineActivity extends ListActivity {
    private String mLine = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.busline);

        final Intent intent = getIntent();
        final Bundle bun = intent.getExtras();
        if (bun != null) {
            mLine = bun.getString("line");
        }
        else
            finish();

        setTitle(mLine);
        BusManager manager = BusManager.getInstance();
    }
}
