/*
    Copyright (c) 2012 Vladimir Shakhov <bogdad@gmail.com>
    Copyright (c) 2012 Other contributors as noted in the AUTHORS file.

    This file is part of Cocaine.

    Cocaine is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    Cocaine is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

#include "util.hpp"
#include <cocaine/dealer/utils/error.hpp>
namespace cocaine {
namespace dealer {
namespace java {

jclass get_class_or_throw(JNIEnv * env, std::string name) {
    jclass message_class = env->FindClass(name.c_str());
    if (message_class == NULL) {
        throw internal_error(name + "class not found ");
    }
    return message_class;
}

jmethodID get_method_or_throw(JNIEnv * env, jclass clas, std::string name, std::string signature) {
    jmethodID m_method = env->GetMethodID(clas, name.c_str(), signature.c_str());
    if (m_method == NULL) {
        throw internal_error(name + " method not found ");
    }
    return m_method;
}


jbyteArray byte_array_from(JNIEnv* env, void* data, size_t size) {
    jbyteArray array = env->NewByteArray(size);
    env->SetByteArrayRegion(array, 0, size, (jbyte*)data);
    return array;
}

jstring from_string(JNIEnv* env, std::string str) {
    return env->NewStringUTF(str.c_str());
}

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

} // namespace java
} // namespace dealer
} // namespace cocaine
