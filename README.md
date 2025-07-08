[![JetBrains Research](https://jb.gg/badges/research.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![tests](http://teamcity.jetbrains.com/app/rest/builds/buildType:(id:Epigenome_Tools_Viktor)/statusIcon.svg)](http://teamcity.jetbrains.com/viewType.html?buildTypeId=Epigenome_Tools_Viktor&guest=1)

[![Maven Central](https://img.shields.io/maven-central/v/org.jetbrains.bio/viktor.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22org.jetbrains.bio%22%20AND%20a:%22viktor%22)

viktor 
======

`viktor` implements a restricted subset of NumPy [ndarray](http://docs.scipy.org/doc/numpy/reference/arrays.ndarray.html) features in
Kotlin using Java Vector API. Here are some highlights:

* A single core data type --- `F64Array`, an n-dimensional primitive array.
* Efficient vectorized operations, which are accelerated using SIMD whenever
  possible.
* Semi-sweet syntax.

    ```kotlin
    val m = F64Array(4, 3)
    m.V[0] = F64Array.full(3, 42.0)  // row-view.
    m.V[_I, 0]                       // column-view.
    m.V[0] = 42.0                    // broadcasting.
    m + 0.5 * m                      // arithmetic operations.
    m.V[0].exp() + 1.0               // math functions.
    ```

Installation
------------

The latest version of `viktor` is available on [Maven Central][maven-central].
If you're using Gradle, just add the following to your `build.gradle` dependencies:
```groovy
implementation 'org.jetbrains.bio:viktor:2.0.0'
```
or, equivalently, add the following to your `build.gradle.kts` dependencies:
```kotlin
implementation("org.jetbrains.bio:viktor:2.0.0")
```
With Maven, use the dependency
```xml
<dependency>
    <groupId>org.jetbrains.bio</groupId>
    <artifactId>viktor</artifactId>
    <version>2.0.0</version>
</dependency>
```


Versions
--------

* Viktor `2.0.0` relies fully on [Java Vector API](https://openjdk.org/jeps/448) and requires at least Java 21.
  - Supports Vector API acceleration on all Java compatible platforms.

* Viktor `1.2.0` supports Java 8, and fully relies on the [boost.simd](https://github.com/JetBrains-Research/boost.simd) for SIMD acceleration.
  - SSE2 and AVX,
  - amd64 / x86-64,
  - Linux, Windows and MacOS.

  
Versions older than `1.1.0` can be downloaded from [GitHub Releases](https://github.com/JetBrains-Research/viktor/releases).

Should you have any problems feel free to file an issue to the
[bug tracker](https://github.com/JetBrains-Research/viktor/issues).

Logging
-------

`viktor` uses [slf4j](http://www.slf4j.org/) logging API to provide error messages.
To see them, you have to add a `slf4j` implementation (also called a binding)
to your project. For example, add the following Gradle dependency to use `log4j`:

```kotlin
dependencies {
    compile('org.slf4j:slf4j-log4j12:2.0.17')
}
```

Building from source
--------------------

Use the following command line: 

```shell
./gradlew jar
```

Note: don't use `./gradlew assemble`, since it includes the signing of the artifacts
and will fail if the correct credentials are not provided.

Testing
-------

No extra configuration is required for running the tests from Gradle:

```shell
./gradlew test
```

Benchmarking
------------

See [benchmarking](./docs/benchmark.md) document for more details.

Publishing
----------

Publishing to [Maven Central][maven-central] is currently done via a dedicated
build configuration of an internal TeamCity server. This allows us
to deploy a cross-platform version.

Documentation
----

Visit [viktor Documentation](./docs/docs.md) for an extensive feature overview,
instructive code examples and benchmarking data. 

[maven-central]: https://search.maven.org/artifact/org.jetbrains.bio/viktor