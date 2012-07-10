#include <cocaine/dealer/dealer.hpp>
#include <cocaine/dealer/response.hpp>
#include <cocaine/dealer/message_policy.hpp>
#include <cocaine/dealer/message_path.hpp>
#include <cocaine/dealer/utils/error.hpp>
#include <locale>

#include "ru_yandex_cocaine_dealer_Dealer.h"
#include "response_holder.hpp"
#include "util.hpp"

using namespace cocaine::dealer;
using namespace cocaine::dealer::java;

JNIEXPORT void JNICALL Java_ru_yandex_cocaine_dealer_Dealer_delete
(JNIEnv *, jobject, jlong dealer_ptr) {
    dealer_t * dealer = (dealer_t *) dealer_ptr;
    delete dealer;
}

JNIEXPORT jlong JNICALL Java_ru_yandex_cocaine_dealer_Dealer_init(JNIEnv *env,
        jobject, jstring config_path) {
    std::string config_path_str = to_string(env, config_path);
    try{
        dealer_t * dealer = new dealer_t(config_path_str);
        return (jlong) dealer;
    } catch( dealer_error& error){
        throw_runtime_exception(env, error.what());
    } catch (internal_error& error){
        throw_runtime_exception(env, error.what());
    }
    return 0;
}

struct msg_raii {
    jbyte *m_data;
    jsize m_size;
    jbyteArray m_array;
    JNIEnv *m_env;
    msg_raii(JNIEnv *env, jbyteArray msg_byte_array) {
        m_env = env;
        m_array = msg_byte_array;
        m_data = m_env->GetByteArrayElements(m_array, NULL);
        m_size = m_env->GetArrayLength(m_array);
    }

    ~msg_raii() {
        m_env->ReleaseByteArrayElements(m_array, m_data, JNI_ABORT);
    }
};

JNIEXPORT jlong JNICALL Java_ru_yandex_cocaine_dealer_Dealer_sendMessage(
        JNIEnv *env, jobject self, jlong dealer_ptr, jstring service,
        jstring handle, jbyteArray msg_bytes, jboolean send_to_all_hosts,
        jboolean urgent, jdouble timeout, jdouble deadline, jint max_retries)
{
    dealer_t *dealer = (dealer_t*) dealer_ptr;
    struct message_policy_t policy(send_to_all_hosts, urgent, 0.0, timeout,
            deadline, max_retries);

    std::string service_str = to_string(env, service);
    std::string handle_str = to_string(env, handle);
    msg_raii msg(env, msg_bytes);
    message_path_t message_path(service_str, handle_str);
    try {
        boost::shared_ptr < response_t > dealer_response = dealer->send_message(
            msg.m_data, msg.m_size,
            message_path, policy);
        response_holder_t *response_ = new response_holder_t(dealer_response);
        return (jlong) response_;
    } catch (dealer_error& error) {
        throw_runtime_exception(env, error.what());
    }
    return 0;
}
