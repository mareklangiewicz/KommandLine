package pl.mareklangiewicz.kommand.jupyter

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.*
import pl.mareklangiewicz.kommand.CliPlatform.Companion.SYS
import org.jetbrains.kotlinx.jupyter.api.libraries.*
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.bad.*

internal class Integration: JupyterIntegration() {
    override fun Builder.onLoaded() {
//        render<BlaBla> { HTML("<p><b>bla1: </b>${it.bla1}</p><p><b>bla2: </b>${it.bla2}</p>") }
        import("kotlinx.coroutines.*")
        import("kotlinx.coroutines.flow.*")
        // FIXME_later: refactor packages, so only high-level functions are imported automatically, and delicate api are NOT.
        //   maybe high-level fun stuff up to pl.mareklangiewicz.kommand package??
        import("pl.mareklangiewicz.kommand.*")
        import("pl.mareklangiewicz.kommand.core.*")
        import("pl.mareklangiewicz.kommand.CliPlatform.Companion.SYS")
        import("pl.mareklangiewicz.kommand.find.*")
        import("pl.mareklangiewicz.kommand.github.*")
    }
}


@OptIn(ExperimentalTime::class)
suspend fun Flow<*>.logm() = logEachWithMillis()

fun Flow<*>.logb() = logEachWithMillisBlocking()


/**
 * Kinda like .exec, but less strict/explicit, because in notebooks we are in more local "experimental" context.
 * I don't want too many shortcut names inside kommandline itself, but here it's fine.
 */
suspend fun Kommand.x(
    cli: CliPlatform = SYS,
    dir: String? = null,
    vararg useNamedArgs: Unit,
    inContent: String? = null,
    inLineS: Flow<String>? = inContent?.lineSequence()?.asFlow(),
    inFile: String? = null,
    outFile: String? = null,
    outFileAppend: Boolean = false,
    errToOut: Boolean = false,
    errFile: String? = null,
    errFileAppend: Boolean = false,
    expectedExit: Int? = 0,
    expectedErr: ((List<String>) -> Boolean)? = null,
    outLinesCollector: FlowCollector<String>? = null,
): List<String> = coroutineScope {
    req(cli.isRedirectFileSupported || (inFile == null && outFile == null)) { "redirect file not supported here" }
    req(inLineS == null || inFile == null) { "Either inLineS or inFile or none, but not both" }
    req(outLinesCollector == null || outFile == null) { "Either outLinesCollector or outFile or none, but not both" }
    val eprocess = cli.start(this@x,
        dir = dir,
        inFile = inFile,
        outFile = outFile,
        outFileAppend = outFileAppend,
        errToOut = errToOut,
        errFile = errFile,
        errFileAppend = errFileAppend,
    )
    val inJob = inLineS?.let { launch { eprocess.stdin.collect(it) }}
    // Note, Have to start pushing to stdin before collecting stdout,
    // because many commands wait for stdin before outputting data.
    val outJob = outLinesCollector?.let { eprocess.stdout.onEach(it::emit).launchIn(this) }
    inJob?.join()
    outJob?.join()
    eprocess
        .awaitResult() // inLineS already used
        .unwrap(expectedExit, expectedErr)
}

fun <K: Kommand, In, Out, Err> TypedKommand<K, In, Out, Err>.xstart(cli: CliPlatform = SYS, dir: String? = null) =
    cli.start(this, dir)


suspend fun <ReducedOut> ReducedScript<ReducedOut>.x(cli: CliPlatform = SYS, dir: String? = null): ReducedOut =
    exec(cli, dir = dir)






/**
 * Blocking flavor of fun Kommand.x(...). Will be deprecated when kotlin notebooks support suspending fun.
 * See: https://github.com/Kotlin/kotlin-jupyter/issues/239
 */
fun Kommand.xb(
    cli: CliPlatform = SYS,
    dir: String? = null,
    vararg useNamedArgs: Unit,
    inContent: String? = null,
    inLineS: Flow<String>? = inContent?.lineSequence()?.asFlow(),
    inFile: String? = null,
    outFile: String? = null,
    outFileAppend: Boolean = false,
    errToOut: Boolean = false,
    errFile: String? = null,
    errFileAppend: Boolean = false,
    expectedExit: Int? = 0,
    expectedErr: ((List<String>) -> Boolean)? = { it.isEmpty() },
    outLinesCollector: FlowCollector<String>? = null,
): List<String> = runBlocking { x(
    cli,
    dir,
    inContent = inContent,
    inLineS = inLineS,
    inFile = inFile,
    outFile = outFile,
    outFileAppend = outFileAppend,
    errToOut = errToOut,
    errFile = errFile,
    errFileAppend = errFileAppend,
    expectedExit = expectedExit,
    expectedErr = expectedErr,
    outLinesCollector = outLinesCollector,
) }

/**
 * Blocking flavor of fun ReducedKommand.x(...). Will be deprecated when kotlin notebooks support suspending fun.
 * See: https://github.com/Kotlin/kotlin-jupyter/issues/239
 */
fun <ReducedOut> ReducedKommand<ReducedOut>.xb(cli: CliPlatform = SYS, dir: String? = null): ReducedOut =
    runBlocking { x(cli, dir) }
