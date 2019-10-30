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
