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
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(0x66cccccc);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(1);
        canvas.drawLine(getWidth(), 0, getWidth(), getHeight(), mPaint);
        super.onDraw(canvas);
    }

    /**
     * Sets next bus stop. It will alter rendering.
     * 
     * @param stop next bus stop
     */
    public void setNextStop(BusStop stop) {
        mNextStop = stop;
    }
}
