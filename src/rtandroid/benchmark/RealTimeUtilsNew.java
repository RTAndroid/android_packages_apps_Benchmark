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

import java.util.ArrayList;
import java.util.List;

import rtandroid.BuildInfo;
import rtandroid.benchmark.data.TestCase;
import rtandroid.cpu.CpuCore;
import rtandroid.cpu.CpuPackage;
import rtandroid.thread.ForeignRealtimeThread;
import rtandroid.thread.SchedulingPolicy;

public class RealTimeUtilsNew
{
    private static final String TAG = RealTimeUtilsNew.class.getSimpleName();
    private static final CpuPackage CPU_PACKAGE = CpuPackage.getCpuPackage();

    private static long getBuildVersion()
    {
        try { return BuildInfo.getVersion(); }
        catch (Exception e)
        {
            Log.e(TAG, "No RT extensions found: " + e.getMessage());
            return -1;
        }
    }

    public static void setPriority(int priority)
    {
        // Real-time extensions are not supported
        if (getBuildVersion() < 0)
        {
            Log.e(TAG, "RT extension not found, RT priority be skipped");
            return;
        }

        int tid = android.os.Process.myTid();
        ForeignRealtimeThread realtimeThread = new ForeignRealtimeThread(tid);

        // Nothing to set
        if (priority == TestCase.NO_PRIORITY) { return; }

        try
        {
            Log.d(TAG, "Setting the RT priority to " + priority);
            realtimeThread.setSchedulingPriority(priority);
            realtimeThread.setSchedulingPolicy(SchedulingPolicy.FIFO);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Failed to find RT extensions: " + e.getMessage());
        }
    }

    public static void setCpuCore(int cpuCore)
    {
        // Real-time extensions are not supported
        if (getBuildVersion() < 0)
        {
            Log.e(TAG, "RT extension not found, CPU lock will be skipped");
            return;
        }

        // Nothing to set
        if (cpuCore == TestCase.NO_CORE_LOCK) { return; }
        List<CpuCore> cpuCores =CpuPackage.getCpuPackage().getCpuCores();
        CpuCore targetCore = null;
        for (CpuCore core : cpuCores)
        {
            if (core.getID() == cpuCore) { targetCore = core; }
        }

        int tid = android.os.Process.myTid();
        ForeignRealtimeThread realtimeThread = new ForeignRealtimeThread(tid);

        try
        {
            // Is this a valid CPU?
            int cpuCount = CPU_PACKAGE.getCpuCores().size();
            if (cpuCore >= cpuCount)
            {
                Log.e(TAG, "Can't set thread affinity for CPU " + cpuCore + ": only " + cpuCount + " CPUs found");
                return;
            }

            // Is this an isolated CPU?
            List<Integer> isolatedCpus = new ArrayList<>();
            boolean isolated = false;
            for (CpuCore currentCpuCore : cpuCores)
            {
                if(currentCpuCore.isIsolated() && currentCpuCore.getID() == cpuCore) { isolated = true; }
            }

            // Warn if we are trying to isolate this process on a non-isolated CPU
            if (!isolated) { Log.w(TAG, "WARNING: trying to isolate a process on a non-isolated CPU " + cpuCore); }

            // Make sure this core is online
            if (targetCore.isOnline())
            {
                Log.d(TAG, "Booting CPU " + cpuCore);
                targetCore.wakeup();
            }

            // And set thread's affinity
            int mask = (1 << cpuCore);
            Log.d(TAG, "Setting the thread affinity to " + mask);
            ArrayList<CpuCore> affineCores = new ArrayList<CpuCore>();
            affineCores.add(targetCore);
            realtimeThread.setAffinity(affineCores);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Failed to find RT extensions: " + e.getMessage());
        }
    }

    public static void lockPowerLevel(int powerLevel)
    {
        // Real-time extensions are not supported
        if (getBuildVersion() < 0)
        {
            Log.e(TAG, "RT extension not found, power lock will be skipped");
            return;
        }

        // Nothing to set
        if (powerLevel == TestCase.NO_POWER_LEVEL) { return; }

        try
        {
            Log.d(TAG, "Locking power level at " + powerLevel + "%");
            if(powerLevel == TestCase.POWER_LEVEL_MIN) { CPU_PACKAGE.lockPower(CpuPackage.PowerLevel.MIN); }
            else { CPU_PACKAGE.lockPower(CpuPackage.PowerLevel.MAX); }
        }
        catch (Exception e)
        {
            Log.e(TAG, "Failed to find RT extensions: " + e.getMessage());
        }
    }

    public static void unlockPowerLevel(int powerLevel)
    {
        // Real-time extensions are not supported
        if (getBuildVersion() < 0)
        {
            Log.e(TAG, "RT extension not found, power unlock will be skipped");
            return;
        }

        // Nothing to reset
        if (powerLevel == TestCase.NO_POWER_LEVEL) { return; }

        try
        {
            Log.d(TAG, "Unlocking power level from " + powerLevel + "%");
            CPU_PACKAGE.unlockPower();
        }
        catch (Exception e)
        {
            Log.e(TAG, "Failed to find RT extensions: " + e.getMessage());
        }
    }

    public static int[] getIsolatedCpus()
    {
        // Real-time extensions are not supported
        if (getBuildVersion() < 0)
        {
            Log.e(TAG, "RT extension not found, power unlock will be skipped");
            return new int[0];
        }

        List<CpuCore> cpuCores =CpuPackage.getCpuPackage().getCpuCores();
        List<Integer> isolatedCpus = new ArrayList<>();
        for (CpuCore cpuCore : cpuCores)
        {
            if(!cpuCore.isIsolated()) { isolatedCpus.add(cpuCore.getID()); }
        }

        int[] isolatedCpuArray = new int[isolatedCpus.size()];
        int i = 0;
        for (Integer isolatedCpu : isolatedCpus) {
            isolatedCpuArray[++i] = isolatedCpu;
        }
        return isolatedCpuArray;
    }

}
