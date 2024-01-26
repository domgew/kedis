package io.github.domgew.kedis.impl

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import kotlin.reflect.KClass

internal sealed class RedisMessage {
    abstract suspend fun writeTo(outgoing: ByteWriteChannel)

    class ParsingException(
        message: String,
        type: KClass<*>,
    ) : Exception("Could not parse $type: $message")

    data class SimpleStringMessage(
        override val value: String,
    ) : StringMessage() {
        override suspend fun writeTo(outgoing: ByteWriteChannel) {
            outgoing.writeFully("+$value\r\n".encodeToByteArray())
        }

        companion object {
            const val TYPE_BYTE: Byte = 43 // '+'

            // "+OK\r\n" -> "OK"
            suspend fun parse(incoming: ByteReadChannel): SimpleStringMessage {
                val bytes = readUntilCR(incoming)
                verifyLFByte<SimpleStringMessage>(incoming)

                return SimpleStringMessage(
                    value = bytes.decodeToString(),
                )
            }
        }
    }

    data class SimpleErrorMessage(
        override val value: String,
    ) : ErrorMessage() {
        override suspend fun writeTo(outgoing: ByteWriteChannel) {
            outgoing.writeFully("-$value\r\n".encodeToByteArray())
        }

        companion object {
            const val TYPE_BYTE: Byte = 45 // '-'

            // "-Error message\r\n" -> "Error message"
            suspend fun parse(incoming: ByteReadChannel): SimpleErrorMessage {
                val bytes = readUntilCR(incoming)
                verifyLFByte<SimpleErrorMessage>(incoming)

                return SimpleErrorMessage(
                    value = bytes.decodeToString(),
                )
            }
        }
    }

    data class IntegerMessage(
        val value: Long,
    ) : NumericMessage() {
        override suspend fun writeTo(outgoing: ByteWriteChannel) {
            outgoing.writeFully(":$value\r\n".encodeToByteArray())
        }

        companion object {
            const val TYPE_BYTE: Byte = 58 // ':'

            // ":[<+|->]<value>\r\n" -> [+|-]value
            suspend fun parse(incoming: ByteReadChannel): IntegerMessage {
                val bytes = readUntilCR(incoming)
                verifyLFByte<IntegerMessage>(incoming)

                return IntegerMessage(
                    value = bytes.decodeToString()
                        .toLong(),
                )
            }
        }
    }

    data class BulkStringMessage(
        val data: ByteArray,
    ) : StringMessage() {
        override val value: String
            get() = data.decodeToString()

        constructor(
            value: String,
        ) : this(
            data = value.encodeToByteArray(),
        )

        override suspend fun writeTo(outgoing: ByteWriteChannel) {
            outgoing.writeFully("\$${data.size}\r\n".encodeToByteArray())
            outgoing.writeFully(data)
            outgoing.writeFully(CRLF_BYTES)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as BulkStringMessage

            return data.contentEquals(other.data)
        }

        override fun hashCode(): Int =
            data.contentHashCode()

        companion object {
            const val TYPE_BYTE: Byte = 36 // '$'

            // "$<length>\r\n<data>\r\n" -> "<data>"
            suspend fun parse(incoming: ByteReadChannel): RedisMessage {
                val length = readLength<BulkStringMessage>(incoming)

                // "$-1\r\n" -> NULL
                if (length == -1) {
                    return NullMessage
                }

                val dataBytes = readNBytes(incoming, length)
                verifyCRByte<BulkStringMessage>(incoming)
                verifyLFByte<BulkStringMessage>(incoming)

                return BulkStringMessage(
                    data = dataBytes,
                )
            }
        }
    }

    data class ArrayMessage(
        val value: List<RedisMessage>,
    ) : ArrayLikeMessage() {
        override fun asList(): List<RedisMessage> =
            value

        override suspend fun writeTo(outgoing: ByteWriteChannel) {
            outgoing.writeFully("*${value.size}\r\n".encodeToByteArray())
            for (message in value) {
                message.writeTo(outgoing)
            }
        }

        companion object {
            const val TYPE_BYTE: Byte = 42 // '*'

            // "*<number-of-elements>\r\n<element-1>...<element-n>" -> [element-1, ..., element-n]
            suspend fun parse(incoming: ByteReadChannel): RedisMessage {
                val length = readLength<ArrayMessage>(incoming)

                // "*-1\r\n" -> NULL
                if (length == -1) {
                    return NullMessage
                }

                val result = Array<RedisMessage>(length) { NullMessage }

                for (i in 0 until length) {
                    result[i] = RedisMessage.parse(incoming)
                }

                return ArrayMessage(
                    value = result.asList(),
                )
            }
        }
    }

