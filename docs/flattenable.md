## Flattenable and Dense Arrays
Let's explore some views from the [previous section](views.md) in detail:
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

