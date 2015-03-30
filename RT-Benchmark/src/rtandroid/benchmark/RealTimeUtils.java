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

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.util.Log;

import java.lang.reflect.Field;

import rtandroid.CpuLock;
import rtandroid.IRealTimeService;
import rtandroid.RealTimeWrapper;
import rtandroid.benchmark.data.TestCase;

public class RealTimeUtils
{
    private static final String TAG = RealTimeUtils.class.getSimpleName();

    private static int getBuildVersion()
    {
        try
        {
            Class cls = Class.forName("rtandroid.RealTimeWrapper");
            Field field = cls.getField("BUILD_VERSION");
            return (Integer) field.get(null);
        }
        catch (Exception ignored)
        {
            return -1;
        }
    }

    public static void setPriority(int priority)
    {
        // Real-time priorities are not supported
        if (getBuildVersion() < 0) { return; }

        // Nothing to set
        if (priority == TestCase.NO_PRIORITY) { return; }

        Log.d(TAG, "Setting the RT priority to " + priority);
        int tid = android.os.Process.myTid();
        try
        {
            IRealTimeService service = RealTimeWrapper.getService();
            service.setSchedulingPolicy(tid, RealTimeWrapper.SCHED_POLICY_FIFO);
            service.setPriority(tid, priority);
        }
        catch (RemoteException e) { throw new RuntimeException(e); }
    }

    public static Object acquireLock(Context context, int powerLevel, int cpuCore)
    {
        // Get a simple WakeLock on a non-rt system
        if (getBuildVersion() < 0)
        {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RT-Benchmark");
            wakeLock.acquire();
            return wakeLock;
        }

        // Acquire a fully functional cpu lock
        CpuLock cpuLock = new CpuLock(context);

        // Set the power level to a fixed value
        if (powerLevel != TestCase.NO_POWER_LEVEL)
        {
            Log.i(TAG, "Setting power lever to " + powerLevel);
            cpuLock.setPowerLevel(powerLevel);
        }

        // Set a cpu core to lock this process on
        if (cpuCore != TestCase.NO_CORE_LOCK)
        {
            Integer[] list = new Integer[] { cpuCore };
            int tid = android.os.Process.myTid();
            Log.i(TAG, "Setting cpu core of tid " + tid + " to " + cpuCore);
            cpuLock.setUsedCores(tid, list, true);
        }

        // This will prevent the cpu from sleep even w/o fixed power level
        cpuLock.acquire();
        return cpuLock;
    }

    public static void releaseLock(Object lock)
    {
        // Release a simple wake lock
        if (getBuildVersion() < 0)
        {
            WakeLock wakeLock = (WakeLock) lock;
            wakeLock.release();
            return;
        }

        // Release the cpu lock
        CpuLock cpuLock = (CpuLock) lock;
        cpuLock.release();
    }
}
