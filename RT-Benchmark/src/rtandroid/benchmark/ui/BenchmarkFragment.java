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

package rtandroid.benchmark.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import rtandroid.benchmark.R;
import rtandroid.benchmark.data.BenchmarkConfiguration;
import rtandroid.benchmark.data.TestCase;
import rtandroid.benchmark.data.TestCaseAdapter;
import rtandroid.benchmark.service.BenchmarkService;
import rtandroid.benchmark.ui.dialogs.BenchmarkPickerDialog;
import rtandroid.benchmark.ui.dialogs.NumberPickerDialog;
import rtandroid.benchmark.ui.dialogs.ProgressDialog;

/**
 * A fragment allowing to execute different benchmarks with different test cases.
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener} interface.
 */
public class BenchmarkFragment extends Fragment implements View.OnClickListener,
                                                           NumberPickerDialog.OnValueSelectedListener,
                                                           BenchmarkPickerDialog.OnValueSelectedListener,
                                                           ProgressDialog.OnProgressListener
{
    private static final String TAG = BenchmarkFragment.class.getSimpleName();

    // Range values and default values
    private static final int PARAMETER_MIN = 200;
    private static final int PARAMETER_MAX = 2000;
    private static final int PARAMETER_STEP = 200;
    private static final int PARAMETER_DEFAULT = 200;
    private static final int CYCLES_MIN = 1000;
    private static final int CYCLES_MAX = 20000;
    private static final int CYCLES_STEP = 1000;
    private static final int CYCLES_DEFAULT = 1000;
    private static final int SLEEP_MIN = 1;
    private static final int SLEEP_MAX = 100;
    private static final int SLEEP_STEP = 1;
    private static final int SLEEP_DEFAULT = 10;

    // Preference keys
    private static final String KEY_BENCHMARK = "benchmark";
    private static final String KEY_PARAMETER = "parameter";
    private static final String KEY_CYCLES = "cycles";
    private static final String KEY_SLEEP = "sleep";

    private final BenchmarkConfiguration mConfig = new BenchmarkConfiguration();

    private OnFragmentInteractionListener mListener;
    private TestCaseAdapter mTestCaseAdapter;
    private List<TestCase> mTestCases;

    private TextView mBenchmarkDisplay;
    private TextView mParameterDisplay;
    private TextView mCyclesDisplay;
    private TextView mSleepDisplay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_benchmark, container, false);

        // Adapt to phone layout
        boolean isTwinMode = (root.findViewById(R.id.start_benchmark) != null);
        if (!isTwinMode)
        {
            View header = inflater.inflate(R.layout.benchmark_settings, null, false);
            View footer = inflater.inflate(R.layout.benchmark_test_cases, null, false);

            ListView listView = (ListView) root.findViewById(R.id.test_case_list);
            listView.addHeaderView(header);
            listView.addFooterView(footer);
        }

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        View root = getView();
        if (root == null) { return; }

        // Find views for benchmark settings
        mBenchmarkDisplay = (TextView) root.findViewById(R.id.input_benchmark_display);
        mParameterDisplay = (TextView) root.findViewById(R.id.input_parameter_display);
        mCyclesDisplay = (TextView) root.findViewById(R.id.input_cycles_display);
        mSleepDisplay = (TextView) root.findViewById(R.id.input_sleep_display);

        // Load last benchmark settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mConfig.Parameter = prefs.getInt(KEY_PARAMETER, PARAMETER_DEFAULT);
        mParameterDisplay.setText(Integer.toString(mConfig.Parameter));
        mConfig.Cycles = prefs.getInt(KEY_CYCLES, CYCLES_DEFAULT);
        mCyclesDisplay.setText(Integer.toString(mConfig.Cycles));
        mConfig.SleepMs = prefs.getInt(KEY_SLEEP, SLEEP_DEFAULT);
        mSleepDisplay.setText(Integer.toString(mConfig.SleepMs) + " ms");

        mConfig.BenchmarkIdx = prefs.getInt(KEY_BENCHMARK, 0);
        if (mConfig.getBenchmark() == null) { mConfig.BenchmarkIdx = 0; }
                                       else { mBenchmarkDisplay.setText(mConfig.getBenchmark().getName()); }

        // Register touch handler
        root.findViewById(R.id.benchmark).setOnClickListener(this);
        root.findViewById(R.id.parameter).setOnClickListener(this);
        root.findViewById(R.id.cycles).setOnClickListener(this);
        root.findViewById(R.id.sleep).setOnClickListener(this);
        root.findViewById(R.id.start_benchmark).setOnClickListener(this);

        // Fill test case list
        mTestCases = new ArrayList<TestCase>();
        mTestCases.addAll(mListener.loadTestCases());
        mTestCaseAdapter = new TestCaseAdapter(getActivity(), mTestCases);

        ListView listView = (ListView) root.findViewById(R.id.test_case_list);
        listView.setAdapter(mTestCaseAdapter);

        // Restore state
        if (savedInstanceState != null)
        {
            mTestCaseAdapter.restoreInstance(savedInstanceState);
        }
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
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        mTestCaseAdapter.saveInstance(outState);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view)
    {
        DialogFragment dialog = null;
        switch (view.getId())
        {
            case R.id.benchmark:
                dialog = BenchmarkPickerDialog.newInstance();
                break;
            case R.id.parameter:
                dialog = NumberPickerDialog.newInstance(R.string.run_input_parameter, PARAMETER_MIN, PARAMETER_MAX, PARAMETER_STEP, mConfig.Parameter, R.string.run_input_no_unit);
                break;
            case R.id.cycles:
                dialog = NumberPickerDialog.newInstance(R.string.run_input_cycles, CYCLES_MIN, CYCLES_MAX, CYCLES_STEP, mConfig.Cycles, R.string.run_input_no_unit);
                break;
            case R.id.sleep:
                dialog = NumberPickerDialog.newInstance(R.string.run_input_sleep, SLEEP_MIN, SLEEP_MAX, SLEEP_STEP, mConfig.SleepMs, R.string.run_input_sleep_unit);
                break;
            case R.id.start_benchmark:
                startBenchmark();
                break;
            default:
                throw new RuntimeException("Click event from unknown view received!");
        }

        // Show dialog if desired
        if (dialog != null)
        {
            dialog.setTargetFragment(this, view.getId());
            dialog.show(getFragmentManager(), null);
        }
    }

    @Override
    public void onValueSelected(int requestCode, int value)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        switch (requestCode)
        {
            case R.id.benchmark:
                mConfig.BenchmarkIdx = value;
                prefs.edit().putInt(KEY_BENCHMARK, mConfig.BenchmarkIdx).commit();
                mBenchmarkDisplay.setText(mConfig.getBenchmark().getName());
                break;
            case R.id.parameter:
                mConfig.Parameter = value;
                prefs.edit().putInt(KEY_PARAMETER, mConfig.Parameter).commit();
                mParameterDisplay.setText(Integer.toString(mConfig.Parameter));
                break;
            case R.id.cycles:
                mConfig.Cycles = value;
                prefs.edit().putInt(KEY_CYCLES, mConfig.Cycles).commit();
                mCyclesDisplay.setText(Integer.toString(mConfig.Cycles));
                break;
            case R.id.sleep:
                mConfig.SleepMs = value;
                prefs.edit().putInt(KEY_SLEEP, mConfig.SleepMs).commit();
                mSleepDisplay.setText(Integer.toString(mConfig.SleepMs));
                break;
            default:
                throw new RuntimeException("Selected value with unknown request code received!");
        }
    }

    private void startBenchmark()
    {
        Set<TestCase> selectedCases = mTestCaseAdapter.getSelectedTestCases();

        // Abort if no test cases were selected
        if (selectedCases.isEmpty())
        {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_missing_test_case_title)
                    .setMessage(R.string.dialog_missing_test_case_msg)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        }

        // Add a warmup case
        selectedCases.add(new TestCase("Warmup Phase", TestCase.NO_PRIORITY, TestCase.NO_POWER_LEVEL, TestCase.NO_CORE_LOCK));

        // Notify listener
        if (mListener != null) { mListener.onBenchmarkStart(mConfig); }

        // Show dialog
        DialogFragment dialog = ProgressDialog.newInstance(mConfig.getBenchmark().getName(), selectedCases.size(), mConfig.Cycles);
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), null);

        // Start service and queue all benchmarks
        Context context = getActivity();
        Intent intent = new Intent(context, BenchmarkService.class);
        intent.putExtra(BenchmarkService.EXTRA_BENCHMARK, mConfig.BenchmarkIdx);
        intent.putExtra(BenchmarkService.EXTRA_PARAMETER, mConfig.Parameter);
        intent.putExtra(BenchmarkService.EXTRA_CYCLES, mConfig.Cycles);
        intent.putExtra(BenchmarkService.EXTRA_SLEEP, mConfig.SleepMs);

        Gson gson = new Gson();
        for (TestCase testCase : selectedCases)
        {
            String jsonTestCase = gson.toJson(testCase, TestCase.class);
            intent.putExtra(BenchmarkService.EXTRA_TEST_CASE, jsonTestCase);
            context.startService(intent);
        }
    }

    @Override
    public void onBenchmarkCanceled()
    {
        Log.d(TAG, "Benchmark was canceled");
    }

    @Override
    public void onTestCaseCompleted(String name, String filename)
    {
        // can't notify without the listener
        if (mListener == null) { return; }

        // Find corresponding test case
        TestCase completedTest = null;
        for (TestCase testCase : mTestCases)
        {
            if (testCase.getName().equals(name)) { completedTest = testCase; }
        }

        // Add only valid tests to shown statistics
        if (completedTest != null) { mListener.onTestCaseCompleted(completedTest, filename); }
    }

    @Override
    public void onBenchmarkFinished()
    {
        // can't notify without the listener
        if (mListener == null) { return; }

        mListener.onBenchmarkFinished();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener
    {
        void onBenchmarkStart(BenchmarkConfiguration config);

        void onTestCaseCompleted(TestCase testCase, String fileName);

        void onBenchmarkFinished();

        List<TestCase> loadTestCases();

        void saveTestCases(List<TestCase> testCases);
    }
}
