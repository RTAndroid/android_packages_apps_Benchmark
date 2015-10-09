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

import rtandroid.benchmark.R;
import rtandroid.benchmark.benchmarks.Benchmark;
import rtandroid.benchmark.benchmarks.BenchmarkManager;

/**
 * Dialog which allows choosing of one of the available benchmarks.
 */
public class BenchmarkPickerDialog extends DialogFragment implements DialogInterface.OnClickListener
{
    private OnValueSelectedListener mListener;

    /**
     * @return New instance of fragment NumberPickerDialog.
     */
    public static BenchmarkPickerDialog newInstance()
    {
        return new BenchmarkPickerDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Create value list
        Benchmark[] benchmarks = BenchmarkManager.getBenchmarks();
        String[] benchmarkNames = new String[benchmarks.length];
        for (int i = 0; i < benchmarks.length; i++)
        {
            benchmarkNames[i] = benchmarks[i].getName();
        }

        // Build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setTitle(R.string.run_input_benchmark)
                .setItems(benchmarkNames, this)
                .create();
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mListener = (OnValueSelectedListener) getTargetFragment();
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(getTargetFragment().toString() + " must implement OnValueSelectedListener");
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i)
    {
        if (mListener != null)
        {
            // Pass value to listener
            int requestCode = getTargetRequestCode();
            mListener.onValueSelected(requestCode, i);
        }
    }

    /**
     * This interface must be implemented by target fragments that show this
     * dialog to allow an passing of chosen value.
     */
    public interface OnValueSelectedListener
    {
        void onValueSelected(int requestCode, int value);
    }
}