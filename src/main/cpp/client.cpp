#include <cocaine/dealer/dealer.hpp>
#include <cocaine/dealer/response.hpp>
#include <cocaine/dealer/message_policy.hpp>
#include <cocaine/dealer/message_path.hpp>
#include <locale>

#include "ru_yandex_cocaine_dealer_Client.h"
#include "response_holder.hpp"
#include "util.hpp"

using namespace cocaine::dealer;
using namespace cocaine::dealer::java;

JNIEXPORT void JNICALL Java_ru_yandex_cocaine_dealer_Client_delete
  (JNIEnv *, jobject, jlong dealer_ptr) {
	dealer_t * dealer = (dealer_t *) dealer_ptr;
	delete dealer;
};

JNIEXPORT jlong JNICALL Java_ru_yandex_cocaine_dealer_Client_init
  (JNIEnv *env, jobject, jstring config_path)
{
	std::string config_path_str = to_string(env, config_path);
	dealer_t * dealer = new dealer_t(config_path_str);
    return (jlong) dealer;
};

JNIEXPORT jlong JNICALL Java_ru_yandex_cocaine_dealer_Client_sendMessage
  (JNIEnv *env, jobject self, jlong dealerPtr, jstring service, jstring handle, jstring text) {
	dealer_t *dealer = (dealer_t*) dealerPtr;
	struct message_policy_t *policy = new message_policy_t();
	std::string service_str = to_string(env, service);
	std::string handle_str = to_string(env, handle);
	std::string text_str = to_string(env, text);
	boost::shared_ptr<response_t> dealer_response = dealer->send_message(text_str.data(), text_str.size(), message_path_t(service_str, handle_str), *policy);
	response_holder_t *response_ = new response_holder_t(dealer_response);
	return (jlong) response_;
}
