viktor [![Build Status](https://travis-ci.org/JetBrains-Research/viktor.svg?branch=master)](https://travis-ci.org/JetBrains-Research/viktor)
======

`viktor` implements a restricted subset of NumPy [ndarray] [ndarray] features in
Kotlin. Here're some of the highlights:

* Three basic data types: `StridedVector`, `StridedMatrix2` and
  `StridedMatrix3` specialized to `double` values.
* Efficient vectorized operations, which are accelerated using SIMD whenever
  possible.
* Semi-sweet syntax.

    ```kotlin
    val m = StridedMatrix(4, 3)
    m[0] = StridedVector.full(3, 42.0)  // row-view.
    m[_I, 0]                            // column-view.
    m[0] = 42.0                         // broadcasting.
    m[0] + m[0]                         // arithmetic operations.
    ```

[ndarray]: http://docs.scipy.org/doc/numpy/reference/arrays.ndarray.html

Installation
------------

The latest version of `viktor` is available on [Bintray] [bintray]. If you're using
Gradle just add the following to your `build.gradle`:

```gradle
repositories {
    maven {
        url 'https://dl.bintray.com/jetbrains-research/maven'
    }
}

dependencies {
    compile 'org.jetbrains.bio:viktor:0.3.0'
}

```

[bintray]: https://bintray.com/jetbrains-research/maven/viktor/view

The version available on Bintray currently targets only SSE2 and AVX on x64
Linux. For any other setup `viktor` would fall back to pure-Kotlin
implementations. If you are interested in SIMD accelerations for a different
instruction set or operating system feel free to file an issue to the
[bug tracker] [issues].

[issues]: https://github.com/JetBrains-Research/viktor/issues

Building from source
--------------------

`viktor` relies on [boost.simd] [boost.simd] for implementing SIMD
accelerations. Therefore, you would need CMake and a C++11 compiler,
but otherwise the build process is as simple as

```bash
$ ./gradlew assemble
```

[boost.simd]: https://github.com/NumScale/boost.simd
