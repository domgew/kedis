package io.github.domgew.kedis.impl

import io.github.domgew.kedis.KedisPipelineClient
import io.github.domgew.kedis.commands.KedisCommand
import io.github.domgew.kedis.commands.KedisFullCommand
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex

@OptIn(InternalCoroutinesApi::class)
internal class DefaultKedisPipelineClient(
    private val client: Lazy<BaseKedisClient>,
) : KedisPipelineClient {

    private var _fired = false
    private val _supervisor: CompletableJob = Job()
    private val _sync = SynchronizedObject()
    private val _queue = ArrayDeque<Queued>()

    override fun cancel() {
        synchronized(_sync) {
            _supervisor.cancel()
        }
    }

    override fun <T> enqueue(
        command: KedisCommand<T>,
    ): Deferred<T> =
        synchronized(_sync) {
            if (_fired) {
                alreadyFired()
            }
            if (!_supervisor.isActive) {
                throw _supervisor.getCancellationException()
            }

            return@synchronized CompletableDeferred<T>(
                parent = _supervisor,
            )
                .also { deferred ->
                    _queue.addLast(
                        Queued.Command(
                            deferred = deferred,
                            command = command as KedisFullCommand<T>,
                        ),
                    )
                }
        }

    override fun enqueueSend() {
        synchronized(_sync) {
            if (_fired) {
                alreadyFired()
            }
            if (!_supervisor.isActive) {
                throw _supervisor.getCancellationException()
            }

            _queue.addLast(
                Queued.FlushHint,
            )
        }
    }

    override suspend fun execute() {
        synchronized(_sync) {
            if (_fired) {
                alreadyFired()
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

        client.value.useExclusively { client ->
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    _supervisor.cancel(
                        it as? CancellationException,
                    )
                }

                try {
                    CoroutineScope(continuation.context + _supervisor).launch {
                        val readQueue = ArrayDeque(_queue)
                        val flushCoordinatorMutex = Mutex(
                            locked = true,
                        )

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
                            item: Queued,
                        ) {
                            if (
                                item is Queued.Command<*>
                                && item.deferred.isActive
                            ) {
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
                                        when (item) {
                                            Queued.FlushHint -> {
                                                client.flush()
                                                flushCoordinatorMutex.lock()
                                            }

                                            is Queued.Command<*> -> {
                                                client.write(
                                                    command = item.command,
                                                    flush = false,
                                                )
                                            }
                                        }
                                    } catch (th: Throwable) {
                                        itemFailed(
                                            th = th,
                                            item = item,
                                        )
                                        return@launch
                                    }
                                }
                                client.flush()
                            } catch (th: Throwable) {
                                generallyFailed(
                                    th = th,
                                )
                            }
                        }
                        val readJob = launch(
                            start = CoroutineStart.UNDISPATCHED,
                        ) {
                            try {
                                for (item in readQueue.dequeueIterator()) {
                                    try {
                                        when (item) {
                                            Queued.FlushHint -> {
                                                flushCoordinatorMutex.unlock()
                                            }

                                            is Queued.Command<*> -> {
                                                item.read(
                                                    client = client,
                                                )
                                            }
                                        }
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
                        continuation.resumeWithException(th)
                    }
                    if (_supervisor.isActive) {
                        _supervisor.completeExceptionally(
                            exception = th,
                        )
                    }
                }
            }
        }
    }

    private fun <T> ArrayDeque<T>.dequeueIterator(): Iterator<T> =
        iterator {
            while (isNotEmpty()) {
                yield(
                    value = removeFirst(),
                )
            }
        }

    private fun alreadyFired(): Nothing {
        throw IllegalStateException("Pipeline already launched")
    }

    private sealed class Queued {

        class Command<T>(
            val deferred: CompletableDeferred<T>,
            val command: KedisFullCommand<T>,
        ) : Queued() {

            suspend fun read(
                client: BaseKedisClient.UsableClient,
            ) {
                deferred.complete(
                    value = client.read(
                        command = command,
                    ),
                )
            }
        }

        data object FlushHint : Queued()
    }
}
