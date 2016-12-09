#include <jni.h>

#ifndef BENCHMARK_H
#define BENCHMARK_H

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_rtandroid_benchmark_service_BenchmarkLib_libSleep(JNIEnv* env, jobject obj, jint us);

#ifdef __cplusplus
}
#endif

#endif // BENCHMARK_H
