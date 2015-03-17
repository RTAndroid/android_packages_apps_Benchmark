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
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import rtandroid.benchmark.R;
import rtandroid.benchmark.data.TestCase;

/**
 * Selectable list item of a test case.
 */
public class TestCaseItem extends RelativeLayout implements View.OnClickListener, View.OnLongClickListener,
                                                                    CompoundButton.OnCheckedChangeListener
{
    private OnCheckedChangeListener mListener;
    private CheckBox mCheckBox;
    private TextView mTitle;
    private TextView mDetails;
    private TestCase mTestCase;

    public TestCaseItem(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        // Check if all necessary views exist
        mCheckBox = (CheckBox) findViewById(R.id.test_case_chosen);
        mTitle = (TextView) findViewById(R.id.test_case_title);
        mDetails = (TextView) findViewById(R.id.test_case_details);

        // Register listeners
        mCheckBox.setOnCheckedChangeListener(this);
        setOnClickListener(this);
        setOnLongClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        // Behave as if checkbox was clicked
        mCheckBox.performClick();
    }

    @Override
    public boolean onLongClick(View view)
    {
        // TODO: Provide possibility to edit and delete
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b)
    {
        if (mListener != null)
        {
            mListener.OnCheckedChanged(this, b);
        }
    }

    /**
     * @return True if test case is selected.
     */
    public boolean isChecked()
    {
        return mCheckBox.isChecked();
    }

    /**
     * @return Displayed test case
     */
    public TestCase getTestCase()
    {
        return mTestCase;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener)
    {
        mListener = listener;
    }

    /**
     * Fill view with data.
     */
    public void fill(TestCase testCase, boolean selected)
    {
        StringBuilder details = new StringBuilder();

        int priority = testCase.getRealtimePriority();
        if (priority == TestCase.NO_REALTIME_PRIORITY)
        {
            details.append("No real-time priority");
        }
        else
        {
            details.append("Real-time priority: ").append(priority);
        }
        details.append(", ");

        int powerLevel = testCase.getPowerLevel();
        if(powerLevel == TestCase.NO_POWER_LEVEL)
        {
            details.append("No power level");
        }
        else
        {
            details.append("Power level: ").append(powerLevel).append("%");
        }

        mTestCase = testCase;
        mTitle.setText(testCase.getName());
        mDetails.setText(details.toString());
        mCheckBox.setChecked(selected);
    }

    /**
     * Interface to be notified when checked state changes.
     */
    public interface OnCheckedChangeListener
    {
        public void OnCheckedChanged(TestCaseItem item, boolean checked);
    }
}
