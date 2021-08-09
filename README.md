# resukt

A Result type representing either a success or a failure.

Unlike other result libraries, this library is implemented
based on [the one in standard library](https://github.com/JetBrains/kotlin/blob/80cce1dc5280eb9135390270c8644a7b8d198071/libraries/stdlib/src/kotlin/util/Result.kt#L22),
with the following opinionated designs.

* Use "failure" instead of "error" because "error" already has a meaning in JVM and Kotlin.
* Doesn't limit failure to be a `Throwable`. Actually I don't recommend defining failure as `Throwable`.
The failures of the logic are very different from the exceptions from the implementation and IO.
* Because of the reason above, the throwable-related functions are put in the `throwable` package 
which I don't recommend to use.
* Except the above designs, this library should be as closed as the standard one.
