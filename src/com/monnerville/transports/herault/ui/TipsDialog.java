package com.monnerville.transports.herault.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import com.monnerville.transports.herault.R;

public class TipsDialog extends AlertDialog.Builder
{
    private Context mContext;
    private int mCurTip = -1;
    final String[] tips;

	public TipsDialog(Context ctx, boolean fromSettings) {
		super(ctx);
        mContext = ctx;

        setTitle(R.string.did_you_know);
        setIcon(R.drawable.logo);
        setCancelable(true);

        tips = ctx.getResources().getStringArray(R.array.tips);
        setMessage(tips[getNextTipId()]);

        setNegativeButton(R.string.next_tip, new OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                setMessage(tips[getNextTipId()]);
                show();
            }
        });

        if (!fromSettings) {
            setNeutralButton(R.string.disable_tips, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int arg1) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                    SharedPreferences.Editor ed = prefs.edit();
                    ed.putBoolean("pref_show_tips_at_startup", false);
                    ed.commit();
                    dialog.cancel();
                }
            });
        }

        setPositiveButton(R.string.close_tips, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.cancel();
            }
        });
	}

    private int getNextTipId() {
        if (mCurTip < tips.length - 1)
            mCurTip++;
        else
            mCurTip = 0;
        return mCurTip;
    }
}
