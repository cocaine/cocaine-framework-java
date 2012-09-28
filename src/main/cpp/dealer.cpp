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
#include <vector>
#include <locale>
#include <cocaine/dealer/dealer.hpp>
#include <cocaine/dealer/response.hpp>
#include <cocaine/dealer/message.hpp>
#include <cocaine/dealer/message_policy.hpp>
#include <cocaine/dealer/message_path.hpp>
#include <cocaine/dealer/utils/error.hpp>

#include "cocaine_dealer_Dealer.h"
#include "response_holder.hpp"
#include "util.hpp"

using namespace cocaine::dealer;
using namespace cocaine::dealer::java;

JNIEXPORT void JNICALL Java_cocaine_dealer_Dealer_nativeDelete
(JNIEnv *, jobject, jlong dealer_ptr) {
    dealer_t * dealer = (dealer_t *) dealer_ptr;
    delete dealer;
}

JNIEXPORT jlong JNICALL Java_cocaine_dealer_Dealer_nativeInit(JNIEnv *env,
        jobject, jstring config_path) {
    std::string config_path_str = to_string(env, config_path);
    try{
        dealer_t * dealer = new dealer_t(config_path_str);
        return (jlong) dealer;
    } catch( dealer_error& error) {
        throw_runtime_exception(env, error.what());
    } catch (internal_error& error) {
        throw_runtime_exception(env, error.what());
    } catch (std::exception & error) {
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

JNIEXPORT jint JNICALL Java_cocaine_dealer_Dealer_nativeGetStoredMessagesCount
  (JNIEnv *env, jobject self, jlong dealer_ptr, jstring service_alias) {
    dealer_t *dealer = (dealer_t*) dealer_ptr;
    std::string service_alias_str = to_string(env, service_alias);
    size_t result = dealer->stored_messages_count(service_alias_str);
    return result;
}

jobject construct_message_policy(JNIEnv *env, const message_policy_t& policy,
        jclass message_policy_class, jmethodID message_policy_constructor)
{
    jlong timeoutMillis = policy.timeout * 1000;
    jlong deadlineMillis = policy.deadline * 1000;
    jobject message_policy_obj = env->NewObject(message_policy_class, message_policy_constructor,
            policy.urgent, policy.persistent, timeoutMillis, deadlineMillis, policy.max_retries);
    return message_policy_obj;
}

jobject construct_message(JNIEnv *env, const message_t& message,
        jclass message_class, jmethodID message_constructor,
        jclass message_policy_class, jmethodID message_policy_constructor) {
    jstring id_string = from_string(env, message.id);
    std::string message_path = message.path.service_alias + "/" + message.path.handle_name;
    jstring message_path_string = from_string(env, message_path);
    jobject message_policy_obj = construct_message_policy(env, message.policy, message_policy_class, message_policy_constructor);
    jbyteArray data = byte_array_from(env, message.data.data(), message.data.size());
    jobject message_obj = env->NewObject(message_class, message_constructor,
            id_string,
            message_path_string,
            data,
            message_policy_obj
    );
    return message_obj;
}

JNIEXPORT jobject JNICALL Java_cocaine_dealer_Dealer_nativeGetStoredMessages
  (JNIEnv * env, jobject, jlong dealer_ptr, jstring service_alias) {
    dealer_t *dealer = (dealer_t*) dealer_ptr;
    std::string service_alias_str = to_string(env, service_alias);
    std::vector<message_t> messages;
    dealer->get_stored_messages(service_alias_str, messages);
    jclass list_clazz = env->FindClass("java/util/ArrayList");
    if (list_clazz == NULL) {
        throw_runtime_exception(env, "could not load ArrayList class");
        return NULL;
    }
    jmethodID list_constructor = env->GetMethodID(list_clazz, "<init>", "()V");
    if (list_constructor == NULL) {
        throw_runtime_exception(env, "could not find ArrayList constructor()");
        return NULL;
    }
    jmethodID add_method = env->GetMethodID(list_clazz, "add", "(Ljava/lang/Object;)Z");
    if (add_method == NULL) {
        throw_runtime_exception(env, "could not find ArrayList add() method ");
        return NULL;
    }
    jobject list = env->NewObject(list_clazz, list_constructor);
    jclass message_class = env->FindClass("cocaine/dealer/Message");
    if (message_class == NULL) {
        throw_runtime_exception(env, "could not find cocaine.dealer.Message class");
        return NULL;
    }
    jmethodID message_constructor = env->GetMethodID(message_class, "<init>", "(Ljava/lang/String;Ljava/lang/String;[BLcocaine/dealer/MessagePolicy;)V");
    if (message_constructor == NULL) {
        throw_runtime_exception(env, "could not find cocaine.dealer.Message constructor(String, String, byte[], MessagePolicy)");
        return NULL;
    }
    jclass message_policy_class = env->FindClass("cocaine/dealer/MessagePolicy");
    if (message_policy_class == NULL) {
        throw_runtime_exception(env, "could not find cocaine.dealer.MessagePolicy class");
        return NULL;
    }
    jmethodID message_policy_constructor = env->GetMethodID(message_policy_class, "<init>", "(ZZJJI)V");
    if (message_policy_constructor == NULL) {
        throw_runtime_exception(env, "could not find cocaine.dealer.MessagePolicy constructor(boolean, boolean, long, long, int)");
        return NULL;
    }
    for (std::vector<message_t>::iterator iter = messages.begin(); iter!=messages.end(); ++iter) {
        jobject message_obj = construct_message(env, *iter, message_class, message_constructor, message_policy_class, message_policy_constructor);
        env->CallBooleanMethod(list, add_method, message_obj);
    }
    return list;
}

JNIEXPORT jlong JNICALL Java_cocaine_dealer_Dealer_nativeSendMessage(
        JNIEnv *env, jobject self, jlong dealer_ptr, jstring service,
        jstring handle, jbyteArray msg_bytes, jboolean urgent, jboolean persistent, jdouble timeout, jdouble deadline, jint max_retries)
{
    dealer_t *dealer = (dealer_t*) dealer_ptr;
    struct message_policy_t policy(urgent, persistent, timeout,
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
    } catch (std::exception & error) {
        throw_runtime_exception(env, error.what());
    }
    return 0;
}
