#include <boost/simd/algorithm.hpp>
#include <boost/simd/constant/minf.hpp>
#include <boost/simd/function/dot.hpp>
#include <boost/simd/function/exp.hpp>
#include <boost/simd/function/expm1.hpp>
#include <boost/simd/function/if_else.hpp>
#include <boost/simd/function/log.hpp>
#include <boost/simd/function/log1p.hpp>
#include <boost/simd/function/max.hpp>
#include <boost/simd/function/min.hpp>

#include "org_jetbrains_bio_viktor_NativeSpeedups.hpp"
#include "simd_math.hpp"
#include "source.hpp"
#include "summing.hpp"

#define JNI_METHOD(rtype, name)                                         \
    JNIEXPORT rtype JNICALL Java_org_jetbrains_bio_viktor_NativeSpeedups_##name

template<typename UnOp>
jboolean transformTo(JNIEnv *env,
                     jdoubleArray jdst, jint dst_offset,
                     jdoubleArray jsrc, jint src_offset,
                     jint length, UnOp const& op)
{
    jboolean dst_is_copy = JNI_FALSE;
    jdouble *dst = reinterpret_cast<jdouble *>(
            env->GetPrimitiveArrayCritical(jdst, &dst_is_copy));
    if (dst_is_copy == JNI_TRUE) return JNI_FALSE;
    jdouble *src = reinterpret_cast<jdouble *>(
            env->GetPrimitiveArrayCritical(jsrc, NULL));
    boost::simd::transform(src + src_offset,
                           src + src_offset + length,
                           dst + dst_offset,
                           op);
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
    return JNI_TRUE;
}

template<typename Fold, typename Empty>
jdouble reduce(JNIEnv *env,
               jdoubleArray jsrc, jint src_offset,
               jint length, Fold const& f, Empty const& e)
{
    jdouble *src = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc, NULL));
    jdouble res = boost::simd::reduce(
        src + src_offset, src + src_offset + length, e, f, e);
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    return res;
}

JNI_METHOD(jdouble, unsafeMin)(JNIEnv *env, jobject,
                               jdoubleArray jsrc, jint src_offset,
                               jint length)
{
    return reduce(env, jsrc, src_offset, length, boost::simd::min, boost::simd::Inf<jdouble>());
}

JNI_METHOD(jdouble, unsafeMax)(JNIEnv *env, jobject,
                               jdoubleArray jsrc, jint src_offset,
                               jint length)
{
    return reduce(env, jsrc, src_offset, length, boost::simd::max, boost::simd::Minf<jdouble>());
}

JNI_METHOD(jboolean, unsafeExp)(JNIEnv *env, jobject,
                                jdoubleArray jdst, jint dst_offset,
                                jdoubleArray jsrc, jint src_offset,
                                jint length)
{
    return transformTo(env, jdst, dst_offset, jsrc, src_offset, length, boost::simd::exp);
}


JNI_METHOD(jboolean, unsafeExpm1)(JNIEnv *env, jobject,
                                  jdoubleArray jdst, jint dst_offset,
                                  jdoubleArray jsrc, jint src_offset,
                                  jint length)
{
    return transformTo(env, jdst, dst_offset, jsrc, src_offset, length, boost::simd::expm1);
}

JNI_METHOD(jboolean, unsafeLog)(JNIEnv *env, jobject,
                                jdoubleArray jdst, jint dst_offset,
                                jdoubleArray jsrc, jint src_offset,
                                jint length)
{
    return transformTo(env, jdst, dst_offset, jsrc, src_offset, length, boost::simd::log);
}

JNI_METHOD(jboolean, unsafeLog1p)(JNIEnv *env, jobject,
                                  jdoubleArray jdst, jint dst_offset,
                                  jdoubleArray jsrc, jint src_offset,
                                  jint length)
{
    return transformTo(env, jdst, dst_offset, jsrc, src_offset, length, boost::simd::log1p);
}

JNI_METHOD(jdouble, unsafeLogSumExp)(JNIEnv *env, jobject,
                                     jdoubleArray jsrc, jint offset,
                                     jint length)
{
    jdouble *src = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc, NULL));
    double res = simdmath::logsumexp(src + offset, length);
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    return res;
}

namespace {

struct logaddexp {
    template <typename T>
    BOOST_FORCEINLINE T operator() (T const &x, T const &y) const {
        T const min = boost::simd::min(x, y);
        T const max = boost::simd::max(x, y);
        T res = max + boost::simd::log1p(boost::simd::exp(min - max));
        res = boost::simd::if_else(x == boost::simd::Minf<T>(), y, res);
        res = boost::simd::if_else(y == boost::simd::Minf<T>(), x, res);
        return res;
    }
};

}

JNI_METHOD(jboolean, unsafeLogAddExp)(JNIEnv *env, jobject,
                                      jdoubleArray jdst, jint dst_offset,
                                      jdoubleArray jsrc1, jint src1_offset,
                                      jdoubleArray jsrc2, jint src2_offset,
                                      jint length)
{
    jboolean dst_is_copy = JNI_FALSE;
    jdouble *dst = reinterpret_cast<jdouble *>(
            env->GetPrimitiveArrayCritical(jdst, &dst_is_copy));
    if (dst_is_copy == JNI_TRUE) return JNI_FALSE;
    jdouble *src1 = reinterpret_cast<jdouble *>(
            env->GetPrimitiveArrayCritical(jsrc1, NULL));
    jdouble *src2 = reinterpret_cast<jdouble *>(
            env->GetPrimitiveArrayCritical(jsrc2, NULL));
    boost::simd::transform(src1 + src1_offset,
                           src1 + src1_offset + length,
                           src2 + src2_offset,
                           dst + dst_offset,
                           logaddexp());
    env->ReleasePrimitiveArrayCritical(jsrc2, src2, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jsrc1, src1, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
    return JNI_TRUE;
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

JNI_METHOD(jdouble, unsafeSum)(JNIEnv *env, jobject,
                               jdoubleArray jvalues, jint offset, jint length)
{
    jdouble *values = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jvalues, NULL));
    source_1d<sum_tag> f(values + offset, length);
    double res = balanced_sum(f);
    env->ReleasePrimitiveArrayCritical(jvalues, values, JNI_ABORT);
    return res;
}

JNI_METHOD(jdouble, unsafeSD)(JNIEnv *env, jobject,
                              jdoubleArray jvalues, jint offset,
                              jint length)
{
    jdouble *values = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jvalues, NULL));
    source_2d<sd_tag> f(values + offset, length);
    double res = twin_balanced_sum(f);
    env->ReleasePrimitiveArrayCritical(jvalues, values, JNI_ABORT);
    return res;
}

JNI_METHOD(jboolean, unsafeCumSum)(JNIEnv *env, jobject,
                                   jdoubleArray jdst, jint dst_offset,
                                   jint length)
{
    jboolean is_copy = JNI_FALSE;
    jdouble *dst = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jdst, &is_copy));
    if (is_copy == JNI_TRUE) return JNI_FALSE;
    source_1d<cum_sum_tag> f(dst + dst_offset, dst + dst_offset, length);
    cum_sum(f);
    env->ReleasePrimitiveArrayCritical(jdst, dst, is_copy == JNI_TRUE ? 0 : JNI_ABORT);
    return JNI_TRUE;
}
