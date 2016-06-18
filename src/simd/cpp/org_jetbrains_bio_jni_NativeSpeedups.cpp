#include <boost/simd/constant/minf.hpp>
#include <boost/simd/function/dot.hpp>
#include <boost/simd/function/exp.hpp>
#include <boost/simd/function/expm1.hpp>
#include <boost/simd/function/if_else.hpp>
#include <boost/simd/function/log.hpp>
#include <boost/simd/function/log1p.hpp>
#include <boost/simd/function/max.hpp>
#include <boost/simd/function/min.hpp>

#include "org_jetbrains_bio_jni_NativeSpeedups.hpp"
#include "simd_math.hpp"
#include "source.hpp"
#include "summing.hpp"
#include "transform_accumulate.hpp"

#define JNI_METHOD(rtype, name)                                         \
    JNIEXPORT rtype JNICALL Java_org_jetbrains_bio_jni_NativeSpeedups_##name

JNI_METHOD(void, unsafePlus)(JNIEnv *env, jobject,
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

JNI_METHOD(void, unsafeMinus)(JNIEnv *env, jobject,
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

JNI_METHOD(void, unsafeTimes)(JNIEnv *env, jobject,
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

JNI_METHOD(void, unsafeDiv)(JNIEnv *env, jobject,
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

namespace {

struct plus_scalar {
    plus_scalar(double const update) : update_(update) {};

    template <typename T>
    BOOST_FORCEINLINE T operator()(T const &x) const { return x + update_; }

private:
    double update_;
};

struct minus_scalar {
    minus_scalar(double const update) : update_(update) {};

    template <typename T>
    BOOST_FORCEINLINE T operator()(T const &x) const { return x - update_; }

private:
    double update_;
};

struct multiplies_scalar {
    multiplies_scalar(double const update) : update_(update) {};

    template <typename T>
    BOOST_FORCEINLINE T operator()(T const &x) const { return x * update_; }

private:
    double update_;
};

struct div_scalar {
    div_scalar(double const update) : update_(update) {};

    template <typename T>
    BOOST_FORCEINLINE T operator()(T const &x) const { return x / update_; }

private:
    double update_;
};

}

JNI_METHOD(void, unsafePlusScalar)(JNIEnv *env, jobject,
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
                           plus_scalar(update));
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
}

JNI_METHOD(void, unsafeMinusScalar)(JNIEnv *env, jobject,
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
                           minus_scalar(update));
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
}

JNI_METHOD(void, unsafeTimesScalar)(JNIEnv *env, jobject,
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
                           multiplies_scalar(update));
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
}

JNI_METHOD(void, unsafeDivScalar)(JNIEnv *env, jobject,
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
                           div_scalar(update));
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
}

JNI_METHOD(void, unsafeNegate)(JNIEnv *env, jobject,
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

JNI_METHOD(void, unsafeExp)(JNIEnv *env, jobject,
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


JNI_METHOD(void, unsafeExpm1)(JNIEnv *env, jobject,
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


JNI_METHOD(void, unsafeLog)(JNIEnv *env, jobject,
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

JNI_METHOD(void, unsafeLog1p)(JNIEnv *env, jobject,
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

JNI_METHOD(void, unsafeLogAddExp)(JNIEnv *env, jobject,
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
                           logaddexp());
    env->ReleasePrimitiveArrayCritical(jsrc1, src1, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jsrc2, src2, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
}


JNI_METHOD(void, unsafeLogRescale)(JNIEnv *env, jobject,
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
                           minus_scalar(total));
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

JNI_METHOD(jdouble, sum)(JNIEnv *env, jobject,
                         jdoubleArray jvalues, jint offset, jint length)
{
    jdouble *values = (jdouble *) env->GetPrimitiveArrayCritical(jvalues, NULL);
    source_1d<sum_tag> f(values + offset, length);
    double res = balanced_sum(f);
    env->ReleasePrimitiveArrayCritical(jvalues, values, JNI_ABORT);
    return res;
}

JNI_METHOD(jdouble, weightedSum)(JNIEnv *env, jobject,
                                 jdoubleArray jvalues, jint values_offset,
                                 jdoubleArray jweights, jint weights_offset,
                                 jint length)
{
    jdouble *values = (jdouble *) env->GetPrimitiveArrayCritical(jvalues, NULL);
    jdouble *weights = (jdouble *) env->GetPrimitiveArrayCritical(jweights, NULL);
    source_1d<weighted_sum_tag> f(values + values_offset,
                                  weights + weights_offset,
                                  length);
    double res = balanced_sum(f);
    env->ReleasePrimitiveArrayCritical(jvalues, values, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jweights, weights, JNI_ABORT);
    return res;
}

JNI_METHOD(jdouble, weightedMean)(JNIEnv *env, jobject,
                                  jdoubleArray jvalues, jint values_offset,
                                  jdoubleArray jweights, jint weights_offset,
                                  jint length)
{
    jdouble *values = (jdouble *) env->GetPrimitiveArrayCritical(jvalues, NULL);
    jdouble *weights = (jdouble *) env->GetPrimitiveArrayCritical(jweights, NULL);
    source_2d<weighted_mean_tag> f(values + values_offset,
                                   weights + weights_offset,
                                   length);
    double res = twin_balanced_sum(f);
    env->ReleasePrimitiveArrayCritical(jvalues, values, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jweights, weights, JNI_ABORT);
    return res;
}

JNI_METHOD(jdouble, sd)(JNIEnv *env, jobject,
                        jdoubleArray jvalues, jint offset,
                        jint length)
{
    jdouble *values = (jdouble *) env->GetPrimitiveArrayCritical(jvalues, NULL);
    source_2d<sd_tag> f(values + offset, length);
    double res = twin_balanced_sum(f);
    env->ReleasePrimitiveArrayCritical(jvalues, values, JNI_ABORT);
    return res;
}

JNI_METHOD(jdouble, weightedSd)(JNIEnv *env, jobject,
                                jdoubleArray jvalues, jint values_offset,
                                jdoubleArray jweights, jint weights_offset,
                                jint length)
{
    jdouble *values = (jdouble *) env->GetPrimitiveArrayCritical(jvalues, NULL);
    jdouble *weights = (jdouble *) env->GetPrimitiveArrayCritical(jweights, NULL);
    source_3d<weighted_sd_tag> f(values + values_offset,
                                 weights + weights_offset,
                                 length);
    double res = tri_balanced_sum(f);
    env->ReleasePrimitiveArrayCritical(jvalues, values, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jweights, weights, JNI_ABORT);
    return res;
}

JNI_METHOD(void, prefixSum)(JNIEnv *env, jobject,
                            jdoubleArray jsrc, jint src_offset,
                            jdoubleArray jdst, jint dst_offset,
                            jint length)
{
    jdouble *src = (jdouble *) env->GetPrimitiveArrayCritical(jsrc, NULL);
    jdouble *dst = (jdouble *) env->GetPrimitiveArrayCritical(jdst, NULL);
    source_1d<cum_sum_tag> f(src + src_offset, dst + dst_offset, length);
    cum_sum(f);
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
}
