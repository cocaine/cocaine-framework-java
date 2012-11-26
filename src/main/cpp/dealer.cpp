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


void construct_message_policy(JNIEnv * env, jobject j_message_policy, message_policy_t& message_policy) {
    jclass cls = env->GetObjectClass(j_message_policy);
    jmethodID m_urgent = get_method_or_throw(env, cls, "getUrgent", "()Z");
    jmethodID m_persistent = get_method_or_throw(env, cls, "getPersistent", "()Z");
    jmethodID m_timeout = get_method_or_throw(env, cls, "getTimeoutSeconds", "()D");
    jmethodID m_ack_timeout = get_method_or_throw(env, cls, "getAckTimeoutSeconds", "()D");
    jmethodID m_deadline = get_method_or_throw(env, cls, "getDeadlineSeconds", "()D");
    jmethodID m_max_retries = get_method_or_throw(env, cls, "getMaxRetries", "()I");
    jboolean j_urgent = env->CallBooleanMethod(j_message_policy, m_urgent);
    jboolean j_persistent = env->CallBooleanMethod(j_message_policy, m_persistent);
    jdouble j_timeout = env->CallDoubleMethod(j_message_policy, m_timeout);
    jdouble j_ack_timeout = env->CallDoubleMethod(j_message_policy, m_timeout);
    jdouble j_deadline = env->CallDoubleMethod(j_message_policy, m_deadline);
    jint j_max_retries = env->CallIntMethod(j_message_policy, m_max_retries);
    message_policy.urgent = j_urgent;
    message_policy.persistent = j_persistent;
    message_policy.timeout = j_timeout;
    message_policy.ack_timeout = j_ack_timeout;
    message_policy.deadline = j_deadline;
    message_policy.max_retries = j_max_retries;
}

void construct_message(JNIEnv *env, jobject j_message, message_t& message) {
    jclass cls = env->GetObjectClass(j_message);
    jmethodID m_id = get_method_or_throw(env, cls, "getId", "()Ljava/lang/String;");
    jmethodID m_service = get_method_or_throw(env, cls, "getService", "()Ljava/lang/String;");
    jmethodID m_handle = get_method_or_throw(env, cls, "getHandle", "()Ljava/lang/String;");
    jmethodID m_data = get_method_or_throw(env, cls, "getData", "()[B");
    jmethodID m_policy = get_method_or_throw(env, cls, "getPolicy", "()Lcocaine/dealer/MessagePolicy;");
    jstring j_id = (jstring) env->CallObjectMethod(j_message, m_id);
    jstring j_service = (jstring) env->CallObjectMethod(j_message, m_service);
    jstring j_handle = (jstring) env->CallObjectMethod(j_message, m_handle);
    jbyteArray j_data = (jbyteArray) env->CallObjectMethod(j_message, m_data);
    jobject j_policy = env->CallObjectMethod(j_message, m_policy);
    message.id = to_string(env, j_id);
    message.path.service_alias = to_string(env, j_service);
    message.path.handle_name = to_string(env, j_handle);
    construct_message_policy(env, j_policy, message.policy);
    msg_raii msg(env, j_data);
    message.data.set_data(msg.m_data, msg.m_size);
}

jobject construct_java_message_policy(JNIEnv *env, const message_policy_t& policy,
        jclass message_policy_class, jmethodID message_policy_constructor)
{
    jlong timeout_millis = policy.timeout * 1000;
    jlong ack_timeout_millis = policy.ack_timeout * 1000;
    jlong deadline_millis = policy.deadline * 1000;
    jobject message_policy_obj = env->NewObject(message_policy_class, message_policy_constructor,
            policy.urgent, policy.persistent, timeout_millis, ack_timeout_millis, deadline_millis, policy.max_retries);
    return message_policy_obj;
}

jobject construct_java_message(JNIEnv *env, const message_t& message,
        jclass message_class, jmethodID message_constructor,
        jclass message_policy_class, jmethodID message_policy_constructor) {
    jstring id_string = from_string(env, message.id);
    std::string service = message.path.service_alias;
    std::string handle = message.path.handle_name;
    jstring j_service = from_string(env, service);
    jstring j_handle = from_string(env, handle);
    jobject message_policy_obj = construct_java_message_policy(env, message.policy, message_policy_class, message_policy_constructor);
    jbyteArray data = byte_array_from(env, message.data.data(), message.data.size());
    jobject message_obj = env->NewObject(message_class, message_constructor,
            id_string,
            j_service,
            j_handle,
            data,
            message_policy_obj
    );
    return message_obj;
}

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
    } catch(dealer_error& error) {
        throw_runtime_exception(env, error.what());
    } catch (internal_error& error) {
        throw_runtime_exception(env, error.what());
    } catch (std::exception & error) {
        throw_runtime_exception(env, error.what());
    }
    return 0;
}

