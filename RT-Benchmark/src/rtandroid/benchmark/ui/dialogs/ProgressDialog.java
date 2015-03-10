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

package rtandroid.benchmark.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Locale;

import rtandroid.benchmark.R;
import rtandroid.benchmark.service.BenchmarkService;

/**
 * Dialog which shows progress of current executed benchmark.
 */
public class ProgressDialog extends DialogFragment implements DialogInterface.OnClickListener
{
    private static final String ARG_BENCHMARK = "benchmark";
    private static final String ARG_CASE_COUNT = "case_count";
    private static final String ARG_CYCLES = "cycles";

    private static final String BUNDLE_CASE_NAME = "test_case_name";
    private static final String BUNDLE_CASES_COMPLETED = "cases_completed";
    private static final String BUNDLE_CYCLES_COMPLETED = "cycles_completed";

    private OnProgressListener mListener;
    private BroadcastReceiver mUpdateReceiver;

    private String mCurrentTestCaseName = "";
    private int mCasesTotal;
    private int mCasesCompleted = 0;
    private int mCyclesTotal;
    private int mCyclesPerRun;
    private int mCyclesCompleted = 0;

    private TextView mTotalProgress;
    private ProgressBar mTotalProgressBar;
    private TextView mCurrentProgress;
    private ProgressBar mCurrentProgressBar;

    /**
     * @return New instance of fragment ProgressDialog.
     */
    public static ProgressDialog newInstance(String benchmarkName, int caseCount, int cycles)
    {
        // Create argument bundle
        Bundle args = new Bundle();
        args.putString(ARG_BENCHMARK, benchmarkName);
        args.putInt(ARG_CASE_COUNT, caseCount);
        args.putInt(ARG_CYCLES, cycles);

        ProgressDialog fragment = new ProgressDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Extract arguments
        Bundle args = getArguments();
        String benchmarkName = args.getString(ARG_BENCHMARK);
        mCasesTotal = args.getInt(ARG_CASE_COUNT);
        mCyclesPerRun = args.getInt(ARG_CYCLES);
        mCyclesTotal = mCasesTotal * mCyclesPerRun;

        // Prepare views
        View v = inflater.inflate(R.layout.dialog_progress, null);
        mTotalProgress = (TextView) v.findViewById(R.id.total_txt);
        mCurrentProgress = (TextView) v.findViewById(R.id.current_txt);
        mTotalProgressBar = (ProgressBar) v.findViewById(R.id.total_bar);
        mTotalProgressBar.setMax(mCasesTotal * mCyclesPerRun);
        mCurrentProgressBar = (ProgressBar) v.findViewById(R.id.current_bar);
        mCurrentProgressBar.setMax(mCyclesPerRun);
        updateProgress();

        // Build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Dialog dlg = builder
                .setTitle(String.format(Locale.US, "Performing %s Benchmark...", benchmarkName))
                .setView(v)
                .setNegativeButton(android.R.string.cancel, this)
                .create();
        dlg.setCanceledOnTouchOutside(false);
        return dlg;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Restore old values
        if(savedInstanceState != null)
        {
            mCurrentTestCaseName = savedInstanceState.getString(BUNDLE_CASE_NAME);
            mCasesCompleted = savedInstanceState.getInt(BUNDLE_CASES_COMPLETED);
            mCyclesCompleted = savedInstanceState.getInt(BUNDLE_CYCLES_COMPLETED);
        }
    }



    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        // Register listener
        mUpdateReceiver = new UpdateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BenchmarkService.ACTION_START);
        filter.addAction(BenchmarkService.ACTION_UPDATE);
        filter.addAction(BenchmarkService.ACTION_FINISHED);
        activity.registerReceiver(mUpdateReceiver, filter);

        try
        {
            mListener = (OnProgressListener) getTargetFragment();
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(getTargetFragment().toString() + " must implement OnValueSelectedListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        // Restore current values
        outState.putString(BUNDLE_CASE_NAME, mCurrentTestCaseName);
        outState.putInt(BUNDLE_CASES_COMPLETED, mCasesCompleted);
        outState.putInt(BUNDLE_CYCLES_COMPLETED, mCyclesCompleted);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();

        getActivity().unregisterReceiver(mUpdateReceiver);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i)
    {
        // Abort benchmark
        stopService();

        // Notify listener
        if(mListener != null)
        {
            mListener.onBenchmarkCanceled();
        }
    }

    private void updateProgress()
    {
        int completedCycles = mCasesCompleted * mCyclesPerRun + mCyclesCompleted;

        mTotalProgress.setText(String.format(Locale.US, "Total: %d%%", 100 * completedCycles / mCyclesTotal));
        mCurrentProgress.setText(String.format(Locale.US, "%s: %d%%", mCurrentTestCaseName, 100 * mCyclesCompleted / mCyclesPerRun));

        mTotalProgressBar.setProgress(mCasesCompleted * mCyclesPerRun + mCyclesCompleted);
        mCurrentProgressBar.setProgress(mCyclesCompleted);
    }

    private void stopService()
    {
        Intent intent = new Intent(getActivity(), BenchmarkService.class);
        getActivity().stopService(intent);
    }

    private class UpdateReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if(action.equals(BenchmarkService.ACTION_START))
            {
                mCurrentTestCaseName = intent.getStringExtra(BenchmarkService.EXTRA_TEST_CASE_NAME);
            }
            else if(action.equals(BenchmarkService.ACTION_UPDATE))
            {
                mCyclesCompleted = intent.getIntExtra(BenchmarkService.EXTRA_ITERATIONS, -1);
                if(mCyclesCompleted == -1 || mCasesCompleted > mCyclesPerRun)
                {
                    throw new RuntimeException("Invalid count of completed cycles received");
                }
            }
            else if(action.equals(BenchmarkService.ACTION_FINISHED))
            {
                mCyclesCompleted = 0;
                mCasesCompleted++;

                // Notify
                if(mListener != null)
                {
                    int id = intent.getIntExtra(BenchmarkService.EXTRA_TEST_CASE_ID, -1);
                    String fileName = intent.getStringExtra(BenchmarkService.EXTRA_FILENAME);
                    mListener.onTestCaseCompleted(id, fileName);
                }

                // Close
                if(mCasesCompleted == mCasesTotal)
                {
                    stopService();
                    dismiss();

                    if(mListener != null)
                    {
                        mListener.onBenchmarkFinished();
                    }
                }
            }
            else
            {
                throw new RuntimeException("Received intent has unknown action!");
            }

            updateProgress();
        }
    }

    /**
     * This interface must be implemented by target fragments that show this
     * dialog to allow reporting of progress.
     */
    public interface OnProgressListener
    {
        public void onTestCaseCompleted(int id, String fileName);

        public void onBenchmarkFinished();

        public void onBenchmarkCanceled();
    }
}