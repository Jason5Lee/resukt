@file:Suppress("UNCHECKED_CAST")

package me.jason5lee.resukt.throwable

import me.jason5lee.resukt.Result
/**
 * Throws it if the result is failure of a [Throwable]. This internal function minimizes
 * inlined bytecode for [getOrThrow] and makes sure that in the future we can
 * add some exception-augmenting logic here (if needed).
 */
@PublishedApi
internal fun Result<*, Throwable>.throwOnFailure() {
    if (value is Result.Failure) throw value.failure as Throwable
}

/**
 * Calls the specified function [block] and returns its encapsulated result if invocation was successful,
 * catching any [Throwable] exception that was thrown from the [block] function execution and encapsulating it as a failure.
 */
public inline fun <R> runCatching(block: () -> R): Result<R, Throwable> {
    return try {
        Result.success(block())
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

/**
 * Calls the specified function [block] with `this` value as its receiver and returns its encapsulated result if invocation was successful,
 * catching any [Throwable] exception that was thrown from the [block] function execution and encapsulating it as a failure.
 */
public inline fun <T, R> T.runCatching(block: T.() -> R): Result<R, Throwable> {
    return try {
        Result.success(block())
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

/**
 * Returns the encapsulated value if this instance represents [success][Result.isSuccess] or throws the encapsulated [Throwable] exception
 * if it is [failure][Result.isFailure].
 *
 * This function is a shorthand for `getOrElse { throw it }` (see [me.jason5lee.resukt.getOrElse]).
 */
public inline fun <T> Result<T, Throwable>.getOrThrow(): T {
    throwOnFailure()
    return value as T
}

/**
 * Returns the encapsulated result of the given [transform] function applied to the encapsulated value
 * if this instance represents [success][Result.isSuccess] or the
 * original encapsulated [Throwable] exception if it is [failure][Result.isFailure].
 *
 * This function catches any [Throwable] exception thrown by [transform] function and encapsulates it as a failure.
 */
public inline fun <R, T> Result<T, Throwable>.mapCatching(transform: (value: T) -> R): Result<R, Throwable> {
    return when {
        isSuccess -> runCatching { transform(value as T) }
        else -> Result(value)
    }
}

/**
 * Returns the encapsulated result of the given [transform] function applied to the encapsulated [Throwable] exception
 * if this instance represents [failure][Result.isFailure] or the
 * original encapsulated value if it is [success][Result.isSuccess].
 *
 * This function catches any [Throwable] exception thrown by [transform] function and encapsulates it as a failure.
 * See [recover] for an alternative that rethrows exceptions.
 */
public inline fun <R, T : R> Result<T, Throwable>.recoverCatching(transform: (exception: Throwable) -> R): Result<R, Throwable> {
    return when (val exception = failureOrNull()) {
        null -> this
        else -> runCatching { transform(exception) }
    }
}
