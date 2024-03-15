@file:Suppress("DEPRECATION")

package io.github.domgew.kedis.utils

import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.WriterSuspendSession
import io.ktor.utils.io.bits.Memory
import io.ktor.utils.io.bits.copyTo
import io.ktor.utils.io.core.Buffer
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.internal.ChunkBuffer
import io.ktor.utils.io.core.readBytes

@Suppress("MemberVisibilityCanBePrivate")
abstract class ATestByteWriteChannel : ByteWriteChannel {
    private val writtenData = ArrayList<Byte>()

    fun getAndRestWithoutLocking(): ByteArray =
        writtenData.toByteArray()
            .also {
                writtenData.clear()
            }

    protected fun addWrittenByte(
        byte: Byte,
    ) {
        writtenData.add(byte)
    }

    protected fun addWrittenBytes(
        bytes: Collection<Byte>,
    ) {
        writtenData.addAll(bytes)
    }

    protected fun addWrittenBytes(
        bytes: ByteArray,
        offset: Int,
        length: Int,
    ) =
        addWrittenBytes(
            bytes = bytes.slice(
                offset until (offset + length),
            ),
        )

    protected fun addWrittenBytes(
        bytes: ByteArray,
    ) =
        addWrittenBytes(
            bytes = bytes.asList(),
        )

    override val availableForWrite: Int
        get() = 1
    override val isClosedForWrite: Boolean
        get() = false
    override val autoFlush: Boolean
        get() = false
    override val totalBytesWritten: Long
        get() = 1
    override val closedCause: Throwable?
        get() = null

    override suspend fun writeAvailable(
        src: ByteArray,
        offset: Int,
        length: Int,
    ): Int =
        length
            .also {
                addWrittenBytes(src, offset, length)
            }

    override suspend fun writeAvailable(
        src: ChunkBuffer,
    ): Int =
        src.writeRemaining
            .also {
                addWrittenBytes(src.readBytes(it))
            }

    override suspend fun writeFully(
        src: ByteArray,
        offset: Int,
        length: Int,
    ) =
        addWrittenBytes(src, offset, length)

    override suspend fun writeFully(
        src: Buffer,
    ) =
        addWrittenBytes(
            bytes = src.readBytes(src.writeRemaining),
        )

    override suspend fun writeFully(
        memory: Memory,
        startIndex: Int,
        endIndex: Int,
    ) =
        addWrittenBytes(
            bytes = ByteArray(endIndex - startIndex + 1)
                .also {
                    memory.copyTo(
                        destination = it,
                        offset = startIndex,
                        length = endIndex - startIndex + 1,
                    )
                },
        )

    @Deprecated(
        message = "Use write { } instead.",
        replaceWith = ReplaceWith(
            expression = "TODO(\"Not yet implemented\")",
        ),
    )
    override suspend fun writeSuspendSession(
        visitor: suspend WriterSuspendSession.() -> Unit,
    ) =
        TODO("Not yet implemented")

    override suspend fun writePacket(
        packet: ByteReadPacket,
    ) =
        addWrittenBytes(packet.readBytes())

    override suspend fun writeLong(l: Long) =
        TODO("Not yet implemented")

    override suspend fun writeInt(i: Int) =
        TODO("Not yet implemented")

    override suspend fun writeShort(s: Short) =
        TODO("Not yet implemented")

    override suspend fun writeByte(b: Byte) =
        addWrittenByte(b)

    override suspend fun writeDouble(d: Double) =
        TODO("Not yet implemented")

    override suspend fun writeFloat(f: Float) =
        TODO("Not yet implemented")

    override suspend fun awaitFreeSpace() {
    }

    override fun close(
        cause: Throwable?,
    ): Boolean =
        true

    override fun flush() {
    }
}
