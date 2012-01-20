package com.monnerville.transports.herault.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.monnerville.transports.herault.R;

/**
 *
 * @author mathias
 */
public final class DaysOfWeekView extends View {
    private Paint mPaint; 
    private int mTotalWidth = 0;
    private int mTotalHeight = 20;
    // Current letter x offset
    private int mInternalPadding = 5;
    private int mLetterSpacing = 5;
    private int mBinCircPattern = 0;
    private int mRoundBorder = 2;
    private int mMargin = 1;

    private static final int[] mDaysResId = {
        R.string.monday_short,
        R.string.tuesday_short,
        R.string.wednesday_short,
        R.string.thursday_short,
        R.string.friday_short,
        R.string.saturday_short,
        R.string.sunday_short,
    };

    // Should be settable as XML attributes
    private int mFontSize = 3;


    // All 3 constructors must be implemented for inflating to work
    public DaysOfWeekView(Context ctx) {
        super(ctx);
        init();
    }

    public DaysOfWeekView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init();
    }

    public DaysOfWeekView(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        // 10: measureText("A");
        mTotalWidth = (int)(7 * (2*mInternalPadding + mLetterSpacing + mPaint.measureText("A")));
        // getTextBounds (String text, int start, int end, Rect bounds)
    }

    public void setBinaryCirculationPattern(int pat) { mBinCircPattern = pat; }

    @Override
    protected void onDraw(Canvas canvas) {
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setStyle(Paint.Style.FILL);
        /*
        mPaint.setTextSize(30);
        mPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.ITALIC));
         * 
         */

        mPaint.setStrokeWidth(1);
        canvas.translate(getPaddingLeft(), 0);
        String day;
        for (int k=0; k < mDaysResId.length; k++) {
            day = getContext().getString(mDaysResId[k]);
            mPaint.setColor(getResources().getColor(R.color.ht_blue));
            canvas.drawRoundRect(new RectF(0, 0, mPaint.measureText("A") + 
                2*mInternalPadding, mTotalHeight-mMargin), mRoundBorder, mRoundBorder, mPaint);

            mPaint.setColor(getResources().getColor(android.R.color.white));
            canvas.drawText(day, mInternalPadding, 14, mPaint);

            canvas.translate(mPaint.measureText("A") + 2*mInternalPadding + mLetterSpacing, 0);
        }
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getPaddingLeft() + getPaddingRight() + mTotalWidth;
        int height = mTotalHeight;
        setMeasuredDimension(width, height);
    }
}
