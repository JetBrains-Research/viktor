#pragma once

#include <complex>

void complex_times(const double * src1, const double * src2, double * dst, size_t length) {
  while (length > 1) {
    std::complex<double> const z = *(reinterpret_cast<std::complex<double> const *>(src1));
    std::complex<double> const w = *(reinterpret_cast<std::complex<double> const *>(src2));
    *(reinterpret_cast<std::complex<double>*>(dst)) = z * w;
    src1 += 2;
    src2 += 2;
    dst += 2;
    length -= 2;
  }
}