JNIEXPORT jint JNICALL Java_cocaine_dealer_Dealer_nativeGetStoredMessagesCount
  (JNIEnv *env, jobject self, jlong dealer_ptr, jstring j_service_alias) {
    dealer_t *dealer = (dealer_t*) dealer_ptr;
    std::string service_alias = to_string(env, j_service_alias);
    try {
        size_t result = dealer->stored_messages_count(service_alias);
        return result;
    } catch(dealer_error& error) {
        throw_runtime_exception(env, error.what());
    } catch (internal_error& error) {
        throw_runtime_exception(env, error.what());
    } catch (std::exception & error) {
        throw_runtime_exception(env, error.what());
    }
    return 0;
}


JNIEXPORT void JNICALL Java_cocaine_dealer_Dealer_nativeRemoveStoredMessage
  (JNIEnv *env, jobject self, jlong dealer_ptr, jobject j_message) {
    dealer_t *dealer = (dealer_t*) dealer_ptr;
    try{
        message_t message;
        construct_message(env, j_message, message);
        dealer->remove_stored_message(message);
    } catch(dealer_error& error) {
        throw_runtime_exception(env, error.what());
    } catch (internal_error& error) {
        throw_runtime_exception(env, error.what());
    } catch (std::exception & error) {
        throw_runtime_exception(env, error.what());
    }
}

JNIEXPORT jobject JNICALL Java_cocaine_dealer_Dealer_nativeGetStoredMessages
  (JNIEnv * env, jobject, jlong dealer_ptr, jstring service_alias) {
    dealer_t *dealer = (dealer_t*) dealer_ptr;
    try {
        std::string service_alias_str = to_string(env, service_alias);
        std::vector<message_t> messages;
        dealer->get_stored_messages(service_alias_str, messages);
        jclass list_class = get_class_or_throw(env, "java/util/ArrayList");
        jclass message_class = get_class_or_throw(env, "cocaine/dealer/Message");
        jclass message_policy_class = get_class_or_throw(env, "cocaine/dealer/MessagePolicy");
        jmethodID list_constructor = get_method_or_throw(env, list_class, "<init>", "()V");
        jmethodID add_method = get_method_or_throw(env, list_class, "add", "(Ljava/lang/Object;)Z");
        jmethodID message_constructor = get_method_or_throw(env, message_class, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[BLcocaine/dealer/MessagePolicy;)V");
        jmethodID message_policy_constructor = get_method_or_throw(env, message_policy_class, "<init>", "(ZZJJJI)V");
        jobject list = env->NewObject(list_class, list_constructor);
        for (std::vector<message_t>::iterator iter = messages.begin(); iter!=messages.end(); ++iter) {
            jobject message_obj = construct_java_message(env, *iter, message_class, message_constructor, message_policy_class, message_policy_constructor);
            env->CallBooleanMethod(list, add_method, message_obj);
        }
        return list;
    } catch(dealer_error& error) {
        throw_runtime_exception(env, error.what());
        return NULL;
    } catch (internal_error& error) {
        throw_runtime_exception(env, error.what());
        return NULL;
    } catch (std::exception & error) {
        throw_runtime_exception(env, error.what());
        return NULL;
    }
}

JNIEXPORT jlong JNICALL Java_cocaine_dealer_Dealer_nativeSendMessage
  (JNIEnv *env, jobject self, jlong dealer_ptr, jstring service,
        jstring handle, jbyteArray msg_bytes, jboolean urgent, jboolean persistent, jdouble timeout, jdouble ack_timeout, jdouble deadline, jint max_retries)
{
    dealer_t *dealer = (dealer_t*) dealer_ptr;
    struct message_policy_t policy(urgent, persistent, timeout, ack_timeout,
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
    } catch(dealer_error& error) {
        throw_runtime_exception(env, error.what());
    } catch (internal_error& error) {
        throw_runtime_exception(env, error.what());
    } catch (std::exception& error) {
        throw_runtime_exception(env, error.what());
    }
    return 0;
}

JNIEXPORT jobject JNICALL Java_cocaine_dealer_Dealer_nativePolicyForService
  (JNIEnv *env, jobject self, jlong dealer_ptr, jstring service) {
    dealer_t *dealer = (dealer_t*) dealer_ptr;
    std::string service_str = to_string(env, service);
    try {
        message_policy_t policy  = dealer->policy_for_service(service_str);
        jclass message_policy_class = get_class_or_throw(env, "cocaine/dealer/MessagePolicy");
        jmethodID message_policy_constructor = get_method_or_throw(env, message_policy_class, "<init>", "(ZZJJJI)V");
        jobject java_policy = construct_java_message_policy(env, policy, message_policy_class, message_policy_constructor);
        return java_policy;
    } catch (dealer_error& error) {
        throw_runtime_exception(env, error.what());
    } catch (internal_error& error) {
        throw_runtime_exception(env, error.what());
    } catch (std::exception& error) {
        throw_runtime_exception(env, error.what());
    }
    return 0;
}
