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