    object NullMessage : RedisMessage() {
        const val TYPE_BYTE: Byte = 95 // '_'

        override suspend fun writeTo(outgoing: ByteWriteChannel) {
            outgoing.writeFully("_\r\n".encodeToByteArray())
        }

        // "_\r\n" -> NULL
        suspend fun parse(incoming: ByteReadChannel): NullMessage {
            verifyCRByte<NullMessage>(incoming)
            verifyLFByte<NullMessage>(incoming)

            return NullMessage
        }

        override fun equals(other: Any?): Boolean {
            return other != null && other is NullMessage
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }

    data class BooleanMessage(
        val value: Boolean,
    ) : RedisMessage() {
        override suspend fun writeTo(outgoing: ByteWriteChannel) {
            val strValue =
                if (value) {
                    "t"
                } else {
                    "f"
                }
            outgoing.writeFully("#$strValue\r\n".encodeToByteArray())
        }

        companion object {
            const val TYPE_BYTE: Byte = 35 // '#'

            private const val TRUE_BYTE: Byte = 116 // t
            private const val FALSE_BYTE: Byte = 102 // f

            // "#<t|f>\r\n" -> true|false
            suspend fun parse(incoming: ByteReadChannel): BooleanMessage {
                val resultByte = incoming.readByte()
                verifyCRByte<BooleanMessage>(incoming)
                verifyLFByte<BooleanMessage>(incoming)

                return BooleanMessage(
                    value = when (resultByte) {
                        TRUE_BYTE -> true
                        FALSE_BYTE -> false
                        else -> throw parsingException<BooleanMessage>(
                            message = "Expected $TRUE_BYTE (t) or $FALSE_BYTE (f), was $resultByte",
                        )
                    },
                )
            }
        }
    }

    data class DoubleMessage(
        val value: Double,
    ) : NumericMessage() {
        override suspend fun writeTo(outgoing: ByteWriteChannel) {
            when (value) {
                Double.POSITIVE_INFINITY -> {
                    outgoing.writeByte(TYPE_BYTE)
                    outgoing.writeFully(INF_BYTES)
                    outgoing.writeFully(CRLF_BYTES)
                }

                Double.NEGATIVE_INFINITY -> {
                    outgoing.writeByte(TYPE_BYTE)
                    outgoing.writeFully(NINF_BYTES)
                    outgoing.writeFully(CRLF_BYTES)
                }

                Double.NaN -> {
                    outgoing.writeByte(TYPE_BYTE)
                    outgoing.writeFully(NAN_BYTES)
                    outgoing.writeFully(CRLF_BYTES)
                }

                else -> {
                    outgoing.writeFully(",$value\r\n".encodeToByteArray())
                }
            }
        }

        companion object {
            const val TYPE_BYTE: Byte = 44 // ','

            // ",inf\r\n" -> INF
            private val INF_BYTES = "inf".encodeToByteArray()

            // ",-inf\r\n" -> -INF
            private val NINF_BYTES = "-inf".encodeToByteArray()

            // ",nan\r\n" -> NaN
            private val NAN_BYTES = "nan".encodeToByteArray()

            // ",[<+|->]<integral>[.<fractional>][<E|e>[sign]<exponent>]\r\n" -> [+|-]<value>
            suspend fun parse(incoming: ByteReadChannel): DoubleMessage {
                val resultBytes = readUntilCR(incoming)
                verifyLFByte<DoubleMessage>(incoming)

                return DoubleMessage(
                    value = when (resultBytes) {
                        INF_BYTES -> Double.POSITIVE_INFINITY
                        NINF_BYTES -> Double.NEGATIVE_INFINITY
                        NAN_BYTES -> Double.NaN
                        else -> resultBytes.decodeToString()
                            .toDouble()
                    },
                )
            }
        }
    }

    data class BigNumberMessage(
        val value: BigInteger,
    ) : NumericMessage() {
        override suspend fun writeTo(outgoing: ByteWriteChannel) {
            outgoing.writeFully("(${value.toString(10)}\r\n".encodeToByteArray())
        }

        companion object {
            const val TYPE_BYTE: Byte = 40 // '('

            // "([+|-]<number>\r\n" -> [+|-]<number>
            suspend fun parse(incoming: ByteReadChannel): BigNumberMessage {
                val resultBytes = readUntilCR(incoming)
                verifyLFByte<BigNumberMessage>(incoming)

                return BigNumberMessage(
                    value = BigInteger.parseString(
                        string = resultBytes.decodeToString(),
                        base = 10,
                    ),
                )
            }
        }
    }

