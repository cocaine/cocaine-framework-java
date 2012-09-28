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

#ifndef COCAINE_DEALER_JAVA_UTIL_H
#define COCAINE_DEALER_JAVA_UTIL_H

#include <jni.h>
#include <string>

namespace cocaine {
namespace dealer {
namespace java {

jclass get_class_or_throw(JNIEnv * env, std::string name);

jmethodID get_method_or_throw(JNIEnv * env, jclass clas, std::string name, std::string signature);

jbyteArray byte_array_from(JNIEnv* env, void* data, size_t size);

jstring from_string(JNIEnv* env, std::string str);

std::string to_string(JNIEnv* env, jstring str);

jint throw_exception(JNIEnv *env, std::string class_name, std::string message);

jint throw_timeout_exception(JNIEnv *env, std::string message);

jint throw_app_exception(JNIEnv *env, std::string message);

jint throw_runtime_exception(JNIEnv *env, std::string message);

} // namespace java
} // namespace dealer
} // namespace cocaine
#endif
