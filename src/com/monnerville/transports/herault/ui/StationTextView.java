package com.monnerville.transports.herault.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import com.monnerville.transports.herault.core.BusStop;

/**
 *
 * @author mathias
 */
public class StationTextView extends TextView {
    private BusStop mNextStop = null;
    private Paint mPaint; 

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
        /*
        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setTextSize(19);
        mPaint.setAntiAlias(true);
         * 
         */
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /*
        setText(mNextStop == null ? "!" : BusStop.TIME_FORMATTER.format(mNextStop.getTime()));
//            gd.setCornerRadius(5);
//        setBackgroundDrawable(gd);
        canvas.drawText(txt, 0, 30, mPaint);
         * 
         */
    }

    /**
     * Sets next bus stop. It will alter rendering.
     * 
     * @param stop next bus stop
     */
    public void setNextStop(BusStop stop) {
        mNextStop = stop;
    }

    /*
    public Drawable getBackgroundDrawable() {
        int colors[] = { 0x00ffffff, mNextStop == null ? 0xffff0000 : 0xff039900, 0x00ffffff };
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
        return gd;
    }
     * 
     */
}
