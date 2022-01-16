package me.jason5lee.resukt.tests

import kotlin.test.*
import me.jason5lee.resukt.*
import me.jason5lee.resukt.throwable.*

class ResultTest {

    class CustomException(message: String) : Exception(message) {
        override fun toString(): String = "CustomException: $message"
    }

    fun error(message: String): Nothing = throw CustomException(message)

    @Test
    fun testRunCatchingSuccess() {
        val ok = runCatching { "OK" }
        checkSuccess(ok, "OK", true)
    }

    @Test
    fun testRunCatchingFailure() {
        val fail = runCatching { error("F") }
        checkFailure(fail, "F", true)
    }

    @Test
    fun testConstructedSuccess() {
        val ok = Result.success("OK")
        checkSuccess(ok, "OK", true)
    }

    @Test
    fun testConstructedFailure() {
        val fail: Result<Unit, CustomException> = Result.failure(CustomException("F"))
        checkFailure(fail, "F", true)
    }

    private fun <T> checkSuccess(ok: Result<T, Throwable>, v: T, topLevel: Boolean = false) {
        assertTrue(ok.isSuccess)
        assertFalse(ok.isFailure)
        assertEquals(v, ok.getOrThrow())
        assertEquals(v, ok.getOrElse { throw it })
        assertEquals(v, ok.getOrNull())
        assertEquals(v, ok.getOrElse { null })
        assertEquals(v, ok.getOrDefault("DEF"))
        assertEquals(v, ok.getOrElse { "EX:$it" })
        assertEquals("V:$v", ok.fold({ "V:$it" }, { "EX:$it" }))
        assertEquals(null, ok.failureOrNull())
        assertEquals(null, ok.fold(onSuccess = { null }, onFailure = { it }))
        assertEquals("Success($v)", ok.toString())
        assertEquals(ok, ok)
        if (topLevel) {
            checkSuccess(ok.map { 42 }, 42)
            checkSuccess(ok.mapCatching { 42 }, 42)
            checkFailure(ok.mapCatching { error("FAIL") }, "FAIL")
            checkSuccess(ok.recover { 42 }, "OK")
            checkSuccess(ok.recoverCatching { 42 }, "OK")
            checkSuccess(ok.recoverCatching { error("FAIL") }, "OK")
            checkSuccess(ok.andThen { Result.success(42) }, 42)
            checkFailure(ok.andThen { Result.failure(CustomException("FAIL")) }, "FAIL")
        }
        var sCnt = 0
        var fCnt = 0
        assertEquals(ok, ok.onSuccess { sCnt++ })
        assertEquals(ok, ok.onFailure { fCnt++ })
        assertEquals(1, sCnt)
        assertEquals(0, fCnt)
    }

    private fun <T> checkFailure(fail: Result<T, Throwable>, msg: String, topLevel: Boolean = false) {
        assertFalse(fail.isSuccess)
        assertTrue(fail.isFailure)
        assertFails { fail.getOrThrow() }
        assertFails { fail.getOrElse { throw it } }
        assertEquals(null, fail.getOrNull())
        assertEquals(null, fail.getOrElse { null })
        assertEquals("DEF", fail.getOrDefault("DEF"))
        assertEquals("EX:CustomException: $msg", fail.getOrElse { "EX:$it" })
        assertEquals("EX:CustomException: $msg", fail.fold({ "V:$it" }, { "EX:$it" }))
        assertEquals(msg, fail.failureOrNull()!!.message)
        assertEquals(msg, fail.fold(onSuccess = { null }, onFailure = { it })!!.message)
        assertEquals("Failure(CustomException: $msg)", fail.toString())
        assertEquals(fail, fail)
        if (topLevel) {
            checkFailure(fail.map { 42 }, msg)
            checkFailure(fail.mapCatching { 42 }, msg)
            checkFailure(fail.mapCatching { error("FAIL") }, msg)
            checkSuccess(fail.recover { 42 }, 42)
            checkSuccess(fail.recoverCatching { 42 }, 42)
            checkFailure(fail.recoverCatching { error("FAIL") }, "FAIL")
            checkFailure(fail.andThen { Result.success(42) }, msg)
            checkFailure(fail.andThen { Result.failure(CustomException("FAIL")) }, msg)
        }
        var sCnt = 0
        var fCnt = 0
        assertEquals(fail, fail.onSuccess { sCnt++ })
        assertEquals(fail, fail.onFailure { fCnt++ })
        assertEquals(0, sCnt)
        assertEquals(1, fCnt)
    }
}