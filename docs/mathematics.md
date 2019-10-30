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

