package io.github.domgew.kedis.utils

import io.ktor.utils.io.ByteWriteChannel

interface CollapsableTestByteWriteChannel : ByteWriteChannel {
    fun getAndRestWithoutLocking(): ByteArray
}

expect class CByteWriteChannel() : CollapsableTestByteWriteChannel
