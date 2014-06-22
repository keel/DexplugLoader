LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := dserv
LOCAL_LDLIBS    := -llog
LOCAL_SRC_FILES := base64.cpp\
                   aes_core.cpp\
                   dserv.cpp
include $(BUILD_SHARED_LIBRARY)
