package com.bbou.coroutines.task2

import kotlinx.coroutines.*
import org.testng.annotations.Test

class MyTaskTests {

    @Test(expectedExceptions = [IllegalStateException::class])
    fun taskRepeatTest() {
        val t = MyTask()
        run(t)
        println("-----")
        run(t)
    }

    @Test(expectedExceptions = [IllegalStateException::class])
    fun baseTaskRepeatTest() {
        val t = MyBaseTask()
        run(t)
        println("-----")
        run(t)
    }

    @Test
    fun baseTaskTest() {
        val b = MyBaseTask()
        run(b)
    }

    @Test
    fun taskTest() {
        val t = MyTask()
        run(t)
    }

    @Test
    fun baseTaskCancelTest() {
        val b = MyBaseTask()
        runCancel(b)
    }

    @Test
    fun taskCancelTest() {
        val t = MyTask()
        runCancel(t)
    }

    private fun run(t: MyTask) {

        try {
            runBlocking {

                println("Run ${where()}")

                val result = t.run(Dispatchers.IO, Parameters(10, 1000))
                println("Done '$result' ${where()}")
            }
            println("End ${where()}")
        } catch (ce: CancellationException) {
            println("Catch $ce ${where()}")
        } finally {
            println("Exit ${where()}")
        }
    }

    private fun run(t: MyBaseTask) {

        try {
            runBlocking {

                println("Run ${where()}")

                val result = t.run(Dispatchers.IO, {}, ::println, Parameters(10, 1000))
                println("Done '$result' ${where()}")
            }
            println("End ${where()}")
        } catch (ce: CancellationException) {
            println("Catch $ce ${where()}")
        } finally {
            println("Exit ${where()}")
        }
    }

    private fun runCancel(t: MyTask) {

        try {
            runBlocking {

                println("Run ${where()}")

                launch {
                    delay(5000L)
                    println("Cancel now ${where()}")
                    t.cancel()
                }

                val result = t.run(Dispatchers.IO, Parameters(times = 10, lapse = 1000))
                println("Done '$result' ${where()}")
            }
            println("End ${where()}")
        } catch (ce: CancellationException) {
            println("Catch $ce ${where()}")
        } finally {
            println("Exit ${where()}")
        }
    }

    private fun runCancel(t: MyBaseTask) {

        try {
            runBlocking {

                println("Run ${where()}")

                launch {
                    delay(5000L)
                    println("Cancel now ${where()}")
                    t.cancel()
                }

                val result = t.run(Dispatchers.IO, {}, ::println, Parameters(times = 10, lapse = 1000))
                println("Done '$result' ${where()}")
            }
            println("End ${where()}")
        } catch (ce: CancellationException) {
            println("Catch $ce ${where()}")
        } finally {
            println("Exit ${where()}")
        }
    }
}