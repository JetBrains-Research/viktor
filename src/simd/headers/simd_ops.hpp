#pragma once

#include <boost/simd/constant/minf.hpp>
#include <boost/simd/function/exp.hpp>
#include <boost/simd/function/if_else.hpp>
#include <boost/simd/function/log1p.hpp>
#include <boost/simd/function/max.hpp>
#include <boost/simd/function/min.hpp>
#include <boost/simd/pack.hpp>

namespace simdops {

struct plus {
    plus(double const update) : update_(update) {};

    template <typename T>
    BOOST_FORCEINLINE T operator()(T const &x) const {
        return x + update_;
    }

private:
    double update_;
};

struct minus {
    minus(double const update) : update_(update) {};

    template <typename T>
    BOOST_FORCEINLINE T operator()(T const &x) const {
        return x - update_;
    }

private:
    double update_;
};

struct multiplies {
    multiplies(double const update) : update_(update) {};

    template <typename T>
    BOOST_FORCEINLINE T operator()(T const &x) const {
        return x * update_;
    }

private:
    double update_;
};

struct div {
    div(double const update) : update_(update) {};

    template <typename T>
    BOOST_FORCEINLINE T operator()(T const &x) const {
        return x / update_;
    }

private:
    double update_;
};

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

}  /* ::simdops */
