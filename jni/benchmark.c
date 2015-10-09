/**
 * All calculations ...
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
#define LOG(...) __android_log_print(ANDROID_LOG_DEBUG, "BenchmarkJNI", __VA_ARGS__)

#include "benchmark.h"

// ---------------------------------------------------

char* mLogFilename;
int mIsLogWritable = 0;

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
		LOG("Failed to create the log file!");
	}
	else
	{
		fprintf(file, "calculationTime;sleepTime\n");
		fclose(file);
		mIsLogWritable = 1;
		LOG("Log file was initialized!");
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
