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
import android.widget.NumberPicker;
import android.widget.TextView;

import rtandroid.benchmark.R;

/**
 * Dialog which allows choosing of a number in certain range with a step.
 */
public class NumberPickerDialog extends DialogFragment implements DialogInterface.OnClickListener
{
    private static final String ARG_TITLE = "title";
    private static final String ARG_MIN = "min_value";
    private static final String ARG_MAX = "max_value";
    private static final String ARG_VALUE = "value";
    private static final String ARG_STEP = "step";
    private static final String ARG_UNIT = "unit";

    private OnValueSelectedListener mListener;
    private String[] mDisplayedValues;
    private NumberPicker mPicker;

    /**
     * @return New instance of fragment NumberPickerDialog.
     */
    public static NumberPickerDialog newInstance(int titleRes, int min, int max, int step, int value, int unit)
    {
        // Check borders
        if(max <= min)
        {
            throw new IllegalArgumentException("Maximum value must be larger than minimum!");
        }

        // Create argument bundle
        Bundle args = new Bundle();
        args.putInt(ARG_TITLE, titleRes);
        args.putInt(ARG_MIN, min);
        args.putInt(ARG_MAX, max);
        args.putInt(ARG_STEP, step);
        args.putInt(ARG_VALUE, value);
        args.putInt(ARG_UNIT, unit);

        NumberPickerDialog fragment = new NumberPickerDialog();
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
        int titleRes = args.getInt(ARG_TITLE);
        int value = args.getInt(ARG_VALUE);
        int min = args.getInt(ARG_MIN);
        int max = args.getInt(ARG_MAX);
        int step = args.getInt(ARG_STEP);
        int unitRes = args.getInt(ARG_UNIT);

        // Create value list
        mDisplayedValues = new String[(max - min) / step + 1];
        for(int i = 0; i < mDisplayedValues.length; i++)
        {
            mDisplayedValues[i] = String.valueOf(min + step * i);
        }

        // Fill views
        View v = inflater.inflate(R.layout.dialog_number_picker, null);
        mPicker = (NumberPicker) v.findViewById(R.id.picker_value);
        mPicker.setMinValue(0);
        mPicker.setMaxValue(mDisplayedValues.length - 1);
        mPicker.setValue((value - min) / step);
        mPicker.setDisplayedValues(mDisplayedValues);
        mPicker.setWrapSelectorWheel(false);
        mPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // Blocks keyboard
        TextView unitName = (TextView) v.findViewById(R.id.picker_unit);
        unitName.setText(unitRes);

        // Build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setTitle(titleRes)
                .setView(v)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, null)
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
        if(mListener != null)
        {
            // Pass value to listener
            int requestCode = getTargetRequestCode();
            String entry = mDisplayedValues[mPicker.getValue()];
            mListener.onValueSelected(requestCode, Integer.parseInt(entry));
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