#include "util.hpp"

namespace cocaine { namespace dealer { namespace java {

std::string to_string(JNIEnv* env, jstring string) {
    std::string value;
    if (string == NULL) {
        return value; // empty string
    }
    const char* raw = env->GetStringUTFChars(string, NULL);
    if (raw != NULL) {
        jsize len = env->GetStringUTFLength(string);
        value.assign(raw, len);
        env->ReleaseStringUTFChars(string, raw);
    }

    return value;
}

jstring from_string(JNIEnv* env, std::string str) {
    return env->NewStringUTF(str.c_str());
}


jint throw_timeout_exception(JNIEnv *env, std::string message) {
    std::string class_name = "java/util/concurrent/TimeoutException";
    return throw_exception(env, class_name, message);
}

jint throw_app_exception(JNIEnv *env, std::string message) {
    std::string class_name = "cocaine/dealer/exceptions/AppException";
    return throw_exception(env, class_name, message);
}

jint throw_runtime_exception(JNIEnv *env, std::string message) {
    std::string class_name = "java/lang/RuntimeException";
    return throw_exception(env, class_name, message);
}

jint throw_exception(JNIEnv *env, std::string class_name, std::string message) {
    jclass ex_class = env->FindClass(class_name.c_str());
    if (ex_class == NULL) {
        std::string no_class_def = "java/lang/NoClassDefFoundError";
        return throw_exception(env, no_class_def, "");
    }
    return env->ThrowNew(ex_class, message.c_str());
}

}}}
