#include <boost/simd/function/dot.hpp>
#include <boost/simd/function/exp.hpp>
#include <boost/simd/function/expm1.hpp>
#include <boost/simd/function/log.hpp>
#include <boost/simd/function/log1p.hpp>

#include "org_jetbrains_bio_jni_DoubleMathNative.hpp"
#include "simd_math.hpp"
#include "simd_ops.hpp"
#include "transform_accumulate.hpp"

#define JNI_METHOD(rtype, name)                                         \
    JNIEXPORT rtype JNICALL Java_org_jetbrains_bio_jni_DoubleMathNative_##name

JNI_METHOD(void, criticalExp)(JNIEnv *env, jobject,
                              jdoubleArray jsrc, jint src_offset,
                              jdoubleArray jdst, jint dst_offset,
                              jint length)
{
    jdouble *src = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc, NULL));
    jdouble *dst = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jdst, NULL));
    boost::simd::transform(src + src_offset, src + src_offset + length,
                           dst + dst_offset, boost::simd::exp);
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
}


JNI_METHOD(void, criticalExpm1)(JNIEnv *env, jobject,
                                jdoubleArray jsrc, jint src_offset,
                                jdoubleArray jdst, jint dst_offset,
                                jint length)
{
    jdouble *src = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc, NULL));
    jdouble *dst = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jdst, NULL));
    boost::simd::transform(src + src_offset, src + src_offset + length,
                           dst + dst_offset, boost::simd::expm1);
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
}


JNI_METHOD(void, criticalLog)(JNIEnv *env, jobject,
                              jdoubleArray jsrc, jint src_offset,
                              jdoubleArray jdst, jint dst_offset,
                              jint length)
{
    jdouble *src = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc, NULL));
    jdouble *dst = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jdst, NULL));
    boost::simd::transform(src + src_offset, src + src_offset + length,
                           dst + dst_offset, boost::simd::log);
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
}

JNI_METHOD(void, criticalLog1p)(JNIEnv *env, jobject,
                                jdoubleArray jsrc, jint src_offset,
                                jdoubleArray jdst, jint dst_offset,
                                jint length)
{
    jdouble *src = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc, NULL));
    jdouble *dst = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jdst, NULL));
    boost::simd::transform(src + src_offset, src + src_offset + length,
                           dst + dst_offset, boost::simd::log1p);
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
}


JNI_METHOD(jdouble, criticalLogSumExp)(JNIEnv *env, jobject,
                                       jdoubleArray jsrc, jint offset,
                                       jint length)
{
    jdouble *src = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc, NULL));
    double res = simdmath::logsumexp(src + offset, length);
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    return res;
}


JNI_METHOD(void, criticalLogAddExp)(JNIEnv *env, jobject,
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
                           simdops::logaddexp());
    env->ReleasePrimitiveArrayCritical(jsrc1, src1, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jsrc2, src2, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
}


JNI_METHOD(void, criticalLogRescale)(JNIEnv *env, jobject,
                                     jdoubleArray jsrc, jint src_offset,
                                     jdoubleArray jdst, jint dst_offset,
                                     jint length)
{
    jdouble *src = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc, NULL));
    jdouble *dst = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jdst, NULL));
    double total = simdmath::logsumexp(src + src_offset, length);
    boost::simd::transform(src + src_offset,
                           src + src_offset + length,
                           dst + dst_offset,
                           simdops::minus(total));
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
}

JNI_METHOD(jdouble, unsafeDot)(JNIEnv *env, jobject,
                               jdoubleArray jsrc1, jint src_offset1,
                               jdoubleArray jsrc2, jint src_offset2,
                               jint length)
{
    jdouble *src1 = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc1, NULL));
    jdouble *src2 = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc2, NULL));
    jdouble res = simdmath::dot(src1 + src_offset1, src2 + src_offset2, length);
    env->ReleasePrimitiveArrayCritical(jsrc1, src1, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jsrc2, src2, JNI_ABORT);
    return res;
}
