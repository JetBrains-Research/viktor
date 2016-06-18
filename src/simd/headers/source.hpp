#pragma once

#include <cmath>

#include <boost/align/is_aligned.hpp>
#include <boost/simd/function/aligned_load.hpp>
#include <boost/simd/function/load.hpp>
#include <boost/simd/function/plus.hpp>
#include <boost/simd/function/store.hpp>
#include <boost/simd/pack.hpp>

typedef boost::simd::pack<double> pack_double;

struct sum_tag;
struct cum_sum_tag;
struct weighted_sum_tag;

template<typename tag>
struct source_1d;

template<typename T>
size_t static_size() {
    return sizeof(T) / sizeof(double);
}

template<>
struct source_1d<sum_tag>
{
    source_1d<sum_tag>(double const *src, std::size_t length)
        : src_(src), length_(length) {}

    template<typename T>
    void procure(T& container)
    {
        container += boost::simd::aligned_load<T>(src_);
        src_ += static_size<T>();
        length_ -= static_size<T>();
    }

    template<typename T>
    bool can_procure(int num, T const& item) const
    {
        return length_ >= num * static_size<T>();
    }

    bool is_aligned() const
    {
        return boost::alignment::is_aligned(pack_double::alignment, src_);
    }

private:
    double const *src_;
    std::size_t length_;
};

template<>
struct source_1d<weighted_sum_tag>
{
    source_1d<weighted_sum_tag>(double const *array,
                                           double const *weights,
                                           std::size_t length)
        : src_(array), weights_(weights), length_(length) {}

    template<typename T>
    void procure(T& container)
    {
        container += (boost::simd::aligned_load<T>(src_)
                      * boost::simd::load<T>(weights_));
        src_ += static_size<T>();
        weights_ += static_size<T>();
        length_ -= static_size<T>();
    }

    template<typename T>
    bool can_procure(int num, T const& item) const
    {
        return length_ >= num * static_size<T>();
    }

    bool is_aligned() const
    {
        return boost::alignment::is_aligned(pack_double::alignment, src_);
    }

private:
    double const *src_;
    double const *weights_;
    std::size_t length_;
};

template<>
struct source_1d<cum_sum_tag>
{
    source_1d<cum_sum_tag>(double const *src,
                                      double *dst,
                                      std::size_t length)
            : src_(src), dst_(dst), length_(length) {}

    template<typename T>
    void procure(T& container)
    {
        container = boost::simd::aligned_load<T>(src_);
        src_ += static_size<T>();
        length_ -= static_size<T>();
    }

    template<typename T>
    bool can_procure(int num, T const& item) const
    {
        return length_ >= num * static_size<T>();
    }

    bool is_aligned() const
    {
        return boost::alignment::is_aligned(pack_double::alignment, src_);
    }

    template<typename T>
    void feed(T const value)
    {
        // TODO: aligned_store?
        // TODO: error: 'T' does not refer to a value
        boost::simd::store(value, dst_);
        dst_ += static_size<T>();
    }

private:
    double const *src_;
    double *dst_;
    std::size_t length_;
};

struct weighted_mean_tag;
struct sd_tag;

template<typename tag>
struct source_2d;

template<>
struct source_2d<weighted_mean_tag>
{
    source_2d<weighted_mean_tag>(double const* array,
                                            double const* weights,
                                            std::size_t length)
        : array_(array), weights_(weights), length_(length) {}

    template<typename T>
    void procure(T& vw, T& w)
    {
        T const value = boost::simd::aligned_load<T>(array_);
        T const weight = boost::simd::load<T>(weights_);
        vw += value * weight;
        w += weight;
        array_ += static_size<T>();
        weights_ += static_size<T>();
        length_ -= static_size<T>();
    }

    template<typename T>
    bool can_procure(int num, T const& item) const
    {
        return length_ >= num * static_size<T>();
    }

    bool is_aligned() const
    {
        return boost::alignment::is_aligned(pack_double::alignment, array_);
    }

    double result(double vw, double w) const
    {
        return vw / w;
    }

private:
    double const* array_;
    double const *weights_;
    std::size_t length_;
};

template<>
struct source_2d<sd_tag>
{
    source_2d<sd_tag>(double const* array,
                                                 std::size_t length)
        : array_(array), length_(length), initial_length_(length) {}

    template<typename T>
    void procure(T& v2, T& v)
    {
        T const value = boost::simd::aligned_load<T>(array_);
        v2 += value * value;
        v += value;
        array_ += static_size<T>();
        length_ -= static_size<T>();
    }

    template<typename T>
    bool can_procure(int num, T const& item) const
    {
        return length_ >= num * static_size<T>();
    }

    bool is_aligned() const
    {
        return boost::alignment::is_aligned(pack_double::alignment, array_);
    }

    double result(double v2, double v) const
    {
        double const res = (v2 - v * v / initial_length_)
            / (initial_length_ - 1);
        return (res < 0.) ? 0. : sqrt(res);
    }

private:
    double const* array_;
    std::size_t length_;
    std::size_t const initial_length_;
};

template<typename tag>
struct source_3d;

struct weighted_sd_tag;

template<>
struct source_3d<weighted_sd_tag>
{
    source_3d<weighted_sd_tag>(double const* array,
                                            double const* weights,
                                            std::size_t length)
        : array_(array), weights_(weights), length_(length) {}

    template<typename T>
    void procure(T& v2w, T& vw, T& w)
    {
        T const value = boost::simd::aligned_load<T>(array_);
        T const weight = boost::simd::load<T>(weights_);
        T const value_weight = value * weight;
        v2w += value_weight * value;
        vw += value_weight;
        w += weight;
        array_ += static_size<T>();
        weights_ += static_size<T>();
        length_ -= static_size<T>();
    }

    template<typename T>
    bool can_procure(int num, T const& item) const
    {
        return length_ >= num * static_size<T>();
    }

    bool is_aligned() const
    {
        return boost::alignment::is_aligned(pack_double::alignment, array_);
    }

    double result(double v2w, double vw, double w) const
    {
        double const res = (v2w / w) - (vw * vw) / (w * w);
        return (res < 0.) ? 0. : sqrt(res);
    }

private:
    double const* array_;
    double const* weights_;
    std::size_t length_;
};
