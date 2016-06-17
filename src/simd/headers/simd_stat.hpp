#pragma once

#include <boost/simd/function/cumsum.hpp>
#include <boost/simd/function/splat.hpp>

#include "source.hpp"
#include "summing.hpp"

namespace simdstat {

BOOST_SYMBOL_EXPORT
double sum(double const *src, size_t length)
{
    one_dimension_source<sum_tag> f = one_dimension_source<sum_tag>(src, length);
    return balanced_sum(f);
}


BOOST_SYMBOL_EXPORT
double weighted_sum(double const *array, double const *weights, size_t length)
{
    one_dimension_source<weighted_sum_tag> f
        = one_dimension_source<weighted_sum_tag>(array, weights, length);
    return balanced_sum(f);
}


BOOST_SYMBOL_EXPORT
double weighted_mean(double const *array, double const *weights, size_t length)
{
    two_dimension_source<weighted_mean_tag> f
        = two_dimension_source<weighted_mean_tag>(array, weights, length);
    return twin_balanced_sum(f);
}


BOOST_SYMBOL_EXPORT
double standard_deviation(double const *array, size_t length)
{
    two_dimension_source<standard_deviation_tag> f
        = two_dimension_source<standard_deviation_tag>(array, length);
    return twin_balanced_sum(f);
}


BOOST_SYMBOL_EXPORT
double weighted_sd(double const *array, double const *weights, size_t length)
{
    three_dimension_source<weighted_sd_tag> f
        = three_dimension_source<weighted_sd_tag>(array, weights, length);
    return tri_balanced_sum(f);
}

BOOST_SYMBOL_EXPORT
void cum_sum(double const *src, double *dst, size_t length)
{
    one_dimension_source<cum_sum_tag> f
        = one_dimension_source<cum_sum_tag>(src, dst, length);
    cum_sum(f);
}

}  /* ::simdstat */
