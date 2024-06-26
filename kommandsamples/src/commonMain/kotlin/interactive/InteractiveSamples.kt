package pl.mareklangiewicz.interactive

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.bad.chkEq
import pl.mareklangiewicz.kground.io.UFileSys
import pl.mareklangiewicz.kground.io.implictx
import pl.mareklangiewicz.kground.io.pathToTmpNotes
import pl.mareklangiewicz.kommand.Kommand
import pl.mareklangiewicz.kommand.ReducedScript
import pl.mareklangiewicz.kommand.XClipSelection
import pl.mareklangiewicz.kommand.ax
import pl.mareklangiewicz.kommand.ideOpen
import pl.mareklangiewicz.kommand.lineRawOrNull
import pl.mareklangiewicz.kommand.samples.*
import pl.mareklangiewicz.kommand.writeFileWithDD
import pl.mareklangiewicz.kommand.xclipOut
import pl.mareklangiewicz.kommand.zenity.zenityAskIf
import pl.mareklangiewicz.ulog.ULog
import pl.mareklangiewicz.ulog.i
import pl.mareklangiewicz.ulog.implictx
import pl.mareklangiewicz.ure.*
import pl.mareklangiewicz.ureflect.getReflectCallOrNull


/**
 * @param reference Either "xclip", or reference in format like from IntelliJ:CopyReference action.
 *   For example, "pl.mareklangiewicz.kommand.demo.MyDemoSamples#getBtop"
 */
@NotPortableApi
@DelicateApi("API for manual interactive experimentation. Requires Zenity, conditionally skips")
suspend fun tryInteractivelySomethingRef(reference: String = "xclip") {
  val log = implictx<ULog>()
  log.i("tryInteractivelySomethingRef(\"$reference\")")
  val ref = if (reference == "xclip")
    xclipOut(XClipSelection.Clipboard).ax().singleOrNull()
      ?: bad { "Clipboard has to have code reference in single line." }
  else reference
  val ure = ure {
    +ure("className") {
      +chWordFirst
      1..MAX of chWordOrDot
      +chWord
    }
    +ch('#')
    +ureIdent().withName("methodName")
  }
  val result = ure.matchEntireOrThrow(ref)
  val className by result.namedValues
  val methodName by result.namedValues
  tryInteractivelyClassMember(className!!, methodName!!)
}

@NotPortableApi
@DelicateApi("API for manual interactive experimentation. Requires Zenity, conditionally skips")
suspend fun tryInteractivelyClassMember(className: String, memberName: String) {
  val log = implictx<ULog>()
  log.i("tryInteractivelyClassMember(\"$className\", \"$memberName\")")
  val call = getReflectCallOrNull(className, memberName) ?: return
  // Note: prepareCallFor fails early if member not found,
  // before we start to interact with the user,
  // but the code is never called without confirmation.
  ifInteractiveCodeEnabled {
    zenityAskIf("Call member $memberName\nfrom class $className?").ax() || return
    val member: Any? = call()
    // Note: call() will either already "do the thing" (when the member is just a fun to call)
    //  or it will only get the property (like ReducedScript/Sample etc.) which will be tried (or not) later.
    member.tryInteractivelyAnything()
  }
}


@DelicateApi("API for manual interactive experimentation. Requires Zenity, conditionally skips")
suspend fun Any?.tryInteractivelyAnything() = when (this) {
  is Sample -> tryInteractivelyCheckSample()
  is Kommand -> toInteractiveCheck().ax()
  is ReducedSample<*> -> tryInteractivelyCheckReducedSample() // Note: ReducedSample is also ReducedScript
  is ReducedScript<*> -> tryInteractivelyCheckReducedScript()
  else -> tryOpenDataInIDE()
}


@DelicateApi("API for manual interactive experimentation. Requires Zenity, conditionally skips")
suspend fun Sample.tryInteractivelyCheckSample() =
  kommand.toInteractiveCheck(expectedLineRaw).ax()

@DelicateApi("API for manual interactive experimentation. Requires Zenity, conditionally skips")
suspend fun ReducedSample<*>.tryInteractivelyCheckReducedSample() {
  reducedKommand.lineRawOrNull() chkEq expectedLineRaw // so also if both are nulls it's treated as fine.
  tryInteractivelyCheckReducedScript("Exec ReducedSample ?")
}

@DelicateApi("API for manual interactive experimentation. Requires Zenity, conditionally skips")
suspend fun ReducedScript<*>.tryInteractivelyCheckReducedScript(
  question: String = "Exec ReducedScript ?",
) {
  zenityAskIf(question).ax() || return
  val reducedOut = ax()
  reducedOut.tryOpenDataInIDE("Open ReducedOut: ${reducedOut.about} in tmp.notes in IDE ?")
}

@DelicateApi("API for manual interactive experimentation. Requires Zenity, conditionally skips")
/** @param question null means default question */
suspend fun Any?.tryOpenDataInIDE(question: String? = null): Any {
  val log = implictx<ULog>()
  val fs = implictx<UFileSys>()
  return when {
    this == null -> log.i("It is null. Nothing to open.")
    this is Unit -> log.i("It is Unit. Nothing to open.")
    this is String && isEmpty() -> log.i("It is empty string. Nothing to open.")
    this is Collection<*> && isEmpty() -> log.i("It is empty collection. Nothing to open.")
    !zenityAskIf(question ?: "Open $about in tmp.notes in IDE ?").ax() -> log.i("Not opening.")
    else -> {
      val lines = if (this is Collection<*>) map { it.toString() } else toString().lines()
      val notes = fs.pathToTmpNotes.toString() // FIXME_later: use Path type everywhere
      writeFileWithDD(lines, notes).ax()
      ideOpen(notes).ax()
    }
  }
}

private val Any?.about: String
  get() = when (this) {
    null -> "null"
    Unit -> "Unit"
    is Number -> this::class.simpleName + ":$this"
    is Collection<*> -> this::class.simpleName + "(size:$size)"
    is CharSequence ->
      if (length < 20) this::class.simpleName + ": \"$this\""
      else this::class.simpleName + "(length:$length)"
    else -> this::class.simpleName ?: "???"
  }

