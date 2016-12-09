/*
 * Copyright (C) 2016 RTAndroid Project
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

import rtandroid.realtime.RealTimeProxy;
import rtandroid.benchmark.data.TestCase;

public class RealTimeUtilsOld
{
    private static final String TAG = RealTimeUtilsOld.class.getSimpleName();
    private static final RealTimeProxy PROXY = new RealTimeProxy();

    public static void setPriority(int priority)
    {
        // Nothing to set
        if (priority == TestCase.NO_PRIORITY) { return; }

        try
        {
            Log.d(TAG, "Setting the RT priority to " + priority);
            PROXY.setSchedulingPolicy(rtandroid.Constants.SCHED_POLICY_FIFO);
            PROXY.setPriority(priority);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Failed to find RT extensions: " + e.getMessage());
        }
    }

    public static void setCpuCore(int cpuCore)
    {
        // Nothing to set
        if (cpuCore == TestCase.NO_CORE_LOCK) { return; }

        try
        {
            // Is this a valid CPU?
            int cpuCount = PROXY.getConfiguredProcessors();
            if (cpuCore >= cpuCount)
            {
                Log.e(TAG, "Can't set thread affinity for CPU " + cpuCore + ": only " + cpuCount + " CPUs found");
                return;
            }

            // Is this an isolated CPU?
            int[] isolatedCPUs = PROXY.getIsolatedProcessors();
            boolean isolated = false;
            for (int cpu : isolatedCPUs)
             if (cpu == cpuCore) { isolated = true; }

            // Warn if we are trying to isolate this process on a non-isolated CPU
            if (!isolated) { Log.w(TAG, "WARNING: trying to isolate a process on a non-isolated CPU " + cpuCore); }

            // Make sure this core is online
            int state = PROXY.getCpuState(cpuCore);
            if (state == rtandroid.Constants.CPU_STATE_OFFLINE)
            {
                Log.d(TAG, "Booting CPU " + cpuCore);
                PROXY.setCpuState(cpuCore, rtandroid.Constants.CPU_STATE_ONLINE);
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
        // Nothing to set
        if (powerLevel == TestCase.NO_POWER_LEVEL) { return; }

        try
        {
            Log.d(TAG, "Locking power level at " + powerLevel + "%");
            PROXY.lockCpuPower(powerLevel);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Failed to find RT extensions: " + e.getMessage());
        }
    }

    public static void unlockPowerLevel(int powerLevel)
    {
        // Nothing to reset
        if (powerLevel == TestCase.NO_POWER_LEVEL) { return; }

        try
        {
            Log.d(TAG, "Unlocking power level from " + powerLevel + "%");
            PROXY.unlockCpuPower();
        }
        catch (Exception e)
        {
            Log.e(TAG, "Failed to find RT extensions: " + e.getMessage());
        }
    }
}