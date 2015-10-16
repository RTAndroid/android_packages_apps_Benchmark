# Copyright (C) 2015 RTAndroid Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_SDK_VERSION := current

LOCAL_JAVA_LIBRARIES := \
    framework

LOCAL_STATIC_JAVA_LIBRARIES := \
    android-common \
    android-support-v4 \
    android-support-v7-appcompat \
    android-support-v13 \
    prebuilt_gson

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
    prebuilt_gson:libs/gson-2.4.jar

LOCAL_PACKAGE_NAME := Benchmark
LOCAL_JNI_SHARED_LIBRARIES := libbenchmark
LOCAL_SRC_FILES := $(call all-subdir-java-files)

appcompat_dir := ../../../prebuilts/sdk/current/support/v7/appcompat/res
res_dirs := res $(appcompat_dir)
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs))
LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages android.support.v7.appcompat

include $(BUILD_PACKAGE)
include $(call all-makefiles-under,$(LOCAL_PATH))
