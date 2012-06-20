#include "util.hpp"

namespace cocaine { namespace dealer { namespace java {

std::string to_string(JNIEnv* env, jstring string)
{
    std::string value;
    if (string == NULL) {
        return value; // empty string
    }
    const char* raw = env->GetStringUTFChars(string, NULL);
    if (raw != NULL) {
        jsize len = env->GetStringUTFLength(string);
        value.assign(raw, len);
        env->ReleaseStringUTFChars(string, raw);
    }

    return value;
};


jstring from_string(JNIEnv* env, std::string str){
	return env->NewStringUTF(str.c_str());
};

}}}
