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

package rtandroid.benchmark.data;

import java.util.ArrayList;
import java.util.Arrays;

public class ResultStatistics
{
    private ArrayList<Integer> mValues = new ArrayList<>();

    public void add(int value)
    {
        mValues.add(value);
    }

    private Integer[] getSortedArray()
    {
        int size = mValues.size();
        Integer[] values = new Integer[size];
        mValues.toArray(values);
        Arrays.sort(values);
        return values;
    }

    public int getMin()
    {
        Integer[] values = getSortedArray();
        return values[0];
    }

    public int getMax()
    {
        Integer[] values = getSortedArray();
        return values[values.length - 1];
    }

    public long getSum()
    {
        long sum = 0;
        for (Integer value : mValues) { sum += value; }
        return sum;
    }

    public double getMean()
    {
        double sum = getSum();
        return sum / mValues.size();
    }

    /** Returns the sample variance */
    public double getVar()
    {
        double avg = getMean();
        double sum = 0.0;
        for (Integer value : mValues) { sum += (value - avg) * (value - avg); }
        return sum / mValues.size();
    }

    /** Returns the sample standard deviation */
    public double getDev()
    {
        double var = getVar();
        return Math.sqrt(var);
    }
}
