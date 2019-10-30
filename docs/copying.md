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

The copy will always be dense (see [Flattenable and Dense Arrays](flattenable.md)),
even if the original was not.
