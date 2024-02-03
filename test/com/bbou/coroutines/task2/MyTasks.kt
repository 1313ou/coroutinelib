package com.bbou.coroutines.task2

import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.yield
import kotlin.coroutines.coroutineContext

data class Parameters(val times: Int, val lapse: Long)

private fun longBlocking(howlong: Long) {
    try {
        Thread.sleep(howlong)
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }
}

suspend fun coreWorker(emitter: ProgressEmitter<Int>, params: Parameters?): String? {

    val job = coroutineContext[Job] ?: return null
    println("Job> ${where()}")
    for (count in 1..params!!.times) {
        job.ensureActive() // checks for cancellation
        longBlocking(params.lapse)
        emitter.emitProgress(count)
        yield() // checks for cancellation
    }
    println("Job< ${where()}")
    return "Kilroy was there"
}

class MyBaseTask : BaseTask2<Parameters, Int, String?>() {

    override suspend fun doInBackground(params: Parameters?): String? {
        return coreWorker(this, params)
    }
}

class MyTask : Task<Parameters, Int, String?>() {

    override suspend fun doInBackground(params: Parameters?): String? {
        return coreWorker(this, params)
    }

    override fun onPostExecute(result: String?) {
        println("Post execute '${result}' ${where()}")
        println(result)
    }

    override fun onPreExecute() {
        println("Pre execute ${where()}")
    }

    override fun onProgressUpdate(progress: Int?) {
        progress?.let { println("progress $it ${where()}") }
    }

    override fun onCancelled() {
        println("cancelled ${where()}")
    }
}

fun where(): String {
    return " @ ${Thread.currentThread()}"
}