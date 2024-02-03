package com.bbou.coroutines.task2

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer

interface ProgressEmitter<T> {
    suspend fun emitProgress(progress: T)
}

abstract class BaseTask2<Param, Progress, Result> : ProgressEmitter<Progress> {

    /**
     * Status
     */
    private var hasRun = AtomicBoolean(false)

    /**
     * Job (prejob + background job)
     */
    private lateinit var job: Job

    /**
     * Worker
     */
    private lateinit var worker: Deferred<Result>

    /**
    Observer
     */
    private lateinit var observer: Job

    /**
    Progress Channel
     */
    private var progressChannel = Channel<Progress>()

    // A P I

    abstract suspend fun doInBackground(params: Param?): Result

    open fun onCancelled() {}

    // R U N

    /**
     * Execute
     * @param dispatcher dispatcher for worker
     * @param pre pre execution block
     * @param progressConsumer progress consumer
     * @param params parameters for bg worker
     * @return result
     */
    suspend fun runAsync(
        dispatcher: CoroutineDispatcher,
        pre: Runnable,
        progressConsumer: Consumer<Progress>,
        params: Param?
    ): Pair<Job, Deferred<Result?>> {

        // check
        if (hasRun.getAndSet(true)) {
            throw IllegalStateException("The task has already run.")
        }

        // run task
        coroutineScope {

            // observer
            observer = launch {
                for (progress in progressChannel) {
                    progressConsumer.accept(progress)
                }
            }

            // run pre job
            job = launch {
                pre.run()

                // run bg job
                withContext(dispatcher) {
                    worker = async {
                        val d = doInBackground(params)
                        progressChannel.close()
                        d
                    }
                }
            }
        }

        // (job, deferred result)
        return job to worker
    }

    /**
     * Execute
     * @param dispatcher dispatcher for worker
     * @param pre pre execution block
     * @param progressConsumer progress consumer
     * @param params parameters for bg worker
     * @return result
     */
    suspend fun run(
        dispatcher: CoroutineDispatcher,
        pre: Runnable,
        progressConsumer: Consumer<Progress>,
        params: Param?
    ): Result? {

        val jobs = runAsync(dispatcher, pre, progressConsumer, params)

        // join out job
        jobs.first.join()

        // result
        return jobs.second.await()
    }

    /**
     * Execute
     * @param dispatcher dispatcher for worker
     * @param pre pre execution block
     * @param progressConsumer progress consumer
     * @param resultConsumer result consumer
     * @param params parameters for job
     * @return result
     */
    suspend fun run(
        dispatcher: CoroutineDispatcher,
        pre: Runnable,
        progressConsumer: Consumer<Progress>,
        resultConsumer: Consumer<Result?>,
        params: Param?
    ): Result? {
        val r: Result? = run(dispatcher, pre, progressConsumer, params)
        resultConsumer.accept(r)
        return r
    }

    /**
     * Publish progress
     * @param progress progress
     */
    override suspend fun emitProgress(progress: Progress) {
        progressChannel.send(progress)
    }

    /**
     * Cancel
     */
    suspend fun cancel() {

        println("Cancel")

        progressChannel.cancel()
        worker.cancelAndJoin()
        job.cancelAndJoin()

        // call back
        onCancelled()
    }
}

