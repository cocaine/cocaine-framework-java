#include <cocaine/dealer/utils/data_container.hpp>
#include <cocaine/dealer/response.hpp>

#include "response_holder.hpp"
#include "ru_yandex_cocaine_dealer_Response.h"
#include "util.hpp"

using namespace cocaine::dealer::java;
using namespace cocaine::dealer;

JNIEXPORT void JNICALL Java_ru_yandex_cocaine_dealer_Response_close
  (JNIEnv *, jobject, jlong c_response_ptr){
	response_holder_t * response_ptr = (response_holder_t *)c_response_ptr;
	delete response_ptr;
}

JNIEXPORT jstring JNICALL Java_ru_yandex_cocaine_dealer_Response_get
  (JNIEnv *env, jobject obj, jlong c_response_ptr, jdouble timeout){
	response_holder_t *response_holder = (response_holder_t *)c_response_ptr;
	data_container * container = new data_container();
	bool has_next = response_holder->get()->get(container, timeout);
	char buf[100] = {0};
	jstring head = env->NewStringUTF((char*) container->data());
	if (has_next){
		jstring tail = Java_ru_yandex_cocaine_dealer_Response_get(env, obj, c_response_ptr, timeout);
		std::string tail_str = to_string(env,tail);
		std::string head_str = to_string(env, head);
		return from_string(env, head_str + tail_str);
	}
	return head;
}
