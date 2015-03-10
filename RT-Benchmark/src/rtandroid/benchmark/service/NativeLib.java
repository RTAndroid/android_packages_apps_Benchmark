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

package rtandroid.benchmark.service;

public class NativeLib
{
    static
    {
        System.loadLibrary("testsuite");
    }

    public NativeLib(String filename)
    {
        libInit(filename);
    }

    private native void libInit(String filename);

    /**
     * Writes a long value to the log file.
     */
    public native void libWriteLong(long value);

    /**
     * Writes the current timestamp to the log file.
     */
    public native void libWriteTime();

    /**
     * Writes the "carriage return" (\n) to the log file.
     */
    public native void libWriteCR();

    /**
     * Sleeps the given number of milliseconds.
     */
    public native long libSleep(int ms);
}
