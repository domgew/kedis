package io.github.domgew.kedis.utils

import io.ktor.utils.io.core.Buffer
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes

@OptIn(ExperimentalForeignApi::class)
actual class TestByteWriteChannel : ATestByteWriteChannel() {
    override fun writeAvailable(
        min: Int,
        block: (Buffer) -> Unit,
    ): Int =
        TODO("Not yet implemented")

    override suspend fun writeAvailable(
        src: CPointer<ByteVar>,
        offset: Int,
        length: Int,
    ): Int =
        length
            .also {
                addWrittenBytes(
                    bytes = src.readBytes(offset + length),
                    offset = offset,
                    length = length,
                )
            }

    override suspend fun writeAvailable(
        src: CPointer<ByteVar>,
        offset: Long,
        length: Long,
    ): Int =
        length.toInt()
            .also {
                addWrittenBytes(
                    bytes = src.readBytes((offset + length).toInt()),
                    offset = offset.toInt(),
                    length = length.toInt(),
                )
            }

    override suspend fun writeFully(
        src: CPointer<ByteVar>,
        offset: Int,
        length: Int,
    ) =
        addWrittenBytes(
            bytes = src.readBytes(offset + length),
            offset = offset,
            length = length,
        )

    override suspend fun writeFully(
        src: CPointer<ByteVar>,
        offset: Long,
        length: Long,
    ) =
        addWrittenBytes(
            bytes = src.readBytes((offset + length).toInt()),
            offset = offset.toInt(),
            length = length.toInt(),
        )
}
