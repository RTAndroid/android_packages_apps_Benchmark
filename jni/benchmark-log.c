/**
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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <pthread.h>
#include <sched.h>
#include <time.h>
#include <unistd.h>
#include <sys/select.h>

#include <android/log.h>
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "BenchmarkJNI", __VA_ARGS__)

#include "benchmark-log.h"

// ---------------------------------------------------

char* mLogFilename;
int mIsLogWritable = 0;

// ---------------------------------------------------

JNIEXPORT void JNICALL Java_rtandroid_benchmark_service_BenchmarkLib_libInit(JNIEnv* env, jobject obj, jstring filename)
{
    // get char* to filename
    mLogFilename = (*env)->GetStringUTFChars(env, filename, 0);

    // check if the logfile can be created
	FILE* file = fopen(mLogFilename, "w");
	if (file == NULL)
	{
		mIsLogWritable = 0;
        LOGE("Failed to create the log file!");
	}
	else
	{
		fprintf(file, "calculationTime;sleepTime\n");
		fclose(file);
		mIsLogWritable = 1;
    }
}

JNIEXPORT void JNICALL Java_rtandroid_benchmark_service_BenchmarkLib_libWriteLong(JNIEnv* env, jobject obj, jlong val)
{
	// nothing to do, if we can't save our results :(
	if (mIsLogWritable == 0) { return; }

	// try to open the file and write the data
	FILE* file = fopen(mLogFilename, "a+");
	if (file == NULL) { mIsLogWritable = 0; }
	else
	{
		fprintf(file, "%ld;", (long int)val);
		fclose(file);
	}
}

JNIEXPORT void JNICALL Java_rtandroid_benchmark_service_BenchmarkLib_libWriteTime(JNIEnv* env, jobject obj)
{
    // nothing to do, if we can't save our results :(
    if (mIsLogWritable == 0) { return; }

    // get time stamp
    struct timespec now;
    clock_gettime(CLOCK_MONOTONIC, &now);

    // and calculate the passed time in us
    jlong time = 0;
    time += now.tv_sec * 1000000;
    time += now.tv_nsec / 1000;

    // use existing functions for writing
    Java_rtandroid_benchmark_service_BenchmarkLib_libWriteLong(env, obj, time);
}

JNIEXPORT void JNICALL Java_rtandroid_benchmark_service_BenchmarkLib_libWriteCR(JNIEnv* env, jobject obj)
{
    // nothing to do, if we can't save our results :(
    if (mIsLogWritable == 0) { return; }

    // try to open the file and break the line
    FILE* file = fopen(mLogFilename, "a+");
    if (file == NULL) { mIsLogWritable = 0; }
    else
    {
        fprintf(file, "\n");
        fclose(file);
    }
}
