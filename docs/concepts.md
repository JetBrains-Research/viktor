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
