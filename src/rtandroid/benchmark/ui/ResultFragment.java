/*
 * Copyright (C) 2017 RTAndroid Project
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

package rtandroid.benchmark.ui;

import android.app.Activity;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import rtandroid.benchmark.R;
import rtandroid.benchmark.data.BenchmarkResult;
import rtandroid.benchmark.ui.views.StatisticView;

/**
 * A fragment showing results of previously executed benchmarks.
 */
public class ResultFragment extends Fragment implements AdapterView.OnItemSelectedListener
{
    private static final Map<Integer, BenchmarkResult.Kind> RESULT_VIEW_MAP;

    static
    {
        Map<Integer, BenchmarkResult.Kind> viewMap = new HashMap<>();
        viewMap.put(R.id.calc_minimum, BenchmarkResult.Kind.CALCULATION_MINIMUM);
        viewMap.put(R.id.calc_mean, BenchmarkResult.Kind.CALCULATION_MEAN);
        viewMap.put(R.id.calc_maximum, BenchmarkResult.Kind.CALCULATION_MAXIMUM);
        viewMap.put(R.id.calc_deviation, BenchmarkResult.Kind.CALCULATION_DEVIATION);
        viewMap.put(R.id.sleep_minimum, BenchmarkResult.Kind.SLEEP_MINIMUM);
        viewMap.put(R.id.sleep_mean, BenchmarkResult.Kind.SLEEP_MEAN);
        viewMap.put(R.id.sleep_maximum, BenchmarkResult.Kind.SLEEP_MAXIMUM);
        viewMap.put(R.id.sleep_deviation, BenchmarkResult.Kind.SLEEP_DEVIATION);
        RESULT_VIEW_MAP = Collections.unmodifiableMap(viewMap);
    }

    private OnFragmentInteractionListener mListener;
    private Spinner mSpinner;
    private SpinnerAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        mAdapter = mListener.getResultAdapter();
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged()
            {
                mSpinner.setSelection(mAdapter.getCount()-1);
            }
        });

        View view = getView();
        if (view == null) { return; }

        mSpinner = (Spinner) getView().findViewById(R.id.run_benchmark_list);
        mSpinner.setAdapter(mAdapter);
        mSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mListener = (OnFragmentInteractionListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
    {
        BenchmarkResult result = (BenchmarkResult) mAdapter.getItem(i);

        view = getView();
        if (view == null) { return; }

        for (Map.Entry<Integer, BenchmarkResult.Kind> entry : RESULT_VIEW_MAP.entrySet())
        {
            StatisticView v = (StatisticView) view.findViewById(entry.getKey());
            v.setResult(result.getResult(entry.getValue()));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
        // Nothing to do here
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener
    {
        /**
         * @return Adapter which returns BenchmarkResult objects on getItem().
         */
        SpinnerAdapter getResultAdapter();
    }
}
