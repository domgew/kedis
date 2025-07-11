package io.github.domgew.kedis.results.scripting

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.domgew.kedis.impl.RedisMessage
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.decodeToString
import kotlinx.io.bytestring.encodeToByteString

public sealed interface DynamicResult {

    public sealed interface SuccessResult : DynamicResult

    public data object NullResult : SuccessResult

    public data class BooleanResult(
        val value: Boolean,
    ) : SuccessResult

    public data class BinaryOrStringResult(
        val data: ByteString,
    ) : SuccessResult {

        public constructor(
            string: String,
        ) : this(
            data = string.encodeToByteString(),
        )

        val string: String by lazy {
            data.decodeToString()
        }
    }

    public data class LongResult(
        val value: Long,
    ) : SuccessResult

    public data class DoubleResult(
        val value: Double,
    ) : SuccessResult

    public data class BigIntegerResult(
        val value: BigInteger,
    ) : SuccessResult

    public data class ListLikeResult(
        val items: List<DynamicResult>,
    ) : SuccessResult

    public data class MapResult(
        val items: List<Pair<DynamicResult, DynamicResult>>,
    ) : SuccessResult

    public data class ErrorResult(
        val string: String,
    ) : DynamicResult

    public companion object {

        internal fun fromMessage(
            message: RedisMessage,
        ): DynamicResult =
            when (message) {
                RedisMessage.NullMessage ->
                    NullResult

                is RedisMessage.StringMessage ->
                    BinaryOrStringResult(
                        data = ByteString(
                            data = message.data,
                        ),
                    )

                is RedisMessage.BooleanMessage ->
                    BooleanResult(
                        value = message.value,
                    )

                is RedisMessage.IntegerMessage ->
                    LongResult(
                        value = message.value,
                    )

                is RedisMessage.DoubleMessage ->
                    DoubleResult(
                        value = message.value,
                    )

                is RedisMessage.BigNumberMessage ->
                    BigIntegerResult(
                        value = message.value,
                    )

                is RedisMessage.ArrayLikeMessage ->
                    ListLikeResult(
                        items = message.asList()
                            .map {
                                fromMessage(
                                    message = it,
                                )
                            },
                    )

                is RedisMessage.MessageMapMessage ->
                    MapResult(
                        items = message.value
                            .entries
                            .map { (key, value) ->
                                fromMessage(
                                    message = key,
                                )
                                    .to(
                                        fromMessage(
                                            message = value,
                                        ),
                                    )
                            },
                    )

                is RedisMessage.ErrorMessage ->
                    ErrorResult(
                        string = message.value,
                    )
            }
    }
}
