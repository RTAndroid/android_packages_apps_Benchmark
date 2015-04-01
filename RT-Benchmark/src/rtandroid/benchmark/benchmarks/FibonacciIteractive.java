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

package rtandroid.benchmark.benchmarks;

public class FibonacciIteractive implements Benchmark
{
    private static final String NAME = "Fibonacci-Interactive";

    @Override
    public void execute(int parameter)
    {
        int prev1 = 0, prev2 = 1;

        for (int i = 0; i < parameter; i++)
        {
            int savePrev1 = prev1;
            prev1 = prev2;
            prev2 = savePrev1 + prev2;
        }
    }

    @Override
    public String getName()
    {
        return NAME;
    }
}
