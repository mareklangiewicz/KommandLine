package pl.mareklangiewicz.kommand.core

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.RmOpt.*

@OptIn(DelicateApi::class)
fun rmFileIfExists(file: String) = ReducedScript { dir ->
  val exists = testIfFileExists(file).ax(dir = dir)
  if (exists) rm(file).ax(dir = dir)
  else listOf("File not found")
}

@OptIn(DelicateApi::class)
fun rmDirIfEmpty(dir: String) = rm { -Dir; +dir }

@DelicateApi
fun rmTreeWithForce(rootDir: String, doubleChk: suspend (path: String) -> Boolean) =
  ReducedScript { dir ->
    doubleChk(rootDir).chkTrue { "ERROR: Can not remove whole '$rootDir' tree. Double chk failed." }
    rm(rootDir, recursive = true, force = true).ax(dir = dir)
  }

@DelicateApi
fun rm(
  path: String,
  vararg useNamedArgs: Unit,
  recursive: Boolean = false,
  force: Boolean = false,
  verbose: Boolean = false,
) = rm { if (recursive) -Recursive; if (force) -Force; if (verbose) -Verbose; +path }

@DelicateApi
fun rm(init: Rm.() -> Unit) = Rm().apply(init)

/** [linux man](https://man7.org/linux/man-pages/man1/rm.1.html) */
@DelicateApi
data class Rm(
  override val opts: MutableList<RmOpt> = mutableListOf(),
  override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<RmOpt> {
  override val name get() = "rm"
}

@DelicateApi
interface RmOpt : KOptTypical {

  /** ignore nonexistent files and arguments, never prompt */
  data object Force : RmOpt, KOptS("f")

  /** prompt before every removal */
  data object PromptAlways : RmOpt, KOptS("i")

  /** prompt once before removing more than three files, or when removing  recursively */
  data object PromptOnce : RmOpt, KOptS("I")

  data object OneFileSystem : RmOpt, KOptL("one-file-system")

  data object Recursive : RmOpt, KOptS("r")

  /** remove empty directories */
  data object Dir : RmOpt, KOptS("d")

  /** explain what is being done */
  data object Verbose : RmOpt, KOptS("v")

  data object Help : RmOpt, KOptL("help")

  data object Version : RmOpt, KOptL("version")
}
