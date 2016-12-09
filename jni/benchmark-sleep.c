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
 * limitations j    under the License.
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

#include "benchmark-sleep.h"

// ---------------------------------------------------

void lib_getTimeDiff(struct timespec* start, struct timespec* end, struct timespec* diff)
{
    if (end->tv_nsec > start->tv_nsec)
    {
		diff->tv_sec = end->tv_sec - start->tv_sec;
		diff->tv_nsec = end->tv_nsec - start->tv_nsec;
	}
	else
	{
		diff->tv_sec = end->tv_sec - start->tv_sec - 1;
		diff->tv_nsec = end->tv_nsec - start->tv_nsec + 1000000000;
    }
}

JNIEXPORT jlong JNICALL Java_rtandroid_benchmark_service_BenchmarkLib_libSleep(JNIEnv* env, jobject obj, jint ms)
{
    // get the current time stamp
    struct timespec start;
    clock_gettime(CLOCK_MONOTONIC, &start);

    // sleep the predefined time
    usleep(ms * 1000);

    // get another time stamp
    struct timespec stop, diff;
    clock_gettime(CLOCK_MONOTONIC, &stop);
    lib_getTimeDiff(&start, &stop, &diff);

    // and calculate the passed time in us
    jlong time = 0;
    time += diff.tv_sec * 1000000;
    time += diff.tv_nsec / 1000;

    // pass it back to the caller
    return time;
}
