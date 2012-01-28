package com.monnerville.transports.herault.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.widget.TextView;

import com.monnerville.transports.herault.R;

public class TipsDialog extends AlertDialog.Builder
{
    private int mCurTip = -1;
    final String[] tips;

	public TipsDialog(Context ctx) {
		super(ctx);

        setTitle(R.string.did_you_know);
        setIcon(R.drawable.logo);
        setCancelable(true);

        tips = ctx.getResources().getStringArray(R.array.tips);
        setMessage(tips[getNextTip()]);

        setPositiveButton(R.string.next_tip, new OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                setMessage(tips[getNextTip()]);
                show();
            }
        });

        setNegativeButton(R.string.disable_tips, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.cancel();
            }
        });
	}

    private int getNextTip() {
        if (mCurTip < tips.length - 1)
            mCurTip++;
        else
            mCurTip = 0;
        return mCurTip;
    }
}
