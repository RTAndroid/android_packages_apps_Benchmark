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

package rtandroid.benchmark.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;

import rtandroid.benchmark.benchmarks.Benchmark;
import rtandroid.benchmark.benchmarks.BenchmarkManager;
import rtandroid.benchmark.data.TestCase;

public class BenchmarkService extends IntentService
{
    public static final String ACTION_START = "rtandroid.benchmark.ACTION_START";
    public static final String ACTION_UPDATE = "rtandroid.benchmark.ACTION_UPDATE";
    public static final String ACTION_FINISHED = "rtandroid.benchmark.ACTION_FINISHED";

    public static final String EXTRA_BENCHMARK = "benchmark";
    public static final String EXTRA_PARAMETER = "parameter";
    public static final String EXTRA_CYCLES = "cycles";
    public static final String EXTRA_SLEEP = "sleep";
    public static final String EXTRA_TEST_CASE = "test_case";

    public static final String EXTRA_TEST_CASE_ID = "test_case_id";
    public static final String EXTRA_TEST_CASE_NAME = "test_case_name";
    public static final String EXTRA_ITERATIONS = "iterations";
    public static final String EXTRA_FILENAME = "filename";

    private static final String TAG = BenchmarkService.class.getSimpleName();
    private static final int EXTRA_NOT_FOUND = -1;

    private volatile BenchmarkExecutor mExecutor;

    public BenchmarkService()
    {
        super(TAG);
        setIntentRedelivery(true);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        Log.d(TAG, "BenchmarkService created");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        // Signal stopping
        if(mExecutor != null)
        {
            mExecutor.cancel();
        }
        Log.d(TAG, "BenchmarkService destroyed");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        // Extract arguments
        int benchmarkIdx = intent.getIntExtra(EXTRA_BENCHMARK, EXTRA_NOT_FOUND);
        int parameter = intent.getIntExtra(EXTRA_PARAMETER, EXTRA_NOT_FOUND);
        int cycles = intent.getIntExtra(EXTRA_CYCLES, EXTRA_NOT_FOUND);
        int sleep = intent.getIntExtra(EXTRA_SLEEP, EXTRA_NOT_FOUND);

        if (benchmarkIdx == EXTRA_NOT_FOUND || parameter == EXTRA_NOT_FOUND || sleep == EXTRA_NOT_FOUND || cycles == EXTRA_NOT_FOUND)
        {
            throw new RuntimeException("Missing extras in Intent from Activity!");
        }

        Benchmark[] benchmarks = BenchmarkManager.getBenchmarks();
        if(benchmarkIdx >= benchmarks.length)
        {
            throw new RuntimeException("Invalid benchmark index in Intent from Activity!");
        }
        Benchmark benchmark = benchmarks[benchmarkIdx];

        Gson gson = new Gson();
        String jsonTestCase = intent.getStringExtra(EXTRA_TEST_CASE);
        TestCase testCase = gson.fromJson(jsonTestCase, TestCase.class);

        // Start actual work in separate thread
        try
        {
            mExecutor = new BenchmarkExecutor(getBaseContext(), benchmark, parameter, cycles, sleep, testCase);
            Thread thread = new Thread(mExecutor);
            thread.start();
            thread.join();
        }
        catch (InterruptedException e)
        {
            // Ignore it
        }
    }
}
