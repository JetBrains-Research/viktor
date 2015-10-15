viktor [![Build Status](https://travis-ci.org/JetBrains-Research/viktor.svg?branch=master)](https://travis-ci.org/JetBrains-Research/viktor)
======

`viktor` implements a restricted subset of NumPy [ndarray] [ndarray] features in
Kotlin. Here're some of the higlights:

* Three basic data types: `StridedVector`, `StridedMatrix2` and
  `StridedMatrix3` specialized to `double` values.
* Efficient vectorized operations, which are transparently accelerated
  using SIMD whenever possible.
* Semi-sweet syntax.

    ```kotlin
    val m = StridedMatrix(4, 3)
    m[0] = StridedVector.full(3, 42.0)  // row-view.
    m[_, 0]                              // column-view.
    m[0] = 42.0                          // broadcasting.
    ```

Requirements
------------

* [Yeppp!] [yeppp] 1.0.0
* [simdstat] [simdstat]

[ndarray]: http://docs.scipy.org/doc/numpy/reference/arrays.ndarray.html
[yeppp]: http://www.yeppp.info
[simdstat]: https://github.com/JetBrains-Research/simdstat
