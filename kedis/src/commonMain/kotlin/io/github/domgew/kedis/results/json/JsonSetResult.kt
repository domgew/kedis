package io.github.domgew.kedis.results.json

// https://redis.io/commands/json.set/
public sealed interface JsonSetResult {

    public data object Ok : JsonSetResult

    public data object Aborted : JsonSetResult
}