    data class BulkErrorMessage(
        override val value: String,
    ) : ErrorMessage() {
        override suspend fun writeTo(outgoing: ByteWriteChannel) {
            val valueBytes = value.encodeToByteArray()

            outgoing.writeFully("!${valueBytes.size}\r\n".encodeToByteArray())
            outgoing.writeFully(valueBytes)
            outgoing.writeFully(CRLF_BYTES)
        }

        companion object {
            const val TYPE_BYTE: Byte = 33 // '!'

            // "!<length>\r\n<error>\r\n" -> "<error>"
            suspend fun parse(incoming: ByteReadChannel): BulkErrorMessage {
                val length = readLength<BulkErrorMessage>(incoming)
                val resultBytes = readNBytes(incoming, length)
                verifyCRByte<BulkErrorMessage>(incoming)
                verifyLFByte<BulkErrorMessage>(incoming)

                return BulkErrorMessage(
                    value = resultBytes.decodeToString(),
                )
            }
        }
    }

    data class VerbatimStringMessage(
        val type: String,
        override val value: String,
    ) : StringMessage() {
        override suspend fun writeTo(outgoing: ByteWriteChannel) {
            val payloadBytes = "$type:$value".encodeToByteArray()

            outgoing.writeFully("=${payloadBytes.size}\r\n".encodeToByteArray())
            outgoing.writeFully(payloadBytes)
            outgoing.writeFully(CRLF_BYTES)
        }

        companion object {
            const val TYPE_BYTE: Byte = 61 // '='

            // "=<length>\r\n<encoding>:<data>\r\n"
            suspend fun parse(incoming: ByteReadChannel): VerbatimStringMessage {
                // length includes three (3) encoding bytes and seperator (":"); therefore we subtract four (3 + 1)
                val length = readLength<VerbatimStringMessage>(incoming) - 4
                val typeBytes = readNBytes(incoming, 3)
                incoming.readByte() // skip separator
                val dataBytes = readNBytes(incoming, length)

                return VerbatimStringMessage(
                    type = typeBytes.decodeToString(),
                    value = dataBytes.decodeToString(),
                )
            }
        }
    }

    data class MessageMapMessage(
        val value: Map<RedisMessage, RedisMessage>,
    ) : RedisMessage() {
        override suspend fun writeTo(outgoing: ByteWriteChannel) {
            outgoing.writeFully("%${value.size}\r\n".encodeToByteArray())
            for (item in value.entries) {
                item.key.writeTo(outgoing)
                item.value.writeTo(outgoing)
            }
        }

        companion object {
            const val TYPE_BYTE: Byte = 37 // '%'

            private val NULL_ENTRY = Pair<RedisMessage, RedisMessage>(
                NullMessage,
                NullMessage,
            )

            // "%<number-of-entries>\r\n<key-1><value-1>...<key-n><value-n>" -> {<key1>: <value-1>), ..., <key-n>: <value-n>}
            suspend fun parse(incoming: ByteReadChannel): MessageMapMessage {
                val length = readLength<MessageMapMessage>(incoming)
                val result = Array<Pair<RedisMessage, RedisMessage>>(length) { NULL_ENTRY }
                for (i in 0 until length) {
                    val key = RedisMessage.parse(incoming)
                    val value = RedisMessage.parse(incoming)
                    result[i] = Pair(key, value)
                }

                return MessageMapMessage(
                    value = result.toMap(),
                )
            }
        }
    }

    data class MessageSetMessage(
        val value: Set<RedisMessage>,
    ) : ArrayLikeMessage() {
        override fun asList(): List<RedisMessage> =
            value.toList()

        override suspend fun writeTo(outgoing: ByteWriteChannel) {
            outgoing.writeFully("~${value.size}\r\n".encodeToByteArray())
            for (item in value) {
                item.writeTo(outgoing)
            }
        }

        companion object {
            const val TYPE_BYTE: Byte = 126 // '~'

            // "~<number-of-elements>\r\n<element-1>...<element-n>" -> [<element-1>, ..., <element-n>]
            suspend fun parse(incoming: ByteReadChannel): RedisMessage {
                val length = readLength<MessageSetMessage>(incoming)
                val result = Array<RedisMessage>(length) { NullMessage }
                for (i in 0 until length) {
                    result[i] = RedisMessage.parse(incoming)
                }

                return MessageSetMessage(
                    value = result.toSet(),
                )
            }
        }
    }

