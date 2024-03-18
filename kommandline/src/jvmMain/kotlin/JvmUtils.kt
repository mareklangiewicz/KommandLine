package pl.mareklangiewicz.kommand

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pl.mareklangiewicz.kground.*

@Deprecated("Use suspend fun Kommand.exec(...)")
fun CliPlatform.execBlocking(
    kommand: Kommand,
    vararg useNamedArgs: Unit,
    dir: String? = null,
    inContent: String? = null,
    inLineS: Flow<String>? = inContent?.lineSequence()?.asFlow(),
    inFile: String? = null,
    outFile: String? = null,
): List<String> = runBlocking {
    exec(kommand, dir = dir, inContent = inContent, inLineS = inLineS, inFile = inFile, outFile = outFile)
}

// TODO_someday: CliPlatform as context receiver
@Deprecated("Use suspend fun Kommand.exec(...)")
fun Kommand.execBlocking(
    cli: CliPlatform,
    vararg useNamedArgs: Unit,
    dir: String? = null,
    inContent: String? = null,
    inLineS: Flow<String>? = inContent?.lineSequence()?.asFlow(),
    inFile: String? = null,
    outFile: String? = null,
): List<String> = cli.execBlocking(this,
    dir = dir,
    inContent = inContent,
    inLineS = inLineS,
    inFile = inFile,
    outFile = outFile,
)

// temporary hack
@Deprecated("Use suspend fun Kommand.exec(...)")
actual fun Kommand.execb(
    cli: CliPlatform,
    vararg useNamedArgs: Unit,
    dir: String?,
    inContent: String?,
    inLineS: Flow<String>?,
    inFile: String?,
    outFile: String?,
) = execBlocking(
    cli,
    dir = dir,
    inContent = inContent,
    inLineS = inLineS,
    inFile = inFile,
    outFile = outFile
)

// also temporary hack
@Deprecated("Use suspend fun ReducedKommand.exec(...)")
actual fun <ReducedOut> ReducedScript<ReducedOut>.execb(cli: CliPlatform, dir: String?): ReducedOut =
    runBlocking { exec(cli, dir) }


fun Flow<*>.logEachWithMillisBlocking() = runBlocking { logEachWithMillis() }
