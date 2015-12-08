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
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import rtandroid.benchmark.R;

/**
 * View displaying a kind of statistical results.
 */
public class StatisticView extends LinearLayout
{
    private static final String KEY_SUPER = "super";
    private static final String KEY_NAME = "name";
    private static final String KEY_VALUE = "value";
    private static final String KEY_MAXIMUM = "maximum";
    private static final String KEY_DISPLAY_MS = "display_ms";

    //
    // Constructors simply passing data forward
    //

    public StatisticView(Context context)
    {
        this(context, null);
    }

    public StatisticView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.view_statistic, this, true);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StatisticView, 0, 0);
        setTitle(a.getString(R.styleable.StatisticView_statisticTitle));
        a.recycle();
    }

    private void setTitle(String title)
    {
        TextView titleView = (TextView) findViewById(R.id.statistic_title);
        titleView.setText(title);
    }

    public void setResult(Map<String, Integer> results)
    {
        TableLayout table = (TableLayout) findViewById(R.id.test_case_table);
        table.removeAllViewsInLayout();

        // Find maximum and display unit
        int max = 0;
        boolean displayMs = true;
        for (Integer value : results.values())
        {
            max = Math.max(max, value);
            displayMs = displayMs && (value > 1000);
        }

        // Add values
        for (Map.Entry<String, Integer> result : results.entrySet())
        {
            addItem(result.getKey(), result.getValue(), max, displayMs);
        }
    }

    private void addItem(String name, int value, int max, boolean displayMs)
    {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        TableLayout table = (TableLayout) findViewById(R.id.test_case_table);

        StatisticViewItem item = (StatisticViewItem) inflater.inflate(R.layout.view_statistic_item, this, false);
        item.setName(name);
        item.setValue(value, displayMs);
        item.setMaxValue(max);
        item.setPadding(40, 2, 20, 0);
        table.addView(item);
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_SUPER, super.onSaveInstanceState());

        TableLayout table = (TableLayout) findViewById(R.id.test_case_table);
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Integer> values = new ArrayList<>();
        ArrayList<Integer> maximum = new ArrayList<>();
        ArrayList<Boolean> displayMs = new ArrayList<>();

        for (int i = 0; i < table.getChildCount(); i++)
        {
            StatisticViewItem item = (StatisticViewItem) table.getChildAt(i);
            if (item != null)
            {
                names.add(item.getName());
                values.add(item.getValue());
                maximum.add(item.getMaxValue());
                displayMs.add(item.isDisplayModeMs());
            }
        }

        boolean[] displayMsArray = new boolean[displayMs.size()];
        for (int i = 0; i < displayMs.size(); i++)
        {
            displayMsArray[i] = displayMs.get(i);
        }

        bundle.putStringArrayList(KEY_NAME, names);
        bundle.putIntegerArrayList(KEY_VALUE, values);
        bundle.putIntegerArrayList(KEY_MAXIMUM, maximum);
        bundle.putBooleanArray(KEY_DISPLAY_MS, displayMsArray);

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;

            ArrayList<String> names = bundle.getStringArrayList(KEY_NAME);
            ArrayList<Integer> values = bundle.getIntegerArrayList(KEY_VALUE);
            ArrayList<Integer> maximum = bundle.getIntegerArrayList(KEY_MAXIMUM);
            boolean[] displayMs = bundle.getBooleanArray(KEY_DISPLAY_MS);

            for (int i = 0; i < names.size(); i++)
            {
                addItem(names.get(i), values.get(i), maximum.get(i), displayMs[i]);
            }

            state = bundle.getParcelable(KEY_SUPER);
        }

        super.onRestoreInstanceState(state);
    }
}
