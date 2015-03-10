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

/**
 * Represents a possible test case.
 */
public class TestCase implements Comparable
{
    public static final int NO_REALTIME_PRIORITY = -1;
    public static final int REALTIME_PRIORITY_MIN = 0;
    public static final int REALTIME_PRIORITY_MAX = 99;

    public static final int NO_POWER_LEVEL = -1;
    public static final int POWER_LEVEL_MIN = 1;
    public static final int POWER_LEVEL_MAX = 100;

    private int mId;
    private String mName;
    private int mRealtimePriority;
    private int mPowerLevel;

    /**
     * Initialize test case as normal android system.
     */
    public TestCase(int id, String name)
    {
        this(id, name, NO_REALTIME_PRIORITY, NO_POWER_LEVEL);
    }

    /**
     * Initialize test case with given values.
     */
    public TestCase(int id, String name, int realtimePriority, int powerLevel)
    {
        mId = id;
        mName = name;
        // Try to set values
        setRealtimePriority(realtimePriority);
        setPowerLevel(powerLevel);
    }

    public int getId()
    {
        return mId;
    }

    public void setId(int id)
    {
        this.mId = id;
    }

    public String getName()
    {
        return mName;
    }

    public void setName(String name)
    {
        this.mName = name;
    }

    public int getRealtimePriority()
    {
        return mRealtimePriority;
    }

    public void setRealtimePriority(int realtimePriority)
    {
        // Catch illegal values
        if(realtimePriority != NO_REALTIME_PRIORITY &&
                (realtimePriority < REALTIME_PRIORITY_MIN && REALTIME_PRIORITY_MAX < realtimePriority))
        {
            throw new RuntimeException("Illegal realtime priority value");
        }
        this.mRealtimePriority = realtimePriority;
    }

    public int getPowerLevel()
    {
        return mPowerLevel;
    }

    public void setPowerLevel(int powerLevel)
    {
        // Catch illegal values
        if(powerLevel != NO_POWER_LEVEL && (powerLevel < POWER_LEVEL_MIN || POWER_LEVEL_MAX < powerLevel))
        {
            throw new RuntimeException("Illegal power level value");
        }
        this.mPowerLevel = powerLevel;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestCase testCase = (TestCase) o;

        if (mId != testCase.mId) return false;
        if (mPowerLevel != testCase.mPowerLevel) return false;
        if (mRealtimePriority != testCase.mRealtimePriority) return false;
        if (!mName.equals(testCase.mName)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = mId;
        result = 31 * result + mName.hashCode();
        result = 31 * result + mRealtimePriority;
        result = 31 * result + mPowerLevel;
        return result;
    }

    @Override
    public int compareTo(Object o)
    {
        if(this.equals(o))
        {
            return 0;
        }

        if(o instanceof TestCase)
        {
            TestCase other = (TestCase)o;
            return this.getName().compareTo(other.getName());
        }

        throw new ClassCastException();
    }
}
