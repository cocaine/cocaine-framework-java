#include <cocaine/dealer/utils/data_container.hpp>
#include <cocaine/dealer/response.hpp>
#include <cocaine/dealer/utils/error.hpp>
#include <sstream>

#include "response_holder.hpp"
#include "ru_yandex_cocaine_dealer_Response.h"
#include "util.hpp"
using namespace cocaine::dealer::java;
using namespace cocaine::dealer;

JNIEXPORT void JNICALL Java_ru_yandex_cocaine_dealer_Response_close
(JNIEnv *, jobject, jlong c_response_ptr) {
    response_holder_t * response_ptr = (response_holder_t *)c_response_ptr;
    delete response_ptr;
}

jint deal_with_error(JNIEnv *env, dealer_error& error);

JNIEXPORT jstring JNICALL Java_ru_yandex_cocaine_dealer_Response_get(
        JNIEnv *env, jobject obj, jlong c_response_ptr, jdouble timeout)
{
    response_holder_t *response_holder = (response_holder_t *) c_response_ptr;
    data_container container;
    bool has_next = false;
    try {
        has_next = response_holder->get()->get(&container, timeout);
    } catch (dealer_error& error) {
        int throw_result = deal_with_error(env, error);
        std::stringstream s;
        s << throw_result;
        return from_string(env, s.str());
    }
    jstring head = from_string(env, "");
    if (!container.empty()) {
        std::string response_str((char*) container.data(), container.size());
        head = env->NewStringUTF(response_str.c_str());
    }
    if (has_next) {
        jstring tail = Java_ru_yandex_cocaine_dealer_Response_get(env, obj,
                c_response_ptr, timeout);
        std::string tail_str = to_string(env, tail);
        std::string head_str = to_string(env, head);
        return from_string(env, head_str + tail_str);
    }
    return head;
}
/*enum error_code {
    request_error   = 400,
    location_error  = 404,
    server_error    = 500,
    app_error       = 502,
    resource_error  = 503,
    timeout_error   = 504,
    deadline_error  = 520
};*/
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
