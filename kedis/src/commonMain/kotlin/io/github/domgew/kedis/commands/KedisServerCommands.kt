package io.github.domgew.kedis.commands

import io.github.domgew.kedis.arguments.server.InfoSectionName
import io.github.domgew.kedis.arguments.server.SyncOption
import io.github.domgew.kedis.commands.server.AuthCommand
import io.github.domgew.kedis.commands.server.BgSaveCommand
import io.github.domgew.kedis.commands.server.FlushCommand
import io.github.domgew.kedis.commands.server.InfoCommand
import io.github.domgew.kedis.commands.server.InfoMapCommand
import io.github.domgew.kedis.commands.server.InfoRawCommand
import io.github.domgew.kedis.commands.server.PingCommand
import io.github.domgew.kedis.commands.server.SelectCommand
import io.github.domgew.kedis.commands.server.WhoAmICommand
import io.github.domgew.kedis.results.server.BgSaveResult
import io.github.domgew.kedis.results.server.InfoSection

public object KedisServerCommands {

    /**
     * Authenticates the connection to the server or throws an exception when it failed.
     *
     * [https://redis.io/commands/auth/](https://redis.io/commands/auth/)
     */
    public fun auth(
        password: String,
        username: String? = null,
    ): KedisCommand<Unit> =
        AuthCommand(
            username = username,
            password = password,
        )

    /**
     * Saves the current DB to disk in the background. When [schedule], it will only be scheduled, otherwise it will be started immediately.
     *
     * [https://redis.io/commands/bgsave/](https://redis.io/commands/bgsave/)
     */
    public fun bgSave(
        schedule: Boolean = false,
    ): KedisCommand<BgSaveResult> =
        BgSaveCommand(
            schedule = schedule,
        )

    /**
     * Clears all redis DBs.
     *
     * [https://redis.io/commands/flushall/](https://redis.io/commands/flushall/)
     * @return Whether the server responded with "OK"
     */
    public fun flushAll(
        sync: SyncOption = SyncOption.SYNC,
    ): KedisCommand<Boolean> =
        FlushCommand(
            target = FlushCommand.FlushTarget.ALL,
            syncOption = sync,
        )

    /**
     * Clears the current redis DB.
     *
     * [https://redis.io/commands/flushdb/](https://redis.io/commands/flushdb/)
     * @return Whether the server responded with "OK"
     */
    public fun flushDb(
        sync: SyncOption = SyncOption.SYNC,
    ): KedisCommand<Boolean> =
        FlushCommand(
            target = FlushCommand.FlushTarget.DB,
            syncOption = sync,
        )

    /**
     * Queries the info for the requested [section]s from the Redis server in a strictly typed form.
     *
     * [https://redis.io/commands/info/](https://redis.io/commands/info/)
     * @return The requested information
     */
    public fun info(
        vararg section: InfoSectionName,
    ): KedisCommand<List<InfoSection>> =
        InfoCommand(
            sections = section.asList(),
        )

    /**
     * Queries the info for the requested [section]s from the Redis server in string map form.
     *
     * [https://redis.io/commands/info/](https://redis.io/commands/info/)
     * @return The requested information - the first key is the lowercase section name, the second the actual field
     */
    public fun infoMap(
        vararg section: InfoSectionName,
    ): KedisCommand<Map<String?, Map<String, String>>> =
        InfoMapCommand(
            sections = section.asList(),
        )

    /**
     * Queries the info for the requested [section]s from the Redis server in string form.
     *
     * [https://redis.io/commands/info/](https://redis.io/commands/info/)
     * @return The requested information
     */
    public fun infoRaw(
        vararg section: InfoSectionName,
    ): KedisCommand<String?> =
        InfoRawCommand(
            sections = section.asList(),
        )

    /**
     * Sends a message ([content]) to the server which should be returned unchanged (e.g. result should equal [content]).
     *
     * [https://redis.io/commands/ping/](https://redis.io/commands/ping/)
     * @return The response from the Redis server - should be [content]
     */
    public fun ping(
        content: String = "PING",
    ): KedisCommand<String> =
        PingCommand(
            content = content,
        )

    /**
     * Selects the redis database to use by [databaseIndex]. Default database is 0.
     *
     * [https://redis.io/commands/select/](https://redis.io/commands/select/)
     */
    public fun select(
        databaseIndex: Int,
    ): KedisCommand<Unit> =
        SelectCommand(
            databaseIndex = databaseIndex,
        )

    /**
     * Asks the Redis server for the current username.
     *
     * [https://redis.io/commands/acl-whoami/](https://redis.io/commands/acl-whoami/)
     * @return The current username
     */
    public fun whoAmI(): KedisCommand<String> =
        WhoAmICommand()
}
