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

package rtandroid.benchmark.service;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.Locale;

import rtandroid.benchmark.utils.RealTimeUtils;
import rtandroid.benchmark.benchmarks.Benchmark;
import rtandroid.benchmark.data.TestCase;

public class BenchmarkExecutor implements Runnable
{
    private static final String TAG = BenchmarkExecutor.class.getSimpleName();
    private static final String RESULT_FOLDER = "Benchmark";
    private static final String FILE_TEMPLATE =  RESULT_FOLDER.toLowerCase(Locale.getDefault()) + "_b=%s_p=%d_s=%d_c=%d_case=%s.csv";
    private static final long GUI_UPDATE_TIME = 500 * 1000 * 1000; // in ns

    private final Context mContext;
    private final Benchmark mBenchmark;
    private final int mParameter;
    private final int mCycles;
    private final int mSleep;
    private final TestCase mTestCase;
    private final BenchmarkLib mLib;
    private final String mFileName;

    private boolean mInterrupted = false;

    public BenchmarkExecutor(Context context, Benchmark benchmark, int parameter, int cycles, int sleep, TestCase testCase)
    {
        mContext = context;
        mBenchmark = benchmark;
        mParameter = parameter;
        mCycles = cycles;
        mSleep = sleep;
        mTestCase = testCase;

        // Assure the folder exists
        File resultFolder = new File(Environment.getExternalStorageDirectory(), RESULT_FOLDER);
        if (!resultFolder.exists())
        {
            boolean res = resultFolder.mkdirs();
            if (!res) { throw new RuntimeException("Cannot create the output directory"); }
        }

        if(!testCase.getName().startsWith("Warmup")) {
            // Generate the filename
            String benchmarkName = mBenchmark.getName().replaceAll("\\s", "").replace('/', '-');
            String caseName = mTestCase.getName().replaceAll("\\s", "").replace('/', '-');
            String fileName = String.format(Locale.US, FILE_TEMPLATE, benchmarkName, mParameter, mSleep, mCycles, caseName);
            mFileName = new File(resultFolder, fileName).getAbsolutePath();

            // Create the library
            mLib = new BenchmarkLib(mFileName);
        }
        else{
            // Create the library
            mLib = new BenchmarkLib();
            mFileName = null;
        }

        mInterrupted = false;
    }

    @Override
    public void run()
    {
        String msgStart = String.format(Locale.US, "Benchmark '%s' with case '%s' started", mBenchmark.getName(), mTestCase.getName());
        Log.d(TAG, msgStart);

        // Keep the CPU power level constant
        int powerLevel = mTestCase.getPowerLevel();
        RealTimeUtils.lockPowerLevel(powerLevel);

        // Set real-time priority value
        int priority = mTestCase.getRealtimePriority();
        RealTimeUtils.setPriority(priority);

        // Set the core affinity
        int cpuCore = mTestCase.getCpuCore();
        RealTimeUtils.setCpuCore(cpuCore);

        // Notify activity about start
        final Intent startIntent = new Intent(BenchmarkService.ACTION_START);
        startIntent.putExtra(BenchmarkService.EXTRA_TEST_CASE_NAME, mTestCase.getName());
        mContext.sendBroadcast(startIntent);

        // Allow a short warmup of the new thread
        for (int iteration = 0; iteration < 50; iteration++)
        {
            mLib.libSleep(mSleep);
            mBenchmark.execute(mParameter);
        }

        // Perform the actual benchmark
        long updateTimestamp = System.nanoTime();
        final Intent updateIntent = new Intent(BenchmarkService.ACTION_UPDATE);
        for (int iteration = 0; (iteration < mCycles) && !mInterrupted; iteration++)
        {
            // Sleep a bit
            long sleepTimeUs = mLib.libSleep(mSleep);

            // Do actual task
            long timestamp = System.nanoTime();
            mBenchmark.execute(mParameter);
            long calcTimeUs = System.nanoTime() - timestamp;

            if(mFileName != null) {
                // Write data to file
                mLib.libWriteLong(calcTimeUs);
                mLib.libWriteLong(sleepTimeUs);
                mLib.libWriteCR();
            }

            // Send progress to activity
            long time = System.nanoTime();
            if ((time - updateTimestamp) >= GUI_UPDATE_TIME)
            {
                updateTimestamp = time;
                updateIntent.putExtra(BenchmarkService.EXTRA_ITERATIONS, iteration);
                mContext.sendBroadcast(updateIntent);
            }
        }

        // Clean everything up
        RealTimeUtils.unlockPowerLevel(powerLevel);

        // Let the CPU cooldown
        try { Thread.sleep(500); }
        catch (Exception ignored) { }

        // Notify the GUI that the worker thread is done
        final Intent finishedIntent = new Intent(BenchmarkService.ACTION_FINISHED);
        finishedIntent.putExtra(BenchmarkService.EXTRA_TEST_CASE_NAME, mTestCase.getName());
        finishedIntent.putExtra(BenchmarkService.EXTRA_FILENAME, mFileName);
        mContext.sendBroadcast(finishedIntent);

        String msgFinish = String.format(Locale.US, "Benchmark '%s' with case '%s' terminated", mBenchmark.getName(), mTestCase.getName());
        Log.d(TAG, msgFinish);
    }

    public void cancel()
    {
        mInterrupted = true;
    }
}
