package pl.mareklangiewicz.kommand.core

import kotlinx.coroutines.flow.*
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.LsOpt.*
import pl.mareklangiewicz.kommand.core.LsOpt.IndicatorStyle.*

fun lsRegFiles(dir: String, wHidden: Boolean = false) =
  ls(dir, wHidden = wHidden, wIndicator = SLASH).reducedOut { toList().filter { !it.endsWith('/') } }

fun lsSubDirs(dir: String, wHidden: Boolean = false) =
  ls(dir, wHidden = wHidden, wIndicator = SLASH).reducedOut {
    toList().filter { it.endsWith('/') }.map { it.dropLast(1) }
  }

/**
 * Generally null values mean: default, so it depends on the underlying system (not necessarily "none").
 * For example, on my machine "man ls" says that default indicator-style is "none",
 * but when I try it (no alias, output piped to file), it works as if it was "slash".
 * BTW default color should always be fine (null == default == "auto"),
 * because it only adds colors when stdout is terminal (not piped to any file or sth)
 */
fun ls(
  vararg paths: String,
  wHidden: Boolean = false,
  wIndicator: IndicatorStyle? = null,
  wColor: ColorType? = null,
  wDirsFirst: Boolean = false,
  init: Ls.() -> Unit = {},
) = Ls().apply {
  for (p in paths) +p
  if (wHidden) -AlmostAll
  wIndicator?.let { -Indicator(it) }
  wColor?.let { -Color(it) }
  if (wDirsFirst) -DirsFirst
  init()
}


