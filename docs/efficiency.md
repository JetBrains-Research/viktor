## Efficiency
### Copying
You might want to keep the number of copying events to a minimum,
especially when working with very large arrays. Copying requires allocation and
(later) disposal of large amounts of memory.

There is one general tip for less copying: use in-place operations whenever possible.
For example, while

```kotlin
val b = (a + 1.0) / 2.0
```

is equivalent to

```kotlin
val b = a + 1.0
b /= 2.0
```

the latter approach avoids one copy allocation and disposal by reusing an array.
The effect is even more pronounced with a longer operation chain.
### Traversal
Flattenable arrays can be traversed in a single loop, so they naturally enjoy
faster computations. Try to avoid large non-flattenable arrays.
Moreover, dense arrays benefit from native SIMD optimizations and faster copying,
so these are the natural choice for efficient calculations.
See [Flattenable and Dense Arrays](flattenable.md) for more details.
### SIMD
`SIMD` stands for "single instruction, multiple data". It’s a broad family
of CPU instruction set extensions, including `MMX`, `SSE`, `AVX` and others.
These extensions allow using extended CPU registers containing multiple data units.
For example, `AVX` defines 256-bit registers that can operate on
four 64-bit floating-point numbers with one instruction.

`viktor` supports SIMD extensions by providing a set of binary JNI libraries
built specifically for several target platforms and extension sets.
We build and include the following libraries for `amd64` (also called `x86_64`)
instruction set extensions:

amd64 (x86_64) | SSE2 | AVX
---------------|------|----
Windows | + | +
Linux | + | +
macOS | + | +

These libraries are only compatible with 64-bit JVM. In all other cases,
`viktor` doesn't use JNI, opting for Kotlin/Java operations instead.