    data class MessagePushMessage(
        val value: List<RedisMessage>,
    ) : ArrayLikeMessage() {
        override fun asList(): List<RedisMessage> =
            value

        override suspend fun writeTo(outgoing: ByteWriteChannel) {
            outgoing.writeFully(">${value.size}\r\n".encodeToByteArray())
            for (item in value) {
                item.writeTo(outgoing)
            }
        }

        companion object {
            const val TYPE_BYTE: Byte = 62 // '>'

            // "><number-of-elements>\r\n<element-1>...<element-n>" -> [<element-1>, ..., <element-n>]
            suspend fun parse(incoming: ByteReadChannel): MessagePushMessage {
                val length = readLength<MessagePushMessage>(incoming)
                val result = Array<RedisMessage>(length) { NullMessage }
                for (i in 0 until length) {
                    result[i] = RedisMessage.parse(incoming)
                }

                return MessagePushMessage(
                    value = result.asList(),
                )
            }
        }
    }

    sealed class ErrorMessage : RedisMessage() {
        abstract val value: String
    }

    sealed class StringMessage : RedisMessage() {
        abstract val value: String
    }

    sealed class NumericMessage : RedisMessage()

    sealed class ArrayLikeMessage : RedisMessage() {
        abstract fun asList(): List<RedisMessage>
    }

    protected suspend fun ByteWriteChannel.writeFully(arr: ByteArray) {
        writeFully(arr, 0, arr.size)
    }

    companion object {
        private const val CR_BYTE: Byte = 13 // \r
        private const val LF_BYTE: Byte = 10 // \n
        private val CRLF_BYTES = ByteArray(2).apply {
            this[0] = CR_BYTE
            this[1] = LF_BYTE
        }

        suspend fun parse(incoming: ByteReadChannel): RedisMessage {
            return when (
                val typeByte = incoming.readByte()
            ) {
                SimpleStringMessage.TYPE_BYTE -> SimpleStringMessage.parse(incoming)

                SimpleErrorMessage.TYPE_BYTE -> SimpleErrorMessage.parse(incoming)

                IntegerMessage.TYPE_BYTE -> IntegerMessage.parse(incoming)

                BulkStringMessage.TYPE_BYTE -> BulkStringMessage.parse(incoming)

                ArrayMessage.TYPE_BYTE -> ArrayMessage.parse(incoming)

                NullMessage.TYPE_BYTE -> NullMessage.parse(incoming)

                BooleanMessage.TYPE_BYTE -> BooleanMessage.parse(incoming)

                DoubleMessage.TYPE_BYTE -> DoubleMessage.parse(incoming)

                BigNumberMessage.TYPE_BYTE -> BigNumberMessage.parse(incoming)

                BulkErrorMessage.TYPE_BYTE -> BulkErrorMessage.parse(incoming)

                VerbatimStringMessage.TYPE_BYTE -> VerbatimStringMessage.parse(incoming)

                MessageMapMessage.TYPE_BYTE -> MessageMapMessage.parse(incoming)

                MessageSetMessage.TYPE_BYTE -> MessageSetMessage.parse(incoming)

                MessagePushMessage.TYPE_BYTE -> MessagePushMessage.parse(incoming)

                else -> throw parsingException<RedisMessage>(
                    message = "Unknown message type: $typeByte",
                )
            }
        }

        private suspend fun readUntilCR(
            incoming: ByteReadChannel,
        ): ByteArray {
            val result = ArrayList<Byte>()
            var currentByte: Byte

            while (true) {
                currentByte = incoming.readByte()
                if (currentByte == CR_BYTE) {
                    break
                }
                result.add(currentByte)
            }

            return result.toByteArray()
        }

        private suspend inline fun <reified T : RedisMessage> readLength(incoming: ByteReadChannel): Int {
            val lengthBytes = readUntilCR(incoming)
            verifyLFByte<T>(incoming)

            return lengthBytes.decodeToString()
                .toInt()
        }

        private suspend fun readNBytes(
            incoming: ByteReadChannel,
            n: Int,
        ): ByteArray {
            val result = ByteArray(n)
            incoming.readFully(result, 0, n)

            return result
        }

        private suspend inline fun <reified T : RedisMessage> verifyLFByte(
            incoming: ByteReadChannel,
        ) {
            val currentByte = incoming.readByte()

            if (currentByte != LF_BYTE) {
                throw parsingException<T>(
                    "Expected $LF_BYTE (LF), was $currentByte",
                )
            }
        }

        private suspend inline fun <reified T : RedisMessage> verifyCRByte(
            incoming: ByteReadChannel,
        ) {
            val currentByte = incoming.readByte()

            if (currentByte != CR_BYTE) {
                throw parsingException<T>(
                    "Expected $CR_BYTE (CR), was $currentByte",
                )
            }
        }

        private inline fun <reified T : RedisMessage> parsingException(
            message: String,
        ): ParsingException =
            ParsingException(
                message = message,
                type = T::class,
            )
    }
}
