using namespace std;

#pragma once

#include <jni.h>
#include <string>

/**
 * Util methods to convert JNI data type to native data type
 */
class JNIConvertor {

public:

    static jstring fromStringToJString(JNIEnv *env, string str) {
        return env->NewStringUTF(str.c_str());
    };

    static string fromJStringToString(JNIEnv *env, jstring jstr) {
        if (jstr == NULL) {
            return string("");
        }
        const char *cstr = (env)->GetStringUTFChars(jstr, 0);
        string result(cstr);
        env->ReleaseStringUTFChars(jstr, cstr);
        return result;
    }

    static jbyteArray fromStringToJByteArray(JNIEnv *env, string str) {
        int byteCount = str.length();
        const jbyte *pNativeMessage = reinterpret_cast<const jbyte *>(str.c_str());
        jbyteArray bytes = env->NewByteArray(byteCount);
        env->SetByteArrayRegion(bytes, 0, byteCount, pNativeMessage);
        return bytes;
    }

    static unsigned char *as_unsigned_char_array(JNIEnv *&env, jbyteArray array) {
        int len = env->GetArrayLength(array);
        unsigned char *buf = new unsigned char[len];
        env->GetByteArrayRegion(array, 0, len, reinterpret_cast<jbyte *>(buf));
        return buf;
    }

    static char *as_char_array(JNIEnv *&env, jbyteArray array) {
        int len = env->GetArrayLength(array);
        char *buf = new char[len];
        env->GetByteArrayRegion(array, 0, len, reinterpret_cast<jbyte *>(buf));
        return buf;
    }

    static jbyteArray as_byte_array(JNIEnv *&env, unsigned char *buf, jsize len) {
        jbyteArray array = env->NewByteArray(len);
        env->SetByteArrayRegion(array, 0, len, reinterpret_cast<jbyte *>(buf));
        return array;
    }

    static string fromJByteArrayToString(JNIEnv *env, jbyteArray jbytes) {
        string result = "";
        if (jbytes != NULL) {
            jbyte *text_input = env->GetByteArrayElements(jbytes, NULL);
            if (text_input != NULL) {
                jsize size = env->GetArrayLength(jbytes);
                result = string((char *) text_input, size);
            }
            env->ReleaseByteArrayElements(jbytes, text_input, JNI_ABORT);
        }
        return result;
    }

    static jbyteArray fromUtf8ToUtf16(JNIEnv *env, jbyteArray key) {
        string key_s = fromJByteArrayToString(env, key);
        wstring key_ws = fromUtf8ToUtf16(key_s);
        string mutated_key = string(key_ws.begin(), key_ws.end());
        return fromStringToJByteArray(env, mutated_key);
    }

    static wstring fromUtf8ToUtf16(const string &str) {
        size_t size = str.size();
        if (size == 0)
            return L"";

        wchar_t *buffer = new wchar_t[size * 2];

        unsigned int *cursor = (unsigned int *) buffer;
        unsigned int uc = 0;
        int need = 0;
        size_t error = string::npos;

        unsigned char ch;
        for (size_t i = 0; i < size; ++i) {
            ch = str.at(i);
            if (need) {
                if ((ch & 0xc0) == 0x80) {
                    uc = (uc << 6) | (ch & 0x3f);
                    need--;
                    if (!need) {
                        if (uc > 0xffff) {
                            uc -= 0x10000;
                            unsigned int high = uc / 0x400 + 0xd800;
                            unsigned int low = uc % 0x400 + 0xdc00;
                            *cursor++ = high;
                            *cursor++ = low;
                        } else {
                            *cursor++ = uc;
                        }
                    }
                } else {
                    i = error;

                    *cursor++ = 0xdbff;
                    *cursor++ = 0xde00 + ((unsigned char) str.at(i));
                    need = 0;
                }
            } else {
                if (ch < 128) {
                    *cursor++ = ch;
                } else if ((ch & 0xe0) == 0xc0) {
                    uc = ch & 0x1f;
                    need = 1;
                    error = i;
                } else if ((ch & 0xf0) == 0xe0) {
                    uc = ch & 0x0f;
                    need = 2;
                    error = i;
                } else if ((ch & 0xf8) == 0xf0) {
                    uc = ch & 0x07;
                    need = 3;
                    error = i;
                } else {
                    // Error
                    *cursor++ = 0xdbff;
                    *cursor++ = 0xde00 + ((unsigned char) str.at(i));
                }
            }
        }
        if (need) {
            // we have some invalid characters remaining we need to add to the string
            for (size_t i = error; i < size; ++i) {
                *cursor++ = 0xdbff;
                *cursor++ = 0xde00 + (unsigned char) str.at(i);
            }
        }

        *cursor++ = 0x00;

        wstring result(buffer);
        delete[]buffer;
        return result;
    }
};
