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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Adapter for a list of benchmark results.
 */
public class BenchmarkResultAdapter extends BaseAdapter implements SpinnerAdapter
{
    private final LayoutInflater mInflater;
    private List<BenchmarkResult> mResults;

    public BenchmarkResultAdapter(Context context, List<BenchmarkResult> results)
    {
        mInflater = LayoutInflater.from(context);
        mResults = results;
    }

    @Override
    public boolean hasStableIds()
    {
        return false;
    }

    @Override
    public int getCount()
    {
        return mResults.size();
    }

    @Override
    public Object getItem(int i)
    {
        return mResults.get(i);
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
            convertView = mInflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        // Fill with correct data
        TextView txtView =  (TextView) convertView.findViewById(android.R.id.text1);
        txtView.setText(mResults.get(i).getName());

        return convertView;
    }

    @Override
    public View getDropDownView(int i, View convertView, ViewGroup parent)
    {
        // Inflate new view if necessary
        if (convertView == null)
        {
            convertView = mInflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        // Fill with correct data
        TextView txtView =  (TextView) convertView.findViewById(android.R.id.text1);
        txtView.setText(mResults.get(i).getName());

        return convertView;
    }
}