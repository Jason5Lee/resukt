package me.jason5lee.resukt.ext

import me.jason5lee.resukt.*

public fun <T> T.asSuccess(): Result<T, Nothing> = Result.success(this)
public fun <F: Any> F.asFailure(): Result<Nothing, F> = Result.failure(this)