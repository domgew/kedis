package io.github.domgew.kedis

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.runTest

/**
 * This is the pipeline client distilled to its coroutine expectations.
 * Its purpose is to test the necessary/expected coroutine contracts across targets and version.
 * Primarily it is introduced to see whether they are true and stay true in future version or be instructed to act.
 */
class CoroutinePipelineContractTest {

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun failsOnMiddleReceive() = runTest {
        val client = Client<Int, Int>(
            sendCallback = {
                delay(100)
            },
            receiveCallback = {
                if (it == 3) {
                    delay(50)
                    throw TestException(
                        id = "middleFail",
                    )
                }

                delay(150)
                return@Client it
            },
        )
        val results = (1..6)
            .map {
                Pair(
                    it,
                    client.enqueue(it),
                )
            }

        val ex = assertFailsWith<TestException> {
            client.execute()
        }

        assertEquals("middleFail", ex.id)

        for ((key, deferred) in results) {
            if (key < 3) {
                val result = deferred.getCompleted()
                assertEquals(key, result)
                continue
            }
            if (key == 3) {
                val ex = deferred.getCompletionExceptionOrNull()
                assertNotNull(ex)
                assertIs<TestException>(ex)
                assertEquals("middleFail", ex.id)
                continue
            }

            assertTrue(deferred.isCancelled)
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun cancelledExecution() = runTest {
        val client = Client<Int, Int>(
            sendCallback = {
                delay(100)
            },
            receiveCallback = {
                delay(150)
                return@Client it
            },
        )
        val results = (1..6)
            .map {
                Pair(
                    it,
                    client.enqueue(it),
                )
            }

        coroutineScope {
            val executeJob = launch {
                client.execute()
            }
            val cancelJob = launch {
                delay(400)
                executeJob.cancel()
            }
            try {
                executeJob.join()
            } catch (ex: CancellationException) {
                return@coroutineScope ex
            }
            cancelJob.cancel()
            return@coroutineScope null
        }

        for ((key, deferred) in results) {
            if (key < 3) {
                val result = deferred.getCompleted()
                assertEquals(key, result)
                continue
            }

            assertTrue(deferred.isCancelled)
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class, InternalCoroutinesApi::class)
    fun cancelledClient() = runTest {
        val client = Client<Int, Int>(
            sendCallback = {
                delay(100)
            },
            receiveCallback = {
                delay(150)
                return@Client it
            },
        )
        val results = (1..6)
            .map {
                Pair(
                    it,
                    client.enqueue(it),
                )
            }

        val ex = coroutineScope {
            val executeJob = launch {
                client.execute()
            }
            val cancelJob = launch {
                delay(400)
                client.cancel()
            }
            try {
                executeJob.join()
                if (executeJob.isCancelled) {
                    return@coroutineScope executeJob.getCancellationException()
                }
            } catch (ex: CancellationException) {
                return@coroutineScope ex
            } finally {
                cancelJob.cancel()
            }

            return@coroutineScope null
        }

        assertNotNull(ex)

        for ((key, deferred) in results) {
            if (key < 3) {
                val result = deferred.getCompleted()
                assertEquals(key, result)
                continue
            }

            assertTrue(deferred.isCancelled)
        }
    }

    private data class TestException(
        val id: String,
    ) : Exception()

    @OptIn(InternalCoroutinesApi::class)
    private class Client<K, V>(
        private val sendCallback: suspend (K) -> Unit,
        private val receiveCallback: suspend (K) -> V,
    ) {

        private var _fired = false
        private val _supervisor: CompletableJob = Job()
        private val _sync = SynchronizedObject()
        private val _queue = ArrayDeque<Queued<K, V>>()

        fun cancel() {
            synchronized(_sync) {
                _supervisor.cancel()
            }
        }

        fun enqueue(
            key: K,
        ): Deferred<V> =
            synchronized(_sync) {
                if (_fired) {
                    throw IllegalStateException()
                }
                if (!_supervisor.isActive) {
                    throw _supervisor.getCancellationException()
                }

                return@synchronized CompletableDeferred<V>(
                    parent = _supervisor,
                )
                    .also {
                        _queue.addLast(
                            Queued(
                                deferred = it,
                                key = key,
                            ),
                        )
                    }
            }

        suspend fun execute() {
            synchronized(_sync) {
                if (_fired) {
                    throw IllegalStateException()
                }
                if (!_supervisor.isActive) {
                    throw _supervisor.getCancellationException()
                }
                _fired = true
            }
            if (_queue.isEmpty()) {
                _supervisor.complete()
                return
            }

            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    _supervisor.cancel(
                        cause = it as? CancellationException,
                    )
                }

                try {
                    CoroutineScope(continuation.context + _supervisor).launch {
                        val sendReceiveCoordinatorMutex = Mutex()
                        val readQueue = ArrayDeque(_queue)

                        fun generallyFailed(
                            th: Throwable,
                        ) {
                            if (continuation.isActive) {
                                continuation.resumeWithException(
                                    exception = th,
                                )
                            }
                            if (_supervisor.isActive) {
                                _supervisor.completeExceptionally(
                                    exception = th,
                                )
                            }
                        }

                        fun itemFailed(
                            th: Throwable,
                            item: Queued<K, V>,
                        ) {
                            if (item.deferred.isActive) {
                                item.deferred
                                    .completeExceptionally(
                                        exception = th,
                                    )
                            }
                            generallyFailed(
                                th = th,
                            )
                        }

                        val writeJob = launch(
                            start = CoroutineStart.UNDISPATCHED,
                        ) {
                            try {
                                for (item in _queue.dequeueIterator()) {
                                    try {
                                        sendCallback(item.key)
                                    } catch (th: Throwable) {
                                        itemFailed(
                                            th = th,
                                            item = item,
                                        )
                                        return@launch
                                    }
                                }
                            } catch (th: Throwable) {
                                generallyFailed(
                                    th = th,
                                )
                                return@launch
                            }
                        }
                        val readJob = launch(
                            start = CoroutineStart.UNDISPATCHED,
                        ) {
                            try {
                                for (item in readQueue.dequeueIterator()) {
                                    try {
                                        item.deferred.complete(
                                            value = receiveCallback(
                                                item.key,
                                            ),
                                        )
                                    } catch (th: Throwable) {
                                        itemFailed(
                                            th = th,
                                            item = item,
                                        )
                                        return@launch
                                    }
                                }
                            } catch (th: Throwable) {
                                generallyFailed(
                                    th = th,
                                )
                                return@launch
                            }
                            if (continuation.isActive) {
                                continuation.resume(Unit)
                            }
                            if (_supervisor.isActive) {
                                _supervisor.complete()
                            }
                        }

                        writeJob.join()
                        readJob.join()
                    }
                } catch (th: Throwable) {
                    if (continuation.isActive) {
                        continuation.tryResumeWithException(th)
                    }
                    if (_supervisor.isActive) {
                        _supervisor.completeExceptionally(
                            exception = th,
                        )
                    }
                }
            }
        }

        private fun <T> ArrayDeque<T>.dequeueIterator(): Iterator<T> =
            iterator {
                while (!isEmpty()) {
                    yield(removeFirst())
                }
            }

        private class Queued<K, V>(
            val deferred: CompletableDeferred<V>,
            val key: K,
        )
    }
}
