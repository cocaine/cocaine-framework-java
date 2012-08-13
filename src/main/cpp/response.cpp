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

#include <cocaine/dealer/utils/data_container.hpp>
#include <cocaine/dealer/response.hpp>
#include <cocaine/dealer/utils/error.hpp>
#include <sstream>

#include "response_holder.hpp"
#include "cocaine_dealer_Response.h"
#include "util.hpp"
using namespace cocaine::dealer::java;
using namespace cocaine::dealer;

namespace {
    jint deal_with_error(JNIEnv *env, dealer_error& error); 
}

JNIEXPORT void JNICALL
Java_cocaine_dealer_Response_close (JNIEnv *, jobject, jlong c_response_ptr) {
    response_holder_t * response_ptr = (response_holder_t *)c_response_ptr;
    delete response_ptr;
}

JNIEXPORT jbyteArray JNICALL
Java_cocaine_dealer_Response_getAllChunks(
        JNIEnv *env, jobject self, jlong c_response_ptr, jdouble timeout) {
    response_holder_t *response_holder = (response_holder_t *) c_response_ptr;
    chunk_data container;
    try {
        std::vector<char> result;
        while (response_holder->get()->get(container, timeout)) {
            if (container.size() > 0) {
                    const char * beg = (char*) container.data();
                    const char * end = beg + container.size();
                    result.insert(result.end(), beg, end);
            }
        }
        jbyteArray j_array = env->NewByteArray(result.size());
        env->SetByteArrayRegion(j_array, 0, result.size(), (jbyte*) &result[0]);
        return j_array;
    } catch (dealer_error& error) {
        int throw_result = deal_with_error(env, error);
    }
}

JNIEXPORT jboolean JNICALL
Java_cocaine_dealer_Response_get
  (JNIEnv * env, jobject self, jobject array_holder, jlong c_response_ptr, jdouble timeout) {
    response_holder_t *response_holder = (response_holder_t *) c_response_ptr;
    chunk_data container;
    bool has_next = false;
    try {
        has_next = response_holder->get()->get(container, timeout);
    } catch (dealer_error& error) {
        int throw_result = deal_with_error(env, error);
        return false;
    }
    jbyteArray j_array = NULL;

    if (container.size() > 0) {
        j_array=env->NewByteArray(container.size());
        env->SetByteArrayRegion(j_array, 0, container.size(), (jbyte*) container.data());
        // could cache classes/fields retrieving
        jclass cls_array_holder = env->GetObjectClass(array_holder);
        jfieldID field_array = env->GetFieldID(cls_array_holder, "array",
                        "[B");
        if (field_array==NULL) {
            throw_runtime_exception(env, "could not find an array field of ArrayHolder");
            return false;
        }
        env->SetObjectField(array_holder, field_array, j_array);
    }
    return has_next;
}

namespace {

jint deal_with_error(JNIEnv *env, dealer_error& error){
    int res = 0;
    std::string error_msg(error.what());
    std::stringstream ss;
    ss<<error.code();
    ss<<" ";
    ss<<error_msg;
    switch (error.code()) {
    case app_error:
        res = throw_app_exception(env, ss.str());
        break;
    case deadline_error:
        res = throw_timeout_exception(env, ss.str());
        break;
    case timeout_error:
        res = throw_timeout_exception(env, ss.str());
        break;
    default:
        res = throw_runtime_exception(env, ss.str());
        break;
    }
    return res;
}

}
