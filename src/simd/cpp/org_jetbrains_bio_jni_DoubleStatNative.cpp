#include "org_jetbrains_bio_jni_DoubleStatNative.hpp"
#include "source.hpp"
#include "summing.hpp"

#define JNI_METHOD(rtype, name)                 \
    JNIEXPORT rtype JNICALL Java_org_jetbrains_bio_jni_DoubleStatNative_##name

JNI_METHOD(jdouble, sum)(JNIEnv *env, jobject,
                         jdoubleArray jvalues, jint offset, jint length)
{
    jdouble *values = (jdouble *) env->GetPrimitiveArrayCritical(jvalues, NULL);
    one_dimension_source<sum_tag> f(values + offset, length);
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
    one_dimension_source<weighted_sum_tag> f(values + values_offset,
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
    two_dimension_source<weighted_mean_tag> f(values + values_offset,
                                              weights + weights_offset,
                                              length);
    double res = twin_balanced_sum(f);
    env->ReleasePrimitiveArrayCritical(jvalues, values, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jweights, weights, JNI_ABORT);
    return res;
}

JNI_METHOD(jdouble, standardDeviation)(JNIEnv *env, jobject,
                                       jdoubleArray jvalues, jint offset,
                                       jint length)
{
    jdouble *values = (jdouble *) env->GetPrimitiveArrayCritical(jvalues, NULL);
    two_dimension_source<standard_deviation_tag> f(
        values + offset, length);
    double res = twin_balanced_sum(f);
    env->ReleasePrimitiveArrayCritical(jvalues, values, JNI_ABORT);
    return res;
}

JNI_METHOD(jdouble, weightedSD)(JNIEnv *env, jobject,
                                jdoubleArray jvalues, jint values_offset,
                                jdoubleArray jweights, jint weights_offset,
                                jint length)
{
    jdouble *values = (jdouble *) env->GetPrimitiveArrayCritical(jvalues, NULL);
    jdouble *weights = (jdouble *) env->GetPrimitiveArrayCritical(jweights, NULL);
    three_dimension_source<weighted_sd_tag> f(values + values_offset,
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
    one_dimension_source<cum_sum_tag> f(
        src + src_offset, dst + dst_offset, length);
    cum_sum(f);
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
}
