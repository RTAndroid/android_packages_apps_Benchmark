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

import java.io.Serializable;
import java.util.Comparator;

import rtandroid.RealTimeWrapper;

/**
 * Represents a possible test case.
 */
public class TestCase
{
    public static final int NO_PRIORITY = -1;
    public static final int PRIORITY_MIN = RealTimeWrapper.THREAD_MIN_PRIORITY;
    public static final int PRIORITY_MAX = RealTimeWrapper.THREAD_MAX_PRIORITY;

    public static final int NO_POWER_LEVEL = -1;
    public static final int POWER_LEVEL_MIN = 1;
    public static final int POWER_LEVEL_MAX = 100;

    public static final int NO_CORE_LOCK = 0;
    public static final int CORE_LOCK_MIN = 1;

    private String mName;
    private int mPriority;
    private int mPowerLevel;
    private int mCpuCore;

    /**
     * Initialize test case with given values.
     */
    public TestCase(String name, int priority, int powerLevel, int cpuCore)
    {
        mName = name;

        // Try to set values
        setPriority(priority);
        setPowerLevel(powerLevel);
        setCpuCore(cpuCore);
    }

    public String getName()
    {
        return mName;
    }

    public void setName(String name)
    {
        mName = name;
    }

    public int getRealtimePriority()
    {
        return mPriority;
    }

    public void setPriority(int priority)
    {
        // Catch illegal values
        if (priority != NO_PRIORITY && (priority < PRIORITY_MIN || PRIORITY_MAX < priority))
        {
            throw new RuntimeException("Illegal realtime priority value");
        }

        mPriority = priority;
    }

    public int getPowerLevel()
    {
        return mPowerLevel;
    }

    public void setPowerLevel(int powerLevel)
    {
        // Catch illegal values
        if (powerLevel != NO_POWER_LEVEL && (powerLevel < POWER_LEVEL_MIN || POWER_LEVEL_MAX < powerLevel))
        {
            throw new RuntimeException("Illegal power level value");
        }

        mPowerLevel = powerLevel;
    }

    public int getCpuCore()
    {
        return mCpuCore;
    }

    public void setCpuCore(int cpuCore)
    {
        // Catch illegal values
        int coreCount = Runtime.getRuntime().availableProcessors();
        if (cpuCore != NO_CORE_LOCK && (cpuCore < CORE_LOCK_MIN || coreCount <= cpuCore))
        {
            throw new RuntimeException("Illegal cpu core value");
        }

        mCpuCore = cpuCore;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        TestCase testCase = (TestCase) o;

        if (mPriority != testCase.mPriority) { return false; }
        if (mPowerLevel != testCase.mPowerLevel) { return false; }
        if (!mName.equals(testCase.mName)) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = mName.hashCode();
        result = 31 * result + mPriority;
        result = 31 * result + mPowerLevel;
        result = 31 * result + mCpuCore;
        return result;
    }

    public static class TestCaseComparator implements Comparator<TestCase>, Serializable
    {
        @Override
        public int compare(TestCase testCase1, TestCase testCase2)
        {
            int value1 = testCase1.getSortingValue();
            int value2 = testCase2.getSortingValue();

            return value1 - value2;
        }
    }

    private int getSortingValue()
    {
        int value = getRealtimePriority() + getPowerLevel();

        if (getCpuCore() != NO_CORE_LOCK) { value += 100; }
        if (getName().contains("Warmup")) { value -= 500; }

        return value;
    }
}
