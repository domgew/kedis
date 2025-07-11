package io.github.domgew.kedis.commands

import io.github.domgew.kedis.commands.scripting.EvalCommand
import io.github.domgew.kedis.results.scripting.DynamicResult

public object KedisScriptingCommands {

    /**
     * Executes the given LUA [script] with the gives [keys] and [args] on Redis.
     *
     * [https://redis.io/docs/latest/commands/eval/](https://redis.io/docs/latest/commands/eval/)
     * @return The [script]'s result
     */
    public fun eval(
        script: String,
        keys: List<String> = emptyList(),
        args: List<String> = emptyList(),
    ): KedisCommand<DynamicResult> =
        EvalCommand(
            script = script,
            keys = keys,
            args = args,
        )
}
