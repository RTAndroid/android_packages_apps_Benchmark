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

package rtandroid.benchmark.data;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import rtandroid.benchmark.R;
import rtandroid.benchmark.data.TestCase.TestCaseComparator;
import rtandroid.benchmark.ui.views.TestCaseItem;

/**
 * Adapter for a list of test cases.
 */
public class TestCaseAdapter extends BaseAdapter implements TestCaseItem.OnCheckedChangeListener
{
    private static final String BUNDLE_SELECTED_CASES = "selected_cases";

    private final LayoutInflater mInflater;
    private final List<TestCase> mTestCases;
    private final Set<TestCase> mSelectedCases;

    public TestCaseAdapter(Context context, List<TestCase> cases)
    {
        mInflater = LayoutInflater.from(context);
        mTestCases = cases;

        mSelectedCases = new TreeSet<TestCase>(new TestCaseComparator());
        mSelectedCases.addAll(cases);
    }

    @Override
    public boolean hasStableIds()
    {
        return false;
    }

    @Override
    public int getCount()
    {
        return mTestCases.size();
    }

    @Override
    public Object getItem(int i)
    {
        return null;
    }

    @Override
    public long getItemId(int i)
    {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent)
    {
        // Inflate new view if necessary
        if (convertView == null)
        {
            convertView = mInflater.inflate(R.layout.test_case_item, parent, false);
        }

        // Fill with correct data
        TestCase testCase = mTestCases.get(i);
        boolean selected = mSelectedCases.contains(testCase);

        TestCaseItem item = (TestCaseItem) convertView;
        item.fill(testCase, selected);
        item.setOnCheckedChangeListener(this);
        item.setOnCreateContextMenuListener((View.OnCreateContextMenuListener) item.getContext());

        return convertView;
    }

    @Override
    public void onCheckedChanged(TestCaseItem item, boolean checked)
    {
        TestCase testCase = item.getTestCase();
        if (checked)
        {
            mSelectedCases.add(testCase);
        }
        else
        {
            mSelectedCases.remove(testCase);
        }
    }

    @Override
    public void notifyDataSetChanged()
    {
        super.notifyDataSetChanged();

        // Remove all not existing test cases
        cleanSelectionSet();
    }

    @Override
    public void notifyDataSetInvalidated()
    {
        super.notifyDataSetInvalidated();

        // Remove all not existing test cases
        cleanSelectionSet();
    }

    /**
     * Remove all non-existing selection elements.
     */
    private void cleanSelectionSet()
    {
        Iterator<TestCase> iter = mSelectedCases.iterator();
        while (iter.hasNext())
        {
            if(!mTestCases.contains(iter.next()))
            {
                iter.remove();
            }
        }
    }

    /**
     * @return All selected test cases.
     */
    public Set<TestCase> getSelectedTestCases()
    {
        return mSelectedCases;
    }

    public void restoreInstance(Bundle savedInstanceState)
    {
        Gson gson = new Gson();
        String jsonTestCases = savedInstanceState.getString(BUNDLE_SELECTED_CASES, "");
        TestCase[] cases = gson.fromJson(jsonTestCases, TestCase[].class);
        mSelectedCases.clear();
        mSelectedCases.addAll(Arrays.asList(cases));
    }

    public void saveInstance(Bundle outState)
    {
        // Convert to array
        TestCase[] selected = new TestCase[mSelectedCases.size()];
        mSelectedCases.toArray(selected);

        // Serialize it
        Gson gson = new Gson();
        String jsonSelectedCases = gson.toJson(selected, TestCase[].class);

        // Save to bundle
        outState.putString(BUNDLE_SELECTED_CASES, jsonSelectedCases);
    }

}