/** [linux man](https://man7.org/linux/man-pages/man1/ls.1.html) */
@OptIn(DelicateApi::class)
data class Ls(
  override val opts: MutableList<LsOpt> = mutableListOf(),
  override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<LsOpt> {
  override val name get() = "ls"
}

@OptIn(DelicateApi::class)
interface LsOpt : KOptTypical {

  /** list one file per line even when stdout is terminal */
  data object One : KOptS("1"), LsOpt

  /** end each output line with NUL, not newline */
  data object Zero : KOptL("zero"), LsOpt

  /** do not ignore entries starting with "." */
  data object All : KOptS("a"), LsOpt
  /** do not list implied "." and ".." */
  data object AlmostAll : KOptS("A"), LsOpt

  data object Author : KOptL("author"), LsOpt

  data object Escape : KOptL("escape"), LsOpt

  data class BlockSize(val size: String) : KOptL("block-size", size), LsOpt

  data object IgnoreBackups : KOptL("ignore-backups"), LsOpt

  data object ListByColumns : KOptS("C"), LsOpt
  data object ListByLines : KOptS("x"), LsOpt

  data class Color(val type: ColorType) : KOptL("color", "$type"), LsOpt
  enum class ColorType {
    ALWAYS, AUTO, NEVER;

    override fun toString() = super.toString().lowercase()
  }

  data object Directory : KOptS("d"), LsOpt

  data object Dired : KOptL("dired"), LsOpt

  /** do not sort, enable -aU, disable -ls --color */
  data object Raw : KOptS("f"), LsOpt

  /** append indicator (one of *=/>@|) to entries */
  data object Classify : KOptS("F"), LsOpt

  /** like classify, except do not add * symbol */
  data object ClassifyFileType : KOptL("file-type"), LsOpt

  data class Format(val type: FormatType) : KOptL("--format=$type"), LsOpt
  enum class FormatType {
    ACROSS, COMMAS, HORIZONTAL, LONG, SINGLECOLUMN, VERBOSE, VERTICAL;

    override fun toString() = super.toString().lowercase()
  }

  /** like -l --time-style=full-iso */
  data object FullTime : KOptL("full-time"), LsOpt

  data object DirsFirst : KOptL("group-directories-first"), LsOpt

  data object NoGroup : KOptS("G"), LsOpt

  data object LongFormat : KOptS("l"), LsOpt // option name is not just Long to avoid clash with Long data type
  data object LongWithoutOwner : KOptS("g"), LsOpt
  data object LongWithoutGroup : KOptS("o"), LsOpt

  data object HumanReadable : KOptS("h"), LsOpt
  data object HumanReadableSI : KOptL("si"), LsOpt

  data object Dereference : KOptL("dereference"), LsOpt
  data object DereferenceCommandLine : KOptL("dereference-command-line"), LsOpt
  data object DereferenceCommandLineSymlinkToDir : KOptL("dereference-command-line-symlink-to-dir"), LsOpt

  data class Hide(val pattern: String) : KOptL("hide", pattern), LsOpt
  /** print ? instead of nongraphic characters */
  data object HideControlChars : KOptL("hide-control-chars"), LsOpt
  /** show nongraphic characters as-is (the default, unless program is 'ls' and output is a terminal) */
  data object ShowControlChars : KOptL("show-control-chars"), LsOpt

  data class Hyperlink(val type: HyperlinkType) : KOptL("hyperlink", "$type"), LsOpt
  enum class HyperlinkType {
    ALWAYS, AUTO, NEVER;

    override fun toString() = super.toString().lowercase()
  }

  data class Indicator(val style: IndicatorStyle) : KOptL("indicator-style", "$style"), LsOpt
  enum class IndicatorStyle {
    NONE, SLASH, FILETYPE, CLASSIFY;

    override fun toString() = if (this == FILETYPE) "file-type" else super.toString().lowercase()
  }

  data object IndicatorSlash : KOptS("p"), LsOpt

  data object INode : KOptL("inode"), LsOpt

  data class Ignore(val pattern: String) : KOptL("ignore", pattern), LsOpt

  data object Kibibytes : KOptL("kibibytes"), LsOpt

  data object Commas : KOptS("m"), LsOpt

  data object NumericUidGid : KOptL("numeric-uid-gid"), LsOpt

  data object Literal : KOptL("literal"), LsOpt

  data object QuoteName : KOptL("quote-name"), LsOpt

  data class Quoting(val style: QuotingStyle) : KOptL("quoting-style", "$style"), LsOpt
  enum class QuotingStyle {
    LITERAL, LOCALE, SHELL, SHELLALWAYS, SHELLESCAPE, SHELLESCAPEALWAYS, C, ESCAPE;

    override fun toString() = when (this) {
      SHELLALWAYS -> "shell-always"
      SHELLESCAPE -> "shell-escape"
      SHELLESCAPEALWAYS -> "shell-escape-always"
      else -> super.toString().lowercase()
    }
  }

  data object Reverse : KOptS("r"), LsOpt

  data object Recursive : KOptS("R"), LsOpt

  data object Size : KOptS("s"), LsOpt

  data class Sort(val type: SortType) : KOptL("sort", "$type"), LsOpt
  enum class SortType {
    NONE, SIZE, TIME, VERSION, EXTENSION;

    override fun toString() = super.toString().lowercase()
  }
  /** largest first */
  data object SortBySize : KOptS("S"), LsOpt
  data object SortByTime : KOptS("t"), LsOpt
  /** do not sort */
  data object SortByNothing : KOptS("U"), LsOpt
  /** FIXME_later: what does it mean: natural sort of (version) numbers within text */
  data object SortByNatural : KOptS("v"), LsOpt
  data object SortByExtension : KOptS("X"), LsOpt

  data class Time(val type: TimeType) : KOptL("time", "$type"), LsOpt

  /**
   * There are duplicates:
   * last access time: ATIME, ACCESS, USE
   * last change time: CTIME, STATUS
   * creation time: BIRTH, CREATION
   */
  enum class TimeType {
    ATIME, ACCESS, USE, CTIME, STATUS, BIRTH, CREATION;

    override fun toString() = super.toString().lowercase()
  }

  data object TimeOfAccess : KOptS("u"), LsOpt
  data object TimeOfChange : KOptS("c"), LsOpt
  data object TimeOfBirth : KOptL("time", "birth"), LsOpt

  data class TimeStyle(val style: String) : KOptL("time-style", style), LsOpt

  data class TabSize(val size: Int) : KOptL("tabsize", "$size"), LsOpt

  data class Width(val columns: Int) : KOptL("width", "$columns"), LsOpt

  data object PrintContext : KOptL("context"), LsOpt

  data object Help : KOptL("help"), LsOpt
  data object Version : KOptL("version"), LsOpt
}
