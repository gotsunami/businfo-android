package com.monnerville.transports.herault.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 *
 * @author mathias
 */
public class StationTextView extends TextView {
    private String mCaption;

    // All 3 constructors must be implemented for inflating to work
    public StationTextView(Context ctx) {
        super(ctx);
        init();
    }

    public StationTextView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init();
    }

    public StationTextView(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
        init();
    }

    private void init() {
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int colors[] = { 0xffffffff, 0xff11155d, 0xffffffff };
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
        gd.setCornerRadius(5);
        setBackgroundDrawable(gd);

        super.onDraw(canvas);
    }
}
