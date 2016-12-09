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

package rtandroid.benchmark.data;

import android.os.Parcel;
import android.os.Parcelable;

import rtandroid.benchmark.benchmarks.Benchmark;
import rtandroid.benchmark.benchmarks.BenchmarkManager;

/**
 * Data class containing all values of benchmark.
 */
public class BenchmarkConfiguration implements Parcelable
{
    public int BenchmarkIdx;
    public int Parameter;
    public int Cycles;
    public int SleepMs;

    public BenchmarkConfiguration()
    {
        // Only for public constructor
    }

    public Benchmark getBenchmark()
    {
        Benchmark[] benchmarks = BenchmarkManager.getBenchmarks();
        if (BenchmarkIdx >= benchmarks.length) { return null; }

        return benchmarks[BenchmarkIdx];
    }

    //
    // Parcelable
    //

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(BenchmarkIdx);
        dest.writeInt(Parameter);
        dest.writeInt(Cycles);
        dest.writeInt(SleepMs);
    }

    public static final Parcelable.Creator<BenchmarkConfiguration> CREATOR = new Parcelable.Creator<BenchmarkConfiguration>()
    {
        public BenchmarkConfiguration createFromParcel(Parcel in)
        {
            return new BenchmarkConfiguration(in);
        }

        public BenchmarkConfiguration[] newArray(int size)
        {
            return new BenchmarkConfiguration[size];
        }
    };

    private BenchmarkConfiguration(Parcel in)
    {
        BenchmarkIdx = in.readInt();
        Parameter = in.readInt();
        Cycles = in.readInt();
        SleepMs =  in.readInt();
    }
}
