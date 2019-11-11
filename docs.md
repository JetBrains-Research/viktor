# viktor Overview

* [General Concepts](#general-concepts)
* [Creating Arrays and Accessing Elements](#creating-arrays-and-accessing-elements)
* [Views](#views)
* [Flattenable and Dense Arrays](#flattenable-and-dense-arrays)
* [Copying](#copying)
* [Arithmetic and Mathematics](#arithmetic-and-mathematics)
* [Statistics and Special Methods](#statistics-and-special-methods)
* [Logarithmic Storage](#logarithmic-storage)
* [Efficiency and SIMD](#efficiency-and-simd)
* [Examples](#examples)

See [README](README.md) for instructions on how to install `viktor`.

The section [Examples](#examples) contains instructive code examples with explanations
and can be used as a tutorial.

## General Concepts

`viktor` is a Kotlin + JNI library that revolves around a n-dimensional array concept
similar to that of NumPy’s [ndarray](http://docs.scipy.org/doc/numpy/reference/arrays.ndarray.html).

This array is described by the class `F64Array`. We call 1-dimensional arrays **vectors**
and 2-dimensional ones **matrices**. Regardless of the number of dimensions, an `F64Array`
always stores its data in an ordinary `DoubleArray` (`double[]` for Java speakers).
The elements are laid out in row-major order (also called C-order). For example,
for a 3-dimensional array `a` with the **shape** of 2x3x2, the elements will be laid out
in the following order:

    a[0, 0, 0] === data[0]
    a[0, 0, 1] === data[1]
    a[0, 1, 0] === data[2]
    a[0, 1, 1] === data[3]
    a[0, 2, 0] === data[4]
    a[0, 2, 1] === data[5]
    a[1, 0, 0] === data[6]
    a[1, 0, 1] === data[7]
    a[1, 1, 0] === data[8]
    a[1, 1, 1] === data[9]
    a[1, 2, 0] === data[10]
    a[1, 2, 1] === data[11]

It’s easy to see that the element `a[i, j, k]` corresponds to the element
`data[6 * i + 2 * j + 1 * k]`. The values `6`, `2`, `1` are called _strides_,
since they are the distances separating the elements with neighboring indices.

Two distinct `F64Array`s may be supported by the same underlying `DoubleArray`.
They can point to intersecting or non-intersecting portions of the underlying array.
For example, by calling a viewer getter method `b = a.V[1]` we can obtain
a view of the second 3x2 matrix slice of `a`:

    b[0, 0] === a[1, 0, 0] === data[6]
    b[0, 1] === a[1, 0, 1] === data[7]
    b[1, 0] === a[1, 1, 0] === data[8]
    b[1, 1] === a[1, 1, 1] === data[9]
    b[2, 0] === a[1, 2, 0] === data[10]
    b[2, 1] === a[1, 2, 1] === data[11]

For all intents and purposes, `b` is an `F64Array` in its own right.
It doesn't keep any reference to `a`. The element `b[i, j]` corresponds to the element
`data[6 + 2 * i + j]`. Value `6` is called *offset*. Any in-place operation on `b` will
be actually performed on the appropriate region of `data` and thus visible through `a`.

## Creating Arrays and Accessing Elements 

### Accessing elements
The elements can be accessed through standard get/set methods:
```kotlin
println(a[1, 1, 1])
a[0, 2, 1] = 42.0
```
    
The only currently supported element type is `Double` (`double` in Java). 
    
### Creating F64Arrays from scratch
There is a number of ways to create an F64Array:
```kotlin
val a = F64Array(2, 3, 2) // zero-filled 2x3x2 array
val b = F64Array.full(2, 3, 2, init = 3.14) // 2x3x2 array filled with 3.14
val c = F64Array(8, 8) { i, j -> if (i == j) 1.0 else 0.0 } // create an 8x8 unit matrix; this method is available for vectors, matrices and 3D arrays
val d = F64Array.of(3.14, 2.78, 1.41) // creates a vector
```

### Creating F64Arrays from Kotlin/Java arrays
```kotlin
val a: DoubleArray = ...
val b = a.asF64Array() // a wrapper method: creates a vector that uses [a] as data storage; only works for vectors
val c: Array<Array<DoubleArray>> = ...
val d = c.toF64Array() // a copying method: allocates a [DoubleArray] for storage and copies the values from [g] to recreate its structure: g[i][j][k] == h[i, j, k]; only works for matrices and above
```
### Retrieving Kotlin/Java array from F64Array
```kotlin
val a: F64Array = ...
val b: DoubleArray = a.data // retrieves the underlying storage [DoubleArray]; may or may not represent all of [b]’s elements
val c: DoubleArray = a.toDoubleArray() // a copying method; converts the vector into a [DoubleArray]: a[i] == c[i]; only works for vectors
val d: Array<*> = a.toGenericArray() // a copying method; converts the array to a corresponding Kotlin/Java structure ([Array<DoubleArray>] for matrices, [Array<Array<DoubleArray>>] for 3D arrays etc.); only works for matrices and above
val e: Any = a.toArray() // returns either a [DoubleArray] or an [Array<*>], depending on the number of dimensions of [a]
```

## Views
A **view** of an `F64Array` is another `F64Array` which uses the same `DoubleArray`
for storage. For example, a matrix column is a view of the matrix.

Each `F64Array` has a special property `V`, called **viewer**. It enables a more idiomatic way
to obtain **views**. In this case, you can use a special `_I` object to signify
"skip this axis", see the examples below.

```kotlin
val a = F64Array(2, 3, 2) // 2x3x2 array
val b = a.view(1) // view the second 3x2 matrix slice of [a]: b[i, j] === a[1, i, j]
val b1 = a.V[1] // another way of doing the same
val c = a.view(0, 1) // view a 2x2 matrix such that c[i, j] === a[i, 0, j]
val c1 = a.V[_I, 0] // another way of doing the same; [_I] indicates that the first axis should be skipped
val d = a.view(1, 2) // view a 2x3 matrix such that d[i, i] === a[i, j, 1]
val e = a.along(1) // a sequence of 2x2 views: a.view(0, 1), a.view(1, 1), a.view(2, 1)
```

The viewer also has setter methods:

```kotlin
a.V[1] = d // copies the contents of [d] into the second 3x2 matrix slice of [a]
a.V[_I, 0] = 42.0 // replaces the corresponding elements of [a] with the value 42.0
a.V[_I] = 3.14 // replaces all elements of [a] with the value 3.14 
```

More sophisticated views can be generated by **slicing**:

```kotlin
val f = a.slice(from = 0, to = 2, axis = 1) // f[i, j, k] === a[i, j, k], but [f] has a shape 2x2x2
val g = a.slice(from = 1, to = 3, axis = 1) // g[i, j, k] === a[i, j + 1, k], and [g] has a shape 2x2x2
val h = a.slice(from = 0, step = 2) // h[i, 0, k] === a[i, 0, k], h[i, 1, k] === a[i, 2, k] 
```

## Flattenable and Dense Arrays
Let's explore some views from the [previous section](#views) in detail:
```kotlin
val a = F64Array(2, 3, 2) // 2x3x2 array
val b = a.view(1) // view the second 3x2 matrix slice of [a]: b[i, j] === a[1, i, j]
val c = a.view(0, 1) // view a 2x2 matrix such that c[i, j] === a[i, 0, j]
val d = a.view(1, 2) // view a 2x3 matrix such that d[i, i] === a[i, j, 1]
```

These three views have different properties.

    b[0, 0] === a[1, 0, 0] === data[6]
    b[0, 1] === a[1, 0, 1] === data[7]
    b[1, 0] === a[1, 1, 0] === data[8]
    b[1, 1] === a[1, 1, 1] === data[9]
    b[2, 0] === a[1, 2, 0] === data[10]
    b[2, 1] === a[1, 2, 1] === data[11]

`b` owns a contiguous region of `data`. It is thus called **dense**.

    d[0, 0] === a[0, 0, 1] === data[1]
    d[0, 1] === a[0, 1, 1] === data[3]
    d[0, 2] === a[0, 2, 1] === data[5]
    d[1, 0] === a[1, 0, 1] === data[7]
    d[1, 1] === a[1, 1, 1] === data[9]
    d[1, 2] === a[1, 2, 1] === data[11]

`d` doesn't own a contiguous region of `data`, but its entries are equidistant.
This means there is a 6-element vector that owns the exact same elements as `d`.
This vector can be obtained by calling `flatten()`:
```kotlin
val e = d.flatten()
```

    e[0] === d[0, 0] === a[0, 0, 1] === data[1]
    e[1] === d[0, 1] === a[0, 1, 1] === data[3]
    e[2] === d[0, 2] === a[0, 2, 1] === data[5]
    e[3] === d[1, 0] === a[1, 0, 1] === data[7]
    e[4] === d[1, 1] === a[1, 1, 1] === data[9]
    e[5] === d[1, 2] === a[1, 2, 1] === data[11]

`b` and `d` are thus both **flattenable** arrays. It can be confirmed by looking
at the property `isFlattenable`.

    c[0, 0] === a[0, 0, 0] === data[0]
    c[0, 1] === a[0, 0, 1] === data[1]
    c[1, 0] === a[1, 0, 0] === data[6]
    c[1, 1] === a[1, 0, 1] === data[7]

`c` doesn't own a contiguous region of `data`, and its entries are not equidistant.
It is thus **not flattenable**. `c.isFlattenable` will return `false`, and `c.flatten()`
will fail with an exception.

Flattenable arrays (especially dense ones) generally allow a more efficient traversal,
since all their elements can be accessed in a single loop.

### Playing with shape
Flattenable arrays can be easily **reshaped**, provided that their number of elements
stays the same. The reshaped array is a view of the original; it can access
the same elements.

```kotlin
val a = F64Array(2, 3, 2)
val b = a.reshape(12) // the same as a.flatten()
val c = a.reshape(2, 6) // essentially flattens the 3x2 matrix slices into 6-element vectors
val d = a.reshape(3, 4) // no elegant interpretation, just the same elements in the same order
```

## Copying
If desired, you may create a **copy** of an array that is completely separate
from the original. The changes to the copy won't propagate to the original (and vice versa),
because the underlying storage arrays will be different.

```kotlin
val a = F64Array.full(2, 3, 2, init = 2.78)
val b = a.copy()
b.fill(3.14) // doesn't affect [a]
```

You can also copy the array contents to another array of the same shape:

```kotlin
b.copyTo(a)
a.V[_I] = b // has the exact same effect
```

The copy will always be dense (see [Flattenable and Dense Arrays](#flattenable-and-dense-arrays)),
even if the original was not.

## Arithmetic and mathematics
If two arrays have the same exact shape, you can use ordinary  arithmetic operators on them:
```kotlin
val a = F64Array.full(2, 3, 2, init = 2.78)
val b = F64Array.full(2, 3, 2, init = 3.14)
val c = a + b // elementwise addition
val d = a / b // elementwise division
```

These operators are **copying**; they will return a new 2x3x2 array with sums or ratios
respectively.

```kotlin
a -= b // in-place elementwise subtraction
b *= b // in-place elementwise multiplication
```

These operators are **in-place**; they will modify the elements of the left part.

You can also use the verbose methods, for example:

```kotlin
val c = a.div(b)
b.timesAssign(b)
```

You can also do the same operations with scalar values, i.e. `Double`s:

```kotlin
val e = a * 42.0
val f = 1.0 / b // yes, this works! we have an extension method Double.div(F64Array)!
```

For more sophisticated examples, you have (equally elementwise) mathematical methods
at your disposal:

```kotlin
val g = a.exp() // elementwise natural exponent, copying
g.logInPlace() // elementwise natural logarithm, in-place
```

The operations `exp`, `expm1`, `log`, `log1p` are available as both copying
and in-place methods.

For vectors, we have a scalar multiplication method `dot()`:

```kotlin
val v1 = F64Array(1000)
val v2 = F64Array(1000)
val v1v2: Double = v1.dot(v2)
```

## Statistics and Special Methods
You can calculate some statistical values:
```kotlin
val a = F64Array(2, 3, 2)
val s: Double = a.sum() // what it says
val m: Double = a.mean() // arithmetic mean
val sd: Double = a.sd() // standard deviation
val aMax = a.max() // maximum value
```

For a vector, there is an in-place method for calculating cumulative sums:

```kotlin
val a = F64Array(1000)
a.cumSum() // after this, each element a[n] is equal to the sum a[0] + … + a[n] of the original values of [a]
```

Also for a vector, you can get the index of the maximum / minimum element
using `argMax()` / `argMin()` respectively, as well as an arbitrary quantile:

```kotlin
val median = a.quantile(0.5) // note that this is a destructive method, which will shuffle the array
```

You can rescale an array so that its entries sum to one with an in-place method `rescale()`:

```kotlin
val a = F64Array.of(3.14, 2.78)
a.rescale() // equivalent to a /= a.sum() but more efficient and precise
```

## Logarithmic Storage
In statistics and machine learning, it’s frequently useful to store the logarithms of values
instead of the values themselves (e.g. when dealing with distribution density).
The logarithms make multiplication and division easy (just add or subtract the logarithms),
but hinder the summation. To this end, we have a special infix method `logAddExp`:

```kotlin
val logA = F64Array(2, 3, 2, init = ln(3.14))
val logB = F64Array(2, 3, 2, init = ln(2.78))
val logC = logA logAddExp logB
```

Like the name suggests, `logAddExp` is equivalent to writing
`(logA.exp() + logB.exp()).log()` but generally more precise and efficient
and less memory-consuming. There's also an in-place variant, `logA.logAddExpAssign(logB)`.

You can also sum the entire logarithmically stored array:

```kotlin
val logSum: Double = logA.logSumExp()
```

This is again equivalent to (but better than) `ln(logA.exp().sum())`.

There is also a version of `rescale()` for logarithmically stored arrays:

```kotlin
logA.logRescale() // equivalent to a -= a.logSumExp(), but more efficient and precise
```

## Efficiency and SIMD
### Excessive Copying
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

### Efficient Traversal
Flattenable arrays can be traversed in a single loop, so they naturally enjoy
faster computations. Try to avoid large non-flattenable arrays.
Moreover, dense arrays benefit from native SIMD optimizations and faster copying,
so these are the natural choice for efficient calculations.
See [Flattenable and Dense Arrays](#flattenable-and-dense-arrays) for more details.

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

## Examples

### Factorials 

Suppose you want a 1000-element vector `f` such that `f[i] == i!`, i.e.
a vector of factorials. Is there an efficient way to do this with `viktor`? Of course!

```kotlin
val f = F64Array(1000) { it.toDouble() } // fill vector f with values so that f[i] == i
f[0] = 1.0 // gotta deal with that edge case
f.logInPlace() // replace all values with their natural logarithms; f[i] == log(i) for i > 0
f.cumSum() // replace all values with cumulative sums; f[i] == sum_{k=1}^i log(k) for i > 0
f.expInPlace() // replace all values with their exponents; f[i] == exp(sum_{k=1}^i log(k)) == prod_{k=1}^i k == i!
```

Bingo! You have a vector of factorials. Sadly, most of these are equal to positive infinity
due to floating-point overflow. You might consider skipping the last exponentiation
and keeping the logarithms of the factorials, which are much less prone to overflow.
See [Logarithmic Storage](#logarithmic-storage) for more details.

### Gaussian Density

Suppose you have a vector `o` of *i.i.d.* random observations, and you want to calculate
the vector of probability density `d` under the standard normal distribution `N(0, 1)`.
Since the density formula is `p(x) = sqrt(2 * pi) * exp(-x^2/2)`, we can do the following:

```kotlin
val d = sqrt(2 * PI) * (- o * o / 2.0).exp()
```

This produces the desired results, but is not very efficient: each of the `*`, `/`, `-`,
`exp` and `*` creates a copy, only the last of which is retained as `d`.
That’s four copies too many. Let’s consider a better approach:

```kotlin
val d = o * o // d == o^2
d /= -2.0 // d == -o^2 / 2
d.expInPlace() // d == exp(-o^2 / 2)
d *= sqrt(2 * PI) // d == sqrt(2 * pi) * exp(-o^2 / 2)
```

Voila! No extra copies created. However, if some observations have large absolute values,
the exponent might underflow, leaving us with a zero density. It’s thus better to store
and operate on log densities! Since `log p(x) = 1/2 log (2 * pi) - x^2 / 2`, we can write:

```kotlin
val logD = o * o
logD /= -2.0
logD += 0.5 * ln(2 * PI)
```

What is the total log likelihood of `o` under the standard normal distribution `N(0, 1)`?
Why, it’s `logD.sum()`, naturally. (Since likelihood is a product of densities,
log likelihood is a sum of log densities.)

### Gaussian mixture

We now suspect our observation vector `o` actually came from a uniform mixture
of two normal distributions, `N(0, 1)` and `N(3, 1)`. What is the total likelihood now?
What is the most probable sequence of components?

Let's create a matrix `logP` to hold the probabilities of observations
belonging to components: `logP[i, j] = log(P(o_j, c_j = i))`, where `c_j` is the mixture
component responsible for the `j`th observation (either `0` or `1`). Using conditional
probabilities:

    P(o_j, c_j = i) = P(o_j | c_j = i) * P(c_j = i)
    
Let's also try to use array-wide operations as much as possible.  

```kotlin
val oColumn = o.reshape(1, o.size) // create a 1x1000 matrix view of [o]
val logP = F64Array.concatenate(oColumn, oColumn) // concatenate into a 2x1000 array; logP[i, j] == o_j
logP.V[1] -= 3.0 // logP[i, j] == o_j - μ_i
logP *= logP // logP[i, j] == (o_j - μ_i) ^ 2
logP /= -2.0 // logP[i, j] == - (o_j - μ_i) ^ 2 / 2
logP += 0.5 * ln(2 * PI) + ln(0.5) // logP[i, j] == - (o_j - μ_i) ^ 2 / 2 + 1/2 log(2 * pi) + log(1/2) == log(P(o_j | c_j = i) * P(c_j = i))
```

Now we can answer our questions. The likelihood of an observation is

    P(o_j) = Σ_i P(o_j, c_j = i)
    
thus we can easily obtain a vector of log-likelihoods by log-summing the columns of `logP`:

```kotlin
val logL = logP.V[0] logAddExp logP.V[1]
```

The total log-likelihood is equal to `logL.sum()`, like in the previous example.

To obtain the most probable sequence of mixture components, we just need to pick the one
with the greatest probability for each observation:

```kotlin
val components = logP.along(1).map { it.argMax() } // a sequence of 0s and 1s
```

This generates a sequence of matrix rows (`along(1)`) and picks the maximum element
index for each row.