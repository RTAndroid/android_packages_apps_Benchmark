/*
 * Copyright (C) 2016 RTAndroid Project
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
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.google.gson.Gson;

import rtandroid.realtime.RealTimeProxy;
import rtandroid.benchmark.R;
import rtandroid.benchmark.data.TestCase;

/**
 * Dialog which allows creation and editing of test cases
 */
public class TestCaseDialog extends DialogFragment implements SeekBar.OnSeekBarChangeListener
{
    private static final String ARG_CASE = "name";

    private OnTestCaseUpdateListener mListener;
    private EditText mName;
    private SeekBar mPriority;
    private TextView mPriorityText;
    private SeekBar mPowerLevel;
    private TextView mPowerLevelText;
    private Spinner mCpuLock;

    private TestCase mOldTestCase;

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
        try {
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

            // We want to be able to lock on isolated cores
            int[] isolatedCpus = new RealTimeProxy().getIsolatedProcessors();
            String[] cores = new String[1 + isolatedCpus.length];
            cores[0] = "Disabled";
            for (int i = 0; i < isolatedCpus.length; i++) { cores[i+1] = "Core " + isolatedCpus[i]; }

            SpinnerAdapter adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, cores);
            mCpuLock = (Spinner) v.findViewById(R.id.input_cpu_core);
            mCpuLock.setAdapter(adapter);

            // Fill with values
            Bundle args = getArguments();
            if (args != null)
            {
                Gson gson = new Gson();
                String jsonTestCase = args.getString(ARG_CASE);
                mOldTestCase = gson.fromJson(jsonTestCase, TestCase.class);

                mName.setText(mOldTestCase.getName());
                mPriority.setProgress(mOldTestCase.getRealtimePriority());
                mPowerLevel.setProgress(mOldTestCase.getPowerLevel());
                int core = mOldTestCase.getCpuCore();
                for (int i = 0; i < isolatedCpus.length; i++)
                {
                    if (isolatedCpus[i] == core)
                    {
                        //core[0] is Disabled
                        mCpuLock.setSelection(i+1);
                        break;
                    }
                }
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
                public void onShow(DialogInterface dialogInterface) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view) { onSubmit(); }
                    });
                }
            });

            return dialog;
        }
        catch(RemoteException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mListener = (OnTestCaseUpdateListener) getTargetFragment();
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

    private void onSubmit()
    {
        if (mListener != null)
        {
            if (mName.getText().toString().isEmpty())
            {
                mName.setError("Please enter a name");
                return;
            }

            // Pass a value to listener
            TestCase newTestCase = new TestCase(mName.getText().toString(), TestCase.NO_PRIORITY, TestCase.NO_POWER_LEVEL, TestCase.NO_CORE_LOCK);
            newTestCase.setCpuCore((int) mCpuLock.getSelectedItemId());

            if (mPriority.getProgress() != 0) { newTestCase.setPriority(mPriority.getProgress()); }
            if (mPowerLevel.getProgress() != 0) { newTestCase.setPowerLevel(mPowerLevel.getProgress()); }

            mListener.onTestCaseUpdated(mOldTestCase, newTestCase);
        }

        dismiss();
    }

    /**
     * This interface must be implemented by target fragments that show this
     * dialog to allow an passing of chosen value.
     */
    public interface OnTestCaseUpdateListener
    {
        void onTestCaseUpdated(TestCase oldTestCase, TestCase newTestCase);
    }
}