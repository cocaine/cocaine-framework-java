#include <cocaine/dealer/utils/data_container.hpp>
#include <cocaine/dealer/response.hpp>
#include <cocaine/dealer/utils/error.hpp>
#include <sstream>

#include "response_holder.hpp"
#include "cocaine_dealer_Response.h"
#include "util.hpp"
using namespace cocaine::dealer::java;
using namespace cocaine::dealer;

JNIEXPORT void JNICALL Java_cocaine_dealer_Response_close
(JNIEnv *, jobject, jlong c_response_ptr) {
    response_holder_t * response_ptr = (response_holder_t *)c_response_ptr;
    delete response_ptr;
}

jint deal_with_error(JNIEnv *env, dealer_error& error);

JNIEXPORT jstring JNICALL Java_cocaine_dealer_Response_getString(
        JNIEnv *env, jobject self, jlong c_response_ptr, jdouble timeout)
{
    response_holder_t *response_holder = (response_holder_t *) c_response_ptr;
    data_container container;
    try {
        std::string total;
        while (response_holder->get()->get(&container, timeout)) {
            if (!container.empty()) {
                    std::string response_str((char*) container.data(), container.size());
                    total = total + response_str;
            }
        }
        jstring head = from_string(env, total);
        return head;
    } catch (dealer_error& error) {
        int throw_result = deal_with_error(env, error);
    }
}

JNIEXPORT jboolean JNICALL Java_cocaine_dealer_Response_get
  (JNIEnv * env, jobject self, jobject array_holder, jlong c_response_ptr, jdouble timeout)
{
    response_holder_t *response_holder = (response_holder_t *) c_response_ptr;
    data_container container;
    bool has_next = false;
    try {
        has_next = response_holder->get()->get(&container, timeout);
    } catch (dealer_error& error) {
        int throw_result = deal_with_error(env, error);
        return false;
    }
    jbyteArray j_array = NULL;

    if (!container.empty()) {
        j_array=env->NewByteArray(container.size());
        env->SetByteArrayRegion(j_array, 0, container.size(), (jbyte*) container.data());
        // could cache classes/fields retrieving
        jclass cls_array_holder = env->GetObjectClass(array_holder);
        jfieldID field_array = env->GetFieldID(cls_array_holder, "array",
                        "Ljava/lang/byte[];");
        if (field_array==NULL) {
            throw_runtime_exception(env, "couldnot find an array field of ArrayHolder");
            return false;
        }
        env->SetObjectField(array_holder, field_array, j_array);
    }
    return has_next;
}


jint deal_with_error(JNIEnv *env, dealer_error& error) {
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
