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
