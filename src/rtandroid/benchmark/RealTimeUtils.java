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

import android.util.Log;

import rtandroid.RealTimeConstants;
import rtandroid.RealTimeProxy;
import rtandroid.benchmark.data.TestCase;

public class RealTimeUtils
{
    private static final String TAG = RealTimeUtils.class.getSimpleName();
    private static final RealTimeProxy PROXY = new RealTimeProxy();

    private static long getBuildVersion()
    {
        try
        {
            return PROXY.getVersion();
        }
        catch (Exception e)
        {
            Log.e(TAG, "Failed to find RT extensions: " + e.getMessage());
            return -1;
        }
    }

    public static void setPriority(int priority)
    {
        // Real-time extensions are not supported
        if (getBuildVersion() < 0) { return; }

        // Nothing to set
        if (priority == TestCase.NO_PRIORITY) { return; }

        try
        {
            Log.d(TAG, "Setting the RT priority to " + priority);
            PROXY.setSchedulingPolicy(RealTimeConstants.SCHED_POLICY_FIFO);
            PROXY.setPriority(priority);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Failed to find RT extensions: " + e.getMessage());
        }
    }

    public static void setCpuCore(int cpuCore)
    {
        // Real-time extensions are not supported
        if (getBuildVersion() < 0) { return; }

        // Nothing to set
        if (cpuCore == TestCase.NO_CORE_LOCK) { return; }

        try
        {
            // Make sure this core is online
            int state = PROXY.getCpuState(cpuCore);
            if (state == RealTimeConstants.CPU_STATE_OFFLINE)
            {
                Log.d(TAG, "Booting CPU " + cpuCore);
                PROXY.setCpuState(cpuCore, RealTimeConstants.CPU_STATE_ONLINE);
            }

            // And set thread's affinity
            int mask = (1 << cpuCore);
            Log.d(TAG, "Setting the thread affinity to " + mask);
            PROXY.setAffinity(mask);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Failed to find RT extensions: " + e.getMessage());
        }
    }

    public static void lockPowerLevel(int powerLevel)
    {
        // Real-time extensions are not supported
        if (getBuildVersion() < 0) { return; }

        // Nothing to set
        if (powerLevel == TestCase.NO_POWER_LEVEL) { return; }

        try
        {
            PROXY.lockCpuPower(powerLevel);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Failed to find RT extensions: " + e.getMessage());
        }
    }

    public static void unlockPowerLevel()
    {
        // Real-time extensions are not supported
        if (getBuildVersion() < 0) { return; }

        try
        {
            PROXY.unlockCpuPower();
        }
        catch (Exception e)
        {
            Log.e(TAG, "Failed to find RT extensions: " + e.getMessage());
        }
    }

    public static int getCpuCoreCount()
    {
        try
        {
            return PROXY.getConfiguredProcessors();
        }
        catch (Exception e)
        {
            Log.e(TAG, "Failed to find RT extensions: " + e.getMessage());
            return Runtime.getRuntime().availableProcessors();
        }
    }
}
