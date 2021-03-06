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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import rtandroid.benchmark.data.BenchmarkResult.Kind;

/**
 * Evaluates data of a test case and provides statistical information.
 */
public class ResultAnalyzer
{
    private final BenchmarkConfiguration mConfig;
    private final String mFileName;

    private final Map<Kind, Integer> mResult = new HashMap<>(Kind.values().length);

    public ResultAnalyzer(BenchmarkConfiguration config, String fileName) throws IOException
    {
        mConfig = config;
        mFileName = fileName;

        evaluate();
    }

    private void evaluate() throws IOException
    {
        ResultStatistics calcValues = new ResultStatistics();
        ResultStatistics sleepValues = new ResultStatistics();

        FileReader fileReader = new FileReader(mFileName);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        String line;
        int sleepUs = mConfig.SleepMs * 1000;
        while ((line = bufferedReader.readLine()) != null)
        {
            String[] parts = line.split(";", 3);
            try
            {
                int calcTimeUs = Integer.parseInt(parts[0]);
                int sleepTimeUs = Integer.parseInt(parts[1]) - sleepUs;

                calcValues.add(calcTimeUs);
                sleepValues.add(sleepTimeUs);
            }
            catch (Exception ignored) { /* ignore all the garbage */ }
        }

        mResult.put(Kind.CALCULATION_MINIMUM,         calcValues.getMin());
        mResult.put(Kind.CALCULATION_MEAN,      (int) calcValues.getMean());
        mResult.put(Kind.CALCULATION_MAXIMUM,         calcValues.getMax());
        mResult.put(Kind.CALCULATION_DEVIATION, (int) calcValues.getDev());

        mResult.put(Kind.SLEEP_MINIMUM,         sleepValues.getMin());
        mResult.put(Kind.SLEEP_MEAN,      (int) sleepValues.getMean());
        mResult.put(Kind.SLEEP_MAXIMUM,         sleepValues.getMax());
        mResult.put(Kind.SLEEP_DEVIATION, (int) sleepValues.getDev());

        bufferedReader.close();
        fileReader.close();
    }

    /**
     * @return Get results in microseconds.
     */
    public Map<Kind, Integer> getResults()
    {
        if (mResult.isEmpty())
        {
            throw new IllegalStateException("No statistics are available before evaluation!");
        }

        return mResult;
    }
}
