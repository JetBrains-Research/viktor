#include <cassert>
#include <cstdint>
#include <cpuid.h>

#include "org_jetbrains_bio_jni_LoaderKt.hpp"

#define JNI_METHOD(rtype, name)                 \
    JNIEXPORT rtype JNICALL Java_org_jetbrains_bio_jni_LoaderKt_##name

// %ecx
#define bit_AVX   (1 << 28)
#define bit_XSAVE (1 << 26)

// %edx
#define bit_SSE2  (1 << 26)

static inline int64_t xgetbv(int ctr) {
   uint32_t eax, edx;
   __asm("xgetbv" : "=a"(eax),"=d"(edx) : "c"(ctr) : );
   return eax | (uint64_t(edx) << 32);
}

JNI_METHOD(jboolean, isAvxSupported)(JNIEnv *env, jclass) {
    uint32_t eax, ebx, ecx, edx;
    __cpuid_count(0, 0, eax, ebx, ecx, edx);
    assert(eax >= 0x00000001);

    __cpuid_count(1, 0, eax, ebx, ecx, edx);
    if ((edx & bit_SSE2) == 0) {
        return false;
    }

    return (ecx & bit_XSAVE) != 0 && (xgetbv(0) & 6) != 0;
}

JNI_METHOD(jboolean, isSse2Supported)(JNIEnv *env, jclass) {
    uint32_t eax, ebx, ecx, edx;
    __cpuid_count(0, 0, eax, ebx, ecx, edx);
    assert(eax >= 0x00000001);

    __cpuid_count(1, 0, eax, ebx, ecx, edx);
    return (ecx & bit_SSE2) != 0;
}
