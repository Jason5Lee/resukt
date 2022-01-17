# resukt

A Result type representing either a success or a failure.

## Usage

This is a Kotlin multi-platform library, supporting JVM, JS and major native targets. If you're a Kotlin JVM user,
just add the following to the dependencies.

```gradle
implementation("me.jason5lee:resukt:1.0.0")
implementation("me.jason5lee:resukt-jvm:1.0.0")
```

## Design

Unlike other result libraries, this library is implemented
based on [the one in the standard library](https://github.com/JetBrains/kotlin/blob/80cce1dc5280eb9135390270c8644a7b8d198071/libraries/stdlib/src/kotlin/util/Result.kt#L22),
with the following opinionated designs.

* The failure type is a generic, so it is `Result<T, F>` instead of `Result<T>`.
* Use "failure" instead of "error" because "error" already has a meaning in JVM and Kotlin.
* The failure type isn't limited to be a `Throwable`. Actually I don't recommend defining failure as a `Throwable`.
The failures of the logic are very different from the exceptions from the implementation and IO.
* Because of the reason above, the `Throwable`-related functions are put in the `throwable` package 
which I don't recommend to use.
* Provides `andThen` and `whenFailure` methods for easy error spreading in a multi-steps procedure. Check [MultiStepsTest.kt](https://github.com/Jason5Lee/resukt/tree/main/src/commonTest/kotlin/me/jason5lee/resukt/tests/MultiStepsTest.kt) for examples.
* Except the designs mentioned above, The implementation of this library is almost the same as the `Result<T>` in standard library.

## Note

For the people from pattern-matching or smart-casting languages: because it does not use the `sealed class`, it provides
a `fold` method having the similar function of pattern-matching. It accepts two lambdas expression, one for the success
case and another for failure case. For example, for a `a: Result<Int, String>`, you can do something like
`a.fold(onSuccess = { it / 2 }, onFailure = { println("Error: $it"); -1 })` .

Because Kotlin's standard library will be used if this `Result` is not imported, if you get a strange compile
error, be sure you have `me.jason5lee.resukt.Result` or `me.jason5lee.resukt.throwable.runCatching` imported.

## Thanks

* [Scott Wlaschin](https://scottwlaschin.com/) who showed me the idea of error modeling.
* [michaelbull/kotlin-result](https://github.com/michaelbull/kotlin-result) for Gradle project and GitHub CI setup.
