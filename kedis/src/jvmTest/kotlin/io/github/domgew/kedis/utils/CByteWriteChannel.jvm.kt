package io.github.domgew.kedis.utils

import io.ktor.utils.io.WriterSuspendSession
import io.ktor.utils.io.bits.Memory
import io.ktor.utils.io.bits.copyTo
import io.ktor.utils.io.core.Buffer
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.internal.ChunkBuffer
import io.ktor.utils.io.core.readBytes
import java.nio.ByteBuffer

actual class CByteWriteChannel : CollapsableTestByteWriteChannel {
    private val writtenData = ArrayList<Byte>()

    override fun getAndRestWithoutLocking(): ByteArray =
        writtenData.toByteArray()
            .also {
                writtenData.clear()
            }

    override val autoFlush: Boolean
        get() = true
    override val availableForWrite: Int
        get() = 1
    override val closedCause: Throwable?
        get() = null
    override val isClosedForWrite: Boolean
        get() = false
    override val totalBytesWritten: Long
        get() = 1

    override suspend fun awaitFreeSpace() {
    }

    override fun close(cause: Throwable?): Boolean {
        return true
    }

    override fun flush() {
    }

    override suspend fun write(
        min: Int,
        block: (ByteBuffer) -> Unit,
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun writeAvailable(src: ChunkBuffer): Int {
        val remaining = src.writeRemaining
        writeFully(src)

        return remaining
    }

    override suspend fun writeAvailable(src: ByteBuffer): Int {
        TODO("Not yet implemented")
    }

    override suspend fun writeAvailable(
        src: ByteArray,
        offset: Int,
        length: Int,
    ): Int {
        writeFully(src, offset, length)
        return src.size
    }

    override fun writeAvailable(
        min: Int,
        block: (ByteBuffer) -> Unit,
    ): Int {
        TODO("Not yet implemented")
    }

    override suspend fun writeByte(b: Byte) {
        val bytes = ByteArray(1)
        bytes[0] = b
        writeFully(bytes, 0, 1)
    }

    override suspend fun writeDouble(d: Double) {
        TODO("Not yet implemented")
    }

    override suspend fun writeFloat(f: Float) {
        TODO("Not yet implemented")
    }

    override suspend fun writeFully(
        memory: Memory,
        startIndex: Int,
        endIndex: Int,
    ) {
        val bytes = ByteArray(endIndex - startIndex)
        memory.copyTo(bytes, startIndex, endIndex)

        writeFully(bytes, 0, bytes.size)
    }

    override suspend fun writeFully(src: Buffer) {
        val remaining = src.writeRemaining
        val bytes = src.readBytes(remaining)
        writeFully(bytes, 0, bytes.size)
    }

    override suspend fun writeFully(src: ByteBuffer) {
        val bytes = src.array()
        writeFully(bytes, 0, bytes.size)
    }

    override suspend fun writeFully(
        src: ByteArray,
        offset: Int,
        length: Int,
    ) {
        val bytes = src.slice(offset until (offset + length))
        writtenData.addAll(bytes)
    }

    override suspend fun writeInt(i: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun writeLong(l: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun writePacket(packet: ByteReadPacket) {
        val bytes = packet.readBytes()
        writeFully(bytes, 0, bytes.size)
    }

    override suspend fun writeShort(s: Short) {
        TODO("Not yet implemented")
    }

    override suspend fun writeSuspendSession(visitor: suspend WriterSuspendSession.() -> Unit) {
        TODO("Not yet implemented")
    }

    override suspend fun writeWhile(block: (ByteBuffer) -> Boolean) {
        TODO("Not yet implemented")
    }
}
