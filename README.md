[![JetBrains Research](https://jb.gg/badges/research.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![tests](http://teamcity.jetbrains.com/app/rest/builds/buildType:(id:Epigenome_Tools_Viktor)/statusIcon.svg)](http://teamcity.jetbrains.com/viewType.html?buildTypeId=Epigenome_Tools_Viktor&guest=1)

[![](https://api.bintray.com/packages/jetbrains-research/maven/viktor/images/download.svg?version=1.1.0)](https://bintray.com/jetbrains-research/maven/viktor/1.1.0/link)

viktor 
======

`viktor` implements a restricted subset of NumPy [ndarray][ndarray] features in
Kotlin. Here are some highlights:

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

[ndarray]: http://docs.scipy.org/doc/numpy/reference/arrays.ndarray.html

Installation
------------

The latest version of `viktor` is available on [Bintray][bintray]. If you're using
Gradle just add the following to your `build.gradle`:

```groovy
repositories {
    jCenter()
}

dependencies {
    compile 'org.jetbrains.bio:viktor:1.1.0'
}
```

With Maven, use the dependency
```xml
<dependency>
    <groupId>org.jetbrains.bio</groupId>
    <artifactId>viktor</artifactId>
    <version>1.1.0</version>
</dependency>
```
and don't forget to add the repository
```xml
<repository>
  <snapshots>
    <enabled>false</enabled>
  </snapshots>
  <id>bintray-jetbrains-research-maven</id>
  <name>bintray</name>
  <url>https://dl.bintray.com/jetbrains-research/maven</url>
</repository>
```

[bintray]: https://bintray.com/jetbrains-research/maven/viktor/view

The version available on Bintray currently targets only:
- SSE2 and AVX,
- amd64 / x86-64,
- Linux, Windows and MacOS.

For any other setup `viktor` would fall back to pure-Kotlin
implementations. If you are interested in SIMD accelerations for a different
instruction set or operating system feel free to file an issue to the
[bug tracker][issues].

[issues]: https://github.com/JetBrains-Research/viktor/issues

Logging
-------

`viktor` uses [slf4j](http://www.slf4j.org/) logging API to provide error messages.
To see them, you have to add a `slf4j` implementation (also called a binding)
to your project. For example, add the following Gradle dependency to use `log4j`:
```gradle
dependencies {
    compile group: 'org.slf4j', name: 'slf4j-log4j12', version: '1.7.25'
}
```

Building from source
--------------------

`viktor` relies on [boost.simd][boost.simd] for implementing SIMD
accelerations. Therefore, you would need a C++11 compiler,
but otherwise the build process is as simple as

```shell
./gradlew assemble
```

[boost.simd]: https://github.com/JetBrains-Research/boost.simd

Testing
-------

No extra configuration is required for running the tests from Gradle

```shell
./gradlew test
```

However, you might need to alter `java.library.path` to run the tests from
the IDE. The following Java command line option should work for IDEA

```shell
-Djava.library.path=./build/libs
```

Publishing
----------

Publishing to [Bintray][bintray] is currently done via a dedicated
build configuration of an internal TeamCity server. This allows us
to deploy a cross-platform version.

Documentation
----

Visit [viktor Documentation](./docs/docs.md) for an extensive feature overview,
instructive code examples and benchmarking data. 
