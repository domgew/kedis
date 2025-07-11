package io.github.domgew.kedis.commands

import io.github.domgew.kedis.annotations.RedisModuleJson
import io.github.domgew.kedis.arguments.json.JsonSetOptions
import io.github.domgew.kedis.commands.json.JsonDelCommand
import io.github.domgew.kedis.commands.json.JsonGetCommand
import io.github.domgew.kedis.commands.json.JsonSetCommand
import io.github.domgew.kedis.commands.json.JsonToggleCommand
import io.github.domgew.kedis.commands.json.JsonTypeCommand
import io.github.domgew.kedis.results.json.JsonSetResult

@RedisModuleJson
public object KedisJsonCommands {

    /**
     * Deletes the values behind the [key]'s [path].
     *
     * [https://redis.io/commands/json.del/](https://redis.io/commands/json.del/)
     * @return The number of deleted properties
     */
    public fun jsonDel(
        key: String,
        path: String? = null,
    ): KedisCommand<Long?> =
        JsonDelCommand(
            key = key,
            path = path,
        )

    /**
     * Gets the JSON encoded values behind the key's paths.
     *
     * [https://redis.io/commands/json.get/](https://redis.io/commands/json.get/)
     * @return The value or NULL
     */
    public fun jsonGet(
        key: String,
        indent: String? = null,
        newLine: String? = null,
        space: String? = null,
        paths: List<String> = emptyList(),
    ): KedisCommand<String?> =
        JsonGetCommand(
            key = key,
            indent = indent,
            newLine = newLine,
            space = space,
            paths = paths,
        )

    /**
     * Sets the JSON value behind the given [key]'s [path], minding the [options].
     *
     * This operation can fail on valid cases - see Redis docs.
     *
     * [https://redis.io/commands/json.set/](https://redis.io/commands/json.set/)
     * @return Whether the operation was successful
     */
    public fun jsonSet(
        key: String,
        path: String,
        value: String,
        options: JsonSetOptions = JsonSetOptions(),
    ): KedisCommand<JsonSetResult> =
        JsonSetCommand(
            key = key,
            path = path,
            value = value,
            options = options,
        )

    /**
     * Toggles the values behind the [key]'s [path].
     *
     * [https://redis.io/commands/json.toggle/](https://redis.io/commands/json.toggle/)
     * @return The new values or null if the type did not match
     */
    public fun jsonToggle(
        key: String,
        path: String,
    ): KedisCommand<List<Boolean?>?> =
        JsonToggleCommand(
            key = key,
            path = path,
        )

    /**
     * Get the types of the values behind the [key]'s [path].
     *
     * [https://redis.io/commands/json.type/](https://redis.io/commands/json.type/)
     * @return The JSON type names
     */
    public fun jsonType(
        key: String,
        path: String? = null,
    ): KedisCommand<List<String>?> =
        JsonTypeCommand(
            key = key,
            path = path,
        )
}
