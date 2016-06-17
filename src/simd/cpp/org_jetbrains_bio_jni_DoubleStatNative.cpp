#include "org_jetbrains_bio_jni_DoubleStatNative.hpp"
#include "simd_stat.hpp"

#define JNI_METHOD(rtype, name)                 \
    JNIEXPORT rtype JNICALL Java_org_jetbrains_bio_jni_DoubleStatNative_##name

JNI_METHOD(jdouble, sum)(JNIEnv *env, jobject,
                         jdoubleArray jvalues, jint offset, jint length)
{
    jdouble *values = (jdouble *) env->GetPrimitiveArrayCritical(jvalues, NULL);
    double res = simdstat::sum(values + offset, length);
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
    double res = simdstat::weighted_sum(values + values_offset,
                                        weights + weights_offset, length);
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
    double res = simdstat::weighted_mean(values + values_offset,
                                         weights + weights_offset, length);
    env->ReleasePrimitiveArrayCritical(jvalues, values, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jweights, weights, JNI_ABORT);
    return res;
}

JNI_METHOD(jdouble, standardDeviation)(JNIEnv *env, jobject,
                                       jdoubleArray jvalues, jint offset,
                                       jint length)
{
    jdouble *values = (jdouble *) env->GetPrimitiveArrayCritical(jvalues, NULL);
    double res = simdstat::standard_deviation(values + offset, length);
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
    double res = simdstat::weighted_sd(values + values_offset,
                                       weights + weights_offset, length);
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
    simdstat::cum_sum(src + src_offset, dst + dst_offset, length);
    env->ReleasePrimitiveArrayCritical(jsrc, src, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(jdst, dst, JNI_ABORT);
}
