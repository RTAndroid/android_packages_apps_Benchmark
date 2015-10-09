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

import rtandroid.benchmark.benchmarks.Benchmark;
import rtandroid.benchmark.benchmarks.BenchmarkManager;

/**
 * Data class containing all values of benchmark.
 */
public class BenchmarkConfiguration
{
    public int BenchmarkIdx;
    public int Parameter;
    public int Cycles;
    public int SleepMs;

    public Benchmark getBenchmark()
    {
        Benchmark[] benchmarks = BenchmarkManager.getBenchmarks();
        if (BenchmarkIdx >= benchmarks.length) { return null; }

        return benchmarks[BenchmarkIdx];
    }
}
