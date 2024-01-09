import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level

fun main() {
    KotlinLoggingConfiguration.logLevel = Level.TRACE
    commonMain()
}
