#pragma once

#include <numeric>

#include <boost/simd/constant/zero.hpp>
#include <boost/simd/function/cumsum.hpp>
#include <boost/simd/function/splat.hpp>
#include <boost/simd/function/sum.hpp>

#include "source.hpp"

typedef boost::simd::pack<double> pack_double;

template<typename tag>
double balanced_sum(source_1d<tag> &f)
{
    double res = 0.;
    while (f.can_procure(1, res) && !f.is_aligned()) {
        f.procure(res);
    }

    pack_double stack[62];
    size_t p = 0;
    for (size_t iteration = 0; f.can_procure(4, stack[0]); ++iteration) {
        pack_double v = boost::simd::Zero<pack_double>();
        f.procure(v);
        f.procure(v);
        pack_double w = boost::simd::Zero<pack_double>();
        f.procure(w);
        f.procure(w);
        v += w;
        size_t bitmask = 1;
        for (; iteration & bitmask; bitmask <<= 1, --p) {
            v += stack[p - 1];
        }
        stack[p++] = v;
    }
    pack_double vsum = boost::simd::Zero<pack_double>();
    for (size_t i = p; i > 0; --i) {
        vsum += stack[i - 1];
    }
    res = std::accumulate(vsum.begin(), vsum.end(), res);
    while (f.can_procure(1, res)) {
        f.procure(res);
    }
    return res;
}

template<typename tag>
double twin_balanced_sum(source_2d<tag>& f)
{
    double stat1 = 0.;
    double stat2 = 0.;
    while (f.can_procure(1, stat1) && !f.is_aligned()) {
        f.procure(stat1, stat2);
    }

    pack_double stack1[62];
    pack_double stack2[62];
    size_t p = 0;
    for (size_t iteration = 0; f.can_procure(4, stack1[0]); ++iteration) {
        pack_double v1 = boost::simd::Zero<pack_double>();
        pack_double v2 = boost::simd::Zero<pack_double>();
        f.procure(v1, v2);
        f.procure(v1, v2);
        pack_double w1 = boost::simd::Zero<pack_double>();
        pack_double w2 = boost::simd::Zero<pack_double>();
        f.procure(w1, w2);
        f.procure(w1, w2);
        v1 += w1;
        v2 += w2;
        size_t bitmask = 1;
        for (; iteration & bitmask; bitmask <<= 1, --p) {
            v1 += stack1[p - 1];
            v2 += stack2[p - 1];
        }
        stack1[p] = v1;
        stack2[p++] = v2;
    }
    pack_double vsum1 = boost::simd::Zero<pack_double>();
    pack_double vsum2 = boost::simd::Zero<pack_double>();
    for (size_t i = p; i > 0; --i) {
        vsum1 += stack1[i - 1];
        vsum2 += stack2[i - 1];
    }
    stat1 = std::accumulate(vsum1.begin(), vsum1.end(), stat1);
    stat2 = std::accumulate(vsum2.begin(), vsum2.end(), stat2);
    while (f.can_procure(1, stat1)) {
        f.procure(stat1, stat2);
    }
    return f.result(stat1, stat2);
}

inline void kahan_update(double &accumulator, double &compensator, double value)
{
    double const new_accumulator = accumulator + value;
    double const first_option = (accumulator - new_accumulator) + value;
    double const second_option = (value - new_accumulator) + accumulator;
    if (std::abs(accumulator) > std::abs(value)) {
        compensator += first_option;
    } else {
        compensator += second_option;
    }
    accumulator = new_accumulator;
}

template<typename tag>
void cum_sum(source_1d<tag> &f)
{
    double accumulator = 0.;
    double compensator = 0.;
    double value = 0.;
    while (f.can_procure(1, value) && !f.is_aligned()) {
        f.procure(value);
        kahan_update(accumulator, compensator, value);
        f.feed(accumulator + compensator);
    }
    pack_double pack_value1 = boost::simd::Zero<pack_double>();
    pack_double pack_value2 = boost::simd::Zero<pack_double>();
    while (f.can_procure(2, pack_value1)) {
        f.procure(pack_value1);
        f.procure(pack_value2);
        pack_double const pack_cum_sum1 = boost::simd::cumsum(pack_value1);
        pack_double const pack_cum_sum2 = boost::simd::cumsum(pack_value2);
        pack_double const pack_feed_slice1 = pack_cum_sum1 + (accumulator + compensator);
        f.feed(pack_feed_slice1);
        kahan_update(accumulator, compensator, pack_cum_sum1[pack_double::static_size - 1]);
        pack_double const pack_feed_slice2 = pack_cum_sum2 + (accumulator + compensator);
        f.feed(pack_feed_slice2);
        kahan_update(accumulator, compensator, pack_cum_sum2[pack_double::static_size - 1]);
    }
    value = 0.;
    while (f.can_procure(1, accumulator)) {
        f.procure(value);
        kahan_update(accumulator, compensator, value);
        f.feed(accumulator + compensator);
    }
}
