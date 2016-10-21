viktor [![Build Status](https://travis-ci.org/JetBrains-Research/viktor.svg?branch=master)](https://travis-ci.org/JetBrains-Research/viktor)
======

`viktor` implements a restricted subset of NumPy [ndarray] [ndarray] features in
Kotlin. Here're some of the highlights:

* A single core data type --- `F64Array` which is backed by either `F64Vector`
  or `F64Matrix`.
* Efficient vectorized operations, which are accelerated using SIMD whenever
  possible.
* Semi-sweet syntax.

    ```kotlin
    val m = F64Array(4, 3)
    m[0] = F64Array.full(3, 42.0)  // row-view.
    m[_I, 0]                       // column-view.
    m[0] = 42.0                    // broadcasting.
    m + 0.5 * m                    // arithmetic operations.
    m[0].exp() + 1.0               // math functions.
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
    compile 'org.jetbrains.bio:viktor:0.3.5'
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

Testing
-------

No extra configuration is required for running the tests from Gradle

```bash
$ ./gradlew test
```

However, you might need to alter `java.library.path` to run the tests from
the IDE. The following Java command line option should work for IDEA

```bash
-Djava.library.path=./build/libs
```

Publishing
----------

You can do it with a one-liner

```bash
./gradlew clean assemble generatePomFileForMavenJavaPublication bintrayUpload
```

Make sure to set Bintray credentials (see API key section
[here](https://bintray.com/profile/edit)) in `$HOME/.gradle/gradle.properties`.

```
$ cat $HOME/.gradle/gradle.properties
bintrayUser=CHANGEME
bintrayKey=CHANGEME
```
