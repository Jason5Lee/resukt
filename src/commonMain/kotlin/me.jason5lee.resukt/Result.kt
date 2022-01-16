@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")

package me.jason5lee.resukt

import kotlin.jvm.JvmField
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmName

/**
 * A Result type representing either a successful outcome with a value of type [T]
 * or a failed outcome with a failure of type [F].
 */
@JvmInline
public value class Result<out T, out F: Any> @PublishedApi internal constructor(
    @PublishedApi
    internal val value: Any?
) {
    /**
     * Returns `true` if this instance represents a successful outcome.
     * In this case [isFailure] returns `false`.
     */
    public val isSuccess: Boolean get() = value !is Failure

    /**
     * Returns `true` if this instance represents a failed outcome.
     * In this case [isSuccess] returns `false`.
     */
    public val isFailure: Boolean get() = value is Failure


    // value & failure retrieval

    /**
     * Returns the encapsulated value if this instance represents [success][Result.isSuccess] or `null`
     * if it is [failure][Result.isFailure].
     *
     * This function is a shorthand for `getOrElse { null }` (see [getOrElse]) or
     * `fold(onSuccess = { it }, onFailure = { null })` (see [fold]).
     */
    public inline fun getOrNull(): T? =
        when {
            isFailure -> null
            else -> value as T
        }

    /**
     * Returns the encapsulated failure if this instance represents [failure][isFailure] or `null`
     * if it is [success][isSuccess].
     *
     * This function is a shorthand for `fold(onSuccess = { null }, onFailure = { it })` (see [fold]).
     */
    public fun failureOrNull(): F? =
        when (value) {
            is Failure -> value.failure as F
            else -> null
        }

    /**
     * Returns a string `Success(v)` if this instance represents [success][Result.isSuccess]
     * where `v` is a string representation of the value or a string `Failure(x)` if
     * it is [failure][isFailure] where `x` is a string representation of the failure.
     */
    public override fun toString(): String =
        when (value) {
            is Failure -> value.toString() // "Failure($failure)"
            else -> "Success($value)"
        }


    // companion with constructors

    /**
     * Companion object for [Result] class that contains its constructor functions
     * [success] and [failure].
     */
    public companion object {
        /**
         * Returns an instance that encapsulates the given [value] as successful value.
         */
        @JvmName("success")
        public inline fun <T> success(value: T): Result<T, Nothing> =
            Result(value)

        /**
         * Returns an instance that encapsulates the given failure.
         */
        @JvmName("failure")
        public inline fun <F: Any> failure(failure: F): Result<Nothing, F> =
            Result(createFailure(failure))
    }

    internal class Failure(
        @JvmField
        val failure: Any
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Failure

            if (failure != other.failure) return false

            return true
        }

        override fun hashCode(): Int {
            return failure.hashCode()
        }

        override fun toString(): String {
            return "Failure($failure)"
        }
    }
}

/**
 * Throws [AssertionError] if the result is failure. This internal function minimizes
 * inlined bytecode for [assertSuccess].
 */
@PublishedApi
internal fun Result<*, *>.throwAssertionErrorOnFailure() {
    if (value is Result.Failure) throw AssertionError("expected success but was `$this`")
}

/**
 * Creates an instance of internal marker [Result.Failure] class to
 * make sure that this class is not exposed in ABI.
 */
@PublishedApi
internal fun <F: Any> createFailure(failure: F): Any =
    Result.Failure(failure)

// -- extensions ---

/**
 * Asserts this instance represents [success][Result.isSuccess].
 * If it doesn't, throws [AssertionError].
 */
public inline fun <T> Result<T, *>.assertSuccess(): T {
    throwAssertionErrorOnFailure()
    return value as T
}

/**
 * Returns the encapsulated value if this instance represents [success][Result.isSuccess] or the
 * result of [onFailure] function for the encapsulated failure if it is [failure][Result.isFailure].
 *
 * This function is a shorthand for `fold(onSuccess = { it }, onFailure = onFailure)` (see [fold]).
 */
