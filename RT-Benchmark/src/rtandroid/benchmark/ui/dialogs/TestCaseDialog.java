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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.google.gson.Gson;

import rtandroid.benchmark.R;
import rtandroid.benchmark.RealTimeUtils;
import rtandroid.benchmark.data.TestCase;

/**
 * Dialog which allows creation and editing of test cases
 */
public class TestCaseDialog extends DialogFragment implements SeekBar.OnSeekBarChangeListener
{
    private static final String ARG_CASE = "name";

    private OnTestCaseSaveListener mListener;
    private EditText mName;
    private SeekBar mPriority;
    private TextView mPriorityText;
    private SeekBar mPowerLevel;
    private TextView mPowerLevelText;
    private Spinner mCpuLock;
    private CheckBox mExclusiveLock;

    private TestCase mTestCase;

    /**
     * @return New instance of fragment TestCaseDialog to create a test case.
     */
    public static TestCaseDialog newInstance()
    {
        return new TestCaseDialog();
    }

    /**
     * @return New instance of fragment TestCaseDialog filled with given test case.
     */
    public static TestCaseDialog newInstance(TestCase testCase)
    {
        // Create argument bundle
        Gson gson = new Gson();
        Bundle args = new Bundle();
        args.putString(ARG_CASE, gson.toJson(testCase));

        TestCaseDialog fragment = new TestCaseDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Retrieve views
        View v = inflater.inflate(R.layout.dialog_test_case, null);
        mName = (EditText) v.findViewById(R.id.input_name);

        mPriority = (SeekBar) v.findViewById(R.id.input_priority);
        mPriority.setMax(TestCase.PRIORITY_MAX);
        mPriority.setOnSeekBarChangeListener(this);
        mPriorityText = (TextView) v.findViewById(R.id.txt_priority);

        mPowerLevel = (SeekBar) v.findViewById(R.id.input_power_level);
        mPowerLevel.setMax(TestCase.POWER_LEVEL_MAX);
        mPowerLevel.setOnSeekBarChangeListener(this);
        mPowerLevelText = (TextView) v.findViewById(R.id.txt_power_level);

        // Get number of CPUs
        int count = RealTimeUtils.getCpuCoreCount();
        if (count < 1) { throw new RuntimeException("Failed to get configured processors!"); }

        // We want to be able to lock on all cores
        String[] cores = new String[count];
        cores[0] = "Disabled";
        for (int i = 1; i < count; i++) { cores[i] = "Core " + i; }

        SpinnerAdapter adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, cores);
        mCpuLock = (Spinner) v.findViewById(R.id.input_cpu_core);
        mCpuLock.setAdapter(adapter);

        mExclusiveLock = (CheckBox) v.findViewById(R.id.input_cpu_lock_exclusive);

        // Fill with values
        Bundle args = getArguments();
        if (args != null)
        {
            Gson gson = new Gson();
            String jsonTestCase = args.getString(ARG_CASE);
            mTestCase = gson.fromJson(jsonTestCase, TestCase.class);

            mName.setText(mTestCase.getName());
            mPriority.setProgress(mTestCase.getRealtimePriority());
            mPowerLevel.setProgress(mTestCase.getPowerLevel());
            mCpuLock.setSelection(mTestCase.getCpuCore());
            mExclusiveLock.setChecked(mTestCase.isCpuLockExclusive());
        }

        onProgressChanged(mPriority, mPriority.getProgress(), false);
        onProgressChanged(mPowerLevel, mPowerLevel.getProgress(), false);

        // Build dialog
        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.dialog_test_case_title)
            .setView(v)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface dialogInterface)
            {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        onSubmit();
                    }
                });
            }
        });

        return dialog;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mListener = (OnTestCaseSaveListener) getTargetFragment();
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(getTargetFragment().toString() + " must implement OnTestCaseSaveListener");
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {

        if (seekBar == mPriority)
        {
            String text = (progress == 0) ? "Disabled" : Integer.toString(progress);
            mPriorityText.setText(text);
        }
        else if (seekBar == mPowerLevel)
        {
            String text = (progress == 0) ? "Disabled" : Integer.toString(progress) + "%";
            mPowerLevelText.setText(text);
        }
        else
        {
            throw new RuntimeException("Unknown seek bar event received");
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {
        // Nothing to do
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {
        // Nothing to do
    }

    public void onSubmit()
    {
        if (mListener != null)
        {
            if (mName.getText().toString().isEmpty())
            {
                mName.setError("Please enter a name");
                return;
            }

            // Pass a value to listener
            if (mTestCase == null)
            {
                mTestCase = new TestCase("", TestCase.NO_PRIORITY, TestCase.NO_POWER_LEVEL, TestCase.NO_CORE_LOCK, false);
            }

            mTestCase.setName(mName.getText().toString());
            mTestCase.setCpuCore((int) mCpuLock.getSelectedItemId());
            mTestCase.setCpuLockExclusive(mExclusiveLock.isChecked());

            if (mPriority.getProgress() != 0) { mTestCase.setPriority(mPriority.getProgress()); }
            if (mPowerLevel.getProgress() != 0) { mTestCase.setPowerLevel(mPowerLevel.getProgress()); }

            mListener.onTestCaseSave(mTestCase);
        }

        dismiss();
    }

    /**
     * This interface must be implemented by target fragments that show this
     * dialog to allow an passing of chosen value.
     */
    public interface OnTestCaseSaveListener
    {
        void onTestCaseSave(TestCase testCase);
    }
}