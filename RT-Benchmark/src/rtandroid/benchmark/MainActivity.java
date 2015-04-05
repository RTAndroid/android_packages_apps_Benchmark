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

package rtandroid.benchmark;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.widget.SpinnerAdapter;
import android.widget.TabHost;

import com.google.gson.Gson;

import junit.framework.Test;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import rtandroid.benchmark.data.BenchmarkConfiguration;
import rtandroid.benchmark.data.BenchmarkResult;
import rtandroid.benchmark.data.BenchmarkResultAdapter;
import rtandroid.benchmark.data.ResultAnalyzer;
import rtandroid.benchmark.data.TestCase;
import rtandroid.benchmark.ui.BenchmarkFragment;
import rtandroid.benchmark.ui.ResultFragment;

public class MainActivity extends ActionBarActivity implements BenchmarkFragment.OnFragmentInteractionListener,
                                                               ResultFragment.OnFragmentInteractionListener
{
    private static final String KEY_TEST_CASES = "test_cases";
    private static final String KEY_RESULTS = "results";

    private static final TestCase[] DEFAULT_TEST_CASES;
    static
    {
        DEFAULT_TEST_CASES = new TestCase[]
        {
            new TestCase("Standard Android (Non-RT)", TestCase.NO_PRIORITY, TestCase.NO_POWER_LEVEL, TestCase.NO_CORE_LOCK),
            new TestCase("Basic Real-Time Support", 40, 70, TestCase.NO_CORE_LOCK),
            new TestCase("Advanced Real-Time Support", 90, 95, TestCase.CORE_LOCK_MIN),
        };
    }

    private FragmentTabHost mTabHost;
    private ViewPager mViewPager;

    private BenchmarkConfiguration mBenchmarkConfig;
    private BenchmarkResult mCurrentResult;
    private List<BenchmarkResult> mResults;
    private BenchmarkResultAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabNavigator navigator = new TabNavigator(getSupportFragmentManager());

        // Create view pager
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(navigator);
        mViewPager.setOnPageChangeListener(navigator);

        // Create tab navigation with empty fragments
        mTabHost = (FragmentTabHost) findViewById(R.id.tabHost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.tabDummyContent);
        mTabHost.setOnTabChangedListener(navigator);

        String[] tabHeader = getResources().getStringArray(R.array.app_tabs);
        for (int i = 0; i < tabHeader.length; i++)
        {
            String id = Integer.toString(i);
            String title = tabHeader[i];
            mTabHost.addTab(mTabHost.newTabSpec(id).setIndicator(title), Fragment.class, null);
        }
    }

    @Override
    public void onBenchmarkStart(BenchmarkConfiguration config)
    {
        mBenchmarkConfig = config;

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        String name = mBenchmarkConfig.getBenchmark().getName();
        String resultName = String.format("%s (%s)", dateFormat.format(new Date()), name);
        mCurrentResult = new BenchmarkResult(resultName);
    }

    @Override
    public void onTestCaseCompleted(TestCase testCase, String fileName)
    {
        try
        {
            ResultAnalyzer result = new ResultAnalyzer(mBenchmarkConfig, fileName);
            result.evaluate();
            mCurrentResult.addResult(testCase.getName(), result.getResults());
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to read log file of test case", e);
        }
    }

    @Override
    public void onBenchmarkFinished()
    {
        // Add result to list
        mResults.add(mCurrentResult);
        mAdapter.notifyDataSetChanged();

        // Save results
        BenchmarkResult[] results = new BenchmarkResult[mResults.size()];
        mResults.toArray(results);

        Gson gson = new Gson();
        String jsonResults = gson.toJson(results, BenchmarkResult[].class);
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .putString(KEY_RESULTS, jsonResults)
            .apply();

        mViewPager.setCurrentItem(1, true);
    }

    @Override
    public List<TestCase> loadTestCases()
    {
        TestCase[] cases = DEFAULT_TEST_CASES;

        // Try to load from settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.contains(KEY_TEST_CASES))
        {
            Gson gson = new Gson();
            String jsonTestCases = prefs.getString(KEY_TEST_CASES, "");
            cases = gson.fromJson(jsonTestCases, TestCase[].class);
        }

        return Arrays.asList(cases);
    }

    @Override
    public void saveTestCases(List<TestCase> testCases)
    {
        // Convert to array
        TestCase[] cases = new TestCase[testCases.size()];
        testCases.toArray(cases);

        // Serialize it
        Gson gson = new Gson();
        String jsonTestCases = gson.toJson(cases, TestCase[].class);

        // Save to settings
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .putString(KEY_TEST_CASES, jsonTestCases)
            .apply();
    }

    @Override
    public SpinnerAdapter getResultAdapter()
    {
        // Try to load from settings
        String defaultResults = getResources().getString(R.string.default_results);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String jsonTestCases = prefs.getString(KEY_RESULTS, defaultResults);

        Gson gson = new Gson();
        BenchmarkResult[] results = gson.fromJson(jsonTestCases, BenchmarkResult[].class);
        mResults = new ArrayList<BenchmarkResult>();
        mResults.addAll(Arrays.asList(results));

        mAdapter = new BenchmarkResultAdapter(this, mResults);
        return mAdapter;
    }

    /**
     * Takes care about the fragment switching.
     */
    private class TabNavigator extends FragmentPagerAdapter implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener
    {
        private final Fragment[] mFragments;

        public TabNavigator(FragmentManager manager)
        {
            super(manager);

            // Prepare all fragments
            mFragments = new Fragment[] { new BenchmarkFragment(), new ResultFragment() };
        }

        @Override
        public Fragment getItem(int i)
        {
            return mFragments[i];
        }

        @Override
        public int getCount()
        {
            return mFragments.length;
        }

        @Override
        public void onTabChanged(String s)
        {
            // Synchronize view pager with tabs
            mViewPager.setCurrentItem(Integer.parseInt(s), true);
        }

        @Override
        public void onPageSelected(int position)
        {
            // Synchronize tabs with view pager
            mTabHost.setCurrentTab(position);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

        @Override
        public void onPageScrollStateChanged(int state) { }
    }
}