public inline fun <R, T : R, F: Any> Result<T, F>.getOrElse(onFailure: (failure: F) -> R): R {
//    contract {
//        callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
//    }
    return when (val failure = failureOrNull()) {
        null -> value as T
        else -> onFailure(failure)
    }
}

/**
 * Returns the encapsulated value if this instance represents [success][Result.isSuccess] or the
 * result of [onFailure] function for this instance as type `Result<T, Nothing>` if it is [failure][Result.isFailure].
 *
 * A common use of this function is `result.whenFailure { return it }` which gets the successful value
 * or returns current failure result. If you know Rust, `.whenFailure { return it }` has a similar effect to `?` in Rust.
 */
public inline fun <R, T : R, F: Any> Result<T, F>.whenFailure(onFailure: (result: Result<Nothing, F>) -> R): R {
//    contract {
//        callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
//    }
    return if (isSuccess) {
        value as T
    } else {
        onFailure(this as Result<Nothing, F>)
    }
}

/**
 * Returns the encapsulated value if this instance represents [success][Result.isSuccess] or the
 * [defaultValue] if it is [failure][Result.isFailure].
 *
 * This function is a shorthand for `getOrElse { defaultValue }` (see [getOrElse]).
 */
public inline fun <R, T : R> Result<T, *>.getOrDefault(defaultValue: R): R {
    if (isFailure) return defaultValue
    return value as T
}

/**
 * Returns the encapsulated result of the given [transform] function applied to the encapsulated failure
 * if this instance represents [failure][Result.isFailure] or the
 * original encapsulated value if it is [success][Result.isSuccess].
 */
public inline fun <R, T : R, F: Any> Result<T, F>.recover(transform: (failure: F) -> R): Result<R, F> {
//    contract {
//        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
//    }
    return when (val failure = failureOrNull()) {
        null -> this
        else -> Result.success(transform(failure))
    }
}

/**
 * Returns the result of [onSuccess] for the encapsulated value if this instance represents [success][Result.isSuccess]
 * or the result of [onFailure] function for the encapsulated failure if it is [failure][Result.isFailure].
 */
public inline fun <R, T, F: Any> Result<T, F>.fold(
    onSuccess: (value: T) -> R,
    onFailure: (failure: F) -> R
): R {
//    contract {
//        callsInPlace(onSuccess, InvocationKind.AT_MOST_ONCE)
//        callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
//    }
    return when (val failure = failureOrNull()) {
        null -> onSuccess(value as T)
        else -> onFailure(failure)
    }
}

// transformation

/**
 * Returns the encapsulated result of the given [transform] function applied to the encapsulated value
 * if this instance represents [success][Result.isSuccess] or the
 * original encapsulated failure if it is [failure][Result.isFailure].
 */
public inline fun <R, T, F: Any> Result<T, F>.map(transform: (value: T) -> R): Result<R, F> {
//    contract {
//        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
//    }
    return when {
        isSuccess -> Result.success(transform(value as T))
        else -> Result(value)
    }
}

/**
 * Returns the given [transform] function applied to the encapsulated value
 * if this instance represents [success][Result.isSuccess] or the
 * original encapsulated failure if it is [failure][Result.isFailure].
 */
public inline fun <R, T, F: Any> Result<T, F>.andThen(transform: (value: T) -> Result<R, F>): Result<R, F> {
    return when {
        isSuccess -> transform(value as T)
        else -> Result(value)
    }
}

// "peek" onto value/exception and pipe

/**
 * Performs the given [action] on the encapsulated failure if this instance represents [failure][Result.isFailure].
 * Returns the original `Result` unchanged.
 */
public inline fun <T, F: Any> Result<T, F>.onFailure(action: (failure: F) -> Unit): Result<T, F> {
//    contract {
//        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
//    }
    failureOrNull()?.let { action(it) }
    return this
}

/**
 * Performs the given [action] on the encapsulated value if this instance represents [success][Result.isSuccess].
 * Returns the original `Result` unchanged.
 */
public inline fun <T, F: Any> Result<T, F>.onSuccess(action: (value: T) -> Unit): Result<T, F> {
//    contract {
//        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
//    }
    if (isSuccess) action(value as T)
    return this
}

// -------------------
