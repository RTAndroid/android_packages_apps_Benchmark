#include <jni.h>

#ifndef BENCHMARK_H
#define BENCHMARK_H

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_rtandroid_benchmark_service_BenchmarkLib_libInit(JNIEnv* env, jobject obj, jstring filename);
JNIEXPORT void JNICALL Java_rtandroid_benchmark_service_BenchmarkLib_libWriteLong(JNIEnv* env, jobject obj, jlong val);
JNIEXPORT void JNICALL Java_rtandroid_benchmark_service_BenchmarkLib_libWriteTime(JNIEnv* env, jobject obj);
JNIEXPORT void JNICALL Java_rtandroid_benchmark_service_BenchmarkLib_libWriteCR(JNIEnv* env, jobject obj);

#ifdef __cplusplus
}
#endif

#endif // BENCHMARK_H
