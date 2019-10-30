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
See [Logarithmic Storage](logstorage.md) for more details.

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