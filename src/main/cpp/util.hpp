#ifndef COCAINE_DEALER_JAVA_UTIL_H
#define COCAINE_DEALER_JAVA_UTIL_H

#include <jni.h>
#include <string>

namespace cocaine {
namespace dealer {
namespace java {

std::string to_string(JNIEnv* env, jstring str);

jstring from_string(JNIEnv* env, std::string str);

jint throw_exception(JNIEnv *env, std::string class_name, std::string message);

jint throw_timeout_exception(JNIEnv *env, std::string message);

jint throw_runtime_exception(JNIEnv *env, std::string message);

}
}
}
#endif
