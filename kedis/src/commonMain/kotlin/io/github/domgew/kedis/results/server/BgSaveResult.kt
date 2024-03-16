package io.github.domgew.kedis.results.server

public sealed interface BgSaveResult {

    public data object Started : BgSaveResult

    public data object Scheduled : BgSaveResult
}
