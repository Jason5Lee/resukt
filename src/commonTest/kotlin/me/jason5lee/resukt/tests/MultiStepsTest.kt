package me.jason5lee.resukt.tests

import kotlin.test.*
import me.jason5lee.resukt.*

class MultiStepsTest {
    private fun Steps.multiStepsFunction(): Result<Int, String> {
        val result =
            step1().whenFailure { return it } +
            step2().whenFailure { return it } +
            step3().whenFailure { return it }
        return Result.success(result)
    }
    @Test
    fun testMultiStepsFunction() {
        testMultipleSteps { multiStepsFunction() }
    }

    @Test
    fun testMultiStepsLambda() {
        testMultipleSteps lambda@{
            val result =
                step1().whenFailure { return@lambda it } +
                step2().whenFailure { return@lambda it } +
                step3().whenFailure { return@lambda it }
            Result.success(result)
        }
    }

    @Test
    fun testMultiStepsAndThen() {
        testMultipleSteps {
            step1().andThen { step1 ->
                val result = step1 +
                    step2().whenFailure { return@andThen it } +
                    step3().whenFailure { return@andThen it }
                Result.success(result)
            }
        }
    }

    private inline fun testMultipleSteps(f: Steps.() -> Result<Int, String>) {
        for (step in Steps.allSteps) {
            assertEquals(step.expectedResult, f(step))
        }
    }

    private class Steps private constructor(
        private val step1: Result<Int, String>,
        private val step2: Result<Int, String>,
        private val step3: Result<Int, String>,
        val expectedResult: Result<Int, String>,
    ) {
        companion object {
            val allSteps: Array<Steps>

            init {
                val step1Fail = Result.failure("step1 failed")
                val step2Fail = Result.failure("step2 failed")
                val step3Fail = Result.failure("step3 failed")
                val success = Result.success(1)

                allSteps = arrayOf(
                    Steps(step1Fail, step2Fail, step3Fail, step1Fail),
                    Steps(step1Fail, step2Fail, success, step1Fail),
                    Steps(step1Fail, success, step3Fail, step1Fail),
                    Steps(step1Fail, success, success, step1Fail),
                    Steps(success, step2Fail, step3Fail, step2Fail),
                    Steps(success, step2Fail, success, step2Fail),
                    Steps(success, success, step3Fail, step3Fail),
                    Steps(success, success, success, Result.success(3)),
                )
            }
        }
        fun step1(): Result<Int, String> = step1
        fun step2(): Result<Int, String> = step2
        fun step3(): Result<Int, String> = step3
    }
}