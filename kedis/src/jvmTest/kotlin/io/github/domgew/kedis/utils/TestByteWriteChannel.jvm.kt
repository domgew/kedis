package io.github.domgew.kedis.utils

import io.ktor.util.moveToByteArray
import java.nio.ByteBuffer

actual class TestByteWriteChannel : ATestByteWriteChannel() {
    override suspend fun write(
        min: Int,
        block: (ByteBuffer) -> Unit,
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun writeAvailable(
        src: ByteBuffer,
    ): Int =
        src.remaining()
            .also {
                addWrittenBytes(
                    bytes = src.moveToByteArray(),
                )
            }

    override fun writeAvailable(
        min: Int,
        block: (ByteBuffer) -> Unit,
    ): Int {
        TODO("Not yet implemented")
    }

    override suspend fun writeFully(
        src: ByteBuffer,
    ) =
        addWrittenBytes(
            bytes = src.moveToByteArray(),
        )

    override suspend fun writeWhile(
        block: (ByteBuffer) -> Boolean,
    ) {
        TODO("Not yet implemented")
    }
}
