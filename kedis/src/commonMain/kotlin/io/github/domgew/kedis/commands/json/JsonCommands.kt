package io.github.domgew.kedis.commands.json

import io.github.domgew.kedis.KedisClient
import io.github.domgew.kedis.arguments.json.JsonGetOptions
import kotlin.reflect.KProperty
import kotlinx.serialization.json.Json

public class JsonPath() {

    private val path = StringBuilder("$")

    public constructor(property: KProperty<*>):this() {
        this.plus(property)
    }

    public operator fun plus(property: KProperty<*>): JsonPath {
        this.path.append(".${getSerialName(property)}")
        return this
    }

    private fun getSerialName(property: KProperty<*>): String {
        return property.name // TODO @SerialName
    }

    override fun toString(): String {
        return this.path.toString()
    }

}

public val ROOT: JsonPath = JsonPath()

public infix operator fun KProperty<*>.plus(property: KProperty<*>): JsonPath {
    return JsonPath().also{
        it.plus(this)
        it.plus(property)
    }
}

public suspend inline fun <reified T> KedisClient.jsonGet(
    key: String,
    path: KProperty<*>,
    options: JsonGetOptions,
): T? = jsonGet<T>(key = key, path = JsonPath(path), options = options)

public suspend inline fun <reified T> KedisClient.jsonGet(
    key: String,
    path: JsonPath,
    options: JsonGetOptions,
): T? = jsonGet(key = key,path = path.toString(),options = options)?.let{
    Json.decodeFromString(it)
}
