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
