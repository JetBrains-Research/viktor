#include <boost/simd/constant/inf.hpp>
#include <boost/simd/constant/minf.hpp>
#include <boost/simd/function/div.hpp>
#include <boost/simd/function/minus.hpp>
#include <boost/simd/function/multiplies.hpp>
#include <boost/simd/function/plus.hpp>
#include <boost/simd/function/unary_minus.hpp>

#include "org_jetbrains_bio_jni_DoubleOpsNative.hpp"
#include "simd_ops.hpp"
#include "transform_accumulate.hpp"

#define JNI_METHOD(rtype, name)                                         \
    JNIEXPORT rtype JNICALL Java_org_jetbrains_bio_jni_DoubleOpsNative_##name

JNI_METHOD(void, criticalPlus)(JNIEnv *env, jobject,
                               jdoubleArray jsrc1, jint src_offset1,
                               jdoubleArray jsrc2, jint src_offset2,
                               jdoubleArray jdst, jint dst_offset,
                               jint length)
{
    jdouble *src1 = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc1, NULL));
    jdouble *src2 = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc2, NULL));
    jdouble *dst = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jdst, NULL));
    boost::simd::transform(src1 + src_offset1,
                           src1 + src_offset1 + length,
                           src2 + src_offset2,
                           dst + dst_offset,
                           boost::simd::plus);
    env->ReleasePrimitiveArrayCritical(jsrc1, src1, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jsrc2, src2, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
}

JNI_METHOD(void, criticalMinus)(JNIEnv *env, jobject,
                                jdoubleArray jsrc1, jint src_offset1,
                                jdoubleArray jsrc2, jint src_offset2,
                                jdoubleArray jdst, jint dst_offset,
                                jint length)
{
    jdouble *src1 = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc1, NULL));
    jdouble *src2 = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc2, NULL));
    jdouble *dst = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jdst, NULL));
    boost::simd::transform(src1 + src_offset1,
                           src1 + src_offset1 + length,
                           src2 + src_offset2,
                           dst + dst_offset,
                           boost::simd::minus);
    env->ReleasePrimitiveArrayCritical(jsrc1, src1, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jsrc2, src2, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
}

JNI_METHOD(void, criticalTimes)(JNIEnv *env, jobject,
                                jdoubleArray jsrc1, jint src_offset1,
                                jdoubleArray jsrc2, jint src_offset2,
                                jdoubleArray jdst, jint dst_offset,
                                jint length)
{
    jdouble *src1 = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc1, NULL));
    jdouble *src2 = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc2, NULL));
    jdouble *dst = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jdst, NULL));
    boost::simd::transform(src1 + src_offset1,
                           src1 + src_offset1 + length,
                           src2 + src_offset2,
                           dst + dst_offset,
                           boost::simd::multiplies);
    env->ReleasePrimitiveArrayCritical(jsrc1, src1, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jsrc2, src2, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
}

JNI_METHOD(void, criticalDiv)(JNIEnv *env, jobject,
                              jdoubleArray jsrc1, jint src_offset1,
                              jdoubleArray jsrc2, jint src_offset2,
                              jdoubleArray jdst, jint dst_offset,
                              jint length)
{
    jdouble *src1 = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc1, NULL));
    jdouble *src2 = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc2, NULL));
    jdouble *dst = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jdst, NULL));
    boost::simd::transform(src1 + src_offset1,
                           src1 + src_offset1 + length,
                           src2 + src_offset2,
                           dst + dst_offset,
                           boost::simd::div);
    env->ReleasePrimitiveArrayCritical(jsrc1, src1, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jsrc2, src2, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
}

JNI_METHOD(void, criticalPlusScalar)(JNIEnv *env, jobject,
                                     jdoubleArray jsrc, jint src_offset,
                                     jdouble update,
                                     jdoubleArray jdst, jint dst_offset,
                                     jint length)
{
    jdouble *src = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc, NULL));
    jdouble *dst = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jdst, NULL));
    boost::simd::transform(src + src_offset,
                           src + src_offset + length,
                           dst + dst_offset,
                           simdops::plus(update));
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
}

JNI_METHOD(void, criticalMinusScalar)(JNIEnv *env, jobject,
                                      jdoubleArray jsrc, jint src_offset,
                                      jdouble update,
                                      jdoubleArray jdst, jint dst_offset,
                                      jint length)
{
    jdouble *src = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc, NULL));
    jdouble *dst = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jdst, NULL));
    boost::simd::transform(src + src_offset,
                           src + src_offset + length,
                           dst + dst_offset,
                           simdops::minus(update));
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
}

JNI_METHOD(void, criticalTimesScalar)(JNIEnv *env, jobject,
                                      jdoubleArray jsrc, jint src_offset,
                                      jdouble update,
                                      jdoubleArray jdst, jint dst_offset,
                                      jint length)
{
    jdouble *src = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc, NULL));
    jdouble *dst = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jdst, NULL));
    boost::simd::transform(src + src_offset,
                           src + src_offset + length,
                           dst + dst_offset,
                           simdops::multiplies(update));
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
}

JNI_METHOD(void, criticalDivScalar)(JNIEnv *env, jobject,
                                    jdoubleArray jsrc, jint src_offset,
                                    jdouble update,
                                    jdoubleArray jdst, jint dst_offset,
                                    jint length)
{
    jdouble *src = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc, NULL));
    jdouble *dst = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jdst, NULL));
    boost::simd::transform(src + src_offset,
                           src + src_offset + length,
                           dst + dst_offset,
                           simdops::div(update));
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
}

JNI_METHOD(void, criticalNegate)(JNIEnv *env, jobject,
                                 jdoubleArray jsrc, jint src_offset,
                                 jdoubleArray jdst, jint dst_offset,
                                 jint length)
{
    jdouble *src = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc, NULL));
    jdouble *dst = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jdst, NULL));
    boost::simd::transform(src + src_offset,
                           src + src_offset + length,
                           dst + dst_offset,
                           boost::simd::unary_minus);
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
}

JNI_METHOD(jdouble, unsafeMin)(JNIEnv *env, jobject,
                               jdoubleArray jsrc, jint src_offset,
                               jint length)
{
    jdouble *src = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc, NULL));
    jdouble min = boost::simd::accumulate(
        src + src_offset, src + src_offset + length,
        boost::simd::Inf<jdouble>(), boost::simd::min);
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    return min;
}

JNI_METHOD(jdouble, unsafeMax)(JNIEnv *env, jobject,
                               jdoubleArray jsrc, jint src_offset,
                               jint length)
{
    jdouble *src = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc, NULL));
    jdouble min = boost::simd::accumulate(
        src + src_offset, src + src_offset + length,
        boost::simd::Minf<jdouble>(), boost::simd::max);
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    return min;
}
