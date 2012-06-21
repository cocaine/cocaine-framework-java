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

JNIEXPORT void JNICALL Java_ru_yandex_cocaine_dealer_Client_delete
(JNIEnv *, jobject, jlong dealer_ptr) {
    dealer_t * dealer = (dealer_t *) dealer_ptr;
    delete dealer;
};

JNIEXPORT jlong JNICALL Java_ru_yandex_cocaine_dealer_Client_init(JNIEnv *env,
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
;

JNIEXPORT jlong JNICALL Java_ru_yandex_cocaine_dealer_Client_sendMessage(
        JNIEnv *env, jobject self, jlong dealer_ptr, jstring service,
        jstring handle, jstring text, jboolean send_to_all_hosts,
        jboolean urgent, jdouble timeout, jdouble deadline, jint max_retries)
{
    dealer_t *dealer = (dealer_t*) dealer_ptr;
    struct message_policy_t policy(send_to_all_hosts, urgent, 0.0, timeout,
            deadline, max_retries);

    std::string service_str = to_string(env, service);
    std::string handle_str = to_string(env, handle);
    std::string text_str = to_string(env, text);
    message_path_t message_path(service_str, handle_str);
    try {
        boost::shared_ptr < response_t > dealer_response = dealer->send_message(
            text_str.data(), text_str.size(),
            message_path, policy);
        response_holder_t *response_ = new response_holder_t(dealer_response);
        return (jlong) response_;
    } catch (dealer_error& error) {
        throw_runtime_exception(env, error.what());
    }
    return 0;
}
