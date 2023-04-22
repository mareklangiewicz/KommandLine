@file:Suppress("ClassName", "SpellCheckingInspection", "unused", "EnumEntryName")

package pl.mareklangiewicz.kommand.github

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.github.Gh.Cmd

fun gh(cmd: Cmd? = null, init: Gh.() -> Unit = {}) = Gh(cmd).apply(init)

/** [gh manual](https://cli.github.com/manual/index) */
data class Gh(
    val cmd: Cmd? = null,
    val cmdargs: MutableList<String> = mutableListOf(),
    val options: MutableList<Option> = mutableListOf(),
) : Kommand {

    override val name get() = "gh"
    override val args get() = cmd?.str?.split(' ').orEmpty() + cmdargs + options.flatMap { it.str }

    enum class Cmd(val str: String) {
        help("help"),
        status("status"),
        secret_list("secret list"),
        secret_set("secret set"),
    }

    sealed class Option(val name: String, val arg: String? = null) {
        val str get() = listOf(name) plusIfNotNull arg
        object help : Option("--help")
        object version : Option("--version")
        /** @param repoPath [HOST/]OWNER/REPO */
        data class repo(val repoPath: String): Option("--repo", repoPath)
    }

    operator fun String.unaryPlus() = cmdargs.add(this)
    operator fun Option.unaryMinus() = options.add(this)
}