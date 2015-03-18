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
import android.widget.TabHost;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import rtandroid.benchmark.data.BenchmarkConfiguration;
import rtandroid.benchmark.data.TestCase;
import rtandroid.benchmark.data.TestCaseResult;
import rtandroid.benchmark.ui.BenchmarkFragment;
import rtandroid.benchmark.ui.ResultFragment;
import rtandroid.benchmark.ui.views.StatisticView;

public class MainActivity extends ActionBarActivity implements BenchmarkFragment.OnFragmentInteractionListener
{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String KEY_TEST_CASES = "test_cases";

    private static final TestCase[] DEFAULT_TEST_CASES;
    private static final Map<Integer, TestCaseResult.Kind> RESULT_VIEW_MAP;

    static
    {
        TestCase[] cases =
        {
            new TestCase(0, "Standard Android (Non-RT)"),
            new TestCase(1, "Partial Real-Time Support", 60, TestCase.NO_POWER_LEVEL),
            new TestCase(2, "Full Real-Time Support", 90, 70),
        };
        DEFAULT_TEST_CASES = cases;

        Map<Integer, TestCaseResult.Kind> viewMap = new HashMap<Integer, TestCaseResult.Kind>();
        viewMap.put(R.id.calc_minimum, TestCaseResult.Kind.CALCULATION_MINIMUM);
        viewMap.put(R.id.calc_mean, TestCaseResult.Kind.CALCULATION_MEAN);
        viewMap.put(R.id.calc_maximum, TestCaseResult.Kind.CALCULATION_MAXIMUM);
        viewMap.put(R.id.calc_deviation, TestCaseResult.Kind.CALCULATION_DEVIATION);
        viewMap.put(R.id.sleep_minimum, TestCaseResult.Kind.SLEEP_MINIMUM);
        viewMap.put(R.id.sleep_mean, TestCaseResult.Kind.SLEEP_MEAN);
        viewMap.put(R.id.sleep_maximum, TestCaseResult.Kind.SLEEP_MAXIMUM);
        viewMap.put(R.id.sleep_deviation, TestCaseResult.Kind.SLEEP_DEVIATION);

        RESULT_VIEW_MAP = Collections.unmodifiableMap(viewMap);
    }

    private FragmentTabHost mTabHost;
    private ViewPager mViewPager;

    private final List<TestCaseResult> mResults = new ArrayList<TestCaseResult>();
    private BenchmarkConfiguration mBenchmarkConfig;

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
        mResults.clear();
        mBenchmarkConfig = config;
    }

    @Override
    public void onTestCaseCompleted(TestCase testCase, String fileName)
    {
        TestCaseResult result = new TestCaseResult(mBenchmarkConfig, testCase, fileName);
        mResults.add(result);
    }

    @Override
    public void onBenchmarkFinished()
    {
        try
        {
            for(TestCaseResult result : mResults) { result.evaluate(); }

            for(Map.Entry<Integer, TestCaseResult.Kind> entry : RESULT_VIEW_MAP.entrySet())
            {
                StatisticView v = (StatisticView) findViewById(entry.getKey());
                v.setResult(getResultMap(entry.getValue()));
            }

            mViewPager.setCurrentItem(1, true);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to read log file of test case", e);
        }
    }

    private Map<String, Integer> getResultMap(TestCaseResult.Kind kind)
    {
        Map<String, Integer> results = new TreeMap<String, Integer>();

        for(TestCaseResult result : mResults)
        {
            String name = result.getTestCase().getName();
            int value = result.getResults().get(kind);
            results.put(name, value);
        }

        return results;
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
            .putString(KEY_TEST_CASES, jsonTestCases)
            .commit();
    }

    /**
     * Takes care about the fragment switching.
     */
    class TabNavigator extends FragmentPagerAdapter implements TabHost.OnTabChangeListener,
                                                                ViewPager.OnPageChangeListener
    {
        private Fragment[] mFragments;

        public TabNavigator(FragmentManager manager) {
            super(manager);

            // Prepare all fragments
            mFragments = new Fragment[]
            {
                BenchmarkFragment.newInstance(),
                ResultFragment.newInstance()
            };
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