/*
 * Copyright (C) 2015 RTAndroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rtandroid.benchmark.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Simple horizontal bar.
 */
public class HorizontalBarView extends View
{
    private static final float OVERHEAD = 1.2f;
    private static final int OFFSET = 3;
    private static final int PRIMARY = 0xFFFFBB33;
    private static final int SECONDARY = 0xFFFF8800;

    private final float mOffsetHorizontal;
    private final float mOffsetVertical;

    private int mValue;
    private int mMaxValue;
    private Paint mPaintPrimary;
    private Paint mPaintSecondary;

    //
    // Constructors simply passing data forward
    //

    public HorizontalBarView(Context context) {
        this(context, null);
    }

    public HorizontalBarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Set some default values
        mMaxValue = 100;
        mValue = 50;

        mOffsetVertical = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, OFFSET, getResources().getDisplayMetrics());
        mOffsetHorizontal = mOffsetVertical * .5f;

        // Load colors
        mPaintPrimary = new Paint();
        mPaintPrimary.setColor(PRIMARY);
        mPaintSecondary = new Paint();
        mPaintSecondary.setColor(SECONDARY);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        float width = getWidth() / (mMaxValue * OVERHEAD) * mValue;
        canvas.drawRect(mOffsetHorizontal, mOffsetVertical, width + mOffsetHorizontal, getHeight(), mPaintSecondary);
        canvas.drawRect(0, 0, width, getHeight() - mOffsetVertical, mPaintPrimary);
    }

    //
    // Member access
    //

    public int getValue()
    {
        return mValue;
    }

    public void setValue(int value)
    {
        this.mValue = value;
        invalidate();
    }

    public int getMaxValue()
    {
        return mMaxValue;
    }

    public void setMaxValue(int max)
    {
        this.mMaxValue = max;
        invalidate();
    }
}
