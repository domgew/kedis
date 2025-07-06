package io.github.domgew.kedis.commands

import io.github.domgew.kedis.arguments.json.JsonSetOptions
import io.github.domgew.kedis.commands.json.JsonGetCommand
import io.github.domgew.kedis.commands.json.JsonSetCommand
import io.github.domgew.kedis.results.json.JsonSetResult

public object KedisJsonCommands {

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
}
