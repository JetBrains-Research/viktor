#include <boost/simd/arch/tags.hpp>

#include "org_jetbrains_bio_viktor_LoaderKt.hpp"

#define JNI_METHOD(rtype, name)                                         \
    JNIEXPORT rtype JNICALL Java_org_jetbrains_bio_viktor_LoaderKt_##name

JNI_METHOD(jboolean, isAvxSupported)(JNIEnv *env, jclass) {
    return boost::simd::avx.is_supported();
}

JNI_METHOD(jboolean, isSse2Supported)(JNIEnv *env, jclass) {
    return boost::simd::sse2.is_supported();
}
