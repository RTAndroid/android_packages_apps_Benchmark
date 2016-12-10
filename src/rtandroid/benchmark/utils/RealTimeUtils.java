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

package rtandroid.benchmark.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rtandroid.benchmark.data.TestCase;
import rtandroid.cpu.CpuCore;
import rtandroid.cpu.CpuPackage;
import rtandroid.thread.ForeignRealtimeThread;
import rtandroid.thread.SchedulingPolicy;

public class RealTimeUtils
{
    private static final String TAG = RealTimeUtils.class.getSimpleName();
    private static final CpuPackage CPU_PACKAGE = CpuPackage.getCpuPackage();

    public static void setPriority(int priority)
    {
        // nothing to set
        if (priority == TestCase.NO_PRIORITY) { return; }

        try
        {
            int tid = android.os.Process.myTid();
            ForeignRealtimeThread realtimeThread = new ForeignRealtimeThread(tid);

            Log.d(TAG, "Setting the RT priority to " + priority);
            realtimeThread.setSchedulingPolicy(SchedulingPolicy.FIFO);
            realtimeThread.setSchedulingPriority(priority);
        }
        catch (Exception e) { Log.e(TAG, "Failed to find RT extensions: " + e.getMessage()); }
    }

    public static void setCpuCore(int cpuCoreID)
    {
        // nothing to set
        if (cpuCoreID == TestCase.NO_CORE_LOCK) { return; }

        // is this a valid CPU?
        List<CpuCore> cpuCores = CPU_PACKAGE.getCpuCores();
        if (cpuCoreID >= cpuCores.size())
        {
            Log.e(TAG, "Can't bind thread to CPU " + cpuCoreID + ": only " + cpuCores.size() + " CPUs found");
            return;
        }

        // get the core object with this ID
        CpuCore targetCore = null;
        for (CpuCore core : cpuCores)
          if (core.getID() == cpuCoreID) { targetCore = core; }

        // make sure it is valid
        if (targetCore == null)
        {
            Log.e(TAG, "Can't bind thread to CPU " + cpuCoreID + ": no such core found");
            return;
        }

        // warn if we are trying to isolate this process on a non-isolated CPU
        if (!targetCore.isIsolated()) { Log.w(TAG, "WARNING: trying to bind thread to a non-isolated CPU " + cpuCoreID); }

        try
        {
            if (!targetCore.isOnline())
            {
                Log.d(TAG, "CPU " + cpuCoreID + " seems to be offline, booting...");
                targetCore.wakeup();
            }

            int tid = android.os.Process.myTid();
            ForeignRealtimeThread realtimeThread = new ForeignRealtimeThread(tid);

            Log.d(TAG, "Setting the CPU core to " + cpuCoreID);
            List<CpuCore> affineCores = Collections.singletonList(targetCore);
            realtimeThread.setAffinity(affineCores);
        }
        catch (Exception e) { Log.e(TAG, "Failed to find RT extensions: " + e.getMessage()); }
    }

    public static void lockPowerLevel(int powerLevel)
    {
        // nothing to set
        if (powerLevel == TestCase.NO_POWER_LEVEL) { return; }

        try
        {
            Log.d(TAG, "Locking power level at " + powerLevel + "%");
            if (powerLevel == TestCase.POWER_LEVEL_MIN) { CPU_PACKAGE.lockPower(CpuPackage.PowerLevel.MIN); }
                                                   else { CPU_PACKAGE.lockPower(CpuPackage.PowerLevel.MAX); }
        }
        catch (Exception e) { Log.e(TAG, "Failed to find RT extensions: " + e.getMessage()); }
    }

    public static void unlockPowerLevel(int powerLevel)
    {
        // nothing to reset
        if (powerLevel == TestCase.NO_POWER_LEVEL) { return; }

        try
        {
            Log.d(TAG, "Unlocking power level from " + powerLevel + "%");
            CPU_PACKAGE.unlockPower();
        }
        catch (Exception e) { Log.e(TAG, "Failed to find RT extensions: " + e.getMessage()); }
    }

    public static Integer[] getIsolatedCpus()
    {
        List<CpuCore> cpuCores = CPU_PACKAGE.getCpuCores();
        List<Integer> isolatedCpus = new ArrayList<Integer>();
        for (CpuCore cpuCore : cpuCores)
         if (cpuCore.isIsolated()) { isolatedCpus.add(cpuCore.getID()); }

        return isolatedCpus.toArray(new Integer[0]);
    }
}
