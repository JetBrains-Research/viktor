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
jboolean transformAssign(JNIEnv *env,
                         jdoubleArray jdst, jint dst_offset,
                         jdoubleArray jsrc, jint src_offset,
                         jint length, BinOp const& op)
{
    jdouble *src = reinterpret_cast<jdouble *>(
            env->GetPrimitiveArrayCritical(jsrc, NULL));
    jboolean dst_is_copy = JNI_FALSE;
    jdouble *dst = reinterpret_cast<jdouble *>(
            env->GetPrimitiveArrayCritical(jdst, &dst_is_copy));
    if (dst_is_copy == JNI_TRUE) return JNI_FALSE;
    boost::simd::transform(dst + dst_offset,
                           dst + dst_offset + length,
                           src + src_offset,
                           dst + dst_offset,
                           op);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    return JNI_TRUE;
}

JNI_METHOD(jboolean, unsafePlusAssign)(JNIEnv *env, jobject,
                                       jdoubleArray jdst, jint dst_offset,
                                       jdoubleArray jsrc, jint src_offset,
                                       jint length)
{
    return transformAssign(env, jdst, dst_offset, jsrc, src_offset, length, boost::simd::plus);
}

JNI_METHOD(jboolean, unsafeMinusAssign)(JNIEnv *env, jobject,
                                        jdoubleArray jdst, jint dst_offset,
                                        jdoubleArray jsrc, jint src_offset,
                                        jint length)
{
    return transformAssign(env, jdst, dst_offset, jsrc, src_offset, length, boost::simd::minus);
}

JNI_METHOD(jboolean, unsafeTimesAssign)(JNIEnv *env, jobject,
                                        jdoubleArray jdst, jint dst_offset,
                                        jdoubleArray jsrc, jint src_offset,
                                        jint length)
{
    return transformAssign(env, jdst, dst_offset, jsrc, src_offset, length, boost::simd::multiplies);
}

JNI_METHOD(jboolean, unsafeDivAssign)(JNIEnv *env, jobject,
                                      jdoubleArray jdst, jint dst_offset,
                                      jdoubleArray jsrc, jint src_offset,
                                      jint length)
{
    return transformAssign(env, jdst, dst_offset, jsrc, src_offset, length, boost::simd::div);
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
jboolean transformInPlace(JNIEnv *env,
                          jdoubleArray jdst, jint dst_offset,
                          jint length, UnOp const& f)
{
    jboolean is_copy = JNI_FALSE;
    jdouble *dst = reinterpret_cast<jdouble *>(
        env->GetPrimitiveArrayCritical(jdst, &is_copy));
    if (is_copy == JNI_TRUE) return JNI_FALSE;
    boost::simd::transform(dst + dst_offset, dst + dst_offset + length,
                           dst + dst_offset, f);
    env->ReleasePrimitiveArrayCritical(jdst, dst, is_copy == JNI_ABORT);
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

JNI_METHOD(jboolean, unsafePlusScalarAssign)(JNIEnv *env, jobject,
                                         jdoubleArray jdst, jint dst_offset,
                                         jint length, jdouble update)
{
    return transformInPlace(env, jdst, dst_offset, length, plus_scalar(update));
}

JNI_METHOD(jboolean, unsafeMinusScalarAssign)(JNIEnv *env, jobject,
                                          jdoubleArray jdst, jint dst_offset,
                                          jint length, jdouble update)
{
    return transformInPlace(env, jdst, dst_offset, length, minus_scalar(update));
}

JNI_METHOD(jboolean, unsafeTimesScalarAssign)(JNIEnv *env, jobject,
                                          jdoubleArray jdst, jint dst_offset,
                                          jint length, jdouble update)
{
    return transformInPlace(env, jdst, dst_offset, length, multiplies_scalar(update));
}

JNI_METHOD(jboolean, unsafeDivScalarAssign)(JNIEnv *env, jobject,
                                        jdoubleArray jdst, jint dst_offset,
                                        jint length, jdouble update)
{
    return transformInPlace(env, jdst, dst_offset, length, div_scalar(update));
}

JNI_METHOD(jboolean, unsafeScalarDivAssign)(JNIEnv *env, jobject,
                                        jdoubleArray jdst, jint dst_offset,
                                        jint length, jdouble update)
{
    return transformInPlace(env, jdst, dst_offset, length, scalar_div(update));
}

JNI_METHOD(jboolean, unsafeNegateInPlace)(JNIEnv *env, jobject,
                                      jdoubleArray jdst, jint dst_offset,
                                      jint length)
{
    return transformInPlace(env, jdst, dst_offset, length, boost::simd::unary_minus);
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

JNI_METHOD(jboolean, unsafeExpInPlace)(JNIEnv *env, jobject,
                                   jdoubleArray jdst, jint dst_offset,
                                   jint length)
{
    return transformInPlace(env, jdst, dst_offset, length, boost::simd::exp);
}


JNI_METHOD(jboolean, unsafeExpm1InPlace)(JNIEnv *env, jobject,
                                     jdoubleArray jdst, jint dst_offset,
                                     jint length)
{
    return transformInPlace(env, jdst, dst_offset, length, boost::simd::expm1);
}


JNI_METHOD(jboolean, unsafeLogInPlace)(JNIEnv *env, jobject,
                                   jdoubleArray jdst, jint dst_offset,
                                   jint length)
{
    return transformInPlace(env, jdst, dst_offset, length, boost::simd::log);
}

JNI_METHOD(jboolean, unsafeLog1pInPlace)(JNIEnv *env, jobject,
                                     jdoubleArray jdst, jint dst_offset,
                                     jint length)
{
    return transformInPlace(env, jdst, dst_offset, length, boost::simd::log1p);
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
                                      jdoubleArray jsrc, jint src_offset,
                                      jint length)
{
    return transformAssign(env, jdst, dst_offset, jsrc, src_offset, length, logaddexp());
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
