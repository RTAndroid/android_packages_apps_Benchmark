#include <jni.h>

#ifndef TESTSUITE_H
#define TESTSUITE_H

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_rtandroid_benchmark_service_NativeLib_libInit(JNIEnv* env, jobject obj, jstring filename);
JNIEXPORT void JNICALL Java_rtandroid_benchmark_service_NativeLib_libWriteLong(JNIEnv* env, jobject obj, jlong val);
JNIEXPORT void JNICALL Java_rtandroid_benchmark_service_NativeLib_libWriteTime(JNIEnv* env, jobject obj);
JNIEXPORT void JNICALL Java_rtandroid_benchmark_service_NativeLib_libWriteCR(JNIEnv* env, jobject obj);
JNIEXPORT void JNICALL Java_rtandroid_benchmark_service_NativeLib_libWriteSemicolon(JNIEnv* env, jobject obj);
JNIEXPORT jlong JNICALL Java_rtandroid_benchmark_service_NativeLib_libSleep(JNIEnv* env, jobject obj, jint us);

#ifdef __cplusplus
}
#endif

#endif // TESTSUITE_H
