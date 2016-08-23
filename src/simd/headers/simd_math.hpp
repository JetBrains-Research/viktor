#pragma once

#include <boost/align/is_aligned.hpp>
#include <boost/simd/algorithm.hpp>
#include <boost/simd/constant/zero.hpp>
#include <boost/simd/constant/minf.hpp>
#include <boost/simd/function/aligned_load.hpp>
#include <boost/simd/function/exp.hpp>
#include <boost/simd/function/log.hpp>
#include <boost/simd/function/sum.hpp>
#include <boost/simd/pack.hpp>

#include "source.hpp"
#include "summing.hpp"

typedef boost::simd::pack<double> pack_double;

namespace simdmath {

BOOST_SYMBOL_EXPORT
double logsumexp(double const *src, size_t length)
{
    using boost::alignment::is_aligned;
    using boost::simd::aligned_load;
    size_t const vector_size = pack_double::static_size;

    double offset = boost::simd::reduce(
        src, src + length,
        boost::simd::Minf<double>(),
        boost::simd::max,
        boost::simd::Minf<jdouble>());
    pack_double voffset(offset);

    double acc = 0.;
    while (length && !is_aligned(src, pack_double::alignment)) {
        acc += boost::simd::exp(*(src++) - offset);
        --length;
    }
    while (length % vector_size) {
        --length;
        acc += boost::simd::exp(src[length] - offset);
    }
    pack_double vacc = boost::simd::Zero<pack_double>();
    for (size_t i = 0; i < length; i += vector_size) {
        vacc += boost::simd::exp(aligned_load<pack_double>(src, i) - voffset);
    }

    return boost::simd::log(acc + boost::simd::sum(vacc)) + offset;
}

BOOST_SYMBOL_EXPORT
double dot(double const *src1, double const *src2, size_t length)
{
    source_1d<weighted_sum_tag> f
        = source_1d<weighted_sum_tag>(src1, src2, length);
    return balanced_sum(f);
}

}  /* ::simdmath */
