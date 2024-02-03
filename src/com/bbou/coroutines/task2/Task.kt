package com.bbou.coroutines.task2

import kotlinx.coroutines.CoroutineDispatcher

/**
 * With no consumer, the standard callbacks
 */
abstract class Task<Param, Progress, Result> : BaseTask2<Param, Progress, Result>() {

    // API extension

    open fun onPreExecute() {}
    open fun onPostExecute(result: Result?) {}
    open fun onProgressUpdate(progress: Progress?) {
        println(progress)
    }

    /**
     * Execute implementation
     * @param dispatcher dispatcher for job
     * @param params parameters for job
     */
    suspend fun run(dispatcher: CoroutineDispatcher, params: Param?) : Result? {
        return run(
            dispatcher,
            { onPreExecute() },
            { progress -> onProgressUpdate(progress) },
            { result -> onPostExecute(result) },
            params
        )
    }
}
