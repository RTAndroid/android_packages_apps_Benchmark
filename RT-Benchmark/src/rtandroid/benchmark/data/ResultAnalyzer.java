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

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

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

    private Map<Kind, Integer> mResult = new HashMap<Kind, Integer>(Kind.values().length);

    public ResultAnalyzer(BenchmarkConfiguration config, String fileName)
    {
        mConfig = config;
        mFileName = fileName;
    }

    public void evaluate() throws IOException
    {
        SummaryStatistics calcStatistic = new SummaryStatistics();
        SummaryStatistics sleepStatistic = new SummaryStatistics();

        BufferedReader reader = new BufferedReader(new FileReader(mFileName));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(";", 3);

            try
            {
                int calcTimeUs = Integer.valueOf(parts[0]);
                int sleepTimeUs = Integer.valueOf(parts[1]) - mConfig.SleepMs * 1000;

                calcStatistic.addValue(calcTimeUs);
                sleepStatistic.addValue(sleepTimeUs);
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                // Ignore all garbage
            }
            catch (NumberFormatException e)
            {
                // Ignore all text lines
            }
        }

        mResult.put(Kind.CALCULATION_MINIMUM, (int) calcStatistic.getMin());
        mResult.put(Kind.CALCULATION_MEAN, (int) calcStatistic.getMean());
        mResult.put(Kind.CALCULATION_MAXIMUM, (int) calcStatistic.getMax());
        mResult.put(Kind.CALCULATION_DEVIATION, (int) calcStatistic.getStandardDeviation());
        mResult.put(Kind.SLEEP_MINIMUM, (int) sleepStatistic.getMin());
        mResult.put(Kind.SLEEP_MEAN, (int) sleepStatistic.getMean());
        mResult.put(Kind.SLEEP_MAXIMUM, (int) sleepStatistic.getMax());
        mResult.put(Kind.SLEEP_DEVIATION, (int) sleepStatistic.getStandardDeviation());

        reader.close();
    }

    /**
     * @return Get results in microseconds.
     */
    public Map<Kind, Integer> getResults()
    {
        if(mResult == null)
        {
            throw new IllegalStateException("No statistics are available before evaluation!");
        }

        return mResult;
    }
}
