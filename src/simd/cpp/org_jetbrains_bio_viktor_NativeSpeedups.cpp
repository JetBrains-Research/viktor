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

template<typename BinOp>
void transformAssign(JNIEnv *env,
                     jdoubleArray jdst, jint dst_offset,
                     jdoubleArray jsrc, jint src_offset,
                     jint length, BinOp const& op)
{
    jdouble *src = reinterpret_cast<jdouble *>(
            env->GetPrimitiveArrayCritical(jsrc, NULL));
    jboolean dst_is_copy = JNI_FALSE;
    jdouble *dst = reinterpret_cast<jdouble *>(
            env->GetPrimitiveArrayCritical(jdst, &dst_is_copy));
    boost::simd::transform(dst + dst_offset,
                           dst + dst_offset + length,
                           src + src_offset,
                           dst + dst_offset,
                           op);
    env->ReleasePrimitiveArrayCritical(jdst, dst, dst_is_copy == JNI_TRUE ? 0 : JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
}

JNI_METHOD(void, unsafePlusAssign)(JNIEnv *env, jobject,
                                   jdoubleArray jdst, jint dst_offset,
                                   jdoubleArray jsrc, jint src_offset,
                                   jint length)
{
    transformAssign(env, jdst, dst_offset, jsrc, src_offset, length, boost::simd::plus);
}

JNI_METHOD(void, unsafeMinusAssign)(JNIEnv *env, jobject,
                                    jdoubleArray jdst, jint dst_offset,
                                    jdoubleArray jsrc, jint src_offset,
                                    jint length)
{
    transformAssign(env, jdst, dst_offset, jsrc, src_offset, length, boost::simd::minus);
}

JNI_METHOD(void, unsafeTimesAssign)(JNIEnv *env, jobject,
                                    jdoubleArray jdst, jint dst_offset,
                                    jdoubleArray jsrc, jint src_offset,
                                    jint length)
{
    transformAssign(env, jdst, dst_offset, jsrc, src_offset, length, boost::simd::multiplies);
}

JNI_METHOD(void, unsafeDivAssign)(JNIEnv *env, jobject,
                                  jdoubleArray jdst, jint dst_offset,
                                  jdoubleArray jsrc, jint src_offset,
                                  jint length)
{
    transformAssign(env, jdst, dst_offset, jsrc, src_offset, length, boost::simd::div);
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

struct scalar_div {
    scalar_div(double const update) : update_(update) {};

    template <typename T>
    BOOST_FORCEINLINE T operator()(T const &x) const { return update_ / x; }

private:
    double update_;
};

}

template<typename UnOp>
void transformInPlace(JNIEnv *env,
                      jdoubleArray jdst, jint dst_offset,
                      jint length, UnOp const& f)
{
    jboolean is_copy = JNI_FALSE;
    jdouble *dst = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jdst, &is_copy));
    boost::simd::transform(dst + dst_offset, dst + dst_offset + length,
                           dst + dst_offset, f);
    env->ReleasePrimitiveArrayCritical(jdst, dst, is_copy == JNI_TRUE ? 0 : JNI_ABORT);
}

JNI_METHOD(void, unsafePlusScalarAssign)(JNIEnv *env, jobject,
                                         jdoubleArray jdst, jint dst_offset,
                                         jint length, jdouble update)
{
    transformInPlace(env, jdst, dst_offset, length, plus_scalar(update));
}

JNI_METHOD(void, unsafeMinusScalarAssign)(JNIEnv *env, jobject,
                                          jdoubleArray jdst, jint dst_offset,
                                          jint length, jdouble update)
{
    transformInPlace(env, jdst, dst_offset, length, minus_scalar(update));
}

JNI_METHOD(void, unsafeTimesScalarAssign)(JNIEnv *env, jobject,
                                          jdoubleArray jdst, jint dst_offset,
                                          jint length, jdouble update)
{
    transformInPlace(env, jdst, dst_offset, length, multiplies_scalar(update));
}

JNI_METHOD(void, unsafeDivScalarAssign)(JNIEnv *env, jobject,
                                        jdoubleArray jdst, jint dst_offset,
                                        jint length, jdouble update)
{
    transformInPlace(env, jdst, dst_offset, length, div_scalar(update));
}

JNI_METHOD(void, unsafeScalarDivAssign)(JNIEnv *env, jobject,
                                        jdoubleArray jdst, jint dst_offset,
                                        jint length, jdouble update)
{
    transformInPlace(env, jdst, dst_offset, length, scalar_div(update));
}

JNI_METHOD(void, unsafeNegateInPlace)(JNIEnv *env, jobject,
                                      jdoubleArray jdst, jint dst_offset,
                                      jint length)
{
    transformInPlace(env, jdst, dst_offset, length, boost::simd::unary_minus);
}

JNI_METHOD(jdouble, unsafeMin)(JNIEnv *env, jobject,
                               jdoubleArray jsrc, jint src_offset,
                               jint length)
{
    jdouble *src = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc, NULL));
    jdouble min = boost::simd::reduce(
        src + src_offset, src + src_offset + length,
        boost::simd::Inf<jdouble>(), boost::simd::min,
        boost::simd::Inf<jdouble>());
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    return min;
}

JNI_METHOD(jdouble, unsafeMax)(JNIEnv *env, jobject,
                               jdoubleArray jsrc, jint src_offset,
                               jint length)
{
    jdouble *src = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jsrc, NULL));
    jdouble min = boost::simd::reduce(
        src + src_offset, src + src_offset + length,
        boost::simd::Minf<jdouble>(), boost::simd::max,
        boost::simd::Minf<jdouble>());
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    return min;
}

JNI_METHOD(void, unsafeExpInPlace)(JNIEnv *env, jobject,
                                   jdoubleArray jdst, jint dst_offset,
                                   jint length)
{
    transformInPlace(env, jdst, dst_offset, length, boost::simd::exp);
}


JNI_METHOD(void, unsafeExpm1InPlace)(JNIEnv *env, jobject,
                                     jdoubleArray jdst, jint dst_offset,
                                     jint length)
{
    transformInPlace(env, jdst, dst_offset, length, boost::simd::expm1);
}


JNI_METHOD(void, unsafeLogInPlace)(JNIEnv *env, jobject,
                                   jdoubleArray jdst, jint dst_offset,
                                   jint length)
{
    transformInPlace(env, jdst, dst_offset, length, boost::simd::log);
}

JNI_METHOD(void, unsafeLog1pInPlace)(JNIEnv *env, jobject,
                                     jdoubleArray jdst, jint dst_offset,
                                     jint length)
{
    transformInPlace(env, jdst, dst_offset, length, boost::simd::log1p);
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
                                  jdoubleArray jdst, jint dst_offset,
                                  jdoubleArray jsrc, jint src_offset,
                                  jint length)
{
    transformAssign(env, jdst, dst_offset, jsrc, src_offset, length, logaddexp());
}


JNI_METHOD(void, unsafeLogRescale)(JNIEnv *env, jobject,
                                   jdoubleArray jdst, jint dst_offset,
                                   jint length)
{
    jboolean is_copy = JNI_FALSE;
    jdouble *dst = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jdst, &is_copy));
    double total = simdmath::logsumexp(dst + dst_offset, length);
    boost::simd::transform(dst + dst_offset,
                           dst + dst_offset + length,
                           dst + dst_offset,
                           minus_scalar(total));
    env->ReleasePrimitiveArrayCritical(jdst, dst, is_copy == JNI_TRUE ? 0 : JNI_ABORT);
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

JNI_METHOD(void, cumSum)(JNIEnv *env, jobject,
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
