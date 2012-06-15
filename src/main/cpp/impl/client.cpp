#include <cocaine/dealer/dealer.hpp>
#include <cocaine/dealer/message_policy.hpp>
#include <cocaine/dealer/message_path.hpp>
#include <cocaine/dealer/utils/error.hpp>

#include <cocaine/helpers/track.hpp>

#include "client.hpp"

using namespace cocaine::dealer;

std::string to_string(JNIEnv* env, jstring string)
{
    std::string value;
    if (string == NULL) {
        return value; // empty string
    }
    const jchar* raw = env->GetStringChars(string, NULL);
    if (raw != NULL) {
        jsize len = env->GetStringLength(string);
        value.assign(raw, len);
        env->ReleaseStringChars(string, raw);
    }
    return value;
};

JNIEXPORT void JNICALL Java_ru_yandex_cocaine_dealer_Client_delete
  (JNIEnv *, jobject, jlong dealer_ptr) {
	dealer_t * dealer = (dealer_t *) dealer_ptr;
	delete dealer;
};

JNIEXPORT jlong JNICALL Java_ru_yandex_cocaine_dealer_Client_init
  (JNIEnv *env, jobject, jstring config_path)
{
	dealer_t * dealer = new dealer_t(to_string(env, config_path));
        return (jlong) dealer;
};

JNIEXPORT jlong JNICALL Java_ru_yandex_cocaine_dealer_Client_sendMessage
  (JNIEnv *env, jobject self, jlong dealerPtr, jstring service, jstring handle, jstring text) {
	dealer_t *dealer = (dealer_t*) dealerPtr;
	struct message_policy *policy = new message_policy();
	std::string service_str = to_string(env, service);
	std::string handle_str = to_string(env, handle);
	std::string text_str = to_string(env, text);
	boost::shared_ptr<response> r = dealer->send_message(text_str, message_path(service_str, handle_str), *policy);
	return 0;
}
