viktor [![Build Status](https://travis-ci.org/JetBrains-Research/viktor.svg?branch=master)](https://travis-ci.org/JetBrains-Research/viktor)
======

`viktor` implements a restricted subset of NumPy [ndarray] [ndarray] features in
Kotlin. Here're some of the highlights:

* Three basic data types: `StridedVector`, `StridedMatrix2` and
  `StridedMatrix3` specialized to `double` values.
* Efficient vectorized operations, which are transparently accelerated
  using SIMD whenever possible.
* Semi-sweet syntax.

    ```kotlin
    val m = StridedMatrix(4, 3)
    m[0] = StridedVector.full(3, 42.0)  // row-view.
    m[_I, 0]                            // column-view.
    m[0] = 42.0                         // broadcasting.
    m + m                               // arithmetic operations.
    ```

Requirements
------------

* [Yeppp!] [yeppp] 1.0.0
* [simdstat] [simdstat]

Installation
------------

The latest version of `viktor` is available on [Bintray] [bintray]. If you're using
Gradle just add the following to your `build.gradle`:

```gradle
repositories {
    maven {
        url "https://dl.bintray.com/jetbrains-research/maven"
    }
}

dependencies {
    compile 'org.jetbrains.bio:viktor:0.1.3'

    compile files("$rootDir/lib/yeppp-bundle-1.0.jar")
    compile files("$rootDir/lib/simd.jar",
                  "$rootDir/lib/simd-sources.jar")
}

```

**Note**: Yeppp! and simdstat aren't available via Maven Central or jCenter. You
will have to download them manually.

[ndarray]: http://docs.scipy.org/doc/numpy/reference/arrays.ndarray.html
[yeppp]: http://www.yeppp.info
[simdstat]: https://github.com/JetBrains-Research/simdstat
[jcenter]: https://bintray.com/bintray/jcenter
